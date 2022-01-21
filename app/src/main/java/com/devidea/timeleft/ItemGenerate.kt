package com.devidea.timeleft

import com.devidea.timeleft.datadase.AppDatabase
import com.devidea.timeleft.datadase.itemdata.ItemEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

class ItemGenerate : InterfaceItem {
    private val appDatabase = AppDatabase.getDatabase(App.context())

    override fun timeItem(): AdapterItem {
        val adapterItem = AdapterItem()
        val now = LocalTime.now()
        val endValue = LocalTime.parse("23:59:59")
        val secondsValue = Duration.between(LocalTime.of(0, 0), now).seconds.toFloat()
        val TimePercent = secondsValue / 86400 * 100
        adapterItem.summery = "오늘의 "
        adapterItem.percentString =
            String.format(Locale.getDefault(), "%.1f", TimePercent)
        adapterItem.leftDay = "남은시간: " + LocalTime.ofSecondOfDay(
            Duration.between(
                now,
                endValue
            ).seconds
        )
        return adapterItem
    }

    override fun yearItem(): AdapterItem {
        val adapterItem = AdapterItem()
        val YearPercent = LocalDate.now().dayOfYear.toFloat() / LocalDate.now().lengthOfYear()
            .toFloat() * 100
        adapterItem.summery = LocalDate.now().year.toString() + "년의 "
        adapterItem.percentString = String.format(Locale.getDefault(), "%.1f", YearPercent)
        adapterItem.leftDay = "남은일: " + (LocalDate.now()
            .lengthOfYear() - LocalDate.now().dayOfYear) + "일"
        return adapterItem
    }

    override fun monthItem(): AdapterItem {
        val adapterItem = AdapterItem()
        val monthOfDay = LocalDate.now().dayOfMonth
        val lengthOfMonth = LocalDate.now().lengthOfMonth()
        val summery = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.KOREAN)
        val MonthPercent = monthOfDay.toFloat() / lengthOfMonth * 100
        adapterItem.summery = summery + "의 "
        adapterItem.percentString = String.format(Locale.getDefault(), "%.1f", MonthPercent)
        adapterItem.leftDay = "남은일: " + (lengthOfMonth - monthOfDay) + "일"
        return adapterItem
    }

    override fun customTimeItem(itemEntity: ItemEntity?): AdapterItem {

        val adapterItem = AdapterItem()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
        val startValue = LocalTime.parse(itemEntity!!.startValue, formatter) //시작시간
        val endValue = LocalTime.parse(itemEntity.endValue, formatter) // 종료시간
        val time = LocalTime.now() //현재 시간
        adapterItem.isAutoUpdate = itemEntity!!.isAutoUpdate
        adapterItem.summery = itemEntity.summery
        adapterItem.startDay = "설정시간: $startValue"
        adapterItem.endDay = "종료시간: $endValue"
        adapterItem.id = itemEntity.id
        if (time.isAfter(startValue) && time.isBefore(endValue)) {
            val range = Duration.between(startValue, endValue).seconds.toFloat()
            val sendTime = Duration.between(startValue, time).seconds.toFloat()
            adapterItem.leftDay = "남은시간: " + LocalTime.ofSecondOfDay(
                Duration.between(
                    time,
                    endValue
                ).seconds
            )
            val timePercent = sendTime / range * 100
            adapterItem.percentString =
                String.format(Locale.getDefault(), "%.1f", timePercent)
        } else {
            adapterItem.percentString = "100"
            adapterItem.leftDay = "설정시간이 지나면 계산해 드릴께요"
        }
        return adapterItem
    }



    override fun customMonthItem(itemEntity: ItemEntity?): AdapterItem {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        var itemInfo = itemEntity
        val adapterItem = AdapterItem()
        var startDate = LocalDate.parse(itemInfo!!.startValue,formatter)
        var endDate = LocalDate.parse(itemInfo.endValue, formatter)
        val today = LocalDate.now()
        val id = itemInfo.id

        //설정일
        var setDay = ChronoUnit.DAYS.between(startDate, endDate).toInt()

        //설정일에서 지난일
        var sendDay = ChronoUnit.DAYS.between(startDate, today).toInt()

        //설정일까지 남은일
        var leftDay = ChronoUnit.DAYS.between(today, endDate).toInt()
        val lengthOfMonth = LocalDate.now().lengthOfMonth()

        //종료일로 넘어가고 자동 업데이트 체크한 경우
        if (today.isAfter(endDate) && itemInfo.isAutoUpdate) {
            //시작일: 종료일, 종료일: 종료일 + 설정일수로 UPDATE
            appDatabase!!.itemDao().updateItem(
                endDate.toString(),
                endDate.plusDays(lengthOfMonth.toLong()).toString(),
                itemInfo.id
            )
            itemInfo = appDatabase.itemDao().getSelectItem(id)
            startDate = LocalDate.parse(itemInfo.startValue)
            endDate = LocalDate.parse(itemInfo.endValue)
            setDay = ChronoUnit.DAYS.between(startDate, endDate).toInt()
            sendDay = ChronoUnit.DAYS.between(startDate, today).toInt()
            leftDay = ChronoUnit.DAYS.between(today, endDate).toInt()
        }
        val monthPercent = sendDay.toFloat() / setDay * 100
        adapterItem.startDay = "설정일: $startDate"
        adapterItem.endDay = "종료일: $endDate"
        adapterItem.leftDay = "남은일: D-$leftDay"
        adapterItem.isAutoUpdate = itemInfo.isAutoUpdate
        adapterItem.summery = itemInfo.summery
        adapterItem.id = itemInfo.id
        if (monthPercent < 100) {
            adapterItem.percentString =
                String.format(Locale.getDefault(), "%.1f", monthPercent)
        } else {
            adapterItem.percentString = "100"
        }
        return adapterItem
    }

}