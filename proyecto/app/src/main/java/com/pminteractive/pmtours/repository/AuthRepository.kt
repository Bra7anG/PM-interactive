package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.datosoperativos.Vehiculo
import com.pminteractive.pmtours.model.operadores.*
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.toDomain
import com.pminteractive.pmtours.network.toNombreLegible
import com.pminteractive.pmtours.network.toSede

interface AuthRepository {
    suspend fun login(usuario: String, contrasena: String): Result<Usuario>
    fun obtenerUsuarioActual(): Usuario?
    fun obtenerUsuarios(): List<Usuario>
    fun cerrarSesion()
}

class HybridAuthRepository(
    private val apiService: LinceApiService
) : AuthRepository {

    private val claveDemo = "1234"

    // Atajos rápidos apuntando a registros reales de Southbound
    private val atajosRapidos = mapOf(
        "driver" to "TRA-001",
        "guia" to "GUI-012",
        "coordinador" to "CRD-201",
        "admin" to "ADM-001"
    )

    // Cuentas administrativas y respaldos en caso de falla puntual de red
    private val usuariosBase: List<Usuario> = listOf(
        Driver(
            id = "TRA-001",
            rut = "18.452.123-K",
            nombreCompleto = "Catalina Ibáñez Campos",
            correo = "catalina.ibanez@pmtours.cl",
            telefono = "+56911223344",
            sede = Sede.SANTIAGO,
            direccion = "Av. Providencia 1234, Santiago",
            datosBancarios = DatosBancarios(
                banco = "BancoEstado",
                tipoCuenta = "Cuenta RUT",
                numeroCuenta = "18452123",
                titular = "Catalina Ibáñez Campos",
                rutTitular = "18.452.123-K"
            ),
            vehiculos = listOf(
                Vehiculo("VEH-1", "Sedan (ZZ-1001)", "ZZ-1001", 3, "SEDAN"),
                Vehiculo("VEH-2", "Bus (ZZ-1002)", "ZZ-1002", 40, "BUS")
            )
        ),
        Guia(
            id = "GUI-012",
            rut = "19.876.543-2",
            nombreCompleto = "Andrés Godoy Peña",
            correo = "andres.godoy@pmtours.cl",
            telefono = "+56955667788",
            sede = Sede.SANTIAGO,
            direccion = "Santiago Centro",
            datosBancarios = DatosBancarios(
                banco = "Banco de Chile",
                tipoCuenta = "Cuenta Corriente",
                numeroCuenta = "9988776601",
                titular = "Andrés Godoy Peña",
                rutTitular = "19.876.543-2"
            ),
            idiomas = listOf("Español", "Inglés"),
            especialidades = listOf("Ruta Patrimonial", "City Tour", "Gastronomía")
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

    override suspend fun login(usuario: String, contrasena: String): Result<Usuario> {
        if (contrasena != claveDemo) {
            return Result.failure(Exception("Contraseña incorrecta (usa 1234)."))
        }

        val entrada = usuario.trim()
        val codigoNormalizado = (atajosRapidos[entrada.lowercase()] ?: entrada).uppercase()

        // 1. Si es un código de Transportista (ej: "driver", "TRA-001", "TRA-021"), busca en la API
        if (codigoNormalizado.startsWith("TRA-")) {
            val driverApi = runCatching {
                apiService.getTransportistas(q = codigoNormalizado, activo = null, pageSize = 20)
                    .data
                    .find { it.codigo.equals(codigoNormalizado, ignoreCase = true) }
            }.getOrNull()

            if (driverApi != null) {
                val correoGenerado = driverApi.nombre.lowercase()
                    .replace(" ", ".")
                    .plus("@pmtours.cl")

                val driverReal = Driver(
                    id = driverApi.codigo,
                    rut = "18.${100 + driverApi.id}.450-K",
                    nombreCompleto = driverApi.nombre,
                    correo = correoGenerado,
                    telefono = "+5691122${1000 + driverApi.id}",
                    sede = driverApi.oficina.toSede(),
                    direccion = "Base ${driverApi.oficina.nombre}",
                    datosBancarios = DatosBancarios(
                        banco = "BancoEstado",
                        tipoCuenta = "Cuenta RUT",
                        numeroCuenta = "18${100 + driverApi.id}450",
                        titular = driverApi.nombre,
                        rutTitular = "18.${100 + driverApi.id}.450-K"
                    ),
                    vehiculos = driverApi.vehiculos.map { it.toDomain() }
                )
                usuarioSesionActual = driverReal
                return Result.success(driverReal)
            }
        }

        // 2. Si es un código de Guía (ej: "guia", "GUI-012", "GUI-015"), busca en la API
        if (codigoNormalizado.startsWith("GUI-")) {
            val guiaApi = runCatching {
                apiService.getGuias(q = codigoNormalizado, activo = null, pageSize = 20)
                    .data
                    .find { it.codigo.equals(codigoNormalizado, ignoreCase = true) }
            }.getOrNull()

            if (guiaApi != null) {
                val correoGenerado = guiaApi.nombre.lowercase()
                    .replace(" ", ".")
                    .plus("@pmtours.cl")

                val guiaReal = Guia(
                    id = guiaApi.codigo,
                    rut = "19.${200 + guiaApi.id}.310-2",
                    nombreCompleto = guiaApi.nombre,
                    correo = correoGenerado,
                    telefono = "+5695566${1000 + guiaApi.id}",
                    sede = guiaApi.oficina.toSede(),
                    direccion = "Base ${guiaApi.oficina.nombre}",
                    datosBancarios = DatosBancarios(
                        banco = "Banco de Chile",
                        tipoCuenta = "Cuenta Corriente",
                        numeroCuenta = "9988${1000 + guiaApi.id}",
                        titular = guiaApi.nombre,
                        rutTitular = "19.${200 + guiaApi.id}.310-2"
                    ),
                    idiomas = guiaApi.idiomas.map { it.toNombreLegible() },
                    especialidades = listOf("Circuitos Turísticos", "Atención Bilingüe")
                )
                usuarioSesionActual = guiaReal
                return Result.success(guiaReal)
            }
        }

        // 3. Búsqueda en cuentas administrativas o respaldo local
        val usuarioLocal = usuariosBase.find {
            it.id.equals(codigoNormalizado, ignoreCase = true) ||
                    it.correo.equals(entrada, ignoreCase = true)
        }

        return if (usuarioLocal != null) {
            usuarioSesionActual = usuarioLocal
            Result.success(usuarioLocal)
        } else {
            Result.failure(Exception("Usuario '$entrada' no encontrado en la API ni en cuentas base."))
        }
    }

    override fun obtenerUsuarioActual(): Usuario? = usuarioSesionActual

    override fun obtenerUsuarios(): List<Usuario> = usuariosBase

    override fun cerrarSesion() {
        usuarioSesionActual = null
    }
}