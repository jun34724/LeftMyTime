package com.devidea.timeleft;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Date;
import java.util.Locale;

public class TimeInfoMonth implements TimeInfo{

    @Override
    public AdapterItem setTimeItem() {
        AdapterItem adapterItem = new AdapterItem();

        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat format_month_day = new SimpleDateFormat("d", Locale.KOREA); //해당달의 일수 format
        YearMonth yearMonth = YearMonth.from(LocalDate.now());

        int month_day = Integer.parseInt(format_month_day.format(date)); //오늘 일수
        int lengthOfMon = yearMonth.lengthOfMonth(); //해당 달의 총 일수

        Log.d("format_Year", String.valueOf(LocalDate.now()));

        float MonthPercent = (float) month_day / lengthOfMon * 100;

        adapterItem.setSummery("Month Left is ");
        adapterItem.setPercentString(String.format(Locale.getDefault(), "%.1f", MonthPercent));

        return adapterItem;
    }
}
