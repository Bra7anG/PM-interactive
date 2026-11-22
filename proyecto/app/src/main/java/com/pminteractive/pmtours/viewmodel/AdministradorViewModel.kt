package com.pminteractive.pmtours.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.datosoperativos.DatosBancarios
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.operadores.*
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.toSede
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.repository.AuthRepository
import com.pminteractive.pmtours.repository.UsuarioLocalEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ResumenAdministrador(
    val totalUsuarios: Int = 0,
    val drivers: Int = 0,
    val guias: Int = 0,
    val coordinadores: Int = 0
)

class AdministradorViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val apiService: LinceApiService = AppContainer.apiService
) : ViewModel() {

    private val _todosLosUsuarios : MutableStateFlow < List < Usuario > > = MutableStateFlow(emptyList())

    private val _filtroRol : MutableStateFlow < RolUsuario? > = MutableStateFlow(null)
    val filtroRol = _filtroRol.asStateFlow()

    private val _filtroSede : MutableStateFlow < Sede? > = MutableStateFlow(null)
    val filtroSede = _filtroSede.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    val usuariosFiltrados : StateFlow < List < Usuario > > = combine(
        _todosLosUsuarios, _filtroRol, _filtroSede
    ) { lista, rol, sede ->
        lista.filter { u ->
            (rol == null || u.rol == rol) &&
                    (sede == null || sede.codigo == "ALL" || u.sede.codigo == sede.codigo)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resumen : StateFlow < ResumenAdministrador > = _todosLosUsuarios
        .map { lista -> calcularResumen(lista) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenAdministrador())

    init {
        cargarTodosLosUsuarios()
    }

    fun cargarTodosLosUsuarios() {
        viewModelScope.launch {
            _cargando.value = true
            val usuariosLocales = authRepository.obtenerUsuarios()

            val usuariosApi = mutableListOf < Usuario > ()
            runCatching {
                val respTransportistas = apiService.getTransportistas(pageSize = 100)
                respTransportistas.data.forEach { t ->
                    usuariosApi.add(
                        Driver(
                            id = t.codigo,
                            rut = "N/A",
                            nombreCompleto = t.nombre,
                            correo = t.nombre.lowercase().replace(" ", ".") + "@pmtours.cl",
                            telefono = "Sin registrar",
                            sede = t.oficina.toSede(),
                            direccion = "",
                            rol = RolUsuario.DRIVER,
                            datosBancarios = DatosBancarios("","","","",""),
                            vehiculos = emptyList()
                        )
                    )
                }

                val respGuias = apiService.getGuias(pageSize = 100)
                respGuias.data.forEach { g ->
                    usuariosApi.add(
                        Guia(
                            id = g.codigo,
                            rut = "N/A",
                            nombreCompleto = g.nombre,
                            correo = g.nombre.lowercase().replace(" ", ".") + "@pmtours.cl",
                            telefono = "Sin registrar",
                            sede = g.oficina.toSede(),
                            direccion = "",
                            rol = RolUsuario.GUIA,
                            datosBancarios = DatosBancarios("","","","",""),
                            idiomas = emptyList(),
                            especialidades = emptyList()
                        )
                    )
                }
            }

            val listaFusionada = (usuariosLocales + usuariosApi).distinctBy { it.id }
            _todosLosUsuarios.value = listaFusionada
            _cargando.value = false
        }
    }

    fun setFiltroRol(rol: RolUsuario?) {
        _filtroRol.value = rol
    }

    fun setFiltroSede(sede: Sede?) {
        _filtroSede.value = sede
    }

    private fun calcularResumen(lista: List < Usuario > ) = ResumenAdministrador(
        totalUsuarios = lista.size,
        drivers = lista.count { it.rol == RolUsuario.DRIVER },
        guias = lista.count { it.rol == RolUsuario.GUIA },
        coordinadores = lista.count { it.rol == RolUsuario.COORDINADOR }
    )

    fun generarArchivoReporte(context: Context): Uri? {
        return runCatching {
            val listaActual = _todosLosUsuarios.value
            val resumenData = resumen.value
            val fechaHoy = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

            val sb = StringBuilder()
            sb.append("====================================\n")
            sb.append("REPORTE OPERATIVO PM TOURS\n")
            sb.append("Generado: $fechaHoy\n")
            sb.append("====================================\n\n")

            sb.append("RESUMEN GLOBAL:\n")
            sb.append("- Total Personal: ${resumenData.totalUsuarios}\n")
            sb.append("- Drivers: ${resumenData.drivers}\n")
            sb.append("- Guías: ${resumenData.guias}\n")
            sb.append("- Coordinadores/Staff: ${resumenData.coordinadores}\n\n")

            sb.append("====================================\n")
            sb.append("LISTA DE STAFF POR SEDE\n")
            sb.append("====================================\n")

            val agrupadosPorSede = listaActual.groupBy { it.sede }
            agrupadosPorSede.forEach { (sede, staff) ->
                sb.append("\n[ SEDE: ${sede.nombreCiudad.uppercase()} (${staff.size}) ]\n")
                staff.sortedBy { it.rol.name }.forEach { usr ->
                    val origen = if (usr.id.contains("LOC")) "(LOCAL)" else "(API/BASE)"
                    sb.append(" - [${usr.rol.name}] ${usr.nombreCompleto} $origen\n")
                    sb.append("   ID: ${usr.id} | Email: ${usr.correo}\n")
                }
            }

            val archivoReporte = File(context.cacheDir, "Reporte_Operativo_PMTours.txt")
            archivoReporte.writeText(sb.toString())

            FileProvider.getUriForFile(context, "${context.packageName}.provider", archivoReporte)
        }.getOrNull()
    }

    // NUEVA FUNCIÓN: Se encarga de la lógica de negocio para crear el usuario
    suspend fun crearUsuarioManual(nombre: String, rol: RolUsuario, sede: Sede): Result < Unit > {
        val prefijo = when(rol) {
            RolUsuario.DRIVER -> "TRA"
            RolUsuario.GUIA -> "GUI"
            RolUsuario.COORDINADOR -> "CRD"
            else -> "ADM"
        }
        val correoGenerado = nombre.lowercase().replace(" ", ".") + "@pmtours.cl"
        val idLocal = "$prefijo-LOC-${System.currentTimeMillis() % 10000}"

        val nuevaEntidad = UsuarioLocalEntity(
            id = idLocal,
            rut = "Sin registrar",
            nombreCompleto = nombre.trim(),
            correo = correoGenerado,
            telefono = "+56900000000",
            sede = sede,
            rol = rol
        )

        return authRepository.crearUsuarioLocal(nuevaEntidad)
    }
}