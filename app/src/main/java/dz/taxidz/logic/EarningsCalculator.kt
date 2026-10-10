package dz.taxidz.logic

import dz.taxidz.data.FuelExpense
import dz.taxidz.data.Trip
import java.util.Calendar
import java.util.TimeZone

/** Pure, JVM-testable earnings math. All amounts in DZD. No Android dependencies. */
object EarningsCalculator {

    data class DailyTotal(val dayStartMillis: Long, val totalDzd: Int, val tripCount: Int)

    fun sumFares(trips: List<Trip>): Int = trips.sumOf { it.fareDzd }

    fun sumFuel(expenses: List<FuelExpense>): Int = expenses.sumOf { it.amountDzd }

    fun netEarnings(trips: List<Trip>, expenses: List<FuelExpense>): Int =
        sumFares(trips) - sumFuel(expenses)

    fun filterBetweenTrips(trips: List<Trip>, from: Long, to: Long): List<Trip> =
        trips.filter { it.timestamp in from..to }

    fun filterBetweenFuel(expenses: List<FuelExpense>, from: Long, to: Long): List<FuelExpense> =
        expenses.filter { it.timestamp in from..to }

    fun startOfDay(millis: Long, tz: TimeZone = TimeZone.getDefault()): Long {
        val c = Calendar.getInstance(tz).apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    /** Totals per day for the last [days] days ending at [now] (oldest first). */
    fun dailyTotals(
        trips: List<Trip>,
        days: Int = 7,
        now: Long = System.currentTimeMillis(),
        tz: TimeZone = TimeZone.getDefault()
    ): List<DailyTotal> {
        require(days >= 1)
        val today = startOfDay(now, tz)
        val dayMs = 24L * 60 * 60 * 1000
        return (days - 1 downTo 0).map { back ->
            val start = today - back * dayMs
            val end = start + dayMs - 1
            val inDay = trips.filter { it.timestamp in start..end }
            DailyTotal(start, inDay.sumOf { it.fareDzd }, inDay.size)
        }
    }

    fun weeklyAverage(trips: List<Trip>, now: Long = System.currentTimeMillis()): Double {
        val dayMs = 24L * 60 * 60 * 1000
        val week = trips.filter { it.timestamp in (now - 6 * dayMs)..now }
        return week.sumOf { it.fareDzd } / 7.0
    }

    /** Returns an error message key, or null when the trip input is valid. */
    fun validateTrip(departure: String, destination: String, fareDzd: Int?): String? {
        if (departure.isBlank() || destination.isBlank()) return "blank_route"
        if (fareDzd == null || fareDzd <= 0) return "bad_fare"
        if (fareDzd > 1_000_000) return "fare_too_big"
        return null
    }

    fun validateFuel(amountDzd: Int?): String? {
        if (amountDzd == null || amountDzd <= 0) return "bad_amount"
        if (amountDzd > 1_000_000) return "amount_too_big"
        return null
    }
}
