package cc.jumper.ballsbot_teleoperation.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.*
import cc.jumper.ballsbot_teleoperation.BotSettings
import cc.jumper.ballsbot_teleoperation.BotState
import cc.jumper.ballsbot_teleoperation.ControllerState
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.network.ApiService
import cc.jumper.ballsbot_teleoperation.network.getApiService
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.time.Instant

class TeleoperationViewModel() : ViewModel() {
    // empty stub to remove time dependencies
    var connectionInfo = Connection(0, "", "", 0, "", "")

    private val _connected = MutableLiveData<Boolean?>()
    val connected: LiveData<Boolean?> = _connected

    private val _botSettings = MutableLiveData<BotSettings?>()
    val botSettings: LiveData<BotSettings?> = _botSettings

    private val _botState = MutableLiveData<BotState?>()
    val botState: LiveData<BotState?> = _botState

    private val _cameraImages = MutableLiveData<List<Bitmap>>()
    val cameraImages: LiveData<List<Bitmap>> = _cameraImages

    private val gson = Gson()
    private var controllerState: ControllerState? = null
    private var job: Job? = null
    private var token: String = ""
    private val apiService: ApiService by lazy { getApiService(connectionInfo) }
    private var _previousHttpErrorMessage: String? = null
    val previousHttpErrorMessage get() = _previousHttpErrorMessage
    var imagesCount = 0

    private suspend fun auth() {
        try {
            token = apiService.postAuth(connectionInfo.key).token
            _connected.postValue(true)
            _previousHttpErrorMessage = null
        } catch (e: HttpException) {
            _connected.postValue(false)
            _previousHttpErrorMessage = formatErrorMessage(e)
        }
    }

    private fun formatErrorMessage(e: HttpException): String {
        return "${e.code()} ${e.message()} - ${e.response()?.errorBody()?.string()}"
    }

    fun checkConnection() {
        runBlocking {
            auth()
        }
    }

    private suspend fun updateBotSettings() {
        _previousHttpErrorMessage = try {
            val settings = apiService.getSettings(token)
            imagesCount = settings.cameras.size
            _botSettings.postValue(settings)
            null
        } catch (e: HttpException) {
            formatErrorMessage(e)
        }
    }

    private suspend fun updateCameraImages() {
        _previousHttpErrorMessage = try {
            val images = mutableListOf<Bitmap>()
            repeat(imagesCount) {
                val stream = apiService.getCameraImage(token, it).body()!!.byteStream()
                val decodedImage = BitmapFactory.decodeStream(stream)
                images.add(decodedImage)
            }
            _cameraImages.postValue(images)
            null
        } catch (e: HttpException) {
            formatErrorMessage(e)
        }
    }

    private suspend fun sendControllerState() {
        _previousHttpErrorMessage = try {
            val controllerState = ControllerState(
                // FIXME
                axes = listOf<Float>(0.0F, 0.0F, 0.0F, 0.0F),
                buttons = listOf<Float>(
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F
                ),
            )
            _botState.postValue(
                apiService.postControllerState(
                    token,
                    gson.toJson(controllerState)
                )
            )
            null
        } catch (e: HttpException) {
            formatErrorMessage(e)
        }
    }

    fun setControllerState(state: ControllerState) {
        controllerState = state.copy()
    }

    fun startUpdates() {
        stopUpdates()
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        job = scope.launch {
            try {
                auth()
                updateBotSettings()
            } catch (e: Throwable) {
                throw e // FIXME
            }

            val timeStep = 250  // millis
            var ts = Instant.now().toEpochMilli() + timeStep
            while (isActive) {
                try {
                    sendControllerState()
                    updateCameraImages()
                } catch (e: Throwable) {
                    throw e // FIXME
                }

                val newTs = Instant.now().toEpochMilli()
                var interval = ts - newTs
                if (interval > 0) {
                    ts += timeStep
                } else {
                    interval = 0
                    ts = newTs + timeStep
                }
                delay(interval)
            }
        }
    }

    fun updatesRunning(): Boolean {
        return (job != null)
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

class TeleoperationViewModelFactory() :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeleoperationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeleoperationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}