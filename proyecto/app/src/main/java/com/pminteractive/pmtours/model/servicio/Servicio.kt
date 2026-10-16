/**
 * OBJETIVO DEL SCRIPT:
 * Modelar la entidad principal de Servicios (transfers, city tours y excursiones) dentro de la app PM Tours[cite: 3, 4].
 *
 * RESUMEN:
 * Representa la orden operativa de cada servicio turístico asignado, integrando logística de ruta,
 * recursos asignados (chofer, vehículo específico y guía), notas de agencias y trazabilidad GPS[cite: 3, 4].
 *
 * DETALLE:
 * - Vincula el 'numeroReserva' para trazabilidad operativa con el sistema central / ERP[cite: 3, 4].
 * - Registra la asignación puntual de recursos mediante 'driverId', 'vehiculoId' (auto/van específica)
 *   y 'guiaId' según los requerimientos del grupo de pasajeros[cite: 4].
 * - Gestiona el ciclo operativo a través de 'EstadoServicio' (asignado, confirmado, en ruta, finalizado, cancelado)[cite: 3, 4].
 * - Incluye 'notasAgencia' para exigencias del cliente (letreros en aeropuerto, uso de polera corporativa)
 *   y 'detalleItinerario' con la descripción de paradas del tour[cite: 4].
 * - Incorpora 'checkIn' y 'checkOut' (tipo RegistroHitoGps) para auditar la puntualidad (llegada 15 min antes)
 *   y registrar las coordenadas de inicio y término del recorrido[cite: 4].
 * - Almacena la 'tarifaAcordada' para alimentar el dashboard de ingresos acumulados de cada trabajador[cite: 4].
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
    val driverId: String?,
    val vehiculoId: String?,        // ID del vehículo seleccionado de la lista del driver
    val guiaId: String?,
    val estado: EstadoServicio = EstadoServicio.ASIGNADO,
    val tarifaAcordada: Double,     // Tarifa a pagar al recurso
    val notasAgencia: String = "",  // Ej: "Polera de la empresa, letrero luminoso"
    val detalleItinerario: String = "",
    val checkIn: RegistroHitoGps? = null,
    val checkOut: RegistroHitoGps? = null
)