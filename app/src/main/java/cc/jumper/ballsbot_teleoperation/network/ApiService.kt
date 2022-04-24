package cc.jumper.ballsbot_teleoperation.network

import android.net.Uri
import cc.jumper.ballsbot_teleoperation.BotSettings
import cc.jumper.ballsbot_teleoperation.BotState
import cc.jumper.ballsbot_teleoperation.CameraImage
import cc.jumper.ballsbot_teleoperation.ControllerState
import cc.jumper.ballsbot_teleoperation.data.Connection
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private fun getApiService(connectionInfo: Connection) : ApiService {
    val baseUri = Uri.Builder()
        .scheme("https")
        .authority("${connectionInfo.hostName}:${connectionInfo.port}")
        .build()
    val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(baseUri.toString())
        .build()
    return retrofit.create(ApiService::class.java)
}

interface ApiService {
    @POST("auth")
    suspend fun postAuth()

    @GET("settings")
    suspend fun getSettings(): BotSettings

    @GET("state")
    suspend fun getState(): BotState

    @GET("camera_image")
    suspend fun getCameraImage(cameraId: Int): CameraImage

    @POST("controller_state")
    suspend fun postControllerState(controllerState: ControllerState)
}