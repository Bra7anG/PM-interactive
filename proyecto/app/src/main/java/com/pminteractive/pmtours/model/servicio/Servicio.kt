/**
 * OBJETIVO DEL SCRIPT:
 * Modelar la entidad de Servicio turístico turístico alineada a las columnas de la planilla y API Lince.
 *
 * RESUMEN:
 * Representa la orden de servicio con su reserva, pasajeros, itinerario, recursos asignados
 * y los hitos de trazabilidad GPS requeridos por la app móvil.
 *
 * DETALLE:
 * - 'paxTexto': Almacena el formato de pasajeros de la planilla (ej: "2+0", "6+0").
 * - 'driverId' y 'vehiculoId': Aplican para servicios tipo TRANSFER y EXCURSION.
 * - 'guiaId': Aplica exclusivamente para servicios tipo GUIDED_SERVICE.
 * - 'checkIn' y 'checkOut': Hitos de control móvil local para auditar puntualidad (15 min antes).
 */
package com.pminteractive.pmtours.model.servicio

data class Servicio(
    val id: String,
    val numeroReserva: String,
    val titulo: String,
    val tipo: TipoServicio,
    val fecha: String,              // Formato YYYY-MM-DD
    val horaInicio: String,         // Formato HH:mm
    val horaFinEstimada: String,    // Formato HH:mm
    val lugarPickup: String,
    val lugarDropOff: String,
    val cantidadPasajeros: Int,
    val paxTexto: String = "2+0",   // Formato de planilla (adultos+niños)
    val driverId: String?,          // Transportista asignado (solo TRANSFER y EXCURSION)
    val vehiculoId: String?,        // Vehículo asignado (solo TRANSFER y EXCURSION)
    val guiaId: String?,            // Guía asignado (solo GUIDED_SERVICE)
    val estado: EstadoServicio = EstadoServicio.ASIGNADO,
    val tarifaAcordada: Double,
    val notasAgencia: String = "",
    val detalleItinerario: String = "",
    val checkIn: RegistroHitoGps? = null,
    val checkOut: RegistroHitoGps? = null
)