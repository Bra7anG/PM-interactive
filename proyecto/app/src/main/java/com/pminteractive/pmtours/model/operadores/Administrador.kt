/**
 * OBJETIVO DEL SCRIPT:
 * Modelar la entidad del Administrador del sistema, implementando el contrato base de Usuario[cite: 3].
 *
 * RESUMEN:
 * Representa al perfil con privilegios de gestión global, encargado de la administración de cuentas,
 * roles, permisos y configuración de parámetros operativos de la plataforma[cite: 3].
 *
 * DETALLE:
 * - Implementa 'Usuario' fijando por defecto su rol en 'RolUsuario.ADMINISTRADOR'[cite: 3].
 * - Mantiene 'datosBancarios' como nulo por defecto, ya que sus funciones son administrativas y no
 *   percibe liquidaciones directas por servicios en terreno[cite: 3, 4].
 * - Incorpora 'nivelAcceso' (por defecto "SUPER_ADMIN") para habilitar acciones exclusivas como alta,
 *   edición y desactivación de perfiles (Drivers, Guías, Coordinadores) y catálogos de prueba[cite: 3].
 * - Se asocia a una 'Sede' de referencia para coordinar aspectos administrativos de la empresa[cite: 3, 4].
 */
package com.pminteractive.pmtours.model.operadores

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede

// 1. Perfil Administrador (Gestión de usuarios y accesos)
data class Administrador(
    override val id: String,
    override val rut: String,
    override val nombreCompleto: String,
    override val correo: String,
    override val telefono: String,
    override val sede: Sede,
    override val direccion: String,
    override val rol: RolUsuario = RolUsuario.ADMINISTRADOR,
    override val datosBancarios: DatosBancarios? = null,
    val nivelAcceso: String = "SUPER_ADMIN"
) : Usuario

