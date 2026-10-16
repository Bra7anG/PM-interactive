/**
 * OBJETIVO DEL SCRIPT:
 * Modelar la entidad del Transportista / Conductor (Driver) dentro del sistema, implementando el contrato base de Usuario[cite: 3].
 *
 * RESUMEN:
 * Representa al chofer encargado del transporte de pasajeros en transfers y excursiones, integrando sus datos personales,
 * cuenta bancaria para liquidaciones y el catálogo de vehículos con los que opera[cite: 3, 4].
 *
 * DETALLE:
 * - Implementa 'Usuario' fijando por defecto su rol en 'RolUsuario.DRIVER'[cite: 3].
 * - Exige 'datosBancarios' obligatorio para procesar el pago de sus servicios (incluso en cancelaciones por no-show)[cite: 4].
 * - Contiene una lista de 'Vehiculo' ('vehiculos') que le permite registrar más de un auto o van, facilitando que
 *   Operaciones asigne el vehículo adecuado según el tamaño del grupo (ej. sedán para 2 pax o van para 6 o más)[cite: 4].
 * - Asocia al chofer con una 'Sede' territorial para coordinar los traslados en su respectiva ciudad[cite: 3, 4].
 */
package com.pminteractive.pmtours.model.operadores

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.datosoperativos.Vehiculo

// Perfil Transportista / Driver
data class Driver(
    override val id: String,
    override val rut: String,
    override val nombreCompleto: String,
    override val correo: String,
    override val telefono: String,
    override val sede: Sede,
    override val direccion: String,
    override val rol: RolUsuario = RolUsuario.DRIVER,
    override val datosBancarios: DatosBancarios,
    val vehiculos: List<Vehiculo> = emptyList()
) : Usuario