package com.pminteractive.pmtours.ui.theme

import androidx.compose.ui.graphics.Color
import com.pminteractive.pmtours.model.servicio.EstadoServicio

data class EstadoServicioVisual(
    val fondo: Color,
    val texto: Color,
    val etiqueta: String
)

fun EstadoServicio.estiloVisual(): EstadoServicioVisual = when (this) {
    EstadoServicio.ASIGNADO -> EstadoServicioVisual(LincePendingSoft, LincePending, "Pendiente")
    EstadoServicio.CONFIRMADO -> EstadoServicioVisual(LinceSuccessSoft, LinceSuccess, "Confirmado")
    EstadoServicio.EN_RUTA -> EstadoServicioVisual(LinceServiceSoft, LinceService, "En ruta")
    EstadoServicio.FINALIZADO -> EstadoServicioVisual(LinceFinishedSoft, LinceFinished, "Finalizado")
    EstadoServicio.RECHAZADO -> EstadoServicioVisual(LinceDangerSoft, LinceDanger, "Rechazado")
    EstadoServicio.CANCELADO -> EstadoServicioVisual(LinceCancelledSoft, LinceCancelled, "Cancelado")
}