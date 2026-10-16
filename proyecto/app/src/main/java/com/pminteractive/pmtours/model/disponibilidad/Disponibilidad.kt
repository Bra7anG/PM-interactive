/**
 * OBJETIVO DEL SCRIPT:
 * Modelar la entidad de bloqueo de disponibilidad y agenda para conductores y guías turísticos[cite: 3, 4].
 *
 * RESUMEN:
 * Representa la inhabilitación de una fecha específica por parte del trabajador en terreno,
 * notificando al equipo de Operaciones que no debe asignarle servicios ese día[cite: 3, 4].
 *
 * DETALLE:
 * - Vincula el registro al 'usuarioId' del Driver o Guía que solicita la indisponibilidad[cite: 3, 4].
 * - Almacena la 'fechaBloqueada' (YYYY-MM-DD) para aplicar las reglas operativas de la empresa:
 *   bloqueo con al menos una semana de anticipación y restricción automática si ya tiene un servicio confirmado[cite: 4].
 * - Contiene un 'motivo' opcional para respaldar la justificación del día libre ante Coordinación[cite: 3].
 * - Registra la 'fechaSolicitud' mediante timestamp para auditar con exactitud cuándo se solicitó el bloqueo[cite: 3, 4].
 */
package com.pminteractive.pmtours.model.disponibilidad

data class BloqueoDisponibilidad(
    val id: String,
    val usuarioId: String,
    val fechaBloqueada: String, // Formato YYYY-MM-DD
    val motivo: String? = null,
    val fechaSolicitud: Long = System.currentTimeMillis()
)