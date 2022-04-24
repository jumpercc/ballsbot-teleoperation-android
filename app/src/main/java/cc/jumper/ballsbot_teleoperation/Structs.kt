package cc.jumper.ballsbot_teleoperation

data class ControllerState(
    val axes: List<Float>,
    val buttons: List<Boolean>,
)

data class BotSettings(
    val cameras: List<String>,
    val distance_sensors: List<String>,
    val lidar: Boolean,
    val manipulator: Boolean,
    val pose: Boolean,
    val ups: Boolean,
)

data class BotState(
    val foo: Int, // TODO(BotState)
)

data class CameraImage(
    val foo: Int, // TODO(CameraImage)
)