/**
 * OBJETIVO DEL SCRIPT:
 * Modelar la entidad del Guía Turístico dentro del sistema, implementando el contrato base de Usuario[cite: 3].
 *
 * RESUMEN:
 * Representa al personal en terreno encargado del guiado de tours, incorporando sus datos de contacto,
 * cuenta para liquidaciones y sus competencias clave (idiomas y rutas temáticas)[cite: 3, 4].
 *
 * DETALLE:
 * - Implementa 'Usuario' fijando por defecto su rol en 'RolUsuario.GUIA'[cite: 3].
 * - Exige 'datosBancarios' como obligatorio para el pago de honorarios por servicios ejecutados[cite: 3, 4].
 * - Incorpora 'idiomas' para que Operaciones pueda filtrar y asignar al guía correcto según el origen
 *   del turista (ej. inglés para pasajeros australianos o alemán)[cite: 4].
 * - Incluye 'especialidades' (viñedos, patrimonio histórico, senderismo) para vincular al guía con
 *   la temática específica de cada excursión o City Tour[cite: 3, 4].
 */
package com.pminteractive.pmtours.model.operadores

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede

// Perfil Guía Turístico
data class Guia(
    override val id: String,
    override val rut: String,
    override val nombreCompleto: String,
    override val correo: String,
    override val telefono: String,
    override val sede: Sede,
    override val direccion: String,
    override val rol: RolUsuario = RolUsuario.GUIA,
    override val datosBancarios: DatosBancarios,
    val idiomas: List<String>,                  // Ej: listOf("Inglés", "Alemán")
    val especialidades: List<String> = emptyList() // Ej: listOf("Ruta Histórica", "Viñedos")
) : Usuario