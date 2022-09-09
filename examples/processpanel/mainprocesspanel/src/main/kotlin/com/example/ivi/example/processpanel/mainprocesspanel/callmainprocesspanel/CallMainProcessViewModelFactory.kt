/*
 * Copyright © 2022 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.example.ivi.example.processpanel.mainprocesspanel.callmainprocesspanel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.ivi.example.processpanel.mainprocesspanel.R
import com.tomtom.ivi.platform.framework.api.common.annotations.IviExperimental
import com.tomtom.ivi.platform.frontend.api.template.compactprocesspanel.CompactProcessControlViewModel
import com.tomtom.ivi.platform.frontend.api.template.compactprocesspanel.CompactProcessMetadataViewModel
import com.tomtom.tools.android.api.livedata.ImmutableLiveData
import com.tomtom.tools.android.api.resourceresolution.drawable.ResourceDrawableResolver
import com.tomtom.tools.android.api.resourceresolution.string.ResourceStringResolver
import com.tomtom.tools.android.api.resourceresolution.string.StaticStringResolver
import com.tomtom.tools.android.api.resourceresolution.string.StringResolver
import com.tomtom.tools.android.api.uicontrols.button.TtButton
import com.tomtom.tools.android.api.uicontrols.button.TtButtonViewModel
import com.tomtom.tools.android.api.uicontrols.compositeviewmodel.StockVisibilityProvidingCompositeViewModel
import com.tomtom.tools.android.api.uicontrols.compositeviewmodel.VisibilityProvidingCompositeViewModel
import com.tomtom.tools.android.api.uicontrols.imageview.ImageDescriptor
import com.tomtom.tools.android.api.uicontrols.imageview.ImageType

@OptIn(IviExperimental::class)
internal class CallMainProcessViewModelFactory(
    private val title: StringResolver = ResourceStringResolver(R.string.ttivi_processcreation_mainprocesspanel_title),
    private var isMuted: MutableLiveData<Boolean> = MutableLiveData(true),
    doEndCall: () -> Unit,
    doDismissCall: () -> Unit,
) {
    @OptIn(IviExperimental::class)
    private val endCallControl = CompactProcessControlViewModel(
        buttonViewModel = TtButtonViewModel(
            actionType = ImmutableLiveData(TtButton.ActionType.DESTRUCTIVE),
            image = ImmutableLiveData(
                ResourceDrawableResolver(
                    R.drawable.ttivi_processcreation_icon_callhangup
                )
            ),
            isEnabled = ImmutableLiveData(true),
            isVisible = ImmutableLiveData(true),
            onClick = doEndCall
        ), isFixedWidth = ImmutableLiveData(true)
    )

    private val toggleMuteControl = CompactProcessControlViewModel(
        buttonViewModel = TtButtonViewModel(
            isActivated = isMuted,
            actionType = ImmutableLiveData(TtButton.ActionType.TERTIARY),
            isEnabled = ImmutableLiveData(true),
            onClick = {
                isMuted.value = !(isMuted.value ?: false)
            },
            image = isMuted.map {
                ResourceDrawableResolver(
                    when (it) {
                        true -> R.drawable.ttivi_processcreation_icon_microphonemuted
                        false -> R.drawable.ttivi_processcreation_icon_microphone
                    }
                )
            },
            isVisible = ImmutableLiveData(true)
        )
    )

    private val closeControl = CompactProcessControlViewModel(
        buttonViewModel = TtButtonViewModel(
            actionType = ImmutableLiveData(TtButton.ActionType.SECONDARY),
            text = ImmutableLiveData(
                ResourceStringResolver(R.string.ttivi_processcreation_common_action_close)
            ),
            isVisible = ImmutableLiveData(true),
            onClick = doDismissCall
        ),
        isFixedWidth = ImmutableLiveData(true)
    )

    fun createPrimaryControls():
        LiveData<VisibilityProvidingCompositeViewModel<CompactProcessControlViewModel>> =
        ImmutableLiveData(
            StockVisibilityProvidingCompositeViewModel(
                endCallControl
            )
        )

    fun createSecondaryControls():
        LiveData<VisibilityProvidingCompositeViewModel<CompactProcessControlViewModel>> =
        ImmutableLiveData(
            StockVisibilityProvidingCompositeViewModel(
                toggleMuteControl,
                closeControl
            )
        )

    fun createMetadata() = CompactProcessMetadataViewModel(
        image = ImmutableLiveData(
            ImageDescriptor(
                ResourceDrawableResolver(R.drawable.ttivi_systemui_debugtab_icon_thumbnail),
                ImageType.AVATAR
            )
        ),
        primaryText = ImmutableLiveData(title),
        secondaryText = ImmutableLiveData(StaticStringResolver("1:00")),
        onClick = ImmutableLiveData {
            // Add click action when Metadata section has been clicked.
        }
    )
}
