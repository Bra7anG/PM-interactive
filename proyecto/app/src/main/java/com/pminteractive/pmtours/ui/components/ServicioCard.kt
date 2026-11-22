package com.pminteractive.pmtours.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.ui.theme.*

// 1. Data class interna para empaquetar el diseño
data class EstiloBadge(
    val fondo: Color,
    val texto: Color,
    val etiquetaResId: Int
)

// 2. Función interna usando TUS colores reales (creando la versión "soft" con alpha)
private fun obtenerEstiloVisual(estado: EstadoServicio): EstiloBadge {
    return when (estado) {
        EstadoServicio.ASIGNADO -> EstiloBadge(
            fondo = LincePending.copy(alpha = 0.1f),
            texto = LincePending,
            etiquetaResId = R.string.estado_asignado
        )
        EstadoServicio.CONFIRMADO -> EstiloBadge(
            fondo = LinceCyan.copy(alpha = 0.1f),
            texto = LinceCyan,
            etiquetaResId = R.string.estado_confirmado
        )
        EstadoServicio.RECHAZADO -> EstiloBadge(
            fondo = LinceError.copy(alpha = 0.1f),
            texto = LinceError,
            etiquetaResId = R.string.estado_rechazado
        )
        EstadoServicio.EN_RUTA -> EstiloBadge(
            fondo = LinceServiceSoft, // Este sí existía en tu código de Calendario
            texto = LinceService,     // Este también
            etiquetaResId = R.string.estado_en_ruta
        )
        EstadoServicio.FINALIZADO -> EstiloBadge(
            fondo = LinceFinishedSurface, // Este lo vi en DetalleServicioScreen
            texto = LinceTextSecondary,   // Usamos texto secundario como borde/texto
            etiquetaResId = R.string.estado_finalizado
        )
        EstadoServicio.CANCELADO -> EstiloBadge(
            fondo = LinceCancelled.copy(alpha = 0.1f),
            texto = LinceCancelled,
            etiquetaResId = R.string.estado_cancelado
        )
    }
}

@Composable
fun EstadoServicioBadge(
    estado: EstadoServicio,
    modifier: Modifier = Modifier,
    tagId: String? = null
) {
    val estilo = obtenerEstiloVisual(estado)

    Surface(
        color = estilo.fondo,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, estilo.texto.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = estilo.etiquetaResId),
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .then(if (tagId != null) Modifier.testTag("servicio_estado_$tagId") else Modifier),
            color = estilo.texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ServicioCard(
    servicio: Servicio,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("servicio_card_${servicio.id}")
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = servicio.horaInicio,
                        color = LinceTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$etiquetaTipo · ${servicio.numeroReserva}",
                        color = LinceCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                EstadoServicioBadge(
                    estado = servicio.estado,
                    tagId = servicio.id
                )
            }

            Text(
                text = servicio.titulo,
                color = LinceTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (subtituloRuta.isNotBlank()) {
                Text(
                    text = subtituloRuta,
                    color = LinceTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}