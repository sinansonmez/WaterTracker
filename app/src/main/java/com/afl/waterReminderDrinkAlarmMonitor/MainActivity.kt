package com.afl.waterReminderDrinkAlarmMonitor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.afl.waterReminderDrinkAlarmMonitor.databinding.ActivityMainBinding
import com.afl.waterReminderDrinkAlarmMonitor.utils.AppDatabase
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.kobakei.ratethisapp.RateThisApp
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: ActivityMainBinding

    companion object {
        var database: AppDatabase? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        MobileAds.initialize(this) {}

        database = AppDatabase.getDatabase(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

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
        Timber.plant(Timber.DebugTree())
        // Monitor launch times and interval from installation
        RateThisApp.onCreate(this);
        // If the condition is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
        // Custom condition: 3 days and 5 launches
        val config = RateThisApp.Config(3, 5)
        RateThisApp.init(config)

        binding.navView.setupWithNavController(navController)
    }
}
