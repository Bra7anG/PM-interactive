package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface ServicioRepository {
    fun obtenerServiciosPorTrabajador(trabajadorId: String): Flow<List<Servicio>>
    fun obtenerServiciosPorSede(driverIdsDeSede: List<String>): Flow<List<Servicio>>
    suspend fun obtenerServicioPorId(servicioId: String): Servicio?
    suspend fun responderAsignacion(servicioId: String, aceptar: Boolean): Result<Unit>
    suspend fun registrarCheckIn(servicioId: String, hito: RegistroHitoGps): Result<Unit>
    suspend fun registrarCheckOut(servicioId: String, hito: RegistroHitoGps): Result<Unit>
}

class FakeServicioRepository : ServicioRepository {

    // Lista observable con StateFlow para que Compose se actualice automáticamente ante cambios
    private val _servicios = MutableStateFlow(
        listOf(
            Servicio(
                id = "SRV-001",
                numeroReserva = "RES-2026-901",
                titulo = "Transfer In: Aeropuerto SCL -> Hotel Cumbres Lastarria",
                tipo = TipoServicio.TRANSFER,
                fecha = "2026-10-20",
                horaInicio = "09:00",
                horaFinEstimada = "10:30",
                lugarPickup = "Aeropuerto Internacional Arturo Merino Benítez (Puerta 4)",
                lugarDropOff = "Hotel Cumbres Lastarria, Santiago",
                cantidadPasajeros = 2,
                driverId = "DRV-001", // Carlos Morales
                vehiculoId = "VEH-02", // Toyota RAV4
                guiaId = null,
                estado = EstadoServicio.ASIGNADO,
                tarifaAcordada = 45000.0,
                notasAgencia = "Pasajeros australianos. Llevar letrero impreso con nombre y vestir polera institucional.",
                detalleItinerario = "Recepción en puerta internacional, asistencia con equipaje y traslado directo por Costanera Norte."
            ),
            Servicio(
                id = "SRV-002",
                numeroReserva = "RES-2026-905",
                titulo = "City Tour Histórico Santiago & Cerro San Cristóbal",
                tipo = TipoServicio.CITY_TOUR_HALF_DAY,
                fecha = "2026-10-20",
                horaInicio = "14:30",
                horaFinEstimada = "18:30",
                lugarPickup = "Hotel Cumbres Lastarria",
                lugarDropOff = "Patio Bellavista, Santiago",
                cantidadPasajeros = 6,
                driverId = "DRV-001",
                vehiculoId = "VEH-01", // Mercedes-Benz Sprinter
                guiaId = "GUI-101", // Valentina Lagos
                estado = EstadoServicio.CONFIRMADO,
                tarifaAcordada = 75000.0,
                notasAgencia = "Coordinar parada de 30 min en pueblito Los Dominicos. Requiere guía en inglés fluido.",
                detalleItinerario = "Recorrido por Centro Histórico, Plaza de Armas, Palacio de La Moneda y ascenso al Cerro San Cristóbal."
            ),
            Servicio(
                id = "SRV-003",
                numeroReserva = "RES-2026-912",
                titulo = "Excursión Full Day: Volcán Osorno y Saltos del Petrohué",
                tipo = TipoServicio.CITY_TOUR_FULL_DAY,
                fecha = "2026-10-22",
                horaInicio = "08:30",
                horaFinEstimada = "17:00",
                lugarPickup = "Hotel Cabaña del Lago, Puerto Varas",
                lugarDropOff = "Hotel Cabaña del Lago, Puerto Varas",
                cantidadPasajeros = 8,
                driverId = null, // Pendiente de asignación por operaciones
                vehiculoId = null,
                guiaId = "GUI-101",
                estado = EstadoServicio.ASIGNADO,
                tarifaAcordada = 120000.0,
                notasAgencia = "Viático de almuerzo incluido en voucher. Entrada a parque nacional previamente pagada.",
                detalleItinerario = "Bordeando Lago Llanquihue hacia Ensenada, visita a Saltos del Petrohué y ascenso a cafetería Volcán Osorno."
            )
        )
    )

    override fun obtenerServiciosPorTrabajador(trabajadorId: String): Flow<List<Servicio>> {
        return _servicios.map { lista ->
            lista.filter { it.driverId == trabajadorId || it.guiaId == trabajadorId }
        }
    }

    override fun obtenerServiciosPorSede(driverIdsDeSede: List<String>): Flow<List<Servicio>> {
        return _servicios.map { lista ->
            lista.filter { it.driverId in driverIdsDeSede || it.driverId == null }
        }
    }

    override suspend fun obtenerServicioPorId(servicioId: String): Servicio? {
        return _servicios.value.find { it.id == servicioId }
    }

    override suspend fun responderAsignacion(servicioId: String, aceptar: Boolean): Result<Unit> {
        val servicioActual = _servicios.value.find { it.id == servicioId }
            ?: return Result.failure(Exception("Servicio no encontrado."))

        val nuevoEstado = if (aceptar) EstadoServicio.CONFIRMADO else EstadoServicio.RECHAZADO
        val servicioActualizado = servicioActual.copy(estado = nuevoEstado)

        _servicios.value = _servicios.value.map {
            if (it.id == servicioId) servicioActualizado else it
        }
        return Result.success(Unit)
    }

    override suspend fun registrarCheckIn(servicioId: String, hito: RegistroHitoGps): Result<Unit> {
        val servicioActual = _servicios.value.find { it.id == servicioId }
            ?: return Result.failure(Exception("Servicio no encontrado."))

        val servicioActualizado = servicioActual.copy(
            estado = EstadoServicio.EN_RUTA,
            checkIn = hito
        )

        _servicios.value = _servicios.value.map {
            if (it.id == servicioId) servicioActualizado else it
        }
        return Result.success(Unit)
    }

    override suspend fun registrarCheckOut(servicioId: String, hito: RegistroHitoGps): Result<Unit> {
        val servicioActual = _servicios.value.find { it.id == servicioId }
            ?: return Result.failure(Exception("Servicio no encontrado."))

        val servicioActualizado = servicioActual.copy(
            estado = EstadoServicio.FINALIZADO,
            checkOut = hito
        )

        _servicios.value = _servicios.value.map {
            if (it.id == servicioId) servicioActualizado else it
        }
        return Result.success(Unit)
    }
}