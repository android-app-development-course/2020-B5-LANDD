package com.example.landd.ui.task

object TaskUtil {
    private val unitList = listOf("B", "K", "M", "G", "T")
    public fun pretty(size: Int): String {
        if (size < 0) {
            return "Unknown"
        }
        var pos = 0
        var fsize = size.toFloat()
        while (fsize > 1024 && pos < unitList.size - 1) {
            fsize /= 1024
            ++pos
        }
        return "%.1f${unitList[pos]}".format(fsize)
    }
}