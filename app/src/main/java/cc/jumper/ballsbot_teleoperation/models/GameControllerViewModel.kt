package cc.jumper.ballsbot_teleoperation.models

import android.view.InputDevice
import androidx.lifecycle.*

enum class KeyName(val index: Int, val label: String) {
    KEY_A(0, "A"),
    KEY_B(1, "B"),
    KEY_X(2, "X"),
    KEY_Y(3, "Y"),
    KEY_LT(6, "LT"),
    KEY_LB(4, "LB"),
    KEY_RT(7, "RT"),
    KEY_RB(5, "RB"),
}

enum class AxisName(val index: Int, val label: String) {
    AXIS_L_HORIZONTAL(0, "LX"),
    AXIS_L_VERTICAL(1, "LY"),
    AXIS_R_HORIZONTAL(2, "RX"),
    AXIS_R_VERTICAL(3, "RY"),
}

class GameControllerViewModel() : ViewModel() {
    private var _keyStates = MutableLiveData(
        KeyName.values().associate { it to false }.toMutableMap()
    )
    val keyStates: LiveData<Map<KeyName, Float>> = _keyStates.map { item ->
        item.map { it.key to toFloatKeyState(it.value) }.toMap()
    }

    private var _axisStates = MutableLiveData(
        AxisName.values().associate { it to 0.0F }.toMutableMap()
    )
    val axisStates: LiveData<Map<AxisName, Float>> = _axisStates.map {
        it.toMap()
    }

    fun getControllerId(): String {
        val idsList = getGameControllerIds()
        return if (idsList.isEmpty()) {
            "None"
        } else {
            idsList.joinToString()
        }
    }

    private fun getGameControllerIds(): List<Int> {
        val gameControllerDeviceIds = mutableListOf<Int>()
        val deviceIds = InputDevice.getDeviceIds()
        deviceIds.forEach { deviceId ->
            InputDevice.getDevice(deviceId).apply {

                // Verify that the device has gamepad buttons, control sticks, or both.
                if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD
                    || sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
                ) {
                    // This device is a game controller. Store its device ID.
                    gameControllerDeviceIds
                        .takeIf { !it.contains(deviceId) }
                        ?.add(deviceId)
                }
            }
        }
        return gameControllerDeviceIds
    }

    fun setKeyPressedState(key: KeyName, pressed: Boolean) {
        _keyStates.mutation {  // to trigger LiveData update
            it.value?.set(key, pressed)
        }
    }

    fun setAxisStates(xl: Float, yl: Float, xr: Float, yr: Float, lt: Float, rt: Float) {
        _axisStates.mutation {
            it.value?.set(AxisName.AXIS_L_HORIZONTAL, xl)
            it.value?.set(AxisName.AXIS_L_VERTICAL, yl)
            it.value?.set(AxisName.AXIS_R_HORIZONTAL, xr)
            it.value?.set(AxisName.AXIS_R_VERTICAL, yr)
        }
        _keyStates.mutation {
            it.value?.set(KeyName.KEY_RT, rt == 1f)
            it.value?.set(KeyName.KEY_LT, lt == 1f)
        }
    }
}

class GameControllerViewModelFactory() :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameControllerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameControllerViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun <K, V> Map<K, V>.toMutableMap(): MutableMap<K, V> {
    return HashMap(this)
}

fun toFloatKeyState(bValue: Boolean): Float {
    return if (bValue) {
        1f
    } else {
        0f
    }
}

fun <T> MutableLiveData<T>.mutation(actions: (MutableLiveData<T>) -> Unit) {
    actions(this)
    this.value = this.value
}