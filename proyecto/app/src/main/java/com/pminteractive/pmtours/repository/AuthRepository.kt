package com.pminteractive.pmtours.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.datosoperativos.Vehiculo
import com.pminteractive.pmtours.model.operadores.*
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.toDomain
import com.pminteractive.pmtours.network.toNombreLegible
import com.pminteractive.pmtours.network.toSede
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

// 1. Declaración de la entidad para usuarios locales
data class UsuarioLocalEntity(
    val id: String,
    val rut: String,
    val nombreCompleto: String,
    val correo: String,
    val telefono: String,
    val sede: Sede,
    val rol: RolUsuario
)

// 2. Interfaz con todos los métodos (incluidos los de Administrador)
interface AuthRepository {
    suspend fun login(usuario: String, contrasena: String): Result<Usuario>
    fun obtenerUsuarioActual(): Usuario?
    fun obtenerUsuarios(): List<Usuario>
    fun observarUsuariosAdmin(): Flow < List < Pair < Usuario, Boolean > > >
    suspend fun crearUsuarioLocal(usuario: UsuarioLocalEntity): Result<Unit>
    suspend fun alternarEstadoUsuario(id: String, activo: Boolean): Result<Unit>
    fun cerrarSesion()
}

// 3. Implementación de la interfaz
class HybridAuthRepository(
    private val apiService: LinceApiService
) : AuthRepository {

    private val claveDemo = "1234"
    private val gson = Gson()
    private val archivoUsuariosLocales = File(AppContainer.filesDir, "usuarios_locales.json")
    private val archivoEstadosUsuarios = File(AppContainer.filesDir, "estados_usuarios.json")

    private val _usuariosLocalesData: MutableStateFlow < List < UsuarioLocalEntity > > = MutableStateFlow(emptyList())
    private val _estadosUsuarios: MutableStateFlow < Map < String, Boolean > > = MutableStateFlow(emptyMap())
    private val _listaReactivaAdmin: MutableStateFlow < List < Pair < Usuario, Boolean > > > = MutableStateFlow(emptyList())

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
            rol = RolUsuario.DRIVER,
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
            rol = RolUsuario.GUIA,
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
            direccion = "Moneda 920, Santiago Centro",
            rol = RolUsuario.COORDINADOR
        ),
        Administrador(
            id = "ADM-001",
            rut = "12.345.678-9",
            nombreCompleto = "Admin PM Tours",
            correo = "admin@pmtours.cl",
            telefono = "+56900001111",
            sede = Sede.SANTIAGO,
            direccion = "Oficina Central PM Tours, Santiago",
            rol = RolUsuario.ADMINISTRADOR
        )
    )

    private var usuarioSesionActual: Usuario? = null

    init {
        cargarDatosLocales()
    }

    private fun cargarDatosLocales() {
        runCatching {
            if (archivoUsuariosLocales.exists()) {
                val tipoLista = object : TypeToken < List < UsuarioLocalEntity > > () {}.type
                val listaParseada: List<UsuarioLocalEntity>? = gson.fromJson(archivoUsuariosLocales.readText(), tipoLista)
                _usuariosLocalesData.value = listaParseada ?: emptyList()
            }
            if (archivoEstadosUsuarios.exists()) {
                val tipoMapa = object : TypeToken < Map < String, Boolean > > () {}.type
                val mapaParseado: Map<String, Boolean>? = gson.fromJson(archivoEstadosUsuarios.readText(), tipoMapa)
                _estadosUsuarios.value = mapaParseado ?: emptyMap()
            }
        }.onSuccess {
            actualizarFlujoReactivo()
        }
    }

    private fun guardarDatosLocales() {
        runCatching {
            archivoUsuariosLocales.writeText(gson.toJson(_usuariosLocalesData.value))
            archivoEstadosUsuarios.writeText(gson.toJson(_estadosUsuarios.value))
        }.onSuccess {
            actualizarFlujoReactivo()
        }
    }

    private fun mapearLocalAUsuario(entidad: UsuarioLocalEntity): Usuario {
        return when (entidad.rol) {
            RolUsuario.DRIVER -> Driver(
                id = entidad.id,
                rut = entidad.rut,
                nombreCompleto = entidad.nombreCompleto,
                correo = entidad.correo,
                telefono = entidad.telefono,
                sede = entidad.sede,
                direccion = "Sin registrar",
                rol = RolUsuario.DRIVER,
                datosBancarios = DatosBancarios("N/A", "N/A", "N/A", "N/A", "N/A"),
                vehiculos = emptyList()
            )
            RolUsuario.GUIA -> Guia(
                id = entidad.id,
                rut = entidad.rut,
                nombreCompleto = entidad.nombreCompleto,
                correo = entidad.correo,
                telefono = entidad.telefono,
                sede = entidad.sede,
                direccion = "Sin registrar",
                rol = RolUsuario.GUIA,
                datosBancarios = DatosBancarios("N/A", "N/A", "N/A", "N/A", "N/A"),
                idiomas = emptyList(),
                especialidades = emptyList()
            )
            RolUsuario.COORDINADOR -> Coordinador(
                id = entidad.id,
                rut = entidad.rut,
                nombreCompleto = entidad.nombreCompleto,
                correo = entidad.correo,
                telefono = entidad.telefono,
                sede = entidad.sede,
                direccion = "Sin registrar",
                rol = RolUsuario.COORDINADOR
            )
            RolUsuario.ADMINISTRADOR -> Administrador(
                id = entidad.id,
                rut = entidad.rut,
                nombreCompleto = entidad.nombreCompleto,
                correo = entidad.correo,
                telefono = entidad.telefono,
                sede = entidad.sede,
                direccion = "Sin registrar",
                rol = RolUsuario.ADMINISTRADOR
            )
        }
    }

    private fun actualizarFlujoReactivo() {
        val listaCompleta = usuariosBase + _usuariosLocalesData.value.map { mapearLocalAUsuario(it) }
        val listaMapeada = listaCompleta.map { usuario ->
            val estaActivo = _estadosUsuarios.value[usuario.id] ?: true
            Pair(usuario, estaActivo)
        }
        _listaReactivaAdmin.value = listaMapeada
    }

    override suspend fun login(usuario: String, contrasena: String): Result<Usuario> {
        if (contrasena != claveDemo) {
            return Result.failure(Exception("Contraseña incorrecta (usa 1234)."))
        }

        val entrada = usuario.trim()
        val codigoNormalizado = (atajosRapidos[entrada.lowercase()] ?: entrada).uppercase()

        val usuarioLocalParaBloqueo = _usuariosLocalesData.value.find { it.id.equals(codigoNormalizado, true) || it.correo.equals(entrada, true) }
        val usuarioBaseParaBloqueo = usuariosBase.find { it.id.equals(codigoNormalizado, true) || it.correo.equals(entrada, true) }
        val idCanonico = usuarioLocalParaBloqueo?.id ?: usuarioBaseParaBloqueo?.id ?: codigoNormalizado

        val estaActivo = _estadosUsuarios.value[idCanonico] ?: true
        if (!estaActivo) return Result.failure(Exception("Esta cuenta ha sido suspendida por el administrador."))

        val usuarioLocal = _usuariosLocalesData.value.find { it.id.equals(codigoNormalizado, true) || it.correo.equals(entrada, true) }
        if (usuarioLocal != null) {
            val u = mapearLocalAUsuario(usuarioLocal)
            usuarioSesionActual = u
            return Result.success(u)
        }

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
                    rol = RolUsuario.DRIVER,
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
                    rol = RolUsuario.GUIA,
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

        val usuarioBase = usuariosBase.find {
            it.id.equals(codigoNormalizado, ignoreCase = true) ||
                    it.correo.equals(entrada, ignoreCase = true)
        }

        return if (usuarioBase != null) {
            usuarioSesionActual = usuarioBase
            Result.success(usuarioBase)
        } else {
            Result.failure(Exception("Usuario '$entrada' no encontrado en la API ni en cuentas base."))
        }
    }

    override fun obtenerUsuarioActual(): Usuario? = usuarioSesionActual

    override fun obtenerUsuarios(): List<Usuario> {
        return usuariosBase + _usuariosLocalesData.value.map { mapearLocalAUsuario(it) }
    }

    override fun observarUsuariosAdmin(): Flow < List < Pair < Usuario, Boolean > > > {
        actualizarFlujoReactivo()
        return _listaReactivaAdmin.asStateFlow()
    }

    override suspend fun crearUsuarioLocal(usuario: UsuarioLocalEntity): Result<Unit> {
        val existeLocal = _usuariosLocalesData.value.any { it.id.equals(usuario.id, true) || it.correo.equals(usuario.correo, true) }
        val existeBase = usuariosBase.any { it.id.equals(usuario.id, true) || it.correo.equals(usuario.correo, true) }

        if (existeLocal || existeBase) {
            return Result.failure(Exception("Ya existe un usuario con ese correo o ID."))
        }

        val listaActual = _usuariosLocalesData.value.toMutableList()
        listaActual.add(0, usuario)
        _usuariosLocalesData.value = listaActual
        guardarDatosLocales()
        return Result.success(Unit)
    }

    override suspend fun alternarEstadoUsuario(id: String, activo: Boolean): Result<Unit> {
        val mapaActual = _estadosUsuarios.value.toMutableMap()
        mapaActual[id] = activo
        _estadosUsuarios.value = mapaActual
        guardarDatosLocales()
        return Result.success(Unit)
    }

    override fun cerrarSesion() {
        usuarioSesionActual = null
    }
}