/**
 * OBJETIVO DEL SCRIPT:
 * Gestionar la persistencia y consulta reactiva de servicios turísticos asignados a operadores.
 *
 * RESUMEN:
 * Expone un flujo de datos en tiempo real mediante StateFlow, implementando las reglas de asignación
 * de la planilla oficial y registrando hitos de Check-in y Check-out en memoria.
 *
 * DETALLE:
 * - Cumple con la regla de asignación de la API: TRANSFER y EXCURSION solo asignan transportista;
 *   GUIDED_SERVICE asigna únicamente guía turístico.
 * - Utiliza códigos de servicio y reserva basados en el esquema de la empresa (SRV-000245, etc.).
 * - Gestiona estados inmutables mediante '.copy()' y emisión a través de 'MutableStateFlow'.
 */
package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _servicios = MutableStateFlow(
        listOf(
            // 1. Servicio completado a principios de mes (suma a liquidación)
            Servicio(
                id = "SRV-000240",
                numeroReserva = "WHFT223100",
                titulo = "Transfer Out: Hotel Lastarria -> Aeropuerto SCL",
                tipo = TipoServicio.TRANSFER,
                fecha = "2026-10-05",
                horaInicio = "07:00",
                horaFinEstimada = "08:30",
                lugarPickup = "Hotel Barrio Lastarria, Santiago",
                lugarDropOff = "Aeropuerto Internacional Arturo Merino Benítez",
                cantidadPasajeros = 2,
                paxTexto = "2+0",
                driverId = "TRA-001",
                vehiculoId = "VEH-14",
                guiaId = null,
                estado = EstadoServicio.FINALIZADO,
                tarifaAcordada = 45000.0,
                notasAgencia = "Pasajeros con vuelo internacional temprano.",
                detalleItinerario = "Traslado directo por Costanera Norte."
            ),
            // 2. Excursión completada la semana pasada (suma a liquidación)
            Servicio(
                id = "SRV-000242",
                numeroReserva = "WHFT223150",
                titulo = "Excursión Viña Concha y Toro · Pirque",
                tipo = TipoServicio.EXCURSION,
                fecha = "2026-10-12",
                horaInicio = "10:00",
                horaFinEstimada = "15:30",
                lugarPickup = "Hotel Cumbres Lastarria, Santiago",
                lugarDropOff = "Hotel Cumbres Lastarria, Santiago",
                cantidadPasajeros = 4,
                paxTexto = "4+0",
                driverId = "TRA-001",
                vehiculoId = "VEH-31",
                guiaId = null,
                estado = EstadoServicio.FINALIZADO,
                tarifaAcordada = 80000.0,
                notasAgencia = "Degustación premium reservada para las 11:30.",
                detalleItinerario = "Ruta directa Santiago a Pirque y retorno."
            ),
            // 3. Servicio cancelado por no-show de cliente (tarifa protegida, suma a liquidación)
            Servicio(
                id = "SRV-000244",
                numeroReserva = "WHFT223210",
                titulo = "Transfer In: Aeropuerto SCL -> Hotel Sheraton",
                tipo = TipoServicio.TRANSFER,
                fecha = "2026-10-15",
                horaInicio = "13:00",
                horaFinEstimada = "14:30",
                lugarPickup = "Aeropuerto Internacional Arturo Merino Benítez",
                lugarDropOff = "Hotel Sheraton, Providencia",
                cantidadPasajeros = 1,
                paxTexto = "1+0",
                driverId = "TRA-001",
                vehiculoId = "VEH-14",
                guiaId = null,
                estado = EstadoServicio.CANCELADO,
                tarifaAcordada = 45000.0,
                notasAgencia = "Cliente perdió vuelo de conexión. Tarifa resguardada para transportista.",
                detalleItinerario = "Espera en aeropuerto y cancelación reportada por operaciones."
            ),
            // 4. Servicio asignado para hoy o próximos días (NO suma aún a liquidación)
            Servicio(
                id = "SRV-000245",
                numeroReserva = "WHFT223423",
                titulo = "Transfer In: Aeropuerto SCL -> Hotel Barrio Lastarria",
                tipo = TipoServicio.TRANSFER,
                fecha = "2026-10-20",
                horaInicio = "09:00",
                horaFinEstimada = "10:30",
                lugarPickup = "Aeropuerto Internacional Arturo Merino Benítez (Puerta 4)",
                lugarDropOff = "Hotel Barrio Lastarria, Santiago",
                cantidadPasajeros = 2,
                paxTexto = "2+0",
                driverId = "TRA-001",
                vehiculoId = "VEH-14",
                guiaId = null,
                estado = EstadoServicio.ASIGNADO,
                tarifaAcordada = 45000.0,
                notasAgencia = "Pasajeros australianos (Smith John x2). Llevar letrero en mano.",
                detalleItinerario = "Recepción en puerta internacional y traslado vía Costanera Norte."
            ),
            // 5. Excursión confirmada a futuro (NO suma aún a liquidación)
            Servicio(
                id = "SRV-000247",
                numeroReserva = "WHFT223488",
                titulo = "Excursión Full Day: Cajón del Maipo & Embalse El Yeso",
                tipo = TipoServicio.EXCURSION,
                fecha = "2026-10-25",
                horaInicio = "08:00",
                horaFinEstimada = "17:00",
                lugarPickup = "Hotel Barrio Lastarria, Santiago",
                lugarDropOff = "Hotel Barrio Lastarria, Santiago",
                cantidadPasajeros = 8,
                paxTexto = "6+2",
                driverId = "TRA-001",
                vehiculoId = "VEH-31",
                guiaId = null,
                estado = EstadoServicio.CONFIRMADO,
                tarifaAcordada = 120000.0,
                notasAgencia = "Revisar cadenas y permisos de acceso municipal.",
                detalleItinerario = "Salida por Camino al Volcán hacia San José de Maipo."
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