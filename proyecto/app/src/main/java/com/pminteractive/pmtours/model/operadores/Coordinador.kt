/**
 * OBJETIVO DEL SCRIPT:
 * Modelar la entidad del Coordinador Operativo dentro del sistema, implementando el contrato base de Usuario[cite: 3].
 *
 * RESUMEN:
 * Representa al personal de operaciones en oficina encargado de gestionar la logística, asignar recursos
 * (conductores y guías) y monitorear la ejecución de los servicios en su sede[cite: 3, 4].
 *
 * DETALLE:
 * - Implementa 'Usuario' asignando por defecto el rol 'RolUsuario.COORDINADOR'[cite: 3].
 * - Mantiene 'datosBancarios' como nulo por defecto, al tratarse de personal administrativo de planta
 *   que no percibe honorarios o liquidaciones por carrera o tour individual en terreno[cite: 3, 4].
 * - Está vinculado a una 'Sede' territorial fija para centralizar el control operativo de su ciudad
 *   (Santiago, Puerto Varas o Punta Arenas)[cite: 3, 4].
 * - Es el perfil responsable de supervisar respuestas a asignaciones, gestionar reasignaciones si alguien bloquea
 *   fechas y fiscalizar la puntualidad del check-in (regla de los 15 minutos de anticipación)[cite: 3, 4].
 */
package com.pminteractive.pmtours.model.operadores

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede

// 2. Perfil Coordinador Operativo (Despacho y asignación en oficina)
data class Coordinador(
    override val id: String,
    override val rut: String,
    override val nombreCompleto: String,
    override val correo: String,
    override val telefono: String,
    override val sede: Sede,
    override val direccion: String,
    override val rol: RolUsuario = RolUsuario.COORDINADOR,
    override val datosBancarios: DatosBancarios? = null
) : Usuario