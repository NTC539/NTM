package com.example.ntm.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class Track(
    val title: String,
    val artist: String,
    val duration: String,
    val cover: Int
)

data class Artist(
    val name: String,
    val description: String,
    val avatar: Int
)

data class Playlist(
    val name: String,
    val trackCount: Int,
    val cover: Int
)

data class HomeCategory(
    @StringRes val titleResId: Int,
    val icon: ImageVector
)

data class SettingItem(
    @StringRes val titleResId: Int,
    @StringRes val subtitleResId: Int? = null,
    val icon: ImageVector
)