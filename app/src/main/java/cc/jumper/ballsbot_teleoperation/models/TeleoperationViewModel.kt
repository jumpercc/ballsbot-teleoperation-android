package cc.jumper.ballsbot_teleoperation.models

import androidx.lifecycle.*
import cc.jumper.ballsbot_teleoperation.BotSettings
import cc.jumper.ballsbot_teleoperation.BotState
import cc.jumper.ballsbot_teleoperation.CameraImage
import cc.jumper.ballsbot_teleoperation.ControllerState
import cc.jumper.ballsbot_teleoperation.data.Connection
import kotlinx.coroutines.*
import java.time.Instant

class TeleoperationViewModel(private val connectionInfo: Connection) : ViewModel() {
    private val _connected = MutableLiveData<Boolean?>()
    val connected: LiveData<Boolean?> = _connected

    private val _botSettings = MutableLiveData<BotSettings?>()
    val botSettings: LiveData<BotSettings?> = _botSettings

    private val _botState = MutableLiveData<BotState?>()
    val botState: LiveData<BotState?> = _botState

    private val _cameraImages = MutableLiveData<List<CameraImage>>()
    val cameraImages: LiveData<List<CameraImage>> = _cameraImages

    private var controllerState: ControllerState? = null
    private var job: Job? = null
    private var sleepInterval: Long = 500

    private fun auth() {
        // TODO(auth)
        _connected.value = true
    }

    fun isConnectionOk(): Boolean {
        auth()
        return connected.value!!
    }

    private fun updateBotSettings() {
        // TODO(getSettings)
    }

    private fun updateBotState() {
        // TODO(getState)
    }

    private fun updateCameraImages() {
        // TODO(updateCameraImages)
    }

    private fun sendControllerState() {
        // TODO(sendControllerState)
    }

    fun setControllerState(state: ControllerState) {
        controllerState = state.copy()
    }

    fun startUpdates() {
        stopUpdates()
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        job = scope.launch {
            auth()
            updateBotSettings()

            val timeStep = 500  // millis
            var ts = Instant.now().toEpochMilli() + timeStep
            var even = true
            while (isActive) {
                sendControllerState()
                if (even) {
                    updateBotState()
                    updateCameraImages()
                }
                even = !even

                val newTs = Instant.now().toEpochMilli()
                val interval = ts - newTs
                if (interval > 0) {
                    sleepInterval = interval
                    ts += timeStep
                } else {
                    sleepInterval = 0
                    ts = newTs + timeStep
                }
                delay(sleepInterval)
            }
        }
    }

    fun stopUpdates() {
        job?.cancel()
        job = null
        _connected.value = null
        _botSettings.value = null
        _botState.value = null
        _cameraImages.value = listOf()
        controllerState = null
    }
}

class TeleoperationViewModelFactory(private val connectionInfo: Connection) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeleoperationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeleoperationViewModel(connectionInfo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}