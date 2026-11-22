package com.pminteractive.pmtours.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource // <-- IMPORT AÑADIDO
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.ui.theme.*

@Composable
fun ServicioCoordinadorCard(
    servicio: Servicio,
    nombreAsignado: String?,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esLocal = servicio.id.startsWith("SRV-LOC-")

    // Reutilizamos los strings de la tarjeta del Driver
    val etiquetaTipo = when (servicio.tipo) {
        TipoServicio.TRANSFER -> stringResource(id = R.string.servicio_tipo_transfer)
        TipoServicio.GUIDED_SERVICE -> stringResource(id = R.string.servicio_tipo_guiado)
        TipoServicio.EXCURSION -> stringResource(id = R.string.servicio_tipo_excursion)
    }

    val paxFormat = stringResource(id = R.string.servicio_pax_abreviado, "")
    val subtituloRuta = buildString {
        val origen = servicio.lugarPickup.substringBefore(",").trim()
        val destino = servicio.lugarDropOff.substringBefore(",").trim()

        if (origen.isNotBlank() && destino.isNotBlank()) {
            append("$origen → $destino")
        } else if (origen.isNotBlank()) {
            append(origen)
        }

        if (servicio.paxTexto.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(paxFormat.replace("", servicio.paxTexto).trim())
        }
    }

    // Identificador de trabajador conectado a strings.xml
    val etiquetaResponsable = when {
        !servicio.driverId.isNullOrBlank() -> stringResource(id = R.string.coord_card_driver, nombreAsignado ?: servicio.driverId)
        !servicio.guiaId.isNullOrBlank() -> stringResource(id = R.string.coord_card_guia, nombreAsignado ?: servicio.guiaId)
        else -> stringResource(id = R.string.coord_card_sin_asignar)
    }

    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (esLocal) LinceCyan.copy(alpha = 0.55f) else LinceSurfaceBorder)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila superior: Hora, Tipo/Reserva, etiqueta LOCAL y Badge de Estado unificado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = servicio.horaInicio,
                            color = LinceTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (esLocal) {
                            Text(
                                text = stringResource(id = R.string.coord_card_local),
                                color = LinceBackground,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LinceCyan)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$etiquetaTipo · ${servicio.numeroReserva}",
                        color = LinceCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }

                EstadoServicioBadge(estado = servicio.estado)
            }

            // Título principal del servicio
            Text(
                text = servicio.titulo,
                color = LinceTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Ruta resumida
            if (subtituloRuta.isNotBlank()) {
                Text(
                    text = subtituloRuta,
                    color = LinceTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Nombre humano del trabajador asignado
            Text(
                text = etiquetaResponsable,
                color = if (nombreAsignado != null || !servicio.driverId.isNullOrBlank() || !servicio.guiaId.isNullOrBlank()) {
                    LinceTextPrimary
                } else {
                    LincePending
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}