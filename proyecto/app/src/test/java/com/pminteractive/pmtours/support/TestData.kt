package com.pminteractive.pmtours.support

import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.datosoperativos.Vehiculo
import com.pminteractive.pmtours.model.operadores.Driver
import com.pminteractive.pmtours.model.operadores.Guia
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.repository.AuthRepository
import com.pminteractive.pmtours.repository.ServicioRepository
import com.pminteractive.pmtours.repository.UsuarioLocalEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

fun driverPrueba(
    id: String = "TRA-001",
    nombreCompleto: String = "Carlos Morales",
    correo: String = "carlos.morales@pmtours.cl"
): Driver = Driver(
    id = id,
    rut = "18.452.123-K",
    nombreCompleto = nombreCompleto,
    correo = correo,
    telefono = "+56911223344",
    sede = Sede.SANTIAGO,
    direccion = "Av. Providencia 1234, Santiago",
    datosBancarios = DatosBancarios(
        banco = "BancoEstado",
        tipoCuenta = "Cuenta RUT",
        numeroCuenta = "18452123",
        titular = nombreCompleto,
        rutTitular = "18.452.123-K"
    ),
    vehiculos = listOf(
        Vehiculo("VEH-01", "Toyota RAV4 2021", "PRTC-89", 4, "SEDAN")
    )
)

fun guiaPrueba(
    id: String = "GUI-001",
    nombreCompleto: String = "Valentina Lagos",
    correo: String = "valentina.lagos@pmtours.cl"
): Guia = Guia(
    id = id,
    rut = "19.876.543-2",
    nombreCompleto = nombreCompleto,
    correo = correo,
    telefono = "+56955667788",
    sede = Sede.PUERTO_VARAS,
    direccion = "Costanera 450, Puerto Varas",
    datosBancarios = DatosBancarios(
        banco = "Banco de Chile",
        tipoCuenta = "Cuenta Corriente",
        numeroCuenta = "9988776601",
        titular = nombreCompleto,
        rutTitular = "19.876.543-2"
    ),
    idiomas = listOf("Español", "Inglés"),
    especialidades = listOf("Ruta Patrimonial")
)

fun servicioPrueba(
    id: String = "SRV-001",
    estado: EstadoServicio = EstadoServicio.ASIGNADO,
    fecha: String = LocalDate.now().toString(),
    horaInicio: String = "12:00",
    horaFinEstimada: String = "13:00",
    driverId: String? = "TRA-001",
    guiaId: String? = null,
    tarifa: Double = 10_000.0,
    tipo: TipoServicio = TipoServicio.TRANSFER,
    titulo: String = "Servicio de prueba",
    checkIn: RegistroHitoGps? = null,
    checkOut: RegistroHitoGps? = null
): Servicio = Servicio(
    id = id,
    numeroReserva = "WHFT-$id",
    titulo = titulo,
    tipo = tipo,
    fecha = fecha,
    horaInicio = horaInicio,
    horaFinEstimada = horaFinEstimada,
    lugarPickup = "Origen de prueba",
    lugarDropOff = "Destino de prueba",
    cantidadPasajeros = 2,
    paxTexto = "2+0",
    driverId = driverId,
    vehiculoId = if (driverId != null) "VEH-01" else null,
    guiaId = guiaId,
    estado = estado,
    tarifaAcordada = tarifa,
    checkIn = checkIn,
    checkOut = checkOut
)

class RepositorioServiciosFake(
    serviciosIniciales: List<Servicio>
) : ServicioRepository {

    private val _servicios = MutableStateFlow(serviciosIniciales)

    override fun obtenerTodosLosServicios(): Flow<List<Servicio>> {
        return _servicios.map { lista -> lista }
    }

    override fun obtenerServiciosPorTrabajador(trabajadorId: String): Flow<List<Servicio>> {
        return _servicios.map { lista ->
            lista.filter { it.driverId == trabajadorId || it.guiaId == trabajadorId }
        }
    }

    override fun obtenerServiciosPorSede(driverIdsDeSede: List<String>): Flow<List<Servicio>> {
        return _servicios.map { lista ->
            lista.filter { it.driverId in driverIdsDeSede || it.driverId == null }
        }
    }

    override suspend fun obtenerServicioPorId(servicioId: String): Servicio? {
        return _servicios.value.find { it.id == servicioId }
    }

    override suspend fun responderAsignacion(servicioId: String, aceptar: Boolean): Result<Unit> {
        val actual = _servicios.value.find { it.id == servicioId }
            ?: return Result.failure(Exception("Servicio no encontrado."))

        val nuevoEstado = if (aceptar) EstadoServicio.CONFIRMADO else EstadoServicio.RECHAZADO
        _servicios.value = _servicios.value.map {
            if (it.id == servicioId) actual.copy(estado = nuevoEstado) else it
        }
        return Result.success(Unit)
    }

    override suspend fun registrarCheckIn(servicioId: String, hito: RegistroHitoGps): Result<Unit> {
        val actual = _servicios.value.find { it.id == servicioId }
            ?: return Result.failure(Exception("Servicio no encontrado."))

        _servicios.value = _servicios.value.map {
            if (it.id == servicioId) actual.copy(estado = EstadoServicio.EN_RUTA, checkIn = hito) else it
        }
        return Result.success(Unit)
    }

    override suspend fun registrarCheckOut(servicioId: String, hito: RegistroHitoGps): Result<Unit> {
        val actual = _servicios.value.find { it.id == servicioId }
            ?: return Result.failure(Exception("Servicio no encontrado."))

        _servicios.value = _servicios.value.map {
            if (it.id == servicioId) actual.copy(estado = EstadoServicio.FINALIZADO, checkOut = hito) else it
        }
        return Result.success(Unit)
    }
}

class RepositorioAuthFake(
    private val usuario: Usuario? = driverPrueba(),
    private val puerta: CompletableDeferred<Unit>? = null
) : AuthRepository {

    var llamadasLogin = 0
    private var usuarioActual: Usuario? = null

    override suspend fun login(correo: String, contrasena: String): Result<Usuario> {
        llamadasLogin++
        puerta?.await()

        val usuarioEncontrado = usuario
            ?: return Result.failure(Exception("Credenciales incorrectas o usuario no encontrado."))

        usuarioActual = usuarioEncontrado
        return Result.success(usuarioEncontrado)
    }

    override fun obtenerUsuarioActual(): Usuario? = usuarioActual

    override fun obtenerUsuarios(): List<Usuario> = listOfNotNull(usuario)

    override fun observarUsuariosAdmin(): Flow<List<Pair<Usuario, Boolean>>> =
        kotlinx.coroutines.flow.flowOf(listOfNotNull(usuario?.let { it to true }))

    override suspend fun crearUsuarioLocal(usuario: UsuarioLocalEntity): Result<Unit> =
        Result.success(Unit)

    override suspend fun alternarEstadoUsuario(id: String, activo: Boolean): Result<Unit> =
        Result.success(Unit)

    override fun cerrarSesion() {
        usuarioActual = null
    }
}
