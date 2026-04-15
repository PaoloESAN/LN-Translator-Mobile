package com.paoloesan.lntranslator_mobile.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.gson.annotations.SerializedName
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class SeccionActualizar(
    override val icono: ImageVector,
    override val titulo: String,
    override val descripcion: String,
    override val contexto: Context
) : Seccion {

    companion object {
        private const val REPO_OWNER = "paoloesan"
        private const val REPO_NAME = "ln-translator-mobile"
        private const val RELEASE_URL =
            "https://github.com/$REPO_OWNER/$REPO_NAME/releases/latest"
    }

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

    private val versionActual by lazy { obtenerVersionActual() }
    private var nuevaVersion by mutableStateOf<String?>(null)
    private var ultimaTagVersion by mutableStateOf<String?>(null)
    private var cargando by mutableStateOf(false)
    private var errorBusqueda by mutableStateOf<String?>(null)
    private var contenidoCambios by mutableStateOf<String?>(null)
    private var mostrarModalCambios by mutableStateOf(false)
    private var consultaRealizada by mutableStateOf(false)

    private fun normalizarVersion(version: String?): String? {
        return version?.trim()?.removePrefix("v")?.takeIf { it.isNotBlank() }
    }

    private fun obtenerVersionActual(): String? {
        return try {
            val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val packageInfo = contexto.packageManager.getPackageInfo(
                    contexto.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
                packageInfo.versionName
            } else {
                contexto.packageManager.getPackageInfo(contexto.packageName, 0).versionName
            }
            normalizarVersion(versionName)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun buscarActualizaciones(strings: UiStrings) {
        cargando = true
        errorBusqueda = null
        consultaRealizada = true

        try {
            val release = githubApiService.getLatestRelease()
            ultimaTagVersion = release.tagName?.trim()
            nuevaVersion = normalizarVersion(release.tagName)
            contenidoCambios = release.body?.trim().takeUnless { it.isNullOrBlank() }

            if (nuevaVersion == null) {
                errorBusqueda = strings.updateNoPublishedVersion
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

    @Composable
    override fun ContenidoModal() {
        val scope = rememberCoroutineScope()
        val strings = LocalStrings.current

        LaunchedEffect(Unit) {
            if (!consultaRealizada) {
                buscarActualizaciones(strings)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = strings.updateCurrentVersion(versionActual ?: strings.updateUnknownVersion),
            )

            Spacer(modifier = Modifier.height(4.dp))

            when {
                cargando -> {
                    Text(
                        text = strings.updateChecking,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                errorBusqueda != null -> {
                    Text(
                        text = errorBusqueda.orEmpty(),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                nuevaVersion == null -> {
                    Text(
                        text = strings.updateNoPublishedVersion,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                nuevaVersion == versionActual -> {
                    Text(
                        text = strings.updateUpToDate,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                else -> {
                    Text(
                        text = strings.updateNewVersionAvailable(nuevaVersion.orEmpty()),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = RELEASE_URL,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        mostrarModalCambios = true
                        if ((!consultaRealizada || contenidoCambios == null) && !cargando) {
                            scope.launch { buscarActualizaciones(strings) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = strings.updateViewChangelogButton)
                }

                Button(
                    onClick = { scope.launch { buscarActualizaciones(strings) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(text = strings.updateCheckButton)
                }
            }
        }

        if (mostrarModalCambios) {
            AlertDialog(
                onDismissRequest = { mostrarModalCambios = false },
                title = {
                    Text(ultimaTagVersion ?: strings.updateLatestChangesTitle)
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        when {
                            cargando -> Text(strings.updateLatestChangesLoading)
                            contenidoCambios != null -> Text(contenidoCambios.orEmpty())
                            else -> Text(strings.updateLatestChangesEmpty)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { mostrarModalCambios = false }) {
                        Text(strings.buttonClose)
                    }
                }
            )
        }
    }

    override fun guardarCambios(cerrarModal: () -> Unit) {
        cerrarModal()
    }
}