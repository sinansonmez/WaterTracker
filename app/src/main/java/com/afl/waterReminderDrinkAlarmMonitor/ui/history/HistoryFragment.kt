package com.afl.waterReminderDrinkAlarmMonitor.ui.history

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.databinding.HistoryFragmentBinding
import com.afl.waterReminderDrinkAlarmMonitor.utils.DatabaseHelper
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

        // spinner icindeki tarihleri hazirlayan fonksiyon
        monthDropdownMenu()

        //grafigi olusturan fonksiyon
        chartFunction(container?.context!!)

        // grafik altindaki iceceklerin oldugu scroll viewlari hazirlayan fonksiyon
        drunkListCreator(container.context!!)

        return binding.root
    }

    //grafigi olusturan fonksiyon
    private fun chartFunction(context: Context) {
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

            binding.drunkChart.data = historyViewModel.generateLineData()
            binding.drunkChart.invalidate()

    }

    // spinner icindeki tarihleri hazirlayan fonksiyon
    private fun monthDropdownMenu() {

        val months = historyViewModel.dateCollector()

        val adapter = ArrayAdapter(context, R.layout.dropdown_menu_popup_item, months)
        binding.filledExposedDropdown.inputType = InputType.TYPE_NULL
        binding.filledExposedDropdown.setAdapter(adapter)
        binding.filledExposedDropdown.onItemClickListener = monthSpinnerListener
        binding.filledExposedDropdown.setText(adapter.getItem(0), false)
    }

    // listener for month spinner
    private val monthSpinnerListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

            // split Ocak 2020 by space and pass month to value for chart generator
            val month = parent?.getItemAtPosition(pos).toString().split(" ")[0]

            // secimi yaptiktan sonra secilen ayin rakamaini chart function a ilet
            val monthNumber = historyViewModel.monthStringToIntConverter(month)
            historyViewModel.monthNumber.value = monthNumber
            chartFunction(context!!)

            // secimi yaptiktan sonra icilen icecekler listesini guncelle
            drunkListCreator(context!!)
        }

    }

    //TODO(List view a cevir)
    // grafik altindaki iceceklerin oldugu scroll viewlari hazirlayan fonksiyon
    private fun drunkListCreator(
        context: Context
    ) {

        binding.drunkFullListContainer.removeAllViews()

        val linearLayoutGeneratorForDrinks = historyViewModel.generateUIandDataForDrunkList(context)

        binding.drunkFullListContainer.addView(linearLayoutGeneratorForDrinks)

//        binding.drunkFullListContainer.addView(historyViewModel.prepareUI(context))


    }

}
