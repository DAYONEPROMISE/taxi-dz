package dz.taxidz

import dz.taxidz.data.FuelExpense
import dz.taxidz.data.Trip
import dz.taxidz.logic.EarningsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class EarningsCalculatorTest {

    private val utc = TimeZone.getTimeZone("UTC")

    @Test fun `sums fares and fuel and computes net`() {
        val trips = listOf(
            Trip(departure = "Alger", destination = "Oran", fareDzd = 500, timestamp = 1_000),
            Trip(departure = "Alger", destination = "Blida", fareDzd = 300, timestamp = 2_000)
        )
        val fuel = listOf(FuelExpense(amountDzd = 200, timestamp = 1_500))
        assertEquals(800, EarningsCalculator.sumFares(trips))
        assertEquals(200, EarningsCalculator.sumFuel(fuel))
        assertEquals(600, EarningsCalculator.netEarnings(trips, fuel))
    }

    @Test fun `empty lists give zero`() {
        assertEquals(0, EarningsCalculator.sumFares(emptyList()))
        assertEquals(0, EarningsCalculator.sumFuel(emptyList()))
        assertEquals(0, EarningsCalculator.netEarnings(emptyList(), emptyList()))
    }

    @Test fun `daily totals group by day oldest first`() {
        val day = 24L * 60 * 60 * 1000
        val base = EarningsCalculator.startOfDay(1_700_000_000_000L, utc)
        val trips = listOf(
            Trip(departure = "A", destination = "B", fareDzd = 100, timestamp = base + 1_000),
            Trip(departure = "A", destination = "C", fareDzd = 200, timestamp = base + 2_000),
            Trip(departure = "A", destination = "D", fareDzd = 400, timestamp = base - day + 3_000)
        )
        val totals = EarningsCalculator.dailyTotals(trips, days = 2, now = base + 5_000, tz = utc)
        assertEquals(2, totals.size)
        assertEquals(400, totals[0].totalDzd)
        assertEquals(1, totals[0].tripCount)
        assertEquals(300, totals[1].totalDzd)
        assertEquals(2, totals[1].tripCount)
    }

    @Test fun `weekly average divides 7-day sum by 7`() {
        val now = 1_700_000_000_000L
        val trips = listOf(Trip(departure = "A", destination = "B", fareDzd = 700, timestamp = now - 1_000))
        assertEquals(100.0, EarningsCalculator.weeklyAverage(trips, now), 0.001)
    }

    @Test fun `validation accepts good input and rejects bad`() {
        assertNull(EarningsCalculator.validateTrip("Alger", "Oran", 500))
        assertEquals("blank_route", EarningsCalculator.validateTrip("", "Oran", 500))
        assertEquals("blank_route", EarningsCalculator.validateTrip("Alger", "  ", 500))
        assertEquals("bad_fare", EarningsCalculator.validateTrip("Alger", "Oran", 0))
        assertEquals("bad_fare", EarningsCalculator.validateTrip("Alger", "Oran", -5))
        assertEquals("bad_fare", EarningsCalculator.validateTrip("Alger", "Oran", null))
        assertEquals("fare_too_big", EarningsCalculator.validateTrip("Alger", "Oran", 2_000_000))
        assertNull(EarningsCalculator.validateFuel(1500))
        assertEquals("bad_amount", EarningsCalculator.validateFuel(0))
        assertEquals("amount_too_big", EarningsCalculator.validateFuel(5_000_000))
    }

    @Test fun `range filters are inclusive`() {
        val trips = listOf(
            Trip(departure = "A", destination = "B", fareDzd = 1, timestamp = 100),
            Trip(departure = "A", destination = "B", fareDzd = 2, timestamp = 200),
            Trip(departure = "A", destination = "B", fareDzd = 3, timestamp = 300)
        )
        assertEquals(2, EarningsCalculator.filterBetweenTrips(trips, 100, 200).size)
        assertEquals(1, EarningsCalculator.filterBetweenTrips(trips, 201, 300).size)
    }
}
