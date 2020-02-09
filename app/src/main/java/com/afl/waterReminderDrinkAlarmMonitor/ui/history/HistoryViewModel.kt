package com.afl.waterReminderDrinkAlarmMonitor.ui.history

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.utils.DatabaseHelper
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.utils.dateParser
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

private val count = 30


// TODO(Convert database request to coroutines)
class HistoryViewModel(private val app: Application) : AndroidViewModel(app) {

    private val db by lazy { DatabaseHelper(app.applicationContext) }

    private val _text = MutableLiveData<String>().apply {
        value = "This is history fragment"
    }
    val text: LiveData<String> = _text

    private val _drinks = MutableLiveData<MutableList<Drink>>().apply {
        value = db.readDrinkDataDetailsDaySum()
    }

    val drinks: LiveData<MutableList<Drink>> = _drinks

    //TODO(sifir yerine asagidaki yorumdaki gibi yap)
    //val monthForChart = monthStringToIntConverter(dateCollector(drinks)[0].split(" ")[0])
    private val _monthNumber = MutableLiveData<Int>().apply {
        value = monthStringToIntConverter(dateCollector()[0].split(" ")[0])
    }

    val monthNumber = _monthNumber

    // function to prepare line data for chart based on live data _drinks
    fun generateLineData(): LineData {

        //TODO(sadece monthnumbera gore degil month number ve year number a gore filtrelemen lazim)
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
        set.setDrawCircles(true)
        set.fillColor = ContextCompat.getColor(app.applicationContext, R.color.water_blue_100)
        set.fillAlpha = 255
        set.color = ContextCompat.getColor(app.applicationContext, R.color.water_blue_100)
        set.lineWidth = 2.5f
        set.setCircleColor(ContextCompat.getColor(app.applicationContext, R.color.water_blue_100))
        set.circleRadius = 4f
        set.fillColor = ContextCompat.getColor(app.applicationContext, R.color.water_blue_100)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawValues(true)
        set.valueTextSize = 10f
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.valueFormatter =
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }

        d.addDataSet(set)

        return d

    }

    // function to create a list of unique dates for spinner adapter
    fun dateCollector(): MutableList<String> {

        val reversedDrinkData = if (_drinks.value != null) {
            _drinks.value?.asReversed()
        } else {
            mutableListOf(Drink())
        }

        // icecek icilen unique date listesini tutan liste
        val dateList = mutableListOf<String>()

        // unique datelerin ve toplam icilen miktar gibi diger bilgileri iceren ve fonksiyonun icinde gelen drinkDatanin her birini dateListe ekle
        if (reversedDrinkData != null) {
            for (drink in reversedDrinkData) {
                if (!dateList.contains(drink.date)) {
                    dateList.add(drink.date)
                }
            }
        } else {
            throw Exception("User need to drink first, drink list is null")
        }

        // 01-02-2020 seklindeki datayi January 2020 seklinde tutan liste
        val formattedDateList = mutableListOf<String>()

        // unique listte tutan her bir tarihi January 2020 olarak formatlayip formattedDateList listesine ekle
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

    //TODO(accessing string from viewmodel is risky because, viewmodel doesnt react to configuration changes, try to improve it)
    // If int version of the month (e.g 1,2 ) is given string version of a month (e.g January, February) is returned
    fun monthIntToStringConverter(monthNumber: Int): String {
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

    //TODO(move drunkListCreator function to viewModel)
    fun generateUIandDataForDrunkList() {

    }

}
