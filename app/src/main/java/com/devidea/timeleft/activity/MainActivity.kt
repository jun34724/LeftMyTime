package com.devidea.timeleft.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import android.app.*
import android.content.Context
import android.content.SharedPreferences
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.devidea.timeleft.*
import com.devidea.timeleft.TopRecyclerView
import com.devidea.timeleft.alarm.AlarmReceiver
import com.devidea.timeleft.alarm.ItemAlarmManager
import com.devidea.timeleft.databinding.ActivityMainBinding
import com.devidea.timeleft.datadase.AppDatabase
import com.devidea.timeleft.datadase.itemdata.ItemEntity
import com.devidea.timeleft.viewmodels.TimeLeftViewModel
import com.devidea.timeleft.viewmodels.TimeLeftViewModelFactory
import kotlinx.coroutines.*
import java.time.LocalDateTime.*
import java.time.format.DateTimeFormatter
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val topItemListArray = ArrayList<AdapterItem>()
    private var backPressedTime: Long = 0

    private lateinit var binding: ActivityMainBinding //activity_main.xml
    private lateinit var viewModel: TimeLeftViewModel

    companion object {
        val ITEM_GENERATE: InterfaceItem = ItemGenerate()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.context())
        const val UPDATE_FLAG_UNABLE = 0
        const val UPDATE_FLAG_FOR_DAY = 1
        const val UPDATE_FLAG_FOR_MONTH = 2
        const val UPDATE_FLAG_FOR_TIME = 3

        const val ALARM_FLAG_UNABLE = 0
        const val ALARM_FLAG_ABLE = 1
        const val ALARM_FLAG_FOR_WEEKEND = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(
            this,
            TimeLeftViewModelFactory(AppDatabase.getDatabase(App.context()).itemDao())
        )[TimeLeftViewModel::class.java]
        val view = binding.root
        setContentView(view)

        initTopRecyclerView()
        initBottomRecyclerView()


        if (prefs.getString("theme", "") != "") {
            when (prefs.getString("theme", "")) {
                "밝게" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                "어둡게" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        binding.day.text = now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))

        viewModel.timeValue.observe(this) {
            binding.time.text = it.toString()
        }

        binding.timeAdd.setOnClickListener {
            val itemName = arrayOfNulls<String>(2)
            itemName[0] = "시간범위 지정하기"
            itemName[1] = "날짜 지정하기"
            AlertDialog.Builder(this)
                .setTitle("원하시는 종류를 선택해주세요")
                .setItems(itemName) { _, which ->
                    when (which) {
                        0 -> startActivity(
                            Intent(
                                applicationContext,
                                CreateTimeActivity::class.java
                            )
                        )
                        1 -> startActivity(
                            Intent(
                                applicationContext,
                                CreateDayActivity::class.java
                            )
                        )
                    }
                }
                .create().show()
        }

        binding.setting.setOnClickListener {
            val intent = Intent(application, SettingActivity::class.java)
            startActivity(intent)
        }

    }

    private fun initTopRecyclerView() {
        binding.recyclerview.layoutManager = LinearLayoutManager(
            this, RecyclerView.HORIZONTAL,
            false
        )

        topItemListArray.add(ITEM_GENERATE.timeItem())
        topItemListArray.add(ITEM_GENERATE.monthItem())
        topItemListArray.add(ITEM_GENERATE.yearItem())

        val topItemAdapter = TopRecyclerView(topItemListArray)
        binding.recyclerview.adapter = topItemAdapter

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.recyclerview)

        binding.indicator.attachToRecyclerView(binding.recyclerview, pagerSnapHelper)
        topItemAdapter.registerAdapterDataObserver(binding.indicator.adapterDataObserver)

        viewModel.recyclerViewValue.observe(this) {
            topItemAdapter.notifyItemChanged(0, it)
        }

    }

    private fun initBottomRecyclerView() {
        binding.recyclerview2.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        val bottomItemAdapter = BottomRecyclerView(refreshItem())
        binding.recyclerview2.adapter = bottomItemAdapter

        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                bottomItemAdapter.updateList(refreshItem())
                delay(1000)
            }
        }
    }

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        //뒤로가기 버튼 리스너에 쓰이는 변수
        val INTERVAL_TIME: Long = 2000
        if (intervalTime in 0..INTERVAL_TIME) {
            finish()
        } else {
            backPressedTime = tempTime
            Toast.makeText(applicationContext, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshItem(): ArrayList<AdapterItem> {
        val itemListArray = ArrayList<AdapterItem>()

        val itemList: List<ItemEntity> = AppDatabase.getDatabase(App.context()).itemDao().item
        for (i in itemList.indices) {
            if ((itemList[i].type == "Time")) {
                itemListArray.add(
                    ITEM_GENERATE.customTimeItem(
                        itemList[i]
                    )
                )
            } else {
                itemListArray.add(
                    ITEM_GENERATE.customMonthItem(
                        itemList[i]
                    )
                )
            }
        }

        return itemListArray
    }

}
