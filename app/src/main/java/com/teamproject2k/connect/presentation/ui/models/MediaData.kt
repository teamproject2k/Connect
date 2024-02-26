package com.teamproject2k.connect.presentation.ui.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MediaData(val uri: Uri, val mediaType: String, val mediaDuration: Long? = null) :
    Parcelable