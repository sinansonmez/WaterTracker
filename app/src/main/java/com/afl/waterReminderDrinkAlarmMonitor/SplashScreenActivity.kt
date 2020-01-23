package com.afl.waterReminderDrinkAlarmMonitor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    val SPLASH_TIMEOUT_TIME = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        // hide title bar
//        window.requestFeature(Window.FEATURE_NO_TITLE)
//
//        //make activity full screen
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            val home = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(home)
            finish()
        }, SPLASH_TIMEOUT_TIME.toLong())

    }


}