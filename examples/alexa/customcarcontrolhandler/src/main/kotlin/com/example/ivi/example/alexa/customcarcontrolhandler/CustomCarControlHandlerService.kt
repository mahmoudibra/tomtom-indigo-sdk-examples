/*
 * Copyright © 2020 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.example.ivi.example.alexa.customcarcontrolhandler

import android.content.Context
import com.amazon.aacsconstants.Action
import com.amazon.aacsconstants.Topic
import com.tomtom.ivi.platform.alexa.api.common.util.AacsSenderWrapper
import com.tomtom.ivi.platform.alexa.api.common.util.Header
import com.tomtom.ivi.platform.alexa.api.common.util.createAasbReplyHeader
import com.tomtom.ivi.platform.alexa.api.common.util.parseAasbMessage
import com.tomtom.ivi.platform.alexa.api.service.alexahandler.AlexaHandlerService
import com.tomtom.ivi.platform.alexa.api.service.alexahandler.AlexaHandlerServiceBase
import com.tomtom.ivi.platform.framework.api.common.iviinstance.createTracer
import com.tomtom.ivi.platform.framework.api.ipc.iviservice.IviDiscoverableServiceIdProvider
import com.tomtom.ivi.platform.framework.api.ipc.iviservice.IviServiceHostContext
import com.tomtom.kotlin.traceevents.TraceEventListener
import com.tomtom.kotlin.traceevents.TraceLog
import com.tomtom.kotlin.traceevents.TraceLogLevel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class CustomCarControlHandlerService(
    iviServiceHostContext: IviServiceHostContext,
    serviceIdProvider: IviDiscoverableServiceIdProvider
) : AlexaHandlerServiceBase(iviServiceHostContext, serviceIdProvider) {

    private val tracer = iviServiceHostContext.createTracer<CustomCarControlHandlerEvents> { this }

    // Instantiate a JSON object which will be used to parse and encode the AASB JSON messages.
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val aacsSender = AacsSenderWrapper(iviServiceHostContext)

    /**
     * The same message action ([Action.CarControl.SET_CONTROLLER_VALUE]) corresponds to multiple
     * AASB message types:
     * - `SetPowerControllerValue`
     * - `SetModeControllerValue`
     * - `SetRangeControllerValue`
     * - `SetToggleControllerValue`
     *
     * The `payload.controllerType` field can be used to differentiate among them.
     */
    object SetControllerValueMessageSerializer :
        JsonContentPolymorphicSerializer<SetControllerValueIncomingMessageBase>(
            SetControllerValueIncomingMessageBase::class
        ) {
        override fun selectDeserializer(element: JsonElement):
            DeserializationStrategy<out SetControllerValueIncomingMessageBase> =
            when (
                element.jsonObject[JSON_KEY_PAYLOAD]?.jsonObject?.get(
                    JSON_KEY_CONTROLLER_TYPE
                )?.jsonPrimitive?.content
            ) {
                JSON_VALUE_CONTROLLER_MODE ->
                    SetModeControllerValueIncomingMessage.serializer()
                JSON_VALUE_CONTROLLER_POWER ->
                    SetPowerControllerValueIncomingMessage.serializer()
                JSON_VALUE_CONTROLLER_TOGGLE ->
                    SetToggleControllerValueIncomingMessage.serializer()
                JSON_VALUE_CONTROLLER_RANGE ->
                    SetRangeControllerValueIncomingMessage.serializer()
                else -> throw IllegalArgumentException("Unknown controller type: $element")
            }
    }

    /**
     * Data class representation of the
     * [CarControl](https://alexa.github.io/alexa-auto-sdk/docs/sdk-docs/modules/car-control/aasb-docs/CarControl/)
     * AASB messages.
     */

    @Serializable(with = SetControllerValueMessageSerializer::class)
    sealed class SetControllerValueIncomingMessageBase {
        abstract val header: Header
    }

    @Serializable
    data class SetModeControllerValueIncomingMessage(
        override val header: Header,
        val payload: SetModeControllerValueIncomingMessagePayload
    ) : SetControllerValueIncomingMessageBase()

    @Serializable
    data class SetModeControllerValueIncomingMessagePayload(
        val controllerType: String,
        val endpointId: String,
        val controllerId: String,
        val value: String
    )

    @Serializable
    data class SetPowerControllerValueIncomingMessage(
        override val header: Header,
        val payload: SetPowerControllerValueIncomingMessagePayload
    ) : SetControllerValueIncomingMessageBase()

    @Serializable
    data class SetPowerControllerValueIncomingMessagePayload(
        val controllerType: String,
        val endpointId: String,
        val turnOn: Boolean
    )

    @Serializable
    data class SetRangeControllerValueIncomingMessage(
        override val header: Header,
        val payload: SetRangeControllerValueIncomingMessagePayload
    ) : SetControllerValueIncomingMessageBase()

    @Serializable
    data class SetRangeControllerValueIncomingMessagePayload(
        val controllerType: String,
        val endpointId: String,
        val controllerId: String,
        val value: Double
    )

    @Serializable
    data class SetToggleControllerValueIncomingMessage(
        override val header: Header,
        val payload: SetToggleControllerValueIncomingMessagePayload
    ) : SetControllerValueIncomingMessageBase()

    @Serializable
    data class SetToggleControllerValueIncomingMessagePayload(
        val controllerType: String,
        val endpointId: String,
        val controllerId: String,
        val turnOn: Boolean
    )

    @Serializable
    data class SetControllerValueOutgoingMessage(
        val header: Header,
        val payload: SetControllerValueOutgoingMessagePayload
    )

    @Serializable
    data class SetControllerValueOutgoingMessagePayload(
        val success: Boolean
    )

    override fun onCreate() {
        super.onCreate()

        // This [AlexaHandlerService] implementation will handle CarControl AASB messages.
        topic = Topic.CAR_CONTROL

        // This [AlexaHandlerService] implementation has default priority, so CarControl AASB
        // messages will be forwarded to this handler first, before reaching the stock IndiGO
        // CarControl message handler.
        priority = AlexaHandlerService.DEFAULT_HANDLER_PRIORITY

        // Copy the `custom_assets.json` file from the assets storage to the internal storage, so
        // that it can be found by AACS.
        copyCustomAssets()

        // This configuration adds 2 additional endpoints to the CarControl configuration that
        // will be sent to AACS:
        // - a "default.light" endpoint, associated to "light" assets that are already available in
        //   the default automotive catalog assets
        //   (see https://github.com/alexa/alexa-auto-sdk/blob/master/modules/car-control/assets/assets-1P.json)
        // - a "default.custom_device" endpoint, associated to a custom
        //   "My.Alexa.Automotive.DeviceName.CustomDevice" asset, defined in the
        //   "custom_assets.json" file.
        // This will allow us to receive a CarControl message when the user says: "Alexa, switch
        // on/off the light" or "Alexa, turn on/off my custom device".
        aacsConfiguration = readAacsConfig(context)

        // All required properties have been set, so the service is now ready.
        serviceReady = true
    }

    @Suppress("kotlin:S6300") // Custom assets file does not contain sensitive data.
    private fun copyCustomAssets() {
        try {
            with(context) {
                val customAssetsPath =
                    getString(R.string.custom_control_config_directory) +
                        File.separator +
                        CUSTOM_ASSETS_FILENAME
                assets.open(customAssetsPath).use { inputFile ->
                    FileOutputStream(filesDir?.resolve(CUSTOM_ASSETS_FILENAME)).use {
                        inputFile.copyTo(it)
                    }
                }
            }
        } catch (exception: IOException) {
            tracer.e("Failed to copy custom assets file.", exception)
        }
    }

    private fun readAacsConfig(context: Context): String? =
        try {
            with(context) {
                val filePath = getString(R.string.custom_control_config_directory) +
                    File.separator +
                    AACS_CONFIG_FILENAME

                assets.open(filePath).bufferedReader().use { it.readText() }
            }
        } catch (exception: IOException) {
            tracer.e("AACS configuration file not found.", exception)
            null
        }

    override suspend fun onMessageReceived(
        action: String,
        messageContents: String
    ): Boolean =
        when (action) {
            Action.CarControl.SET_CONTROLLER_VALUE -> handleSetControllerValue(messageContents)
            // We are only interested in handling `SetPowerControllerValue` incoming messages.
            // We return `false` for any other action, so that the message can be forwarded to other
            // CarControl message handlers.
            else -> false
        }

    private fun handleSetControllerValue(message: String): Boolean {
        parseAasbMessage<SetControllerValueIncomingMessageBase>(
            jsonParser,
            message
        )?.let { setMessage ->
            tracer.setControllerValueMessageReceived(setMessage)
            return when (setMessage) {
                is SetPowerControllerValueIncomingMessage ->
                    setPowerControllerValue(setMessage)
                // We are only interested in handling `SetPowerControllerValue` incoming messages.
                // We return `false` for any other SetXXXXControllerValue message, so that the
                // message can be forwarded to other CarControl message handlers.
                else -> false
            }
        }
        return false
    }

    private fun setPowerControllerValue(message: SetPowerControllerValueIncomingMessage): Boolean {
        return when (message.payload.endpointId.toDeviceName()) {
            LIGHT_ID,
            CUSTOM_DEVICE_ID -> {
                sendSetControllerValueReply(message.header.id, true)
                true
            }
            // We are only interested in handling `SetPowerControllerValue` messages for the "light"
            // and the "custom_device" endpoint devices.
            // We return `false` for any other device, so that the message can be forwarded to other
            // CarControl message handlers.
            else -> false
        }
    }

    private fun sendSetControllerValueReply(messageId: String, success: Boolean) {
        val messageToSend = SetControllerValueOutgoingMessage(
            createAasbReplyHeader(
                messageId,
                Topic.CAR_CONTROL,
                Action.CarControl.SET_CONTROLLER_VALUE
            ),
            SetControllerValueOutgoingMessagePayload(success)
        )
        aacsSender.sendMessage(
            jsonParser.encodeToString(messageToSend),
            Topic.CAR_CONTROL,
            Action.CarControl.SET_CONTROLLER_VALUE
        )
    }

    /**
     * Returns the Alexa device name from an endpoint ID.
     * The format of an endpoint ID is either "zone.deviceName" for zoned properties or just
     * "deviceName" for zoneless properties.
     * For example:
     * driver.ac -> ac
     * rear.passenger.ac -> ac
     * steeringWheel -> steeringWheel
     */
    private fun String.toDeviceName() = substringAfterLast(".")

    /**
     * There is no CarControl event or request that needs to be sent periodically while AACS is
     * active, so [onEnginesStarted] can just be an empty implementation.
     */
    override suspend fun onEnginesStarted() {}

    /**
     * There is no CarControl event or request that needs to be sent periodically while AACS is
     * active, so [onEnginesStarted] can just be an empty implementation.
     */
    override suspend fun onEnginesStopped() {}

    interface CustomCarControlHandlerEvents : TraceEventListener {

        @TraceLogLevel(TraceLog.LogLevel.DEBUG)
        fun aacsMessageReceived(msg: String)

        @TraceLogLevel(TraceLog.LogLevel.DEBUG)
        fun setControllerValueMessageReceived(payload: SetControllerValueIncomingMessageBase)

        @TraceLogLevel(TraceLog.LogLevel.WARN)
        fun unknownControllerId()
    }

    private companion object {
        const val AACS_CONFIG_FILENAME = "aacs_customcarcontrol_config.json"
        const val CUSTOM_ASSETS_FILENAME = "custom_assets.json"

        const val JSON_VALUE_CONTROLLER_MODE = "MODE"
        const val JSON_VALUE_CONTROLLER_POWER = "POWER"
        const val JSON_VALUE_CONTROLLER_TOGGLE = "TOGGLE"
        const val JSON_VALUE_CONTROLLER_RANGE = "RANGE"
        const val JSON_KEY_CONTROLLER_TYPE = "controllerType"
        const val JSON_KEY_PAYLOAD = "payload"

        const val LIGHT_ID = "light"
        const val CUSTOM_DEVICE_ID = "custom_device"
    }
}
