package com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afl.waterReminderDrinkAlarmMonitor.databinding.FragmentDashboardBinding
import com.google.android.material.snackbar.Snackbar
import com.afl.waterReminderDrinkAlarmMonitor.*
import com.afl.waterReminderDrinkAlarmMonitor.utils.*
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private val db by lazy {
        DatabaseHelper(
            this.requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dao = AppDatabase.getDatabase(container?.context).dao()

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.dashboardViewModel = dashboardViewModel

        // Specify the current activity as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = this

        binding.ageEditText.onChange {
            if (binding.ageEditText.text.isNullOrEmpty()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.please_type_age),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else dashboardViewModel.ageHandler(it)
        }

        binding.weightEditText.onChange {
            if (binding.weightEditText.text.isNullOrEmpty()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.please_type_weight),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else dashboardViewModel.weightHandler(it)
        }

        // Set Selected Change Listener for gender selector
        binding.genderRadio.setOnClickedButtonListener(dashboardViewModel.genderHandler)
        binding.metricRadio.setOnClickedButtonListener(dashboardViewModel.metricHandler)

        // listener for seekbar
        binding.waterSeekBar.onProgressChangedListener = dashboardViewModel.seekbarHandler

        // observer for weight to calculate if age is also entered
        dashboardViewModel.weight.observe(this, Observer { newWeight ->
            if (!binding.ageEditText.text.isNullOrEmpty()) {
                val progress = dashboardViewModel.waterCalculate()
                binding.waterSeekBar.setProgress(progress)
            }
        })

        // observer for weight to calculate if weight is also entered
        dashboardViewModel.age.observe(this, Observer { newAge ->
            if (!binding.weightEditText.text.isNullOrEmpty()) {
                val progress = dashboardViewModel.waterCalculate()
                binding.waterSeekBar.setProgress(progress)
            }
        })

        // observer for metric value, if value is American max value is set to 200 or otherwise to 5000
        dashboardViewModel.metric.observe(this, Observer { newMetric ->

            // this is second check if age and weight is empty do nothing otherwise recalculate water amount
            if (!(binding.weightEditText.text.isNullOrEmpty() or binding.ageEditText.text.isNullOrEmpty())) {
                val progress = dashboardViewModel.waterCalculate()
                binding.waterSeekBar.setProgress(progress)
            }

            // this is an if statement to set the max value of water seek bar dynamically
            if (newMetric == "American") {
                binding.waterSeekBar.configBuilder.max(200F).build()
            } else {
                binding.waterSeekBar.configBuilder.max(5000F).build()
            }
        })

        // observer for gender, if age and weight is empty guide user to enter age otherwise recalculate water amount
        dashboardViewModel.gender.observe(this, Observer { newGender ->
            if (binding.weightEditText.text.isNullOrEmpty() or binding.ageEditText.text.isNullOrEmpty()) {
                // TODO(bu uyari dolu olsa da cikiyor)
                Snackbar.make(
                    binding.root,
                    getString(R.string.please_type_age_weigh),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                val progress = dashboardViewModel.waterCalculate()
                binding.waterSeekBar.setProgress(progress)
            }
        })

        // observer for water seek bar to update it is status when water amount is changed
        dashboardViewModel.waterAmount.observe(this, Observer { newAmount ->
            val user = db.readData()

            // su miktari degistigi takdirde kullanici databasedden alinir, su miktari yeni su miktari
            // ile guncellenir, text guncellenir, waterseekbar guncellenir ve database update edilir
            user.water = newAmount

            val metricText = if (user.metric == "American") " OZ" else " ML"
            binding.waterAmountText.text = newAmount.toString() + metricText

            db.updateUser(user)
        })

        if (db.checkUserTableCount() == 1) {
            readUserData()
        }

        return binding.root
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

    // function to read the data from database and set the values
    private fun readUserData() {

        val user = db.readData()

        // TODO: 1/18/21 drink fragment bug sebebi bu olabilir
        // set metric selector
        if (user.metric == "Metric") {
            binding.metricRadio.position = 0
            dashboardViewModel.metricHandlerOnCreate(0)
        } else {
            binding.metricRadio.position = 1
            dashboardViewModel.metricHandlerOnCreate(1)
        }

        // set gender selector
        if (user.gender == "Male") {
            binding.genderRadio.position = 0
            dashboardViewModel.genderHandlerOnCreate(0)
        } else {
            binding.genderRadio.position = 1
            dashboardViewModel.genderHandlerOnCreate(1)
        }

        // set weight edit text
        // TODO java.lang.IndexOutOfBoundsException: setSpan (4 ... 4) ends beyond length 3 hatasi
        Timber.d("weight length: ${user.weight}")
        val currentText = binding.weightEditText.text
        Timber.d("current text ${currentText?.length}")
        binding.weightEditText.setText(user.weight.toString())
        // set age edit text
        binding.ageEditText.setText(user.age.toString())
        // set water seek bar to water amount
        binding.waterSeekBar.setProgress(user.water.toFloat())
    }
}
