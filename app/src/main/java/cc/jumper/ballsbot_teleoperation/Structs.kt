package cc.jumper.ballsbot_teleoperation

import com.google.gson.annotations.SerializedName
import java.util.*

data class ControllerState(
    @SerializedName("axes") val axes: List<Float>,
    @SerializedName("buttons") val buttons: List<Float>,
)

data class BotSettings(
    val cameras: List<String>,
    val distance_sensors: List<String>,
    val lidar: Boolean,
    val manipulator: Boolean,
    val pose: Boolean,
    val ups: Boolean,
)

data class DistanceSensorState(
    val ts: Double,
    val distance: Float,
    val direction: String,
    val offset_y: Float,
)

data class Pose(
    val ts: Double,
    val x: Double,
    val y: Double,
    val teta: Double,
    val self_ts: Double,
    val imu_ts: Double,
    val odometry_ts: Double,
)

data class RawAngle(
    val ts: Double,
    val value: Double,
)

data class AngleState(
    val real: Double,
    val intended: Double,
    val jammed: Byte?,
)

data class ManipulatorPose(
    val points: List<List<Double>>,
    val rotations: List<List<Double>>,
    val claw_points: List<List<Double>>,
)

data class ManipulatorState(
    val servo_positions: Map<String, Float>,
    val raw_angles: Map<String, RawAngle>,
    val angles: List<AngleState>,
    val current_pose: ManipulatorPose,
)

data class BotState(
    val lidar: List<List<Double>>,
    val distance_sensors: Map<String, DistanceSensorState>,
    val pose: Pose,
    val ups: Double,
    val manipulator: ManipulatorState,
)