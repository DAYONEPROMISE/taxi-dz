package dz.taxidz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {
    @Insert
    suspend fun insert(expense: FuelExpense): Long

    @Delete
    suspend fun delete(expense: FuelExpense)

    @Query("SELECT * FROM fuel_expenses ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<FuelExpense>>

    @Query("SELECT IFNULL(SUM(amountDzd), 0) FROM fuel_expenses WHERE timestamp BETWEEN :from AND :to")
    fun observeSumBetween(from: Long, to: Long): Flow<Int>

    @Query("SELECT IFNULL(SUM(amountDzd), 0) FROM fuel_expenses")
    fun observeTotal(): Flow<Int>
}
