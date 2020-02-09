package com.afl.waterReminderDrinkAlarmMonitor.ui.history

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.HistoryFragmentBinding
import com.afl.waterReminderDrinkAlarmMonitor.utils.DatabaseHelper
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.utils.dateParser
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter

class HistoryFragment : Fragment() {

    companion object {
        fun newInstance() =
            HistoryFragment()
    }

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var binding: HistoryFragmentBinding

    private val db by lazy {
        DatabaseHelper(
            this.requireContext()
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(inflater, R.layout.history_fragment, container, false)

        historyViewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.historyViewModel = historyViewModel

        // Specify the current activity as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = this

        val drinks = db.readDrinkDataDetailsDaySum()

        //bu mevuct icilen tarihlerden ilkinin ayinin rakam halini chart function a veriyor ve o ay icin chart olusturuluyor
        val monthForChart = monthStringToIntConverter(dateCollector(drinks)[0].split(" ")[0])

        // spinner icindeki tarihleri hazirlayan fonksiyon
        monthDropdownMenu(drinks)

        //grafigi olusturan fonksiyon
        chartFunction(container?.context!!, drinks, monthForChart)

        // grafik altindaki iceceklerin oldugu scroll viewlari hazirlayan fonksiyon
        drunkListCreator(container.context!!, drinks, monthForChart)

        return binding.root
    }

    //grafigi olusturan fonksiyon
    private fun chartFunction(context: Context, drinks: MutableList<Drink>, monthNumber: Int) {
        binding.drunkChart.description.isEnabled = false

        binding.drunkChart.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.reply_white_50
            )
        )

        binding.drunkChart.setDrawGridBackground(false)
        binding.drunkChart.setTouchEnabled(false)

        val l = binding.drunkChart.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val rightAxis = binding.drunkChart.axisRight
        rightAxis.isEnabled = false
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val leftAxis = binding.drunkChart.axisLeft
        leftAxis.isEnabled = true

        val limit = db.readData().water
        val ll = LimitLine(limit.toFloat(), "target")
        ll.lineColor = ContextCompat.getColor(context, R.color.reply_red_400)
        ll.lineWidth = 4f
        ll.textColor = ContextCompat.getColor(context, R.color.reply_red_400)
        ll.textSize = 12f

        leftAxis.addLimitLine(ll)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMaximum = limit.toFloat() + 500f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val xAxis = binding.drunkChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setCenterAxisLabels(true)

