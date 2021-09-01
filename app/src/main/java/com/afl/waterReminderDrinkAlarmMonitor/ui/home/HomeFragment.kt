package com.afl.waterReminderDrinkAlarmMonitor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.FragmentHomeBinding
import com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard.DashboardViewModel
import com.afl.waterReminderDrinkAlarmMonitor.utils.*
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // admob setup
        // dummy ad banner id ca-app-pub-3940256099942544/6300978111
        val adRequest = AdRequest.Builder().build()
        _binding!!.adView.loadAd(adRequest)

        // setting information in progress circle
        lifecycleScope.launch {
            val drunkAmount = getDrinkAmount(dao)

            val user = Repository(dao).readUserData()
            val waterAmount = user?.water
            val metric = user?.metric

            navigateToDashboardScreen(waterAmount)
            adjustProgressWheel(drunkAmount, waterAmount, metric)

            val today =
                SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()
            val drunks = Repository(dao).readDrinkDataDetailsSelectedDay(today)

            withContext(Dispatchers.Main) {
                if (drunks != null) {
                    _binding!!.drinksRecyclerView.adapter = DrinksContainerAdapter(drunks)
                }
            }
        }
        _binding!!.drinksRecyclerView.layoutManager =
            LinearLayoutManager(container?.context, LinearLayoutManager.HORIZONTAL, false)

        _binding!!.drinksRecyclerView.isNestedScrollingEnabled = false

        _binding!!.drinkWaterButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_home_to_drinksFragment)
        }

        // This disables back button
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {}
            })

        val view = binding.root
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAnalytics.setCurrentScreen(
            this.activity!!,
            this.javaClass.simpleName,
            this.javaClass.simpleName
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_binding != null) {
            _binding!!.adView.adListener = null
            _binding!!.adView.removeAllViews()
            _binding!!.adView.destroy()
        }
        _binding = null
    }

    private fun drunkAmountPercFormatter(drunkAmount: Int?, waterAmount: Int?): Int {
        val perc = percentageParser(drunkAmount, waterAmount)
        return (perc.times(360F)).toInt()
    }

    private fun drunkAmountPercTextFormatter(drunkAmount: Int?, waterAmount: Int?): String {

        val perc = percentageParser(drunkAmount, waterAmount)
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
            _binding!!.wheelProgress.setStepCountText(
                drunkAmountPercTextFormatter(drunkAmount, waterAmount)
            )
            _binding!!.wheelProgress.setPercentage(
                drunkAmountPercFormatter(drunkAmount, waterAmount)
            )
            val metricAbbr = if (metric == "American") " OZ" else " ML"
            _binding!!.drunkText.text = drunkAmount.toString() + metricAbbr
            _binding!!.targetText.text = waterAmount.toString() + metricAbbr
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
