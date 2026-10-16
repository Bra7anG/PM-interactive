package com.pminteractive.pmtours.model.servicio

enum class EstadoServicio {
    ASIGNADO,      // Coordinación lo asignó, trabajador debe confirmar o rechazar
    CONFIRMADO,    // Aceptado por el Driver / Guía
    RECHAZADO,     // Rechazado (vuelve a coordinación para reasignación)
    EN_RUTA,       // Check-in GPS realizado
    FINALIZADO,    // Check-out realizado
    CANCELADO      // Cancelado por cliente / no-show (se mantiene pago al trabajador)
}