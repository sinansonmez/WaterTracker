package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.Context
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.afl.waterReminderDrinkAlarmMonitor.R

class DrinksContainerGenerator {

    fun createLinearLayout(orientation: String, context: Context?): LinearLayout {

        // linear layout parametreleri
        val paramForAllDrinks = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        paramForAllDrinks.setMargins(16, 0, 16, 0)

        val linearLayoutForAllDrinkAndText = LinearLayout(context)

        linearLayoutForAllDrinkAndText.layoutParams = paramForAllDrinks
        linearLayoutForAllDrinkAndText.orientation =
            when (orientation) {
                "horizontal" -> LinearLayout.HORIZONTAL
                "vertical" -> LinearLayout.VERTICAL
                else -> LinearLayout.HORIZONTAL
            }

        return linearLayoutForAllDrinkAndText
    }

    fun createTextViewForDrinks(context: Context?, drinkType: String): TextView {

        // create the text view to store drink name
        return TextView(context).apply {
            text = when (drinkType) {
                "water" -> context?.getString(R.string.water)
                "coffee" -> context?.getString(R.string.coffee)
                "tea" -> context?.getString(R.string.tea)
                "juice" -> context?.getString(R.string.juice)
                "soda" -> context?.getString(R.string.soda)
                "beer" -> context?.getString(R.string.beer)
                "wine" -> context?.getString(R.string.wine)
                "milk" -> context?.getString(R.string.milk)
                "yogurt" -> context?.getString(R.string.yogurt)
                "milkshake" -> context?.getString(R.string.milkshake)
                "energy" -> context?.getString(R.string.energy)
                "lemonade" -> context?.getString(R.string.lemonade)
                else -> "Water"
            }

            setTextColor(ContextCompat.getColor(context!!, R.color.reply_black_800))

            isAllCaps = true
            gravity = Gravity.CENTER
        }
    }

    fun createAmountTextViewForDrinks(context: Context?, drinkAmount: String, metric: String): TextView {
        return TextView(context).apply {
            text = "$drinkAmount $metric"
            isAllCaps = true
            setPadding(5)
            gravity = Gravity.CENTER
        }
    }

    fun createImageViewForDrinks(context: Context?,drinkType: String): ImageButton {
        return ImageButton(context).apply {

            val imageID = resources.getIdentifier(
                "com.afl.waterReminderDrinkAlarmMonitor:drawable/ic_${drinkType}_blue",
                null,
                null
            )
            setImageResource(imageID)
            setBackgroundColor(ContextCompat.getColor(context!!, R.color.reply_white_50))
            isClickable = true
        }
    }
}
