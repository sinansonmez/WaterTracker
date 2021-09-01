package com.afl.waterReminderDrinkAlarmMonitor.ui.dashboard

import android.app.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ceryle.radiorealbutton.RadioRealButtonGroup
import com.afl.waterReminderDrinkAlarmMonitor.utils.DatabaseHelper
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.User
import com.xw.repo.BubbleSeekBar
import java.text.SimpleDateFormat
import java.util.*

// TODO coroutine gecisi tamamla
class DashboardViewModel(private val app: Application) : AndroidViewModel(app) {

    val db by lazy { DatabaseHelper(app.applicationContext) }

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    // variable to hold age data
    private val _age = MutableLiveData<String>()
    val age: LiveData<String> = _age

    // variable to hold gender data
    private val _gender = MutableLiveData<String>().apply { value = "Male" }
    val gender: LiveData<String> = _gender

    // variable to hold weight data
    private val _weight = MutableLiveData<String>()
    val weight: LiveData<String> = _weight

    // variable to hold waterAmount data
    private val _waterAmount = MutableLiveData<Int>()
    val waterAmount: LiveData<Int> = _waterAmount

    // variable to hold metric value
    private val _metric = MutableLiveData<String>().apply { value = "Metric" }
    val metric: LiveData<String> = _metric

    // variable to hold drunk amount
    private val _drunkAmount = MutableLiveData<Int>()
    val drunkAmount: LiveData<Int> = _drunkAmount

    // variable to hold drink amount from drinks fragment that user choose
    private val _drinkAmount = MutableLiveData<Int>().apply {
        val user = db.readData()
        value = if (user.metric == "American") 8 else 200
    }
    val drinkAmount: LiveData<Int> = _drinkAmount

    // variable to hold selected drink type
    private val _drinkType = MutableLiveData<String>().apply { value = "water" }
    val drinkType: LiveData<String> = _drinkType

    fun ageHandler(userAge: String) {
        _age.value = userAge
    }

    fun weightHandler(userWeight: String) {
        _weight.value = userWeight
    }

    // function to handle the gender selection
    val genderHandler = RadioRealButtonGroup.OnClickedButtonListener { _, position ->
        if (position == 0) _gender.value = "Male" else _gender.value = "Female"
    }

    // function to handle the gender selection on create
    fun genderHandlerOnCreate(pos: Int) {
        if (pos == 0) _gender.value = "Male" else _gender.value = "Female"
    }

    // function to handle the metric selection
    val metricHandler = RadioRealButtonGroup.OnClickedButtonListener { _, position ->
        if (position == 0) _metric.value = "Metric" else _metric.value = "American"
    }

    // function to handle the metric selection on create
    fun metricHandlerOnCreate(pos: Int) {
        if (pos == 0) _metric.value = "Metric" else _metric.value = "American"
    }

    // function to calculate to drink how much water to drink daily
    fun waterCalculate(): Float {
        val weightValue = if (_weight.value != "") {
            _weight.value?.toInt()
        } else return 0F

        val ageValue = if (_age.value != "") {
            _age.value?.toInt()
        } else return 0F

        _waterAmount.value = if (ageValue!! < 30) {
            weightValue!! * 40
        } else if (ageValue >= 30 || ageValue <= 55) {
            weightValue!! * 35
        } else {
            _waterAmount.value!! * 30
        }

        if (_gender.value == "Male") {
            _waterAmount.value = (_waterAmount.value!! * 1.05).toInt()
        }

        if (_metric.value == "American") {
            _waterAmount.value = (_waterAmount.value!! / 29.574).toInt()
        }

        val userFromDatabase = db.readData()

        // eger kullanicinin database de bir water i varsa tekrardan su hesaplama direk onu kaydet
        if (db.checkUserTableCount() == 1) {
            _waterAmount.value = userFromDatabase.water
        } else {
            _waterAmount.value
        }

        _text.value =
            _waterAmount.value.toString() + if (_metric.value == "American") " OZ" else " ML"

        val user = User(
            age = ageValue,
            weight = weightValue!!,
            gender = _gender.value!!,
            metric = _metric.value!!,
            water = _waterAmount.value!!
        )

        insertUpdateUser(user)

        return user.water.toFloat()
    }

    private fun insertUpdateUser(user: User) {

        // check if there is a current user in the database update it otherwise insert it
        if (db.checkUserTableCount() < 1) {
            db.insertData(user)
        } else {
            db.updateUser(user)
        }
    }

    private fun drunkAmountHandler() {
        _drunkAmount.value = db.readDrinkData()
    }

    fun drinkTypeHandler(drinkType: String) {
        _drinkType.value = drinkType
    }

    fun drink() {
        val user = db.readData()
        val drinkAmounts = when (_drinkType.value) {
            "water" -> 1.0
            "coffee" -> 0.8
            "tea" -> 0.85
            "juice" -> 0.55
            "soda" -> 0.6
            "beer" -> -1.6
            "wine" -> -1.6
            "milk" -> 0.78
            "yogurt" -> 0.5
            "milkshake" -> 0.55
            "energy" -> 0.6
            "lemonade" -> 0.8
            else -> 0.0
        }

        val date = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()
        val time = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time).toString()
        val drinkType = _drinkType.value.toString()

        val amount = (_drinkAmount.value!!.toInt() * drinkAmounts).toInt()
        val metric = if (user.metric == "American") "oz" else "ml"

        val drink = Drink(
            date = date, time = time, drink = drinkType, amount = amount, metric = metric
        )

        db.insertDrinkData(drink)
        drunkAmountHandler()

        _drinkType.value = ""
    }

    fun drinkAmountHandler(action: String) {
        val user = db.readData()

        if (user.metric == "American") {
            if (action == "plus") {
                _drinkAmount.value = _drinkAmount.value?.plus(1)
            } else {
                _drinkAmount.value = _drinkAmount.value?.minus(1)
                if (_drinkAmount.value!! < 1) _drinkAmount.value = 1
            }
        } else {
            if (action == "plus") {
                _drinkAmount.value = _drinkAmount.value?.plus(50)
            } else {
                _drinkAmount.value = _drinkAmount.value?.minus(50)
                if (_drinkAmount.value!! < 50) _drinkAmount.value = 50
            }
        }
    }

    fun resetDrinkAmount(value: Int) {
        _drinkAmount.value = value
    }

    val seekbarHandler = object : BubbleSeekBar.OnProgressChangedListener {
        override fun onProgressChanged(
            bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean
        ) {
            _waterAmount.value = progress
        }

        override fun getProgressOnActionUp(
            bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float
        ) {
        }

        override fun getProgressOnFinally(
            bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean
        ) {
        }
    }
}
