/**
 * OBJETIVO DEL SCRIPT:
 * Definir el contrato de autenticación y su implementación con datos sintéticos para pruebas.
 *
 * RESUMEN:
 * Valida credenciales de acceso para operadores, coordinadores y administradores,
 * asociando usuarios de prueba consistentes con los códigos de la API de Lince.
 *
 * DETALLE:
 * - Utiliza identificadores concordantes con la API: 'TRA-001' para Transportistas y 'GUI-012' para Guías.
 * - Asigna sedes con sus códigos oficiales (SCL, PUX, PUQ).
 * - Devuelve 'Result<Usuario>' para un control limpio de éxito o error en la interfaz de Compose.
 */
package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.datosoperativos.Vehiculo
import com.pminteractive.pmtours.model.operadores.*

interface AuthRepository {
    suspend fun login(correo: String, contrasena: String): Result<Usuario>
    fun obtenerUsuarioActual(): Usuario?
    fun cerrarSesion()
}

class FakeAuthRepository : AuthRepository {

    private val usuariosPrueba: List<Usuario> = listOf(
        Driver(
            id = "TRA-001",
            rut = "18.452.123-K",
            nombreCompleto = "Carlos Morales",
            correo = "cccc",
            telefono = "+56911223344",
            sede = Sede.SANTIAGO,
            direccion = "Av. Providencia 1234, Santiago",
            datosBancarios = DatosBancarios(
                banco = "BancoEstado",
                tipoCuenta = "Cuenta RUT",
                numeroCuenta = "18452123",
                titular = "Carlos Morales",
                rutTitular = "18.452.123-K"
            ),
            vehiculos = listOf(
                Vehiculo("VEH-31", "Mercedes-Benz Sprinter 2022", "ZZ-1031", 16, "VAN"),
                Vehiculo("VEH-14", "Toyota RAV4 2021", "PRTC-89", 4, "SEDAN")
            )
        ),
        Guia(
            id = "GUI-012",
            rut = "19.876.543-2",
            nombreCompleto = "Valentina Lagos",
            correo = "valentina.lagos@pmtours.cl",
            telefono = "+56955667788",
            sede = Sede.PUERTO_VARAS,
            direccion = "Costanera Vicente Pérez Rosales 450, Puerto Varas",
            datosBancarios = DatosBancarios(
                banco = "Banco de Chile",
                tipoCuenta = "Cuenta Corriente",
                numeroCuenta = "9988776601",
                titular = "Valentina Lagos",
                rutTitular = "19.876.543-2"
            ),
            idiomas = listOf("Español", "Inglés", "Alemán"),
            especialidades = listOf("Ruta Patrimonial", "Volcanes", "Gastronomía Local")
        ),
        Coordinador(
            id = "CRD-201",
            rut = "15.321.654-8",
            nombreCompleto = "Luis Figueroa",
            correo = "luis.figueroa@pmtours.cl",
            telefono = "+56999881122",
            sede = Sede.SANTIAGO,
            direccion = "Moneda 920, Santiago Centro"
        ),
        Administrador(
            id = "ADM-001",
            rut = "12.345.678-9",
            nombreCompleto = "Admin PM Tours",
            correo = "admin@pmtours.cl",
            telefono = "+56900001111",
            sede = Sede.SANTIAGO,
            direccion = "Oficina Central PM Tours, Santiago"
        )
    )

    private var usuarioSesionActual: Usuario? = null

    override suspend fun login(correo: String, contrasena: String): Result<Usuario> {
        val usuario = usuariosPrueba.find { it.correo.equals(correo.trim(), ignoreCase = true) }

        return if (usuario != null && contrasena.length >= 4) {
            usuarioSesionActual = usuario
            Result.success(usuario)
        } else {
            Result.failure(Exception("Credenciales incorrectas o usuario no encontrado."))
        }
    }

    override fun obtenerUsuarioActual(): Usuario? = usuarioSesionActual

    override fun cerrarSesion() {
        usuarioSesionActual = null
    }
}