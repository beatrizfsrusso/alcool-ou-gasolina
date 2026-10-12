package com.example.myapplication.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

/**
 * Acesso à localização e abertura do mapa.
 *
 * Usa o [LocationManager] do próprio Android (sem Google Play Services), o que é
 * suficiente para guardar a localização de um posto.
 */
object Localizacao {

    /** Tempo máximo esperando um fix novo antes de desistir. */
    private const val TIMEOUT_MS = 10_000L

    fun temPermissao(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    /** A posição mais recente já registrada no aparelho, ou `null` se não houver nenhuma. */
    @SuppressLint("MissingPermission")
    private fun ultimaConhecida(context: Context): Location? {
        if (!temPermissao(context)) return null
        val gerenciador =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        var melhor: Location? = null
        for (provedor in provedores(gerenciador)) {
            val local = try {
                gerenciador.getLastKnownLocation(provedor)
            } catch (e: SecurityException) {
                null
            } catch (e: IllegalArgumentException) {
                null
            }
            if (local != null && (melhor == null || local.time > melhor.time)) {
                melhor = local
            }
        }
        return melhor
    }

    /**
     * Entrega a localização atual do usuário. Usa a última posição conhecida quando
     * existe; caso contrário pede um fix novo aos provedores e espera até [TIMEOUT_MS].
     *
     * O callback é sempre chamado exatamente uma vez — com `null` quando não foi
     * possível obter a posição.
     */
    @SuppressLint("MissingPermission")
    fun obterLocalizacao(context: Context, onResultado: (Location?) -> Unit) {
        if (!temPermissao(context)) {
            onResultado(null)
            return
        }

        ultimaConhecida(context)?.let {
            onResultado(it)
            return
        }

        val gerenciador =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (gerenciador == null) {
            onResultado(null)
            return
        }

        val habilitados = provedores(gerenciador).filter {
            try {
                gerenciador.isProviderEnabled(it)
            } catch (e: IllegalArgumentException) {
                false
            }
        }
        if (habilitados.isEmpty()) {
            onResultado(null)
            return
        }

        val principal = Handler(Looper.getMainLooper())
        var finalizado = false

        lateinit var listener: LocationListener
        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (finalizado) return
                finalizado = true
                gerenciador.removeUpdates(this)
                principal.removeCallbacksAndMessages(null)
                onResultado(location)
            }
        }

        principal.postDelayed({
            if (!finalizado) {
                finalizado = true
                gerenciador.removeUpdates(listener)
                onResultado(null)
            }
        }, TIMEOUT_MS)

        try {
            for (provedor in habilitados) {
                gerenciador.requestLocationUpdates(
                    provedor, 0L, 0f, listener, Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            if (!finalizado) {
                finalizado = true
                principal.removeCallbacksAndMessages(null)
                onResultado(null)
            }
        }
    }

    private fun provedores(gerenciador: LocationManager): List<String> {
        val candidatos = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        return candidatos.filter { nome ->
            try {
                gerenciador.allProviders.contains(nome)
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Intent que abre as coordenadas em um app de mapas.
     * Retorna `null` quando nenhum app consegue tratar o esquema `geo:`.
     */
    fun intentMapa(
        context: Context,
        latitude: Double,
        longitude: Double,
        rotulo: String?
    ): Intent? {
        val coordenadas = Uri.encode("$latitude,$longitude")
        val marcador = rotulo?.takeIf { it.isNotBlank() }
            ?.let { Uri.encode("($it)") }
            .orEmpty()
        val uri = Uri.parse("geo:$coordenadas?q=$coordenadas$marcador")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return if (intent.resolveActivity(context.packageManager) != null) intent else null
    }
}
