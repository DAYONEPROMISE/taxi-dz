package dz.taxidz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert
    suspend fun insert(trip: Trip): Long

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<Trip>>

    @Query("SELECT IFNULL(SUM(fareDzd), 0) FROM trips WHERE timestamp BETWEEN :from AND :to")
    fun observeSumBetween(from: Long, to: Long): Flow<Int>

    @Query("SELECT IFNULL(SUM(fareDzd), 0) FROM trips")
    fun observeTotal(): Flow<Int>
}
