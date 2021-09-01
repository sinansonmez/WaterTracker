package com.afl.waterReminderDrinkAlarmMonitor.ui.history

import android.app.Application
import android.content.Context
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.utils.*
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.*

private const val count = 30

class HistoryViewModel(private val app: Application) : AndroidViewModel(app) {

    private val db by lazy { DatabaseHelper(app.applicationContext) }

    private val dao = AppDatabase.getDatabase(app).dao()

    private val _text = MutableLiveData<String>().apply {
        value = "This is history fragment"
    }
    val text: LiveData<String> = _text

    private val _drinks = MutableLiveData<MutableList<Drink>>().apply {
        value = db.readDrinkDataDetailsDaySum()
    }
    val drinks: LiveData<MutableList<Drink>> = _drinks

    private val _monthNumber = MutableLiveData<Int>()
    val monthNumber = _monthNumber

    init {
        if (_drinks.value!!.isNotEmpty()) _monthNumber.value =
            monthStringToIntConverter(dateCollector()[0].split(" ")[0])
    }

    // function to prepare line data for chart based on live data _drinks
    fun generateLineData(): LineData {

        val selectedDrinks =
            _drinks.value?.filter {
                it.date.substring(5, 7).toInt() == _monthNumber.value
            } as MutableList

        val amountGraph = mutableListOf<Int>()

        // logic to create a list of amounts in the graph
        for (index in 1..31) {

            // eger index gunune ait bir tarihte database de kayit var ise onu filter ile ilgili gunun icerisinde tut
            val drinkIndexDay =
                selectedDrinks.filter { dateParser(it.date).day == index } as MutableList

            // Eger yukarida o gunde icecek var ise onu amount arrayine ekle yoksa 0 ekle
            if (drinkIndexDay.count() == 1) {
                amountGraph.add(drinkIndexDay[0].amount)
            } else amountGraph.add(0)
        }

        val d = LineData()

        // actual list which holds graph data
        val entries = ArrayList<Entry>()
        for (index in 0..count) {
            entries.add(
                Entry(index + 0.5f, amountGraph[index].toFloat())
            )
        }

        val set = LineDataSet(entries, "Daily Drunk Amount")
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawFilled(true)
        set.setDrawCircles(false)
        set.fillColor = ContextCompat.getColor(app.applicationContext, R.color.water_blue_100)
        set.color = ContextCompat.getColor(app.applicationContext, R.color.water_blue_100)
        set.setDrawValues(false)
        set.axisDependency = YAxis.AxisDependency.LEFT

        d.addDataSet(set)

        return d
    }

    // function to create a list of unique dates for spinner adapter
    fun dateCollector(): MutableList<String> {
        val reversedDrinkData = _drinks.value?.asReversed()
        // icecek icilen unique date listesini tutan liste
        val dateList = mutableListOf<String>()
        // unique datelerin ve toplam icilen miktar gibi diger bilgileri iceren ve
        // fonksiyonun icinde gelen drinkDatanin her birini dateListe ekle
        if (reversedDrinkData != null) {
            for (drink in reversedDrinkData) {
                if (!dateList.contains(drink.date)) {
                    dateList.add(drink.date)
                }
            }
        } else {
            throw Exception("User need to drink first, drink list is null")
        }

        // 2020-07-31 seklindeki datayi January 2020 seklinde tutan liste
        val formattedDateList = mutableListOf<String>()
        for (date in dateList) {
            val month = monthIntToStringConverter(dateParser(date).month)
            val year = dateParser(date).year
            val formattedDatetobeIncluded = "$month $year"
            if (!formattedDateList.contains(formattedDatetobeIncluded)) {
                formattedDateList.add(formattedDatetobeIncluded)
            }
        }
        return formattedDateList
    }

    // TODO(accessing string from viewmodel is risky because,
    //  viewmodel doesnt react to configuration changes, try to improve it)
    // If int version of the month (e.g 1,2 ) is given string version of a month (e.g January, February) is returned
    private fun monthIntToStringConverter(monthNumber: Int): String {
        var monthFormatted = ""

        when (monthNumber) {
            1 -> monthFormatted = app.getString(R.string.january)
            2 -> monthFormatted = app.getString(R.string.february)
            3 -> monthFormatted = app.getString(R.string.march)
            4 -> monthFormatted = app.getString(R.string.april)
            5 -> monthFormatted = app.getString(R.string.may)
            6 -> monthFormatted = app.getString(R.string.june)
            7 -> monthFormatted = app.getString(R.string.july)
            8 -> monthFormatted = app.getString(R.string.august)
            9 -> monthFormatted = app.getString(R.string.september)
            10 -> monthFormatted = app.getString(R.string.october)
            11 -> monthFormatted = app.getString(R.string.november)
            12 -> monthFormatted = app.getString(R.string.december)
        }

        return monthFormatted
    }

