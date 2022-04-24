package cc.jumper.ballsbot_teleoperation

import android.app.Application
import cc.jumper.ballsbot_teleoperation.data.ConnectionsRoomDatabase

class TeleoperationApplication : Application() {
    val database: ConnectionsRoomDatabase by lazy { ConnectionsRoomDatabase.getDatabase(this) }
}