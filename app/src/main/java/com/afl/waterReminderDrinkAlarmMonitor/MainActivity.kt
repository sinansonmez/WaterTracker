package com.afl.waterReminderDrinkAlarmMonitor

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.afl.waterReminderDrinkAlarmMonitor.databinding.ActivityMainBinding
import com.afl.waterReminderDrinkAlarmMonitor.utils.AppDatabase
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        var database: AppDatabase? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainActivity.database =
            Room.databaseBuilder(this, AppDatabase::class.java, "SQLITE_DATABASE.db")
                .addMigrations(AppDatabase.migration)
                .build()

        MobileAds.initialize(this) {}

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_setting,
                R.id.navigation_notifications,
                R.id.navigation_history
            )
        )

        navView.setupWithNavController(navController)

    }


}
