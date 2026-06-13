package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.google.gson.annotations.SerializedName
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {
    private const val REPO_OWNER = "paoloesan"
    private const val REPO_NAME = "ln-translator-mobile"

    private data class GithubAsset(
        @SerializedName("name") val name: String?,
        @SerializedName("browser_download_url") val browserDownloadUrl: String?
    )

    private data class GithubLatestReleaseResponse(
        @SerializedName("tag_name") val tagName: String?,
        @SerializedName("body") val body: String?,
        @SerializedName("assets") val assets: List<GithubAsset>?
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
    private var jobDescarga: kotlinx.coroutines.Job? = null

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
    var urlDescargaActual by mutableStateOf<String?>(null)

    fun normalizarVersion(version: String?): String? {
        return version?.trim()?.removePrefix("v")?.takeIf { it.isNotBlank() }
    }

    fun obtenerVersionActual(context: Context): String? {
        if (versionActual != null) return versionActual
        
        // Clean up any downloaded APK on startup to free up space
        try {
            val file = File(context.cacheDir, "shared_novels/LNTranslator.apk")
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}

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

    fun verificarArchivoDescargado(context: Context): Boolean {
        val file = File(context.cacheDir, "shared_novels/LNTranslator.apk")
        descargaCompletada = file.exists()
        return descargaCompletada
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
                // Find download URL for LNTranslator.apk or any .apk asset
                val apkAsset = release.assets?.firstOrNull { it.name?.equals("LNTranslator.apk", ignoreCase = true) == true }
                    ?: release.assets?.firstOrNull { it.name?.endsWith(".apk", ignoreCase = true) == true }
                urlDescargaActual = apkAsset?.browserDownloadUrl

                // If the version is different, delete any old downloaded APK
                val file = File(context.cacheDir, "shared_novels/LNTranslator.apk")
                val savedVer = DataStoreManager.getString(context, "available_update_version")
                if (savedVer != versionStr) {
                    if (file.exists()) {
                        file.delete()
                    }
                }
                descargaCompletada = file.exists()

                // If there's a new version, update DataStore
                val versionActualNormalized = versionActual
                if (versionStr != versionActualNormalized) {
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

    fun iniciarDescarga(context: Context) {
        val url = urlDescargaActual ?: return
        if (progresoDescarga >= 0f || simulandoInstalacion) return

        progresoDescarga = 0f
        descargaCompletada = false
        simulandoInstalacion = false
        errorBusqueda = null

        jobDescarga = scopeActualizar.launch(Dispatchers.IO) {
            try {
                // Ensure output directory exists inside cacheDir (mapped to shared_novels in file_paths.xml)
                val dir = File(context.cacheDir, "shared_novels")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val outputFile = File(dir, "LNTranslator.apk")
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                // Follow redirects manually since GitHub release URLs redirect to AWS S3 (302 redirects)
                var connection = URL(url).openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                var status = connection.responseCode
                var redirectCount = 0
                while (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_SEE_OTHER) {
                    if (redirectCount > 5) break
                    val newUrl = connection.getHeaderField("Location")
                    connection.disconnect()
                    connection = URL(newUrl).openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = true
                    status = connection.responseCode
                    redirectCount++
                }

                if (status == HttpURLConnection.HTTP_OK) {
                    val contentLength = connection.contentLength
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(outputFile)
                    val buffer = ByteArray(4096)
                    var bytesRead: Long = 0
                    var read: Int

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesRead += read
                        if (contentLength > 0) {
                            progresoDescarga = bytesRead.toFloat() / contentLength
                        }
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                    connection.disconnect()

                    // Successful download
                    progresoDescarga = -1f
                    descargaCompletada = true
                } else {
                    errorBusqueda = "HTTP $status"
                    progresoDescarga = -1f
                }
            } catch (e: Exception) {
                errorBusqueda = e.localizedMessage ?: "Error"
                progresoDescarga = -1f
            }
        }
    }

    fun iniciarInstalacion(context: Context, strings: UiStrings) {
        val file = File(context.cacheDir, "shared_novels/LNTranslator.apk")
        if (!file.exists()) {
            errorBusqueda = strings.updateApkNotFound
            descargaCompletada = false
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            errorBusqueda = "${strings.updateApkNotFound} (${e.localizedMessage})"
        }
    }
}
