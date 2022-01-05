package com.example.gardeningapp.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeManager
import com.example.gardeningapp.PlantsApplication
import com.example.gardeningapp.R
import com.example.gardeningapp.utils.ReminderBroadcast
import com.example.gardeningapp.adapters.PlantsAdapter
import com.example.gardeningapp.database.Plant
import com.example.gardeningapp.databinding.ActivityMainBinding
import com.example.gardeningapp.utils.*
import com.example.gardeningapp.viewmodels.PlantsViewModel
import com.example.gardeningapp.viewmodels.PlantsViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity: ThemeActivity() {
    private val plantViewModel: PlantsViewModel by viewModels {
        PlantsViewModelFactory((application as PlantsApplication).repository)
    }
    private lateinit var binding: ActivityMainBinding
    private var calendar = Calendar.getInstance()
    private var selectedRadioButton = 0
    private val selectedDay = MutableLiveData(0)
    private var nextReminderTimeMs = 0L

    @SuppressLint("UnspecifiedImmutableFlag", "MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupNavigationDrawer()
        createNotificationChannel()

        binding.radioButtonLayout.setOnCheckedChangeListener { group, checkedId ->
            val rb = findViewById<RadioButton>(checkedId)
            selectedRadioButton = group.indexOfChild(rb) + 1
            selectedDay.value = selectedRadioButton
        }

        binding.fabButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AddPlantActivity::class.java)
            startActivityForResult(intent, ADD_PLANT_ACTIVITY_REQUEST_CODE)
        }

        selectedDay.observe(this) { day ->
                plantViewModel.getPlantsByDay(day).observe(this) { plantList ->
                getPlantListFromLocalDB(plantList)
            }
        }

        val menu = binding.navigationView.menu
        val switchButton = menu.findItem(R.id.nav_switch).actionView.findViewById<View>(R.id.switch_button) as SwitchCompat
        switchButton.setOnClickListener {
            ThemeManager.instance.changeTheme(NightTheme(), it)
            ThemeManager.instance.reverseChangeTheme(LightTheme(), it)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        nextReminderTimeMs = plantViewModel.getNextReminderTimeMs()
        if(nextReminderTimeMs > 0) {
            setAlarm(nextReminderTimeMs)
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
            window.statusBarColor = myAppTheme.statusBarColor(this@MainActivity)
        }
        binding.apply {
            toolbar.setBackgroundColor(myAppTheme.toolbarColor(this@MainActivity))
            root.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this@MainActivity))
            navigationView.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this@MainActivity))
            navigationView.getHeaderView(0).findViewById<TextView>(R.id.tv_app_name).setTextColor(myAppTheme.firstActivityTextColor(this@MainActivity))
            navigationView.itemTextColor = ColorStateList.valueOf(myAppTheme.firstActivityTextColor(this@MainActivity))
            navigationView.itemIconTintList = ColorStateList.valueOf(myAppTheme.firstActivityIconColor(this@MainActivity))
            tvEmptyPlantList.setTextColor(myAppTheme.firstActivityTextColor(this@MainActivity))
        }
    }

    private fun setAlarm(reminderTime: Long) {
        val intent = Intent(this, ReminderBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder channel"
            val description = "Channel for reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notify", name, importance)
            channel.description = description

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupNavigationDrawer() {
        val drawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar,
            R.string.open_drawer, R.string.close_drawer)
        toggle.syncState()
        binding.navigationView.bringToFront()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_mode_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.all_plants -> {
                binding.radioButtonLayout.visibility = View.GONE
                selectedDay.value = 0
                true
            }
            R.id.watering_days -> {
                binding.radioButtonLayout.visibility = View.VISIBLE
                (binding.radioButtonLayout.getChildAt(calendar.get(Calendar.DAY_OF_WEEK) - 1) as RadioButton).isChecked = true
                selectedDay.value = selectedRadioButton
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getPlantListFromLocalDB(plants: List<Plant>) {
        plants.let {
            if(it.isNotEmpty()) {
                binding.rvPlantList.visibility = View.VISIBLE
                binding.tvEmptyPlantList.visibility = View.GONE
                binding.ivEmptyPlantList.visibility = View.GONE
                setupPlantsRecyclerView(it)
            } else {
                binding.rvPlantList.visibility = View.GONE
                binding.tvEmptyPlantList.visibility = View.VISIBLE
                binding.ivEmptyPlantList.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPlantsRecyclerView(plantList: List<Plant>) {
        binding.rvPlantList.layoutManager = LinearLayoutManager(this)
        binding.rvPlantList.setHasFixedSize(true)

        val plantsAdapter = PlantsAdapter(this, plantList as MutableList<Plant>)
        binding.rvPlantList.adapter = plantsAdapter

        plantsAdapter.setOnClickListener(object: PlantsAdapter.OnClickListener {
            override fun onClick(position: Int, model: Plant) {
                val intent = Intent(this@MainActivity, PlantDetailsActivity::class.java)
                intent.putExtra(PLANT_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object: SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvPlantList.adapter as PlantsAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, UPDATE_PLANT_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.rvPlantList)

        val deleteSwipeHandler = object: SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvPlantList.adapter as PlantsAdapter
                adapter.removeAt(viewHolder.adapterPosition, plantViewModel)
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvPlantList)
    }

    @SuppressLint("SimpleDateFormat")
    private fun nextReminderDateTime(nextReminderTimeMs: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy, HH:mm")
        val date = Date(nextReminderTimeMs)
        return formatter.format(date)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == ADD_PLANT_ACTIVITY_REQUEST_CODE) {
                Toast.makeText(this, "Plant inserted successfully!", Toast.LENGTH_SHORT).show()
            } else if(requestCode == UPDATE_PLANT_ACTIVITY_REQUEST_CODE) {
                Toast.makeText(this, "Plant updated successfully!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("Activity", "Cancelled or back pressed.")
        }
    }

    companion object{
        private const val ADD_PLANT_ACTIVITY_REQUEST_CODE = 1
        private const val UPDATE_PLANT_ACTIVITY_REQUEST_CODE = 2
        internal const val PLANT_DETAILS = "plant details"
    }
}