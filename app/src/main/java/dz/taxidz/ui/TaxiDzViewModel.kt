package dz.taxidz.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dz.taxidz.data.FuelExpense
import dz.taxidz.data.TaxiDzDatabase
import dz.taxidz.data.Trip
import dz.taxidz.logic.EarningsCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatsUi(
    val todayTotal: Int = 0,
    val todayCount: Int = 0,
    val weekTotal: Int = 0,
    val weekFuel: Int = 0,
    val weekNet: Int = 0,
    val grandTotal: Int = 0,
    val last7: List<EarningsCalculator.DailyTotal> = emptyList()
)

class TaxiDzViewModel(app: Application) : AndroidViewModel(app) {
    private val db = Room.databaseBuilder(app, TaxiDzDatabase::class.java, "taxidz.db").build()
    private val trips = db.tripDao()
    private val fuel = db.fuelDao()

    val tripList: StateFlow<List<Trip>> =
        trips.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val fuelList: StateFlow<List<FuelExpense>> =
        fuel.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<StatsUi> = combine(tripList, fuelList) { t, f ->
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000
        val dayStart = EarningsCalculator.startOfDay(now)
        val today = t.filter { it.timestamp in dayStart..now }
        val weekTrips = t.filter { it.timestamp in (now - 6 * dayMs)..now }
        val weekFuel = f.filter { it.timestamp in (now - 6 * dayMs)..now }
        StatsUi(
            todayTotal = today.sumOf { it.fareDzd },
            todayCount = today.size,
            weekTotal = weekTrips.sumOf { it.fareDzd },
            weekFuel = weekFuel.sumOf { it.amountDzd },
            weekNet = weekTrips.sumOf { it.fareDzd } - weekFuel.sumOf { it.amountDzd },
            grandTotal = t.sumOf { it.fareDzd },
            last7 = EarningsCalculator.dailyTotals(t, 7, now)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUi())

    fun addTrip(departure: String, destination: String, fareDzd: Int) {
        viewModelScope.launch {
            trips.insert(Trip(departure = departure.trim(), destination = destination.trim(), fareDzd = fareDzd))
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch { trips.delete(trip) }
    }

    fun addFuel(amountDzd: Int, liters: Double?, note: String) {
        viewModelScope.launch {
            fuel.insert(FuelExpense(amountDzd = amountDzd, liters = liters, note = note.trim()))
        }
    }

    fun deleteFuel(expense: FuelExpense) {
        viewModelScope.launch { fuel.delete(expense) }
    }
}
