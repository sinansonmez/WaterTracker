package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.single_drink_container.view.*

//TODO(secilen ay ile birlikte iceceklerin dogru gosterildigine emin ol)
// once ana sayfadaki drink listi hazirlayabilirsin
// https://www.youtube.com/watch?v=53BsyxwSBJk&list=PL0dzCUj1L5JGfHj1lwxOq67zAJV3e1S9S&index=3&t=0s
class DrinksContainerAdapter(private val drinks: MutableList<Drink>) :
    RecyclerView.Adapter<CustomViewHolder>() {

    // number of items
    override fun getItemCount(): Int {
        return drinks.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val containerForDrinks =
            layoutInflater.inflate(R.layout.single_drink_container, parent, false)
        return CustomViewHolder(containerForDrinks)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        when (holder) {
            is CustomViewHolder -> {
                holder.bind(drinks[position])
            }
        }

    }


}

//TODO(drink name i resource dan getir boylelikle farkl)
class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val db by lazy {
        DatabaseHelper(view.context)
    }

    val drinkNameText = view.drinkText
    val amountText = view.amountText
    val drinkImage = view.drinkImage

    fun bind(drink: Drink) {
        drinkNameText.text = drink.drink
        amountText.text = drink.amount.toString()

        val imageID = view.context.resources.getIdentifier(
            "com.afl.waterReminderDrinkAlarmMonitor:drawable/ic_${drink.drink}_blue",
            null,
            null
        )

        drinkImage.setImageResource(imageID)

        view.setOnClickListener {

            //TODO(database i rooma tasiyip drunk readDrinkData Livedata yaptiginda database degistiginde ana sayfa guncellenir )
            MaterialAlertDialogBuilder(view.context)
                .setMessage(view.context.resources.getString(R.string.drink_image_button_dialog_message))
                .setPositiveButton(
                    view.context.getString(R.string.drunk_list_action_dialog_yes_button),
                    DialogInterface.OnClickListener { _, _ ->
                        db.deleteSelectedDrinkData(drink.id)
//                    dashboardViewModel.drunkAmountHandler()
                        Log.d("database", "drink is deleted")
                    })
                .setNegativeButton(
                    view.context.getString(R.string.drunk_list_action_dialog_no_button),
                    DialogInterface.OnClickListener { dialogInterface, _ ->
                        dialogInterface.cancel()
                    })
                .show()

        }

    }


}

