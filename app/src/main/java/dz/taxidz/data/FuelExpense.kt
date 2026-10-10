package dz.taxidz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Fuel / expense entry. Amount in Algerian dinars (DZD). */
@Entity(tableName = "fuel_expenses")
data class FuelExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountDzd: Int,
    val liters: Double? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
