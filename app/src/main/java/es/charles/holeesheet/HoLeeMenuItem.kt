package es.charles.holeesheet

import androidx.annotation.DrawableRes

data class HoLeeMenuItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val subtitle: String? = null,
    @DrawableRes val icon: Int
)
