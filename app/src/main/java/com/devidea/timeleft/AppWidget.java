package com.devidea.timeleft;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.room.Room;

import static com.devidea.timeleft.MainActivity.ITEM_GENERATE;
import static com.devidea.timeleft.MainActivity.appDatabase;

public class AppWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        int[] appWidgetIds;
        Log.d("widget", "onReceive() action = " + action);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase.class, "ItemData")
                        .allowMainThreadQueries()
                        .build();
            }

            appWidgetIds = appDatabase.DatabaseDao().get();

            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            }
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.d("widget", "appWidgetId is " + appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Intent intent = new Intent(context, AppWidget.class);

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        Log.d("widget", "alert on");

    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AppWidget.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);//알람 해제
        pendingIntent.cancel(); //인텐트 해제
        Log.d("widget", "alert off");
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        try {
            appDatabase.DatabaseDao().delete(appWidgetIds[0]);
        } catch (Exception e) {
            e.printStackTrace();
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase.class, "ItemData")
                        .allowMainThreadQueries()
                        .build();

                appDatabase.DatabaseDao().delete(appWidgetIds[0]);
            }
        }

        Log.d("widget", "onDeleted done");
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String type = null;
        try {

            type = appDatabase.DatabaseDao().getType(appWidgetId);
        } catch (Exception e) {
            e.printStackTrace();
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase.class, "ItemData")
                        .allowMainThreadQueries()
                        .build();

                type = appDatabase.DatabaseDao().getType(appWidgetId);
            }
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        Intent intentR = new Intent(context, AppWidget.class);
        intentR.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentR, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.refrash, pendingIntent);

        Intent appIntent=new Intent(context, MainActivity.class);
        PendingIntent pe=PendingIntent.getActivity(context, 0, appIntent, 0);
        views.setOnClickPendingIntent(R.id.percent, pe);

        if (type != null) {
            switch (type) {
                case "embedYear":

                    views.setTextViewText(R.id.summery, ITEM_GENERATE.yearItem().getSummery());
                    views.setTextViewText(R.id.percent, ITEM_GENERATE.yearItem().getPercentString() + "%");
                    views.setProgressBar(R.id.progress, 100, (int) Float.parseFloat(ITEM_GENERATE.yearItem().getPercentString()), false);

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    break;

                case "embedMonth":

                    views.setTextViewText(R.id.summery, ITEM_GENERATE.monthItem().getSummery());
                    views.setTextViewText(R.id.percent, ITEM_GENERATE.monthItem().getPercentString() + "%");
                    views.setProgressBar(R.id.progress, 100, (int) Float.parseFloat(ITEM_GENERATE.monthItem().getPercentString()), false);

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    break;

                case "embedTime":

                    views.setTextViewText(R.id.summery, ITEM_GENERATE.timeItem().getSummery());
                    views.setTextViewText(R.id.percent, ITEM_GENERATE.timeItem().getPercentString() + "%");
                    views.setProgressBar(R.id.progress, 100, (int) Float.parseFloat(ITEM_GENERATE.timeItem().getPercentString()), false);

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    break;

                default:
                    AdapterItem adapterItem;

                    //widgetID를 통해 TypeID 검색후 getSelectItem 쿼리를 통해 해당 아이템 객체 불러옴
                    EntityItemInfo entityItemInfo = appDatabase.DatabaseDao().getSelectItem(appDatabase.DatabaseDao().getTypeID(appWidgetId));
                    if (type.equals("Time")) {
                        adapterItem = ITEM_GENERATE.customTimeItem(entityItemInfo);
                    } else {
                        adapterItem = ITEM_GENERATE.customMonthItem(entityItemInfo);
                    }
                    views.setTextViewText(R.id.summery, adapterItem.getSummery());
                    views.setTextViewText(R.id.percent, adapterItem.getPercentString() + "%");
                    views.setProgressBar(R.id.progress, 100, (int) Float.parseFloat(adapterItem.getPercentString()), false);
                    appWidgetManager.updateAppWidget(appWidgetId, views);

            }
        }

        Log.d("widget", type + "update done");


    }

}