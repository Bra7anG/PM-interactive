package com.pminteractive.pmtours.network.dto

// 1. Envoltorios estándar de la API (todas las respuestas vienen dentro de "data")
data class ApiResponse<T>(
    val data: T
)

data class PaginatedResponse<T>(
    val data: List<T>,
    val meta: PaginacionMetaDto
)

data class PaginacionMetaDto(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)

// 2. Catálogos generales (/api/v1/catalogos)
data class CatalogosDto(
    val oficinas: List<OficinaDto>,
    val idiomas: List<IdiomaDto>,
    val tiposServicio: List<TipoServicioCatalogoDto>,
    val estadosServicio: List<CodigoDescripcionDto>,
    val statusPL: List<CodigoDescripcionDto>,
    val tiposVehiculo: List<TipoVehiculoCatalogoDto>
)

data class OficinaDto(
    val codigo: String, // "SCL", "PUX", "PUQ"
    val nombre: String  // "Santiago", "Puerto Varas", "Punta Arenas"
)

data class IdiomaDto(
    val codigo: String, // "ES", "EN", "PT", "DE", "FR"
    val nombre: String  // "Español", "Inglés", etc.
)

data class TipoServicioCatalogoDto(
    val tipo: String,   // "TRANSFER", "GUIDED_SERVICE", "EXCURSION"
    val srv: String     // "TR", "GS", "EX"
)

data class CodigoDescripcionDto(
    val codigo: String,
    val descripcion: String
)

data class TipoVehiculoCatalogoDto(
    val tipo: String,   // "SEDAN", "VAN", "MINIBUS", "BUS"
    val capacidad: Int
)

// 3. Personal, flota y clientes (/transportistas, /guias, /clientes)
data class VehiculoDto(
    val id: Int,
    val patente: String,
    val tipo: String,
    val capacidad: Int
)

data class TransportistaDto(
    val id: Int,
    val codigo: String, // ej. "TRA-020"
    val nombre: String,
    val oficina: OficinaDto,
    val vehiculos: List<VehiculoDto>,
    val evaluacionPromedio: Double,
    val fechaIngreso: String,
    val activo: Boolean
)

data class GuiaDto(
    val id: Int,
    val codigo: String, // ej. "GUI-012"
    val nombre: String,
    val oficina: OficinaDto,
    val idiomas: List<IdiomaDto>,
    val evaluacionPromedio: Double,
    val fechaIngreso: String,
    val activo: Boolean
)

data class ClienteDto(
    val id: Int,
    val codigo: String, // ej. "CLI-007"
    val nombre: String,
    val pais: String
)

// 4. Servicios (/api/v1/servicios)
data class PersonaRefDto(
    val id: Int,
    val codigo: String,
    val nombre: String
)

data class ReservaDto(
    val referencia: String,
    val nombre: String,
    val estado: String
)

data class PaxDto(
    val adultos: Int,
    val ninos: Int,
    val total: Int,
    val texto: String   // ej. "2+0"
)

data class AsignacionDto(
    val statusPL: String?,      // "CF", "RQ", "DL", "CX" o null
    val actualizadoEn: String?
)

data class ServicioDto(
    val id: Int,
    val codigo: String,
    val srv: String,
    val tipo: String,
    val fecha: String,
    val horaPickup: String,
    val descripcion: String,
    val descripcionPadre: String?,
    val estadoServicio: String,
    val reserva: ReservaDto,
    val pax: PaxDto,
    val pickup: String,
    val dropoff: String,
    val notas: String?,
    val oficina: OficinaDto,
    val cliente: ClienteDto,
    val idiomaRequerido: IdiomaDto?,
    val guia: PersonaRefDto?,
    val transportista: PersonaRefDto?,
    val vehiculo: VehiculoDto?,
    val asignacion: AsignacionDto
)

// 5. Indicadores / KPIs (/api/v1/resumen)
data class ConteoPorClaveDto(
    val clave: String,
    val servicios: Int,
    val pax: Int
)

data class ResumenDto(
    val fechaDesde: String?,
    val fechaHasta: String?,
    val totalServicios: Int,
    val totalPax: Int,
    val porEstadoServicio: List<ConteoPorClaveDto>,
    val porStatusPL: List<ConteoPorClaveDto>,
    val porOficina: List<ConteoPorClaveDto>,
    val porTipo: List<ConteoPorClaveDto>,
    val porDia: List<ConteoPorClaveDto>
)