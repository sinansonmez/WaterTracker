package com.afl.waterReminderDrinkAlarmMonitor.ui.history

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.ui.utils.DatabaseHelper
import com.afl.waterReminderDrinkAlarmMonitor.ui.utils.Drink
import com.afl.waterReminderDrinkAlarmMonitor.ui.utils.dateParser
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

private val count = 30

class HistoryViewModel(private val app: Application) : AndroidViewModel(app) {

    val db by lazy { DatabaseHelper(app.applicationContext) }

    private val _text = MutableLiveData<String>().apply {
        value = "This is history fragment"
    }
    val text: LiveData<String> = _text

    private val _drinks = MutableLiveData<MutableList<Drink>>().apply {
        value = db.readDrinkDataDetailsDaySum().asReversed()
    }

    val drinks: LiveData<MutableList<Drink>> = _drinks

    //TODO(sifir yerine asagidaki yorumdaki gibi yap)
    //val monthForChart = monthStringToIntConverter(dateCollector(drinks)[0].split(" ")[0])
    private val _monthNumber = MutableLiveData<Int>().apply {
        value = 0
    }

    val monthNumber = _monthNumber

    // grafik icerisindeki datayi hazirlayan fonksyion
    fun generateLineData(drinks: MutableList<Drink>): LineData {

        //TODO(sadece monthnumbera gore degil month number ve year number a gore filtrelemen lazim)
        val selectedDrinks =
            _drinks.value?.filter { it.date.substring(5, 7).toInt() == _monthNumber.value } as MutableList

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


}
