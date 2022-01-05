package com.example.gardeningapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.example.gardeningapp.PlantsApplication
import com.example.gardeningapp.database.Plant
import com.example.gardeningapp.R
import com.example.gardeningapp.databinding.ActivityAddPlantBinding
import com.example.gardeningapp.utils.DayViewCheckBox
import com.example.gardeningapp.viewmodels.PlantsViewModel
import com.example.gardeningapp.viewmodels.PlantsViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.content.res.ColorStateList
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.example.gardeningapp.utils.LightTheme
import com.example.gardeningapp.utils.MyAppTheme

class AddPlantActivity: ThemeActivity(), View.OnClickListener {
    private val plantViewModel: PlantsViewModel by viewModels {
        PlantsViewModelFactory((application as PlantsApplication).repository)
    }
    private lateinit var binding: ActivityAddPlantBinding
    private var cal = Calendar.getInstance()
    private val weekdays = IntArray(7)
    private var saveImageToInternalStorage: Uri? = null
    private var imageCameraUri: Uri? = null
    private var mPlantDetails: Plant? = null

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setCurrentTime()
        setOnClickListeners()

        if(intent.hasExtra(MainActivity.PLANT_DETAILS)) {
            mPlantDetails = intent.getParcelableExtra(MainActivity.PLANT_DETAILS) as Plant?
        }

        if(mPlantDetails != null) {
            supportActionBar?.title = "Edit Plant"

            saveImageToInternalStorage = Uri.parse(mPlantDetails!!.image)
            binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)
            binding.etSpecie.setText(mPlantDetails!!.specie)
            binding.etScientificName.setText(mPlantDetails!!.scientificName)
            binding.etDescription.setText(mPlantDetails!!.description)

            val ll = binding.checkboxLayout
            for(i in 0 until binding.checkboxLayout.childCount) {
                val v = ll.getChildAt(i)
                if(mPlantDetails!!.weekdays?.get(i) == '1') {
                    (v as DayViewCheckBox).isChecked = true
                    onCheckboxClicked(v)
                }
            }

