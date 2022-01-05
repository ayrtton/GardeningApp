package com.example.gardeningapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.example.gardeningapp.R
import com.example.gardeningapp.databinding.ActivityPlantDetailsBinding
import com.example.gardeningapp.database.Plant
import com.example.gardeningapp.utils.DayViewCheckBox
import com.example.gardeningapp.utils.LightTheme
import com.example.gardeningapp.utils.MyAppTheme

class PlantDetailsActivity: ThemeActivity() {
    private lateinit var binding: ActivityPlantDetailsBinding

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var plantDetails: Plant? = null

        if(intent.hasExtra(MainActivity.PLANT_DETAILS)) {
            plantDetails = intent.getParcelableExtra(MainActivity.PLANT_DETAILS) as Plant?
        }

        if(plantDetails != null) {
            val remindersAmount = plantDetails.weekdays?.count {
                plantDetails.weekdays!!.contains('1')
            }

            setSupportActionBar(binding.toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            binding.ivPlantImage.setImageURI(Uri.parse(plantDetails.image))
            binding.tvSpecie.text = plantDetails.specie
            if(plantDetails.scientificName?.isNotEmpty() == true) {
                binding.tvScientificName.text = plantDetails.scientificName
                binding.tvScientificName.visibility = View.VISIBLE
            }

            if(plantDetails.description?.isNotEmpty() == true) {
                binding.tvDescription.text = plantDetails.description
                binding.tvDescription.visibility = View.VISIBLE
            }

            if(remindersAmount != null && remindersAmount > 0) {
                val ll = binding.checkboxLayout
                for(i in 0 until binding.checkboxLayout.childCount) {
                    val v = ll.getChildAt(i)
                    if (plantDetails.weekdays?.get(i) == '1') {
                        (v as DayViewCheckBox).isChecked = true
                    }
                }
                binding.tvWateringTime.text = plantDetails.time
                binding.cvWateringDays.visibility = View.VISIBLE
                binding.cvWateringTime.visibility = View.VISIBLE
            }
        }
    }

    override fun getStartTheme(): AppTheme {
        return LightTheme()
    }

    override fun syncTheme(appTheme: AppTheme) {
        val myAppTheme = appTheme as MyAppTheme

        val window = this.window
        window.apply {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = myAppTheme.statusBarColor(this@PlantDetailsActivity)
        }
        binding.apply {
            toolbar.setBackgroundColor(myAppTheme.toolbarColor(this@PlantDetailsActivity))
            root.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this@PlantDetailsActivity))
            tvSpecie.setTextColor(myAppTheme.firstActivityTextColor(this@PlantDetailsActivity))
            tvScientificName.setTextColor(myAppTheme.firstActivityTextColor(this@PlantDetailsActivity))
            tvDescription.setTextColor(myAppTheme.firstActivityTextColor(this@PlantDetailsActivity))
        }
    }
}