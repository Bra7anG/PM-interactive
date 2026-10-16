package com.pminteractive.pmtours.model.servicio

// Registro estructurado del Check-in y Check-out
data class RegistroHitoGps(
    val timestamp: Long,
    val latitud: Double,
    val longitud: Double,
    val direccionReferencia: String,
    val aTiempo: Boolean // true si llegó al menos 15 minutos antes del horario acordado
)