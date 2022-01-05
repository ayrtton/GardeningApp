package com.example.gardeningapp.utils

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import com.example.gardeningapp.R

class DayViewCheckBox(context: Context, attrs: AttributeSet?): AppCompatCheckBox(context, attrs) {
    override fun setChecked(t: Boolean){
        if(t) {
            this.setTextColor(Color.WHITE)
        } else {
            this.setTextColor(ContextCompat.getColor(context, R.color.green01_medium))
        }
        super.setChecked(t)
    }
}
