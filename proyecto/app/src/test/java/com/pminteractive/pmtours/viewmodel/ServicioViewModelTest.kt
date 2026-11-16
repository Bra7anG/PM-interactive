package com.pminteractive.pmtours.viewmodel

import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.support.RepositorioServiciosFake
import com.pminteractive.pmtours.support.servicioPrueba
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ServicioViewModelTest {

    private lateinit var scope: CoroutineScope
    private val hoy: LocalDate = LocalDate.now()

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun cargaSoloServiciosDelTrabajador() {
        val repositorio = RepositorioServiciosFake(
            listOf(
                servicioPrueba(id = "A", driverId = "TRA-001"),
                servicioPrueba(id = "B", driverId = "TRA-999")
            )
        )

        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        assertEquals(listOf("A"), viewModel.servicios.value.map { it.id })
    }

    @Test
    fun cargaServiciosAsignadosComoGuia() {
        val repositorio = RepositorioServiciosFake(
            listOf(
                servicioPrueba(id = "G", driverId = null, guiaId = "GUI-001"),
                servicioPrueba(id = "OTRO", driverId = "TRA-001", guiaId = null)
            )
        )

        val viewModel = ServicioViewModel(repositorio, "GUI-001", scope)

        assertEquals(listOf("G"), viewModel.servicios.value.map { it.id })
    }

    @Test
    fun filtraFinalizadosDeOtrosMesesYMantieneActivos() {
        val repositorio = RepositorioServiciosFake(
            listOf(
                servicioPrueba(
                    id = "FINALIZADO_MES",
                    estado = EstadoServicio.FINALIZADO,
                    fecha = hoy.toString()
                ),
                servicioPrueba(
                    id = "FINALIZADO_OTRO_MES",
                    estado = EstadoServicio.FINALIZADO,
                    fecha = hoy.minusMonths(1).toString()
                ),
                servicioPrueba(
                    id = "ACTIVO",
                    estado = EstadoServicio.ASIGNADO,
                    fecha = hoy.minusMonths(1).toString()
                ),
                servicioPrueba(
                    id = "CANCELADO",
                    estado = EstadoServicio.CANCELADO,
                    fecha = hoy.minusMonths(1).toString()
                )
            )
        )

        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        assertEquals(
            setOf("FINALIZADO_MES", "ACTIVO", "CANCELADO"),
            viewModel.servicios.value.map { it.id }.toSet()
        )
    }

    @Test
    fun resumenMesActualSumaFinalizadosYCanceladosDelMes() = runBlocking {
        val repositorio = RepositorioServiciosFake(
            listOf(
                servicioPrueba(
                    id = "FINALIZADO",
                    estado = EstadoServicio.FINALIZADO,
                    fecha = hoy.toString(),
                    tarifa = 45_000.0
                ),
                servicioPrueba(
                    id = "CANCELADO",
                    estado = EstadoServicio.CANCELADO,
                    fecha = hoy.toString(),
                    tarifa = 30_000.0
                ),
                servicioPrueba(
                    id = "FINALIZADO_OTRO_MES",
                    estado = EstadoServicio.FINALIZADO,
                    fecha = hoy.minusMonths(1).toString(),
                    tarifa = 99_999.0
                ),
                servicioPrueba(
                    id = "ACTIVO",
                    estado = EstadoServicio.ASIGNADO,
                    fecha = hoy.toString(),
                    tarifa = 99_999.0
                )
            )
        )

        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)
        val resumen = withTimeout(5_000) {
            viewModel.resumenMesActual.first { it.nombreMes.isNotEmpty() }
        }

        assertEquals(75_000.0, resumen.totalAcumulado, 0.001)
        assertEquals(1, resumen.serviciosCompletados)
        assertEquals(1, resumen.serviciosCanceladosConPago)
    }

    @Test
    fun obtenerServicioPorIdExistenteYDesconocido() {
        val repositorio = RepositorioServiciosFake(
            listOf(servicioPrueba(id = "A"))
        )
        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        assertEquals("A", viewModel.obtenerServicioPorId("A")?.id)
        assertNull(viewModel.obtenerServicioPorId("NO-EXISTE"))
    }

    @Test
    fun responderAsignacionAceptadaActualizaAConfirmado() {
        val repositorio = RepositorioServiciosFake(
            listOf(servicioPrueba(id = "A", estado = EstadoServicio.ASIGNADO))
        )
        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        viewModel.responderAsignacion("A", aceptar = true)

        assertEquals(
            EstadoServicio.CONFIRMADO,
            viewModel.servicios.value.first { it.id == "A" }.estado
        )
    }

    @Test
    fun responderAsignacionRechazadaActualizaARechazado() {
        val repositorio = RepositorioServiciosFake(
            listOf(servicioPrueba(id = "A", estado = EstadoServicio.ASIGNADO))
        )
        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        viewModel.responderAsignacion("A", aceptar = false)

        assertEquals(
            EstadoServicio.RECHAZADO,
            viewModel.servicios.value.first { it.id == "A" }.estado
        )
    }

    @Test
    fun checkInRegistraHitoYCambiaAEnRuta() {
        val repositorio = RepositorioServiciosFake(
            listOf(
                servicioPrueba(
                    id = "A",
                    estado = EstadoServicio.CONFIRMADO,
                    fecha = hoy.plusDays(1).toString()
                )
            )
        )
        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        viewModel.realizarCheckIn("A")

        val servicio = viewModel.servicios.value.first { it.id == "A" }
        assertEquals(EstadoServicio.EN_RUTA, servicio.estado)
        assertNotNull(servicio.checkIn)
        assertTrue(servicio.checkIn!!.aTiempo)
    }

    @Test
    fun checkInTardioMarcaFueraDePlazo() {
        val repositorio = RepositorioServiciosFake(
            listOf(
                servicioPrueba(
                    id = "A",
                    estado = EstadoServicio.CONFIRMADO,
                    fecha = hoy.minusDays(1).toString()
                )
            )
        )
        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        viewModel.realizarCheckIn("A")

        val servicio = viewModel.servicios.value.first { it.id == "A" }
        assertEquals(EstadoServicio.EN_RUTA, servicio.estado)
        assertNotNull(servicio.checkIn)
        assertFalse(servicio.checkIn!!.aTiempo)
    }

    @Test
    fun checkOutFinalizaServicioYRegistraHito() {
        val repositorio = RepositorioServiciosFake(
            listOf(servicioPrueba(id = "A", estado = EstadoServicio.EN_RUTA))
        )
        val viewModel = ServicioViewModel(repositorio, "TRA-001", scope)

        viewModel.realizarCheckOut("A")

        val servicio = viewModel.servicios.value.first { it.id == "A" }
        assertEquals(EstadoServicio.FINALIZADO, servicio.estado)
        assertNotNull(servicio.checkOut)
    }
}
