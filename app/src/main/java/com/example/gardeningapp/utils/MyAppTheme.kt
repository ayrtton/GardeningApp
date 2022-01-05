package com.example.gardeningapp.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.dolatkia.animatedThemeManager.AppTheme
import com.example.gardeningapp.R

interface MyAppTheme : AppTheme {
    fun firstActivityBackgroundColor(context: Context): Int
    fun firstActivityTextColor(context: Context): Int
    fun firstActivityIconColor(context: Context): Int
    fun statusBarColor(context: Context): Int
    fun toolbarColor(context: Context): Int
}

class LightTheme : MyAppTheme {
    override fun id(): Int {
        return 0
    }

    override fun firstActivityBackgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.white)
    }

    override fun firstActivityTextColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.primary_text_color)
    }

    override fun firstActivityIconColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.primary_text_color)
    }

    override fun statusBarColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.green01_dark)
    }

    override fun toolbarColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.green01_medium)
    }
}

class NightTheme : MyAppTheme {
    override fun id(): Int {
        return 1
    }

    override fun firstActivityBackgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.dark_cyan02)
    }

    override fun firstActivityTextColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.white)
    }

    override fun firstActivityIconColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.white)
    }

    override fun statusBarColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.black)
    }

    override fun toolbarColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.dark_cyan01)
    }
}