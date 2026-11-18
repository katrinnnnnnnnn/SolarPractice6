package ua.kpi.solarpractice6

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EnergyDataTest {
    @Test fun csvRoundTrip() {
        val records=listOf(EnergyRecord(8,250,22.5), EnergyRecord(9,400,23.0))
        assertEquals(records,EnergyData.parseCsv(EnergyData.csv(records)))
    }
    @Test fun jsonRoundTrip() {
        val records=listOf(EnergyRecord(10,750,28.5))
        assertEquals(records,EnergyData.parseJson(EnergyData.json(records)))
    }
    @Test fun invalidHourRejected() {
        assertThrows(IllegalArgumentException::class.java) { EnergyData.parseCsv("hour,power_w,temperature_c\n25,10,25.0\n") }
    }
}
