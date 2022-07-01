package cc.jumper.ballsbot_teleoperation.network

import android.net.Uri
import cc.jumper.ballsbot_teleoperation.BotSettings
import cc.jumper.ballsbot_teleoperation.BotState
import cc.jumper.ballsbot_teleoperation.data.Connection
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.decodeCertificatePem
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private fun getHttpClientWithMyCertificate(certificateText: String): OkHttpClient {
    val certificate = certificateText.decodeCertificatePem()
    val certificates: HandshakeCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(certificate)
        // Uncomment if standard certificates are also required.
        // .addPlatformTrustedCertificates()
        .build()
    return OkHttpClient.Builder()
        .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
        .build()
}

fun getApiService(connectionInfo: Connection): ApiService {
    val baseUri = Uri.Builder()
        .scheme("https")
        .encodedAuthority("${connectionInfo.hostName}:${connectionInfo.port}")
        .build()
    val retrofit = Retrofit.Builder()
        .client(getHttpClientWithMyCertificate(connectionInfo.certificate))
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(baseUri.toString())
        .build()
    return retrofit.create(ApiService::class.java)
}

data class AuthResponse(
    val token: String
)

interface ApiService {
    @FormUrlEncoded
    @POST("auth")
    suspend fun postAuth(
        @Field("key") key: String
    ): AuthResponse

    @GET("settings")
    suspend fun getSettings(
        @Query("token") token: String
    ): BotSettings

    @FormUrlEncoded
    @POST("controller_state")
    suspend fun postControllerState(
        @Field("token") token: String,
        @Field("controller_state") controllerState: String,
        @Field("mode") mode: String
    ): BotState

    @Streaming
    @GET("camera_image")
    suspend fun getCameraImage(
        @Query("token") token: String,
        @Query("image_index") cameraId: Int
    ): Response<ResponseBody>
}

fun isCertificateValid(certificate: String): Boolean {
    return try {
        certificate.decodeCertificatePem()
        true
    } catch (e: Throwable) {
        false
    }
}