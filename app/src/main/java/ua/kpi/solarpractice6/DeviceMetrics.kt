package ua.kpi.solarpractice6

import android.content.Context
import android.os.BatteryManager
import android.app.ActivityManager
import android.os.Process
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

data class DeviceMetrics(val battery: Int, val appMemoryMb: Long, val availableMemoryMb: Long, val network: String)

object DeviceMetricsReader {
    fun read(context: Context): DeviceMetrics {
        val battery = (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also(am::getMemoryInfo)
        val usedBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
        val network = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Мобільна мережа"
            else -> "Немає підключення"
        }
        return DeviceMetrics(battery, usedBytes / 1024 / 1024, mem.availMem / 1024 / 1024, network)
    }
}