    // If string version of a month (e.g January, February) is given int version of the month (e.g 1,2 ) is returned
    fun monthStringToIntConverter(month: String): Int {
        return when (month) {
            app.getString(R.string.january) -> 1
            app.getString(R.string.february) -> 2
            app.getString(R.string.march) -> 3
            app.getString(R.string.april) -> 4
            app.getString(R.string.may) -> 5
            app.getString(R.string.june) -> 6
            app.getString(R.string.july) -> 7
            app.getString(R.string.august) -> 8
            app.getString(R.string.september) -> 9
            app.getString(R.string.october) -> 10
            app.getString(R.string.november) -> 11
            app.getString(R.string.december) -> 12
            else -> 0
        }
    }

    fun generateUIandDataForDrunkList(context: Context): LinearLayout {

        val viewGenerator = DrinksContainerGenerator()

        val linearLayoutForAllDrinkAndText =
            viewGenerator.createLinearLayout("vertical", context)

        // icecek icilen unique date listesini tutan liste
        val dateList = mutableListOf<String>()

        // reversed drink data but filtered with selected month
        val reversedDrinkData =
            _drinks.value?.asReversed()?.filter {
                it.date.substring(5, 7).toInt() == _monthNumber.value
            } as MutableList

        // unique datelerin ve toplam icilen miktar gibi diger bilgileri iceren ve
        // fonksiyonun icinde gelen drinkDatanin her birini dateListe ekle
        for (drink in reversedDrinkData) {
            if (!dateList.contains(drink.date)) {
                dateList.add(drink.date)
            }
        }

        // her bir tarih icin o gunde icilen icecekleri getir
        for (date in dateList) {

            val dateTextView = TextView(context).apply {

                val day = dateParser(date).day
                val month = monthIntToStringConverter(dateParser(date).month)
                val year = dateParser(date).year

                // tarihi 25 ocak 2020 seklinde yaziyor
                text = "$day $month $year"
                isAllCaps = true
                setPadding(16, 0, 0, 0)

                setBackgroundColor(ContextCompat.getColor(context, R.color.water_blue_50))
                setTextColor(ContextCompat.getColor(context, R.color.reply_white_50))
            }

            // her gun icindeki iceceklerin toplu halde bulundugu linear layout
            // sonra bunlari scroll edebilmek icin alttaki scroll view a koyuyorum
            val linearLayoutForSelectedDayDrinks =
                viewGenerator.createLinearLayout("horizontal", context)
            viewModelScope.launch {

                // o gunde icilen butun iceceklerin listesi
                val drinksInDate = Repository(dao).readDrinkDataDetailsSelectedDay(date)

                if (drinksInDate != null) {
                    withContext(Dispatchers.Main) {
                        // Her bir drinkin adini, miktarini ve metrici getir
                        for (drink in drinksInDate) {

                            val drinkType = drink.drink
                            val drinkAmount = drink.amount.toString()
                            val metric = drink.metric

                            // her bir icecegi icinde tutacak bir linear layout olustur ve yukaridaki parametreleri uygula
                            val linearLayoutForEachDrink =
                                viewGenerator.createLinearLayout("vertical", context)

                            // iciecek adini tutacak text view olustur
                            val drinkText =
                                viewGenerator.createTextViewForDrinks(context, drinkType)

                            // iceceklerin miktarini tutacak text view olustur
                            val amountText =
                                viewGenerator.createAmountTextViewForDrinks(
                                    context,
                                    drinkAmount,
                                    metric
                                )

                            // iceceklerin gorselini tutacak image button view olustur ve stylingi yap
                            val imageView =
                                viewGenerator.createImageViewForDrinks(context, drinkType)

                            // son olarak yularida olusturdugun viewlari once linear layouta ekle
                            // sonra linear layout'u da scrollable view icerisine ekle
                            linearLayoutForEachDrink.addView(imageView)
                            linearLayoutForEachDrink.addView(drinkText)
                            linearLayoutForEachDrink.addView(amountText)
                            linearLayoutForSelectedDayDrinks.addView(linearLayoutForEachDrink)
                        }
                    }
                }
            }

            // o gunde icilen butun icecekleri tutan scroll view
            val scrollViewForAllDrinksInSelectedDay =
                HorizontalScrollView(context).apply {
                    addView(linearLayoutForSelectedDayDrinks)
                }

            linearLayoutForAllDrinkAndText.addView(dateTextView)
            linearLayoutForAllDrinkAndText.addView(scrollViewForAllDrinksInSelectedDay)
        }
        return linearLayoutForAllDrinkAndText
    }
}
