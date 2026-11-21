package com.pminteractive.pmtours.network.dto

data class HealthDto(
    val status: String,
    val db: String,
    val version: String,
    val time: String
) {
    val estaOperativo: Boolean
        get() = status.equals("ok", ignoreCase = true) && db.equals("up", ignoreCase = true)
}