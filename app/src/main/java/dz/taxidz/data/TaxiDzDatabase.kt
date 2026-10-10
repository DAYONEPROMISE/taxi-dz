package dz.taxidz.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Trip::class, FuelExpense::class], version = 1, exportSchema = false)
abstract class TaxiDzDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun fuelDao(): FuelDao
}