        xAxis.setDrawGridLines(false)
        xAxis.axisMinimum = 0f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        binding.drunkChart.data = historyViewModel.generateLineData(drinks)
        binding.drunkChart.invalidate()
    }

    // spinner icindeki tarihleri hazirlayan fonksiyon
    private fun monthDropdownMenu(drinkData: MutableList<Drink>) {

        val months = dateCollector(drinkData)

        val adapter = ArrayAdapter(context, R.layout.dropdown_menu_popup_item, months)
        binding.filledExposedDropdown.inputType = InputType.TYPE_NULL
        binding.filledExposedDropdown.setAdapter(adapter)
        binding.filledExposedDropdown.onItemClickListener = monthSpinnerListener
        binding.filledExposedDropdown.setText(adapter.getItem(0), false)
    }


    // listener for month spinner
    val monthSpinnerListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

            // split Ocak 2020 by space and pass month to value for chart generator
            val month = parent?.getItemAtPosition(pos).toString().split(" ")[0]

            // secimi yaptiktan sonra secilen ayin rakamaini chart function a ilet
            val monthNumber = monthStringToIntConverter(month)
            historyViewModel.monthNumber.value = monthNumber
            val drinks = db.readDrinkDataDetailsDaySum()
            chartFunction(context!!, drinks, monthNumber)

            // secimi yaptiktan sonra icilen icecekler listesini guncelle
            drunkListCreator(context,drinks,monthNumber)
        }

    }

    //TODO(List view a cevir)
    //TODO(spinnerdan secilen ayin iceceklerini getir)
    // grafik altindaki iceceklerin oldugu scroll viewlari hazirlayan fonksiyon
    private fun drunkListCreator(context: Context?, drinkData: MutableList<Drink>, monthNumber: Int) {

        binding.drunkFullListLayout.removeAllViews()

        // icecek icilen unique date listesini tutan liste
        val dateList = mutableListOf<String>()

        val reversedDrinkData = drinkData.reversed().filter {
            it.date.substring(5,7).toInt() == monthNumber
        }

        // { it.date.substring(5, 7).toInt() == monthNumber } as MutableList

        // unique datelerin ve toplam icilen miktar gibi diger bilgileri iceren ve fonksiyonun icinde gelen drinkDatanin her birini dateListe ekle
        for (drink in reversedDrinkData) {
            if (!dateList.contains(drink.date)) {
                dateList.add(drink.date)
            }
        }

        //her bir tarih icin o gunde icilen icecekleri getir
        for (date in dateList) {

            val dateTextView = TextView(context).apply {

                val day = dateParser(date).day
                val month = monthIntToStringConverter(dateParser(date).month)
                val year = dateParser(date).year

                // tarihi 25 ocak 2020 seklinde yaziyor
                text = "$day $month $year"
                isAllCaps = true
                setPadding(16, 0, 0, 0)

                setBackgroundColor(ContextCompat.getColor(context!!, R.color.water_blue_50))
                setTextColor(ContextCompat.getColor(context, R.color.reply_white_50))
            }

            // linear layout parametreleri
            val param = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            param.gravity = Gravity.CENTER
            param.setMargins(
                16,
                0,
                16,
                0
            )

            // her gun icindeki iceceklerin toplu halde bulundugu linear layout sonra bunlari scroll edebilmek icin alttaki scroll view a koyuyorum
            val linearLayoutForSelectedDayDrinks = LinearLayout(context).apply {
                id = View.generateViewId()
                orientation = LinearLayout.HORIZONTAL
                layoutParams = param
            }

            // o gunde icilen butun iceceklerin listesi
            val drinksInDate = db.readDrinkDataDetailsSelectedDay(date)

            // Her bir drinkin adini, miktarini ve metrici getir
            for (drink in drinksInDate) {

                val drinkType = drink.drink
                val drinkAmount = drink.amount.toString()
                val metric = drink.metric

                // linear layout parametreleri
                val paramForEachDrink = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                paramForEachDrink.gravity = Gravity.CENTER
                paramForEachDrink.setMargins(16, 0, 16, 0)

                // her bir icecegi icinde tutacak bir linear layout olustur ve yukaridaki parametreleri uygula
                val linearLayoutForEachDrink = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = paramForEachDrink
                }

                // iciecek adini tutacak text view olustur
                val drinkText = TextView(context).apply {
                    text = when (drinkType) {
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
                }

                // son olarak yularida olusturdugun viewlari once linear layouta ekle sonra linear layout'u da scrollable view icerisine ekle
                linearLayoutForEachDrink.addView(imageView)
                linearLayoutForEachDrink.addView(drinkText)
                linearLayoutForEachDrink.addView(amountText)
                linearLayoutForSelectedDayDrinks.addView(linearLayoutForEachDrink)

            }

            // o gunde icilen butun icecekleri tutan scroll view
            val scrollViewForAllDrinksInSelectedDay = HorizontalScrollView(context).apply {
                addView(linearLayoutForSelectedDayDrinks)
            }

            binding.drunkFullListLayout.addView(dateTextView)
            binding.drunkFullListLayout.addView(scrollViewForAllDrinksInSelectedDay)

        }
    }

    // function to create a list of unique dates for spinner adapter
    fun dateCollector(drinkData: MutableList<Drink>): MutableList<String> {

        val reversedDrinkData = drinkData.reversed()

        // icecek icilen unique date listesini tutan liste
        val dateList = mutableListOf<String>()

        // unique datelerin ve toplam icilen miktar gibi diger bilgileri iceren ve fonksiyonun icinde gelen drinkDatanin her birini dateListe ekle
        for (drink in reversedDrinkData) {
            if (!dateList.contains(drink.date)) {
                dateList.add(drink.date)
            }
        }

        // 01-02-2020 seklindeki datayi January 2020 seklinde tutan liste
        val formattedDateList = mutableListOf<String>()

        // unique listte tutan her bir tarihi January 2020 olarak formatlayip formattedDateList listesine ekle
        for (date in dateList) {

            val monthStringId = monthIntToStringConverter(dateParser(date).month)
            val month = monthIntToStringConverter(dateParser(date).month)
            val year = dateParser(date).year

            val formattedDatetobeIncluded = "$month $year"

            if (!formattedDateList.contains(formattedDatetobeIncluded)) {
                formattedDateList.add(formattedDatetobeIncluded)
            }
        }

        return formattedDateList

    }

    // bir ayin rakami verildiginde string olarak OCAK SUBAT degerini veriyor
    fun monthIntToStringConverter(monthNumber: Int): String {
        var monthFormatted = ""

        when (monthNumber) {
            1 -> monthFormatted = getString(R.string.january)
            2 -> monthFormatted = getString(R.string.february)
            3 -> monthFormatted = getString(R.string.march)
            4 -> monthFormatted = getString(R.string.april)
            5 -> monthFormatted = getString(R.string.may)
            6 -> monthFormatted = getString(R.string.june)
            7 -> monthFormatted = getString(R.string.july)
            8 -> monthFormatted = getString(R.string.august)
            9 -> monthFormatted = getString(R.string.september)
            10 -> monthFormatted = getString(R.string.october)
            11 -> monthFormatted = getString(R.string.november)
            12 -> monthFormatted = getString(R.string.december)
        }

        return monthFormatted
    }

    // bir ayin OCAK SUBAT gibi string degeri verildiginde int olarak 1,2,3 degeri veriyoe
    fun monthStringToIntConverter(month: String): Int {
        return when (month) {
            getString(R.string.january) -> 1
            getString(R.string.february) -> 2
            getString(R.string.march) -> 3
            getString(R.string.april) -> 4
            getString(R.string.may) -> 5
            getString(R.string.june) -> 6
            getString(R.string.july) -> 7
            getString(R.string.august) -> 8
            getString(R.string.september) -> 9
            getString(R.string.october) -> 10
            getString(R.string.november) -> 11
            getString(R.string.december) -> 12
            else -> 0
        }
    }

}
