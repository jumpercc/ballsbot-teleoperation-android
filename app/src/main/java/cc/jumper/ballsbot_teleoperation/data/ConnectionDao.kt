package cc.jumper.ballsbot_teleoperation.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: Connection)

    @Update
    suspend fun update(connection: Connection)

    @Delete
    suspend fun delete(connection: Connection)

    @Query("SELECT * from connection WHERE id = :id")
    fun getConnection(id: Int): Flow<Connection>

    @Query("SELECT * from connection ORDER BY description ASC")
    fun getConnections(): Flow<List<Connection>>
}