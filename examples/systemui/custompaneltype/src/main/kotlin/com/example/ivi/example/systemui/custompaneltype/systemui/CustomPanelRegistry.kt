package com.example.ivi.example.systemui.custompaneltype.systemui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.example.ivi.example.systemui.custompaneltype.common.CustomSystemUiPanel
import com.tomtom.ivi.platform.framework.api.ipc.iviservice.IviInstanceBoundIviServiceProvider
import com.tomtom.ivi.platform.frontend.api.common.frontend.panels.Panel
import com.tomtom.ivi.platform.systemui.api.common.frontendcoordinator.FrontendRegistry
import com.tomtom.ivi.platform.systemui.api.common.frontendcoordinator.IviPanelRegistry
import com.tomtom.ivi.platform.systemui.api.common.frontendcoordinator.IviPanelRegistry.Companion.extractPanels
import com.tomtom.ivi.platform.systemui.api.common.frontendcoordinator.PanelRegistry
import com.tomtom.ivi.platform.systemui.api.common.frontendcoordinator.panelcoordination.mapToSingle
import com.tomtom.tools.android.api.livedata.flatMap

/**
 * Contains the currently active [Panel]s.
 *
 * If any frontend added a [CustomSystemUiPanel], then it can be found in [customPanel]. The
 * [Panel]s for TomTom IndiGO's stock system UI can be found in [iviPanelRegistry].
 */
internal class CustomPanelRegistry(
    val customPanel: LiveData<CustomSystemUiPanel?>,
    val iviPanelRegistry: IviPanelRegistry
) : PanelRegistry {

    companion object {

        /**
         * Maps panels from all frontends in the [FrontendRegistry] to a field per panel type.
         *
         * Assumes that there is at most one [customPanel] present, through usage of [mapToSingle].
         */
        fun create(
            frontendRegistry: FrontendRegistry,
            lifecycleOwner: LifecycleOwner,
            iviServiceProvider: IviInstanceBoundIviServiceProvider
        ) = CustomPanelRegistry(
            customPanel = frontendRegistry.frontends.flatMap { it.panels }.mapToSingle(),
            iviPanelRegistry = IviPanelRegistry.build(
                frontendRegistry.extractPanels(),
                lifecycleOwner,
                iviServiceProvider
            )
        )
    }
}
