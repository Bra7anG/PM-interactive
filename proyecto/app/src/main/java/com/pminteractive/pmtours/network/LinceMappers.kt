package com.pminteractive.pmtours.network

import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.datosoperativos.Vehiculo
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.network.dto.IdiomaDto
import com.pminteractive.pmtours.network.dto.OficinaDto
import com.pminteractive.pmtours.network.dto.ServicioDto
import com.pminteractive.pmtours.network.dto.VehiculoDto
import java.time.LocalTime

// Convierte el código de oficina de la API ("SCL", "PUX", "PUQ") al enum Sede de la app
fun OficinaDto.toSede(): Sede {
    return when (codigo.uppercase()) {
        "SCL" -> Sede.SANTIAGO
        "PUX" -> Sede.PUERTO_VARAS
        "PUQ" -> Sede.PUNTA_ARENAS
        else -> Sede.SANTIAGO
    }
}

// Extrae el nombre legible del idioma ("Español", "Inglés") en lugar del código ("ES", "EN")
fun IdiomaDto.toNombreLegible(): String = nombre

// Convierte el vehículo de la API al modelo interno de flota
fun VehiculoDto.toDomain(): Vehiculo {
    val tipoFormateado = tipo.lowercase().replaceFirstChar { it.uppercase() }
    return Vehiculo(
        id = "VEH-$id",
        modelo = "$tipoFormateado ($patente)",
        patente = patente,
        capacidadPasajeros = capacidad,
        tipo = tipo
    )
}

// Traduce un ServicioDto de la API al modelo Servicio que entienden las pantallas de la app
fun ServicioDto.toDomain(): Servicio {
    val tipoMapeado = when (tipo.uppercase()) {
        "TRANSFER" -> TipoServicio.TRANSFER
        "GUIDED_SERVICE" -> TipoServicio.GUIDED_SERVICE
        "EXCURSION" -> TipoServicio.EXCURSION
        else -> TipoServicio.TRANSFER
    }

    // La API entrega solo horaPickup; calculamos +2 horas estimadas por defecto
    val horaFinCalculada = runCatching {
        LocalTime.parse(horaPickup).plusHours(2).toString()
    }.getOrDefault("")

    // Enriquecemos el itinerario con datos extra que trae la API (tour padre, cliente e idioma)
    val infoExtra = buildList {
        if (!descripcionPadre.isNullOrBlank()) add("Tour: $descripcionPadre")
        add("Cliente: ${cliente.nombre}")
        if (idiomaRequerido != null) add("Idioma: ${idiomaRequerido.nombre}")
    }.joinToString(" · ")

    return Servicio(
        id = codigo, // Ej: "SRV-000245"
        numeroReserva = reserva.referencia,
        titulo = descripcion,
        tipo = tipoMapeado,
        fecha = fecha,
        horaInicio = horaPickup,
        horaFinEstimada = horaFinCalculada,
        lugarPickup = pickup,
        lugarDropOff = dropoff,
        cantidadPasajeros = pax.total,
        paxTexto = pax.texto,
        driverId = transportista?.codigo, // Ej: "TRA-020"
        vehiculoId = vehiculo?.let { "VEH-${it.id}" },
        guiaId = guia?.codigo, // Ej: "GUI-012"
        estado = mapearEstadoServicio(estadoServicio, asignacion.statusPL),
        tarifaAcordada = 0.0, // La API es de solo lectura y no maneja tarifas
        notasAgencia = notas ?: "",
        detalleItinerario = infoExtra
    )
}

// Regla consistente para unificar estadoServicio (Tourplan) y statusPL (planilla)
fun mapearEstadoServicio(estadoServicio: String, statusPL: String?): EstadoServicio {
    return when {
        estadoServicio == "XX" || statusPL == "CX" -> EstadoServicio.CANCELADO
        statusPL == "DL" -> EstadoServicio.RECHAZADO
        statusPL == "CF" -> EstadoServicio.CONFIRMADO
        else -> EstadoServicio.ASIGNADO // Cubre "RQ" (en solicitud) y null (sin asignar)
    }
}