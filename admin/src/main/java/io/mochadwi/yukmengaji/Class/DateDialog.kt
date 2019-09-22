package io.mochadwi.yukmengaji.Class

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import java.util.*

@SuppressLint("ValidFragment")
class DateDialog(view: View) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    internal var txtDate: EditText

    init {

        txtDate = view as EditText
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        val date = day.toString() + "-" + (month + 1) + "-" + year
        txtDate.setText(date)
    }
}
