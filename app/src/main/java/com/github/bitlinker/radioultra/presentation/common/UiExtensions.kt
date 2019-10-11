package com.github.bitlinker.radioultra.presentation.common

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.github.bitlinker.radioultra.R

fun androidx.appcompat.widget.Toolbar.applyMenuTint(isTintMenu: Boolean, isTintNavigation: Boolean) {
    val tintColor = getToolbarTintColor(this.context)

    if (isTintNavigation) {
        val navIcon = this.navigationIcon
        if (navIcon != null) {
            applyTint(navIcon, tintColor)
        }
    }

    if (isTintMenu) {
        val menu = menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val icon = item.icon
            if (icon != null) {
                item.icon = applyTint(icon, tintColor)
            }
        }
    }
}

private @ColorInt
fun getToolbarTintColor(context: Context): Int {
    return ResourcesCompat.getColor(context.resources, R.color.toolbarTintColor, null)
}

private fun applyTint(icon: Drawable, @ColorInt tintColor: Int): Drawable {
    DrawableCompat.setTint(icon, tintColor)
    return icon
}