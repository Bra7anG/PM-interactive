package com.pminteractive.pmtours.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkMonitor {

    private val _estaConectado = MutableStateFlow(true)
    val estaConectado: StateFlow<Boolean> = _estaConectado.asStateFlow()

    @Volatile
    private var inicializado = false

    fun inicializar(context: Context) {
        if (inicializado) return
        synchronized(this) {
            if (inicializado) return

            runCatching {
                val cm = context.applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                _estaConectado.value = verificarConexionActual(cm)

                cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        _estaConectado.value = true
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        val tieneInternet = networkCapabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET
                        )
                        _estaConectado.value = tieneInternet
                    }

                    override fun onLost(network: Network) {
                        _estaConectado.value = verificarConexionActual(cm)
                    }

                    override fun onUnavailable() {
                        _estaConectado.value = false
                    }
                })
                inicializado = true
            }
        }
    }

    private fun verificarConexionActual(cm: ConnectivityManager): Boolean {
        return runCatching {
            val redActiva = cm.activeNetwork ?: return false
            val capacidades = cm.getNetworkCapabilities(redActiva) ?: return false
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }.getOrDefault(false)
    }
}