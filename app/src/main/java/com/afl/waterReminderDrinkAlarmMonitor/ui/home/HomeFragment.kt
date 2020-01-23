package com.afl.waterReminderDrinkAlarmMonitor.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.afl.waterReminderDrinkAlarmMonitor.ui.utils.DatabaseHelper
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.FragmentHomeBinding
import com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard.DashboardViewModel
import com.google.android.gms.ads.AdRequest
import java.text.DecimalFormat

class HomeFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentHomeBinding

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val db by lazy {
            DatabaseHelper(
                container!!.context
            )
        }

        val user = db.readData()

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.dashboardViewModel = dashboardViewModel

        // Specify the current activity as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = this

        //admob setup
        // dummy ad banner id ca-app-pub-3940256099942544/6300978111
        // real ad banner id ca-app-pub-7954399632679605/9743680462
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
//        binding.adView.adListener = object : AdListener(){
//            override fun onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//                Log.d("database", "add is loaded")
//            }
//
//            override fun onAdFailedToLoad(errorCode : Int) {
//                // Code to be executed when an ad request fails.
//                Log.d("database", "add is failed to load")
//            }
//
//            override fun onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//                Log.d("database", "add is opened")
//            }
//
//            override fun onAdClicked() {
//                // Code to be executed when the user clicks on an ad.
//                Log.d("database", "add is clicked")
//            }
//
//            override fun onAdLeftApplication() {
//                // Code to be executed when the user has left the app.
//            }
//
//            override fun onAdClosed() {
//                // Code to be executed when the user is about to return
//                // to the app after tapping on an ad.
//            }
//
//        }


        val waterAmount = user.water
        // if user opens the app for the first time direct it to settings fragment
        if (waterAmount == 0) {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_setting)
        }

        // set metric and water amount based on the information from database
        var metric = if (user.metric == "American") " OZ" else " ML"
        binding.targetText.text = waterAmount.toString() + metric

        // setting information in progress circle
        val drunkAmount = db.readDrinkData()
        val perc = drunkAmount.toFloat() / waterAmount.toFloat()
        val percForPercentage = (perc * 360F).toInt()
        val df = DecimalFormat("##%")
        val percForStepCountText = if (waterAmount == 0) "0%" else df.format(perc)
        binding.wheelProgress.setStepCountText(percForStepCountText)
        binding.wheelProgress.setPercentage(percForPercentage)
        binding.drunkText.text = drunkAmount.toString() + metric

        // dynamically creating drunk list
        // TODO("https://medium.com/mindorks/creating-dynamic-layouts-in-android-d4008b72f2d")
        var drunks = db.readDrinkDataDetails()

        for (drink in drunks) {
            val drinkType = drink.drink + " "
            val textView = TextView(container?.context).apply {
                text = drinkType
            }
            binding.drunkListLayout.addView(textView)
        }

        // icilmesi gereken su miktari degistikce progress wheeldaki su miktarini guncelliyor
        dashboardViewModel.waterAmount.observe(this, Observer { newAmount ->
            binding.wheelProgress.setStepCountText(newAmount.toString())
            binding.targetText.text = newAmount.toString()
        })

        // icilen miktar guncelledikce progress wheelini tamamlanma oraninini guncelliyor
        dashboardViewModel.drunkAmount.observe(this, Observer { newDrunkAmount ->
            val perc = (newDrunkAmount.toFloat() / waterAmount.toFloat()) * 360F
            binding.wheelProgress.setPercentage(perc.toInt())
            binding.drunkText.text = newDrunkAmount.toString()

        })

        binding.drinkWaterButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_home_to_drinksFragment)
            db.readDrinkDataDetails()
        }

        return binding.root
    }
}