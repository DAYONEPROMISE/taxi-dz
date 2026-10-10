package dz.taxidz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One taxi trip, entered manually (no GPS in v1). Fare in Algerian dinars (DZD). */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val departure: String,
    val destination: String,
    val fareDzd: Int,
    /** Epoch millis of the trip (defaults to creation time). */
    val timestamp: Long = System.currentTimeMillis()
)
