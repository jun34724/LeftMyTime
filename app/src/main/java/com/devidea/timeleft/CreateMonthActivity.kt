package com.devidea.timeleft

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.DatePickerDialog.OnDateSetListener
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import android.widget.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CreateMonthActivity : AppCompatActivity() {

    lateinit var startDay : String
    lateinit var endDay : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_month)

        val inputTitle = findViewById<EditText>(R.id.input_summery)
        val inputStartDay = findViewById<EditText>(R.id.input_start_day)
        val inputEndDay = findViewById<EditText>(R.id.input_end_day)
        val inputUpdateRate = findViewById<EditText>(R.id.input_update_rate)
        val btnSave = findViewById<Button>(R.id.save)

        val itemSave = ItemSave()

        inputStartDay.setOnClickListener { v ->
            val datePickerDialog = DatePickerDialog(
                this, {datePicker, year, month, day ->
                    var monthis = month+1
                    startDay = "$year-$monthis-$day"
                    inputStartDay.setText(startDay)
                      },
                LocalDate.now().year, LocalDate.now().monthValue-1, LocalDate.now().dayOfMonth)
            datePickerDialog.show()
        }

        inputEndDay.setOnClickListener { v ->
            val timePickerDialog = DatePickerDialog(
                this, {datePicker, year, month, day ->
                    var monthis = month+1
                    endDay = "$year-$monthis-$day"
                    inputEndDay.setText(endDay)
                      },
                LocalDate.now().year, LocalDate.now().monthValue-1, LocalDate.now().dayOfMonth)
            timePickerDialog.show()
        }

        //isInitialized is able instance variable, not a local variable.
        btnSave.setOnClickListener {
            if (::startDay.isInitialized && ::endDay.isInitialized && inputTitle.length()>0){
                itemSave.saveMonthItem(inputTitle.text.toString(), startDay, endDay, 2, 20)
                finish()
            }else{
                Toast.makeText(this,"입력확인",Toast.LENGTH_SHORT).show()
            }}

    }
}