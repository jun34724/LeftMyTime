package com.devidea.timeleft

import android.content.Intent
import android.appwidget.AppWidgetManager
import android.app.PendingIntent
import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.*
import java.util.ArrayList

class AppWidgetConfigure : Activity() {
    private val appDatabase = AppDatabase.getInstance(App.context())

    /*
    var summitButton: Button? = null
    var spinner: Spinner? = null
    var checkBox: CheckBox? = null
    var radioGroup: RadioGroup? = null

     */
    var value: String = "0"
    var AppWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    var entityWidgetInfo: EntityWidgetInfo? = null
    var context: Context = this@AppWidgetConfigure
    var isFirstSelected: Boolean = false
    //var Preview: TextView? = null
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        //setResult = canceled 설정. 최종 summit 전 뒤로가기시 widget 취소
        setResult(RESULT_CANCELED)
        //위젯 레이아웃 설정
        setContentView(R.layout.appwidget_configure)
        // Intent에서 widget id 가져오기
        val intent: Intent = getIntent()
        val extras: Bundle? = intent.getExtras()
        if (extras != null) {
            AppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        // If they gave us an intent without the widget id, just bail.
        if (AppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        val mOnClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View) {
                Log.d("click", value)
                val context: Context = this@AppWidgetConfigure
                // appwidget 인스턴스 가져오기
                val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
                val views = RemoteViews(
                    context.getPackageName(),
                    R.layout.app_widget
                )

                //위젯에 새로고침 버튼 추가
                val intentR: Intent = Intent(context, AppWidget::class.java)
                intentR.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intentR,
                    PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.refrash, pendingIntent)
                val appIntent: Intent = Intent(context, MainActivity::class.java)
                val pe: PendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.percent, pe)
                when (value) {
                    "embedYear" -> {
                        views.setTextViewText(
                            R.id.summery,
                            MainActivity.ITEM_GENERATE.yearItem().summery
                        )
                        views.setTextViewText(
                            R.id.percent,
                            MainActivity.ITEM_GENERATE.yearItem().percentString + "%"
                        )
                        views.setProgressBar(
                            R.id.progress,
                            100,
                                MainActivity.ITEM_GENERATE.yearItem().percentString!!.toFloat().toInt(),
                            false
                        )
                        appWidgetManager.updateAppWidget(AppWidgetId, views)
                        entityWidgetInfo = EntityWidgetInfo(AppWidgetId, -1, value)
                        appDatabase!!.DatabaseDao()
                            .saveWidget(entityWidgetInfo)
                    }
                    "embedMonth" -> {
                        views.setTextViewText(
                            R.id.summery,
                            MainActivity.ITEM_GENERATE.monthItem().summery
                        )
                        views.setTextViewText(
                            R.id.percent,
                            MainActivity.ITEM_GENERATE.monthItem()
                                .percentString + "%"
                        )
                        views.setProgressBar(
                            R.id.progress,
                            100,
                                MainActivity.ITEM_GENERATE.monthItem().percentString
                                !!.toFloat().toInt(),
                            false
                        )
                        appWidgetManager.updateAppWidget(AppWidgetId, views)
                        entityWidgetInfo = EntityWidgetInfo(AppWidgetId, -1, value)
                        appDatabase!!.DatabaseDao()
                            .saveWidget(entityWidgetInfo)
                    }
                    "embedTime" -> {
                        views.setTextViewText(
                            R.id.summery,
                            MainActivity.ITEM_GENERATE.timeItem().summery
                        )
                        views.setTextViewText(
                            R.id.percent,
                            MainActivity.ITEM_GENERATE.timeItem().percentString + "%"
                        )
                        views.setProgressBar(
                            R.id.progress,
                            100,
                                MainActivity.ITEM_GENERATE.timeItem().percentString
                                !!.toFloat().toInt(),
                            false
                        )
                        appWidgetManager.updateAppWidget(AppWidgetId, views)
                        entityWidgetInfo = EntityWidgetInfo(AppWidgetId, -1, value)
                        appDatabase!!.DatabaseDao()
                            .saveWidget(entityWidgetInfo)
                    }
                    "0" -> {
                        setResult(RESULT_CANCELED)
                        Toast.makeText(context, "취소되었습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else -> {
                        val entityItemInfo: EntityItemInfo =
                            appDatabase!!.DatabaseDao()
                                .getSelectItem(value.toInt())
                        val adapterItem: AdapterItem
                        if ((entityItemInfo.type == "Time")) {
                            adapterItem =
                                MainActivity.ITEM_GENERATE.customTimeItem(entityItemInfo)
                        } else {
                            adapterItem =
                                MainActivity.ITEM_GENERATE.customMonthItem(entityItemInfo)
                        }
                        views.setTextViewText(R.id.summery, adapterItem.summery)
                        views.setTextViewText(R.id.percent, adapterItem.percentString + "%")
                        views.setProgressBar(
                            R.id.progress,
                            100,
                                adapterItem.percentString!!.toFloat().toInt(),
                            false
                        )
                        appWidgetManager.updateAppWidget(AppWidgetId, views)
                        entityWidgetInfo = EntityWidgetInfo(
                            AppWidgetId,
                            adapterItem.id,
                            entityItemInfo.type
                        )
                        appDatabase!!.DatabaseDao()
                            .saveWidget(entityWidgetInfo)
                    }
                }
                val resultValue: Intent = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            }
        }
        val radioGroupButtonChangeListener: RadioGroup.OnCheckedChangeListener =
            object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                    var Preview: TextView = findViewById(R.id.summery_preview)
                    when (checkedId) {
                        R.id.yearButton -> {
                            Preview.setText(
                                MainActivity.ITEM_GENERATE.yearItem().summery
                            )
                            value = "embedYear"
                        }
                        R.id.monthButton -> {
                            Preview.setText(
                                MainActivity.ITEM_GENERATE.monthItem().summery
                            )
                            value = "embedMonth"
                        }
                        R.id.timeButton -> {
                            Preview.setText(
                                MainActivity.ITEM_GENERATE.timeItem().summery
                            )
                            value = "embedTime"
                        }
                    }
                }
            }

        var summitButton: Button = findViewById(R.id.summit_button)
        var spinner: Spinner = findViewById(R.id.spinner)
        var checkBox: CheckBox = findViewById(R.id.checkBox)
        var radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        var Preview: TextView//? = null // = findViewById(R.id.summery_preview)

        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener)
        summitButton.setOnClickListener(mOnClickListener)
        spinner.setEnabled(false)
        val itemName: ArrayList<String?> = ArrayList()
        val entityItemInfo: List<EntityItemInfo?> =
            appDatabase!!.DatabaseDao().item
        for (i in appDatabase!!.DatabaseDao().item.indices) {
            itemName.add(
                appDatabase!!.DatabaseDao().item.get(i)?.summery
            )
        }
        checkBox.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (appDatabase!!.DatabaseDao().item.size != 0) {
                    if (checkBox.isChecked()) {
                        for (i in 0 until radioGroup.getChildCount()) {
                            radioGroup.getChildAt(i).setEnabled(false)
                        }
                        value = entityItemInfo.get(0)!!.id.toString()
                        Preview = findViewById(R.id.summery_preview)
                        Preview.setText(entityItemInfo.get(0)!!.summery)
                        spinner.setSelection(0)
                        isFirstSelected = true
                        spinner.setEnabled(true)
                    } else {
                        for (i in 0 until radioGroup.getChildCount()) {
                            radioGroup.getChildAt(i).setEnabled(true)
                        }
                        isFirstSelected = false
                        spinner.setEnabled(false)
                    }
                } else {
                    checkBox.setChecked(false)
                    Toast.makeText(context, "저장된 항목이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
        val adapter: ArrayAdapter<String?> =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, itemName)
        spinner.setAdapter(adapter)
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            public override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                Preview = findViewById(R.id.summery_preview)
                Preview.setText(entityItemInfo.get(position)!!.summery)
                if (isFirstSelected) {
                    Preview.setText(entityItemInfo.get(position)!!.summery)
                    value = entityItemInfo.get(position)!!.id.toString()
                    Log.d("value", value)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })


    }
}