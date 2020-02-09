package com.afl.waterReminderDrinkAlarmMonitor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.afl.waterReminderDrinkAlarmMonitor.utils.DatabaseHelper
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.FragmentHomeBinding
import com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard.DashboardViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentHomeBinding

    val db by lazy {
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

        binding.drunkListLayout.removeAllViews()

        for (drink in drunks) {
            // Her bir drinkin adini, miktarini ve metrici getir
            val drinkType = drink.drink
            val drinkAmount = drink.amount.toString()
            val metric = drink.metric

            // linear layout parametreleri
            val param = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            param.gravity = Gravity.CENTER
            param.setMargins(16, 0, 16, 0)

            // her bir icecegi icinde tutacak bir linear layout olustur ve yukaridaki parametreleri uygula
            val linearLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = param
            }

            // iciecek adini tutacak text view olustur
            val drinkText = TextView(context).apply {
                text = when(drinkType) {
                    "water" -> getString(R.string.water)
                    "coffee" -> getString(R.string.coffee)
                    "tea" -> getString(R.string.tea)
                    "juice" -> getString(R.string.juice)
                    "soda" -> getString(R.string.soda)
                    "beer" -> getString(R.string.beer)
                    "wine" -> getString(R.string.wine)
                    "milk" -> getString(R.string.milk)
                    "yogurt" -> getString(R.string.yogurt)
                    "milkshake" -> getString(R.string.milkshake)
                    "energy" -> getString(R.string.energy)
                    "lemonade" -> getString(R.string.lemonade)
                    else -> "Water"
                }
                setTextColor(ContextCompat.getColor(context!!, R.color.reply_black_800))

                isAllCaps = true
                gravity = Gravity.CENTER
            }

            // iceceklerin miktarini tutacak text view olustur
            val amountText = TextView(context).apply {
                text = "$drinkAmount $metric"
                isAllCaps = true
                setPadding(5)
                gravity = Gravity.CENTER
            }

            // iceceklerin gorselini tutacak image button view olustur ve stylingi yap
            val imageView = ImageButton(context).apply {

                val imageID = resources.getIdentifier(
                    "com.afl.waterReminderDrinkAlarmMonitor:drawable/ic_${drinkType}_blue",
                    null,
                    null
                )
                setImageResource(imageID)
                setBackgroundColor(ContextCompat.getColor(context!!, R.color.reply_white_50))
                isClickable = true

                // kullanici icecege tikladiginda dialog box ac ve silmek istedigini sor
                setOnClickListener {

                    MaterialAlertDialogBuilder(context)
                        .setMessage(resources.getString(R.string.drink_image_button_dialog_message))
                        .setPositiveButton(
                            context.getString(R.string.drunk_list_action_dialog_yes_button),
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
            }

            // son olarak yularida olusturdugun viewlari once linear layouta ekle sonra linear layout'u da scrollable view icerisine ekle
            linearLayout.addView(imageView)
            linearLayout.addView(drinkText)
            linearLayout.addView(amountText)
            binding.drunkListLayout.addView(linearLayout)
        }
    }


}