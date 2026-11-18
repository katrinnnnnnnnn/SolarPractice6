package ua.kpi.solarpractice6

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.Locale

/** Навчальний набір показників; не є показниками фізичної електростанції. */
data class EnergyRecord(val hour: Int, val power: Int, val temperature: Double)

object EnergyData {
    fun sample(): List<EnergyRecord> = (0..23).map { hour ->
        val power = if (hour in 6..19) ((hour - 6) * (19 - hour) * 10).coerceAtLeast(0) else 0
        EnergyRecord(hour, power, 20.0 + hour / 4.0)
    }

    fun csv(rows: List<EnergyRecord>): String = buildString {
        appendLine("hour,power_w,temperature_c")
        rows.forEach { appendLine("${it.hour},${it.power},${String.format(Locale.US, "%.2f", it.temperature)}") }
    }

    fun parseCsv(value: String): List<EnergyRecord> = value.lineSequence().drop(1)
        .filter { it.isNotBlank() }.map { line ->
            val cols = line.split(',')
            require(cols.size == 3) { "Некоректна кількість стовпців CSV" }
            validate(EnergyRecord(cols[0].trim().toInt(), cols[1].trim().toInt(), cols[2].trim().toDouble()))
        }.toList()

    private val gson = Gson()
    fun json(rows: List<EnergyRecord>): String = gson.toJson(rows)
    fun parseJson(value: String): List<EnergyRecord> {
        val type = object : TypeToken<List<EnergyRecord>>() {}.type
        val records: List<EnergyRecord> = gson.fromJson(value, type)
        return records.map(::validate)
    }

    private fun validate(r: EnergyRecord): EnergyRecord {
        require(r.hour in 0..23 && r.power >= 0 && r.temperature.isFinite()) { "Некоректний показник" }
        return r
    }

    fun write(context: Context, name: String, content: String): File = File(context.filesDir, name).apply {
        writeText(content, Charsets.UTF_8)
    }
    fun read(context: Context, name: String): String = File(context.filesDir, name).readText(Charsets.UTF_8)
}
