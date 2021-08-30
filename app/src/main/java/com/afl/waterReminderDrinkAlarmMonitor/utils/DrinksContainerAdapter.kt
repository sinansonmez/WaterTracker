package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.single_drink_container.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DrinksContainerAdapter(private val drinks: MutableList<Drink>) :
    RecyclerView.Adapter<CustomViewHolder>() {

    // number of items
    override fun getItemCount(): Int {
        return drinks.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val containerForDrinks = layoutInflater.inflate(R.layout.single_drink_container, parent, false)
        return CustomViewHolder(containerForDrinks)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        when (holder) {
            is CustomViewHolder -> { holder.bind(drinks[position]) }
        }
    }
}

class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val drinkNameText = view.drinkText
    private val amountText = view.amountText
    private val drinkImage = view.drinkImage

    fun bind(drink: Drink) {
        drinkNameText.text = drinkNameGetter(drink.drink, view.context)
        amountText.text = "${drink.amount} ${drink.metric}"

        val imageID = view.context.resources.getIdentifier(
            "com.afl.waterReminderDrinkAlarmMonitor:drawable/ic_${drink.drink}_blue",
            null,
            null
        )

        drinkImage.setImageResource(imageID)

        view.setOnClickListener {

            MaterialAlertDialogBuilder(view.context)
                .setMessage(view.context.resources.getString(R.string.drink_image_button_dialog_message))
                .setPositiveButton(view.context.getString(R.string.drunk_list_action_dialog_yes_button)) { dialogInterface, _ ->
                    val dao = AppDatabase.getDatabase(view.context).dao()
                    CoroutineScope(Dispatchers.IO).launch {
                        Repository(dao).deleteSelectedDrinkData(drink)
                        it.findNavController().navigate(R.id.action_navigation_home_self)
                    }
                }.setNegativeButton(view.context.getString(R.string.drunk_list_action_dialog_no_button)) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .show()
        }
    }
}

private fun drinkNameGetter(drink:String, context: Context): String {

    return when (drink) {
        "water" -> context.getString(R.string.water)
        "coffee" -> context.getString(R.string.coffee)
        "tea" -> context.getString(R.string.tea)
        "juice" -> context.getString(R.string.juice)
        "soda" -> context.getString(R.string.soda)
        "beer" -> context.getString(R.string.beer)
        "wine" -> context.getString(R.string.wine)
        "milk" -> context.getString(R.string.milk)
        "yogurt" -> context.getString(R.string.yogurt)
        "milkshake" -> context.getString(R.string.milkshake)
        "energy" -> context.getString(R.string.energy)
        "lemonade" -> context.getString(R.string.lemonade)
        else -> "Drink"
    }
}
