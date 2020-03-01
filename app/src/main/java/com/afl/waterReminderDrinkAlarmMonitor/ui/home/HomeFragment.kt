package com.afl.waterReminderDrinkAlarmMonitor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.FragmentHomeBinding
import com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard.DashboardViewModel
import com.afl.waterReminderDrinkAlarmMonitor.utils.*
import com.google.android.gms.ads.AdRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentHomeBinding

    private val db by lazy {
        DatabaseHelper(
            this.requireContext()
        )
    }


    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dao = AppDatabase.getDatabase(container?.context).dao()

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

        // setting information in progress circle
        lifecycleScope.launch {
            val drunkAmount = getDrinkAmount(dao)

            val user = Repository(dao).readUserData()
            val waterAmount = user?.water
            val metric = user?.metric

            navigateToDashboardScreen(waterAmount)
            adjustProgressWheel(drunkAmount, waterAmount, metric)

            val today = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()
            val drunks = Repository(dao).readDrinkDataDetailsSelectedDay(today)

            withContext(Dispatchers.Main){
                if (drunks != null) {
                    binding.drinksRecyclerView.adapter = DrinksContainerAdapter(drunks)

                }
            }
        }

        binding.drinksRecyclerView.layoutManager = LinearLayoutManager(container?.context, LinearLayoutManager.HORIZONTAL, false)

        binding.drinksRecyclerView.isNestedScrollingEnabled = false

        binding.drinkWaterButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_home_to_drinksFragment)
        }

        return binding.root
    }

    private fun drunkAmountPercFormatter(drunkAmount: Int?, waterAmount: Int?): Int {
        val perc = percentageParser(drunkAmount,waterAmount)
        return (perc.times(360F)).toInt()

    }

    private fun drunkAmountPercTextFormatter(drunkAmount: Int?, waterAmount: Int?): String {

        val perc = percentageParser(drunkAmount,waterAmount)
        val df = DecimalFormat("##%")
        return if (waterAmount == 0) "0%" else df.format(perc)
    }

    private fun percentageParser(drunkAmount: Int?, waterAmount: Int?): Float {

        return if (waterAmount != null && drunkAmount != null) {
            drunkAmount.toFloat().div(waterAmount.toFloat())
        } else {
            0f
        }

    }

    private suspend fun getDrinkAmount(dao: Dao): Int? {
        val sum = Repository(dao).readDrinkSumData()

        val date = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()

        // eger gun bugunun gunu degilse 0 getiriyor
        val size = sum?.lastIndex
        return if (size == -1) {
            0
        } else {
            if (sum!![size!!].date == date) sum[size].total else 0
        }

    }

    private suspend fun adjustProgressWheel(drunkAmount: Int?, waterAmount: Int?, metric: String?) {

        withContext(Dispatchers.Main) {
            binding.wheelProgress.setStepCountText(
                drunkAmountPercTextFormatter(drunkAmount, waterAmount)
            )
            binding.wheelProgress.setPercentage(
                drunkAmountPercFormatter(drunkAmount, waterAmount)
            )
            val metricAbbr = if (metric == "American") " OZ" else " ML"
            binding.drunkText.text = drunkAmount.toString() + metricAbbr
            binding.targetText.text = waterAmount.toString() + metricAbbr

        }

    }

    private suspend fun navigateToDashboardScreen(waterAmount: Int?) {

        withContext(Dispatchers.Main) {

            // if user opens the app for the first time direct it to settings fragment
            if (waterAmount == 0 || waterAmount == null) {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_setting)
            }
        }

    }


}