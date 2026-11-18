/**
 * OBJETIVO DEL SCRIPT:
 * Componente visual reutilizable para renderizar una tarjeta de servicio turístico en listas.
 *
 * RESUMEN:
 * Muestra la información esencial del traslado o excursión: horario, tipo de servicio,
 * etiqueta de estado con color diferenciado y trayecto o descripción con pasajeros.
 *
 * DETALLE:
 * - Compatible con los tipos oficiales: TRANSFER, GUIDED_SERVICE y EXCURSION.
 * - Emplea la paleta de colores oficial de Lince (LinceSurface, LinceCyan, etc.).
 * - Permite navegación interactiva mediante el evento 'onCardClick'.
 */
package com.pminteractive.pmtours.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.ui.theme.*

@Composable
fun ServicioCard(
    servicio: Servicio,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estilo = servicio.estado.estiloVisual()

    val etiquetaTipo = when (servicio.tipo) {
        TipoServicio.TRANSFER -> "TRANSFER"
        TipoServicio.GUIDED_SERVICE -> "SERVICIO GUIADO"
        TipoServicio.EXCURSION -> "EXCURSIÓN"
    }

    val textoRuta = if (servicio.lugarDropOff.isNotBlank() && servicio.tipo == TipoServicio.TRANSFER) {
        "${servicio.lugarPickup.substringBefore(",")} → ${servicio.lugarDropOff.substringBefore(",")}"
    } else {
        "${servicio.titulo} · ${servicio.paxTexto} pax"
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        text = etiquetaTipo,
                        color = LinceCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = estilo.fondo,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = estilo.etiqueta,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("servicio_estado_${servicio.id}"),
                        color = estilo.texto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = textoRuta,
                color = LinceTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
        }
    }
}