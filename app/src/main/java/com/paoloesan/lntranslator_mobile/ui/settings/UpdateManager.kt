package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.annotations.SerializedName
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.time.Duration.Companion.milliseconds

object UpdateManager {
    private const val REPO_OWNER = "paoloesan"
    private const val REPO_NAME = "ln-translator-mobile"

    private data class GithubLatestReleaseResponse(
        @SerializedName("tag_name") val tagName: String?,
        @SerializedName("body") val body: String?
    )

    private interface GithubApiService {
        @GET("repos/$REPO_OWNER/$REPO_NAME/releases/latest")
        suspend fun getLatestRelease(): GithubLatestReleaseResponse
    }

    private val githubApiService: GithubApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubApiService::class.java)
    }

    private val scopeActualizar = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // State variables
    var versionActual: String? = null
        private set

    var nuevaVersion by mutableStateOf<String?>(null)
    var ultimaTagVersion by mutableStateOf<String?>(null)
    var cargando by mutableStateOf(false)
    var errorBusqueda by mutableStateOf<String?>(null)
    var contenidoCambios by mutableStateOf<String?>(null)
    var consultaRealizada by mutableStateOf(false)
    var progresoDescarga by mutableFloatStateOf(-1f)
    var descargaCompletada by mutableStateOf(false)
    var simulandoInstalacion by mutableStateOf(false)

    fun normalizarVersion(version: String?): String? {
        return version?.trim()?.removePrefix("v")?.takeIf { it.isNotBlank() }
    }

    fun obtenerVersionActual(context: Context): String? {
        if (versionActual != null) return versionActual
        return try {
            val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
                packageInfo.versionName
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }
            versionActual = normalizarVersion(versionName)
            versionActual
        } catch (_: Exception) {
            null
        }
    }

    suspend fun buscarActualizaciones(context: Context, strings: UiStrings) {
        cargando = true
        errorBusqueda = null
        consultaRealizada = true
        obtenerVersionActual(context)

        try {
            val release = githubApiService.getLatestRelease()
            ultimaTagVersion = release.tagName?.trim()
            val versionStr = normalizarVersion(release.tagName)
            nuevaVersion = versionStr
            contenidoCambios = release.body?.trim().takeUnless { it.isNullOrBlank() }

            if (nuevaVersion == null) {
                errorBusqueda = strings.updateNoPublishedVersion
            } else {
                // If there's a new version, update DataStore
                val versionActualNormalized = versionActual
                if (versionStr != versionActualNormalized) {
                    val savedVer = DataStoreManager.getString(context, "available_update_version")
                    if (savedVer != versionStr) {
                        DataStoreManager.putString(context, "available_update_version", versionStr)
                        DataStoreManager.putBoolean(context, "update_seen", false)
                    }
                } else {
                    // Up to date, clear stored version
                    DataStoreManager.remove(context, "available_update_version")
                    DataStoreManager.remove(context, "update_seen")
                }
            }
        } catch (http: HttpException) {
            errorBusqueda = if (http.code() == 404) strings.updateNoPublishedReleasesYet
            else strings.updateHttpError(http.code())
        } catch (_: Exception) {
            errorBusqueda = strings.updateConnectionError
        } finally {
            cargando = false
        }
    }

    fun comprobarActualizacionesSilenciosamente(context: Context) {
        scopeActualizar.launch(Dispatchers.IO) {
            try {
                obtenerVersionActual(context)
                val release = githubApiService.getLatestRelease()
                val versionStr = normalizarVersion(release.tagName)
                if (versionStr != null && versionStr != versionActual) {
                    val savedVer = DataStoreManager.getString(context, "available_update_version")
                    if (savedVer != versionStr) {
                        DataStoreManager.putString(context, "available_update_version", versionStr)
                        DataStoreManager.putBoolean(context, "update_seen", false)
                    }
                } else {
                    DataStoreManager.remove(context, "available_update_version")
                    DataStoreManager.remove(context, "update_seen")
                }
            } catch (_: Exception) {
                // Fail silently on background check
            }
        }
    }

    fun iniciarDescarga() {
        if (progresoDescarga >= 0f || simulandoInstalacion) return
        progresoDescarga = 0f
        simulandoInstalacion = false
        scopeActualizar.launch {
            for (i in 1..100) {
                delay(100.milliseconds)
                progresoDescarga = i / 100f
            }
            progresoDescarga = -1f
            descargaCompletada = true
        }
    }

    fun iniciarInstalacion() {
        if (progresoDescarga >= 0f || simulandoInstalacion) return
        simulandoInstalacion = true
        scopeActualizar.launch {
            delay(2000.milliseconds)
            simulandoInstalacion = false
            descargaCompletada = false
        }
    }
}
