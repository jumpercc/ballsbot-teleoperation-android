package cc.jumper.ballsbot_teleoperation.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection")
data class Connection(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    @ColumnInfo(name = "host_name")
    val hostName: String,
    val port: Int,
    val key: String,
)