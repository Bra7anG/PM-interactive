package com.pminteractive.pmtours.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

interface DisponibilidadRepository {
    fun obtenerDiasBloqueados(trabajadorId: String): Flow<Set<LocalDate>>
    suspend fun alternarBloqueo(
        trabajadorId: String,
        fecha: LocalDate,
        bloquear: Boolean
    ): Result<Unit>
}

class FakeDisponibilidadRepository : DisponibilidadRepository {

    private val hoy: LocalDate = LocalDate.now()

    private val _bloqueos = MutableStateFlow(
        mapOf(
            "TRA-001" to setOf(hoy.plusDays(9), hoy.plusDays(10)),
            "GUI-012" to setOf(hoy.plusDays(8))
        )
    )

    override fun obtenerDiasBloqueados(trabajadorId: String): Flow<Set<LocalDate>> {
        return _bloqueos.map { bloqueos -> bloqueos[trabajadorId] ?: emptySet() }
    }

    override suspend fun alternarBloqueo(
        trabajadorId: String,
        fecha: LocalDate,
        bloquear: Boolean
    ): Result<Unit> {
        _bloqueos.value = _bloqueos.value.toMutableMap().apply {
            val diasActuales = this[trabajadorId] ?: emptySet()
            this[trabajadorId] = if (bloquear) {
                diasActuales + fecha
            } else {
                diasActuales - fecha
            }
        }
        return Result.success(Unit)
    }
}
