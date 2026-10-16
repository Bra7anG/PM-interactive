/**
 * OBJETIVO DEL SCRIPT:
 * Definir el contrato base para cualquier usuario dentro del sistema PM Tours / Southbound[cite: 3].
 *
 * RESUMEN:
 * Establece los atributos comunes obligatorios (identificación, contacto, sede y rol)[cite: 3]
 * que deben compartir todos los operadores de la app.
 *
 * DETALLE:
 * - Implementa 'sealed interface' para restringir la jerarquía y asegurar que solo los
 *   roles autorizados (Driver, Guía, Coordinador, Administrador) puedan ser un Usuario válido[cite: 3].
 * - Vincula al trabajador con una 'Sede' fija para segmentar la logística entre Santiago,
 *   Puerto Varas y Punta Arenas[cite: 3, 4].
 * - 'datosBancarios' queda como opcional (nullable) para los perfiles administrativos o de
 *   oficina que no reciben pagos por servicio en terreno.
 */
package com.pminteractive.pmtours.model.operadores

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede

// Interfaz base común para todos los trabajadores de Southbound / PM Tours
sealed interface Usuario {
    val id: String
    val rut: String
    val nombreCompleto: String
    val correo: String
    val telefono: String
    val sede: Sede              // Sucursal operativa
    val direccion: String       // Dirección de domicilio o residencia
    val rol: RolUsuario
    val datosBancarios: DatosBancarios?
}
