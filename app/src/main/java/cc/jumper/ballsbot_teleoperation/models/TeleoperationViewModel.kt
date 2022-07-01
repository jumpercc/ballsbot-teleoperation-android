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
import java.net.ConnectException
import java.time.Instant

val IDLE_CONTROLLER_STATE = ControllerState(
    axes = mutableListOf<Float>(0.0F, 0.0F, 0.0F, 0.0F),
    buttons = mutableListOf<Float>(
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

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val gson = Gson()
    private var controllerState: ControllerState = IDLE_CONTROLLER_STATE
    private var job: Job? = null
    private var token: String = ""
    private val apiService: ApiService by lazy { getApiService(connectionInfo) }
    private var _previousHttpErrorMessage: String? = null
    val previousHttpErrorMessage get() = _previousHttpErrorMessage
    var imagesCount = 0
    private var updatesPerSecond = 0
    private var botMode = "manual"

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
            updatesPerSecond = settings.updates_per_second
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
            _botState.postValue(
                apiService.postControllerState(
                    token,
                    gson.toJson(controllerState),
                    botMode
                )
            )
            null
        } catch (e: HttpException) {
            formatErrorMessage(e)
        }
    }

    fun setControllerButtonsState(buttonsState: Map<Int, Float>) {
        buttonsState.forEach() {
            controllerState.buttons[it.key] = it.value
        }
    }

    fun setControllerAxesState(axesState: Map<Int, Float>) {
        axesState.forEach() {
            controllerState.axes[it.key] = it.value
        }
    }

    fun setBotMode(newMode: String) {
        botMode = newMode
    }

    fun getBotMode(): String {
        return botMode
    }

    fun startUpdates() {
        stopUpdates()
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        job = scope.launch {
            try {
                auth()
                updateBotSettings()

                val timeStep = 1000 / updatesPerSecond  // millis
                var ts = Instant.now().toEpochMilli() + timeStep
                while (isActive) {
                    sendControllerState()
                    updateCameraImages()

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
            } catch (e: ConnectException) {
                _errorMessage.postValue("connection error")
            } catch (e: java.net.SocketTimeoutException) {
                _errorMessage.postValue("connection timeout")
            } catch (e: Throwable) {
                _errorMessage.postValue(e.toString())
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
        controllerState = IDLE_CONTROLLER_STATE
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