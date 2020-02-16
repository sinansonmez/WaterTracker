package com.afl.waterReminderDrinkAlarmMonitor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afl.waterReminderDrinkAlarmMonitor.MainActivity
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.FragmentHomeBinding
import com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard.DashboardViewModel
import com.afl.waterReminderDrinkAlarmMonitor.utils.*
import com.google.android.gms.ads.AdRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.dashboardViewModel = dashboardViewModel

        // Specify the current activity as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = this

        //TODO(bu calisiyor her yere uygula)
        val userData = dashboardViewModel.roomDenemeFun()
        Log.d("deneme", "userData: $userData")

        //admob setup
        // dummy ad banner id ca-app-pub-3940256099942544/6300978111
        // real ad banner id ca-app-pub-7954399632679605/9743680462
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val user = db.readData()
        val waterAmount = user.water

        // if user opens the app for the first time direct it to settings fragment
        if (waterAmount == 0) {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_setting)
        }

        // set metric and water amount based on the information from database
        val metric = if (user.metric == "American") " OZ" else " ML"
        binding.targetText.text = waterAmount.toString() + metric

        // setting information in progress circle
        val drunkAmount = db.readDrinkData()

        binding.wheelProgress.setStepCountText(
            drunkAmountPercTextFormatter(drunkAmount, waterAmount)
        )
        binding.wheelProgress.setPercentage(
            drunkAmountPercFormatter(drunkAmount, waterAmount)
        )
        binding.drunkText.text = drunkAmount.toString() + metric

        // dynamically creating drunk list
        drunkListCreator(container?.context)

        // icilmesi gereken su miktari degistikce progress wheeldaki su miktarini guncelliyor
        dashboardViewModel.waterAmount.observe(this, Observer { newAmount ->
            binding.wheelProgress.setStepCountText(newAmount.toString())
            binding.targetText.text = newAmount.toString()
        })

        // icilen miktar guncelledikce progress wheelini tamamlanma oraninini, wheel icindeki Perc texti ve altinda yazan drunk amountu guncelliyor
        dashboardViewModel.drunkAmount.observe(this, Observer { newDrunkAmount ->
            val newPerc = drunkAmountPercFormatter(newDrunkAmount, waterAmount)
            binding.wheelProgress.setPercentage(newPerc)

            val newPercText = drunkAmountPercTextFormatter(newDrunkAmount, waterAmount)
            binding.wheelProgress.setStepCountText(newPercText)

            binding.drunkText.text = newDrunkAmount.toString() + metric

            drunkListCreator(container?.context)

        })

        binding.drinksRecyclerView.layoutManager = LinearLayoutManager(container?.context, LinearLayoutManager.HORIZONTAL,false)
        binding.drinksRecyclerView.isNestedScrollingEnabled = false
        val today = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()
        val drunks = db.readDrinkDataDetailsSelectedDay(today)
//        val drunks = dao.readDrinkDataDetailsSelectedDay(today)
        binding.drinksRecyclerView.adapter = DrinksContainerAdapter(drunks)


        binding.drinkWaterButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_home_to_drinksFragment)
        }

        return binding.root
    }

    private fun drunkAmountPercFormatter(drunkAmount: Int, waterAmount: Int): Int {
        val perc = drunkAmount.toFloat() / waterAmount.toFloat()
        return (perc * 360F).toInt()

    }

    private fun drunkAmountPercTextFormatter(drunkAmount: Int, waterAmount: Int): String {
        val perc = drunkAmount.toFloat() / waterAmount.toFloat()
        val df = DecimalFormat("##%")
        return if (waterAmount == 0) "0%" else df.format(perc)
    }

    private fun drunkListCreator(context: Context?) {

        val today = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()

        val drunks = db.readDrinkDataDetailsSelectedDay(today)

        val viewGenerator = DrinksContainerGenerator()

//        binding.drunkListLayout.removeAllViews()

        for (drink in drunks) {
            // Her bir drinkin adini, miktarini ve metrici getir
            val drinkType = drink.drink
            val drinkAmount = drink.amount.toString()
            val metric = drink.metric

            val linearLayout = viewGenerator.createLinearLayout("vertical", context)

            // iciecek adini tutacak text view olustur
            val drinkText = viewGenerator.createTextViewForDrinks(context, drinkType)

            // iceceklerin miktarini tutacak text view olustur
            val amountText =
                viewGenerator.createAmountTextViewForDrinks(context, drinkAmount, metric)

            // iceceklerin gorselini tutacak image button view olustur ve stylingi yap
            val imageView = viewGenerator.createImageViewForDrinks(context, drinkType)

            imageView.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setMessage(resources.getString(R.string.drink_image_button_dialog_message))
                    .setPositiveButton(
                        context!!.getString(R.string.drunk_list_action_dialog_yes_button),
                        DialogInterface.OnClickListener { _, _ ->
                            db.deleteSelectedDrinkData(drink.id)
                            dashboardViewModel.drunkAmountHandler()
                        })
                    .setNegativeButton(
                        context.getString(R.string.drunk_list_action_dialog_no_button),
                        DialogInterface.OnClickListener { dialogInterface, _ ->
                            dialogInterface.cancel()
                        })
                    .show()
            }


            // son olarak yularida olusturdugun viewlari once linear layouta ekle sonra linear layout'u da scrollable view icerisine ekle
            linearLayout.addView(imageView)
            linearLayout.addView(drinkText)
            linearLayout.addView(amountText)
//            binding.drunkListLayout.addView(linearLayout)
        }

    }


}