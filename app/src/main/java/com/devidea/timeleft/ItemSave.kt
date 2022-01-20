package com.devidea.timeleft

class ItemSave {
    private val appDatabase = AppDatabase.getInstance(App.context())

    fun saveMonthItem(summery: String, start: String, end: String, autoUpdate: Boolean) {
        val entityItemInfo = EntityItemInfo(
            "Month",
            start,
            end,
            summery,
            autoUpdate
        )
        writeDatabase(entityItemInfo)
    }

    fun saveTimeItem(
        summery: String,
        startValue: String,
        endValue: String,
        autoUpdate: Boolean
    ) {
        val entityItemInfo =
            EntityItemInfo("Time", startValue, endValue, summery, autoUpdate)
        writeDatabase(entityItemInfo)
    }

    private fun writeDatabase(entityItemInfo: EntityItemInfo) {
        appDatabase!!.DatabaseDao().saveItem(entityItemInfo)
        MainActivity.refreshItem()
    }
}