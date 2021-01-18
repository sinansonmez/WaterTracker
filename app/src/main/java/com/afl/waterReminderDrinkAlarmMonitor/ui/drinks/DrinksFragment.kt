package com.afl.waterReminderDrinkAlarmMonitor.ui.drinks

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.DrinksFragmentBinding
import com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard.DashboardViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import timber.log.Timber

class DrinksFragment : Fragment() {

    companion object {
        fun newInstance() =
            DrinksFragment()
    }

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var _binding: DrinksFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var drinkButtonCheckStatus = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        _binding = DrinksFragmentBinding.inflate(inflater, container, false)

        // admob setup
        // dummy ad banner id ca-app-pub-3940256099942544/6300978111
        // real ad banner id ca-app-pub-7954399632679605/9743680462
        val adRequest = AdRequest.Builder().build()
        _binding!!.adView.loadAd(adRequest)
        _binding!!.metricText.text = "sth"

        _binding!!.drinkButton.setOnClickListener {
            // oncelikle secili bir icecek var mi diye kontrol ediyor
            if (!drinkButtonCheckStatus) {
                Snackbar.make(
                    _binding!!.root,
                    getString(R.string.select_drink),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {

                lifecycleScope.launch {
                    dashboardViewModel.drink()
                    it.findNavController().navigate(R.id.action_drinksFragment_to_navigation_home)
                }
            }
        }
        _binding!!.plusButton.setOnClickListener { dashboardViewModel.drinkAmountHandler("plus") }
        _binding!!.minusButton.setOnClickListener { dashboardViewModel.drinkAmountHandler("minus") }

        dashboardViewModel.drinkAmount.observe(this, Observer { newAmount ->
            _binding!!.amountText.text = newAmount.toString()
        })

        dashboardViewModel.metric.observe(this, Observer { newMetric ->
            when (newMetric) {
                "American" -> {
                    _binding!!.metricText.text = " oz"
                }
                "Metric" -> {
                    _binding!!.metricText.text = " ml"
                }
            }
            Timber.d("new metric: $newMetric")
        })

        // This callback will only be called when Fragment is at least Started.
  /*      requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Timber.d("back button is called")
                    findNavController().navigateUp()
                }

            })*/

        buttonListeners()

        val view = binding.root
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("on start is called")
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


    // onchecked lister to manage only one selected toggle button exist at a time
    private var toggleButtonHandler =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (buttonView != _binding!!.waterButton) _binding!!.waterButton.isChecked = false
                if (buttonView != _binding!!.coffeeButton) _binding!!.coffeeButton.isChecked = false
                if (buttonView != _binding!!.teaButton) _binding!!.teaButton.isChecked = false
                if (buttonView != _binding!!.juiceButton) _binding!!.juiceButton.isChecked = false
                if (buttonView != _binding!!.sodaButton) _binding!!.sodaButton.isChecked = false
                if (buttonView != _binding!!.beerButton) _binding!!.beerButton.isChecked = false
                if (buttonView != _binding!!.wineButton) _binding!!.wineButton.isChecked = false
                if (buttonView != _binding!!.milkButton) _binding!!.milkButton.isChecked = false
                if (buttonView != _binding!!.yogurtButton) _binding!!.yogurtButton.isChecked = false
                if (buttonView != _binding!!.milkshakeButton) _binding!!.milkshakeButton.isChecked =
                    false
                if (buttonView != _binding!!.energyButton) _binding!!.energyButton.isChecked = false
                if (buttonView != _binding!!.lemonadeButton) _binding!!.lemonadeButton.isChecked =
                    false

                drinkButtonCheckStatus = true

                dashboardViewModel.drinkTypeHandler(buttonView.tag.toString())
            }
        }

    private fun buttonListeners() {
        _binding!!.waterButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.coffeeButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.teaButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.juiceButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.sodaButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.beerButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.wineButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.milkButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.yogurtButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.milkshakeButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.energyButton.setOnCheckedChangeListener(toggleButtonHandler)
        _binding!!.lemonadeButton.setOnCheckedChangeListener(toggleButtonHandler)
    }
}
