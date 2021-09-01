package com.afl.waterReminderDrinkAlarmMonitor.ui.drinks

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.DrinksFragmentBinding
import com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard.DashboardViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch

class DrinksFragment : Fragment() {

    companion object {
        fun newInstance() =
            DrinksFragment()
    }

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: DrinksFragmentBinding
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var drinkButtonCheckStatus = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(inflater, R.layout.drinks_fragment, container, false)

        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.dashboardViewModel = dashboardViewModel

        // Specify the current activity as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = this

        // admob setup
        // dummy ad banner id ca-app-pub-3940256099942544/6300978111
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.drinkButton.setOnClickListener {
            // oncelikle secili bir icecek var mi diye kontrol ediyor
            if (!drinkButtonCheckStatus) {
                Snackbar.make(binding.root, getString(R.string.select_drink), Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                lifecycleScope.launch {
                    dashboardViewModel.drink()
                    it.findNavController().navigate(R.id.action_drinksFragment_to_navigation_home)
                }
            }
            drinkButtonCheckStatus = false
        }

        buttonListeners()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAnalytics.setCurrentScreen(
            this.activity!!, this.javaClass.simpleName, this.javaClass.simpleName
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.adView.adListener = null
        binding.adView.removeAllViews()
        binding.adView.destroy()
    }

    // onchecked lister to manage only one selected toggle button exist at a time
    private var toggleButtonHandler =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (buttonView != binding.waterButton) binding.waterButton.isChecked = false
                if (buttonView != binding.coffeeButton) binding.coffeeButton.isChecked = false
                if (buttonView != binding.teaButton) binding.teaButton.isChecked = false
                if (buttonView != binding.juiceButton) binding.juiceButton.isChecked = false
                if (buttonView != binding.sodaButton) binding.sodaButton.isChecked = false
                if (buttonView != binding.beerButton) binding.beerButton.isChecked = false
                if (buttonView != binding.wineButton) binding.wineButton.isChecked = false
                if (buttonView != binding.milkButton) binding.milkButton.isChecked = false
                if (buttonView != binding.yogurtButton) binding.yogurtButton.isChecked = false
                if (buttonView != binding.milkshakeButton) binding.milkshakeButton.isChecked = false
                if (buttonView != binding.energyButton) binding.energyButton.isChecked = false
                if (buttonView != binding.lemonadeButton) binding.lemonadeButton.isChecked = false

                drinkButtonCheckStatus = true
                dashboardViewModel.drinkTypeHandler(buttonView.tag.toString())
            } else {
                drinkButtonCheckStatus = false
                dashboardViewModel.drinkTypeHandler("")
            }
        }

    private fun buttonListeners() {
        val buttons = mutableListOf(
            binding.waterButton,
            binding.coffeeButton,
            binding.teaButton,
            binding.juiceButton,
            binding.sodaButton,
            binding.beerButton,
            binding.wineButton,
            binding.milkButton,
            binding.yogurtButton,
            binding.milkshakeButton,
            binding.energyButton,
            binding.lemonadeButton
        )
        buttons.forEach { it.setOnCheckedChangeListener(toggleButtonHandler) }
    }
}