            binding.tvWateringTime.text = mPlantDetails!!.time
            binding.btnSave.text = "UPDATE"
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
            window.statusBarColor = myAppTheme.statusBarColor(this@AddPlantActivity)
        }
        binding.apply {
            val gray = ContextCompat.getColor(this@AddPlantActivity, R.color.primary_text_color)
            val green = ContextCompat.getColor(this@AddPlantActivity, R.color.green01_medium)
            val textInputColors = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_active), intArrayOf( android.R.attr.state_focused),
                intArrayOf( android.R.attr.state_hovered), intArrayOf( android.R.attr.state_enabled)),
            intArrayOf(gray, green, gray, gray))

            toolbar.setBackgroundColor(myAppTheme.toolbarColor(this@AddPlantActivity))
            root.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this@AddPlantActivity))
            tilSpecie.setBoxStrokeColorStateList(textInputColors)
            etSpecie.setTextColor(myAppTheme.firstActivityTextColor(this@AddPlantActivity))
            tilScientificName.setBoxStrokeColorStateList(textInputColors)
            etScientificName.setTextColor(myAppTheme.firstActivityTextColor(this@AddPlantActivity))
            tilDescription.setBoxStrokeColorStateList(textInputColors)
            etDescription.setTextColor(myAppTheme.firstActivityTextColor(this@AddPlantActivity))
        }
    }

    private fun setCurrentTime() {
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        binding.tvWateringTime.text = String.format(Locale.getDefault(), "%d:%02d", hour, minute)
    }

    private fun setOnClickListeners() {
        binding.ivPlaceImage.setOnClickListener(this)
        binding.everyDay.setOnClickListener(this)
        for(i in 0 until binding.checkboxLayout.childCount) {
            val v = binding.checkboxLayout.getChildAt(i)
            v.setOnClickListener(this)
        }
        binding.tvWateringTime.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_place_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.tv_watering_time -> {
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        binding.tvWateringTime.text = SimpleDateFormat("HH:mm").format(cal.time)
                    }

                TimePickerDialog(
                    this@AddPlantActivity,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }

            R.id.btn_save -> {
                when {
                    binding.etSpecie.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter the specie...", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please add an image...", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val plant = Plant(
                            if (mPlantDetails == null) 0 else mPlantDetails!!.id,
                            binding.etSpecie.text.toString(),
                            binding.etScientificName.text.toString(),
                            binding.etDescription.text.toString(),
                            saveImageToInternalStorage.toString(),
                            weekdays.joinToString(""),
                            binding.tvWateringTime.text.toString()
                        )

                        if(mPlantDetails == null) plantViewModel.insert(plant)
                        else plantViewModel.update(plant)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }

            else -> {
                onCheckboxClicked(v)
            }
        }
    }

    private fun onCheckboxClicked(v: View) {
        val checked = (v as CheckBox).isChecked
        when(v.id) {
            R.id.dv_sunday -> {
                if (checked) {
                    weekdays[0] = 1
                } else {
                    weekdays[0] = 0
                    binding.everyDay.isChecked = false
                }
            }

            R.id.dv_monday -> {
                if (checked) {
                    weekdays[1] = 1
                } else {
                    weekdays[1] = 0
                    binding.everyDay.isChecked = false
                }
            }

            R.id.dv_tuesday -> {
                if (checked) {
                    weekdays[2] = 1
                } else {
                    weekdays[2] = 0
                    binding.everyDay.isChecked = false
                }
            }

            R.id.dv_wednesday -> {
                if (checked) {
                    weekdays[3] = 1
                } else {
                    weekdays[3] = 0
                    binding.everyDay.isChecked = false
                }
            }

            R.id.dv_thursday -> {
                if (checked) {
                    weekdays[4] = 1
                } else {
                    weekdays[4] = 0
                    binding.everyDay.isChecked = false
                }
            }

            R.id.dv_friday -> {
                if (checked) {
                    weekdays[5] = 1
                } else {
                    weekdays[5] = 0
                    binding.everyDay.isChecked = false
                }
            }

            R.id.dv_saturday -> {
                if (checked) {
                    weekdays[6] = 1
                } else {
                    weekdays[6] = 0
                    binding.everyDay.isChecked = false
                }
            }

            R.id.every_day -> {
                val ll = binding.checkboxLayout
                for(i in 0 until binding.checkboxLayout.childCount) {
                    val v = ll.getChildAt(i)
                    (v as DayViewCheckBox).isChecked = checked
                    onCheckboxClicked(v)
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        contentURI?.let {
                            val bitmap = if(Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                            } else {
                                val source = ImageDecoder.createSource(this.contentResolver, contentURI)
                                ImageDecoder.decodeBitmap(source)
                            }
                            saveImageToInternalStorage = saveImageToInternalStorage(bitmap)
                            Log.e("Saved Image: ", "Path :: $saveImageToInternalStorage")
                            binding.ivPlaceImage.setImageBitmap(bitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddPlantActivity, "Failed to get image from gallery!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                try {
                    imageCameraUri?.let {
                        val bitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(this.contentResolver, imageCameraUri)
                        } else {
                            val source =
                                ImageDecoder.createSource(this.contentResolver, imageCameraUri!!)
                            ImageDecoder.decodeBitmap(source)
                        }
                        saveImageToInternalStorage = saveImageToInternalStorage(bitmap)
                        Log.e("Saved Image: ", "Path :: $saveImageToInternalStorage")
                        binding.ivPlaceImage.setImageBitmap(bitmap)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@AddPlantActivity, "Failed to get image from camera!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Your permissions are turned off.")
            .setPositiveButton("Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    imageCameraUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())

                    val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageCameraUri)
                    startActivityForResult(intentCamera, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "PlantsImages"
    }
}