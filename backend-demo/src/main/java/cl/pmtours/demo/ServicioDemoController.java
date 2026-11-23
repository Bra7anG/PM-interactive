package cl.pmtours.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/servicios")
@Tag(name = "Servicios", description = "Gestion de servicios turisticos: aceptar/rechazar asignaciones y registro de check-in/check-out")
public class ServicioDemoController {

    private static final Logger log = LoggerFactory.getLogger(ServicioDemoController.class);

    private final Map<String, ServicioDemo> servicios = new ConcurrentHashMap<>();

    public ServicioDemoController() {
        String hoy = LocalDate.now().toString();
        String proximaHora = LocalTime.now().plusHours(1).withSecond(0).withNano(0).toString();

        servicios.put("SRV-000245", new ServicioDemo("SRV-000245", "ASIGNADO", hoy, "09:00", null, null));
        servicios.put("SRV-000251", new ServicioDemo("SRV-000251", "CONFIRMADO", hoy, proximaHora, null, null));
        servicios.put("SRV-000247", new ServicioDemo("SRV-000247", "CONFIRMADO", LocalDate.now().plusDays(1).toString(), "08:00", null, null));
        servicios.put("SRV-000257", new ServicioDemo("SRV-000257", "ASIGNADO", hoy, "09:00", null, null));
    }

    @GetMapping
    @Operation(summary = "Listar estado de servicios", description = "Muestra el estado actual de todos los servicios registrados en memoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de servicios y sus estados")
    })
    public Map<String, ServicioDemo> listarServicios() {
        return servicios;
    }

    @PatchMapping("/{id}/respuesta")
    @Operation(summary = "Aceptar o rechazar asignacion", description = "Cambia el estado de ASIGNADO a CONFIRMADO o RECHAZADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asignacion procesada"),
            @ApiResponse(responseCode = "400", description = "Falta el campo 'aceptar'"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Transicion de estado invalida")
    })
    public ResponseEntity<?> responderAsignacion(
            @PathVariable String id,
            @RequestBody(required = false) RespuestaRequest request
    ) {
        log.info(">>> PATCH /respuesta id={} aceptar={}", id, request != null ? request.aceptar() : null);

        if (request == null || request.aceptar() == null) {
            log.warn("<<< 400 falta campo 'aceptar' para {}", id);
            return ResponseEntity.badRequest().body(new ErrorResponse("Debe indicar el campo 'aceptar'."));
        }

        ServicioDemo actual = obtenerOCrear(id, "ASIGNADO");
        if (!"ASIGNADO".equals(actual.estado())) {
            log.warn("<<< 409 transicion invalida desde {} para {}", actual.estado(), id);
            return transicionInvalida(actual);
        }

        ServicioDemo actualizado = actual.conEstado(request.aceptar() ? "CONFIRMADO" : "RECHAZADO");
        servicios.put(id, actualizado);
        log.info("<<< 200 {} -> {}", id, actualizado.estado());
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/check-in")
    @Operation(summary = "Registrar check-in", description = "Cambia el estado de CONFIRMADO a EN_RUTA y guarda la posicion GPS de inicio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-in registrado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos GPS"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "409", description = "El servicio no esta en estado CONFIRMADO")
    })
    public ResponseEntity<?> registrarCheckIn(
            @PathVariable String id,
            @RequestBody(required = false) HitoRequest request
    ) {
        log.info(">>> POST /check-in id={} lat={} lon={}", id,
                request != null ? request.latitud() : null,
                request != null ? request.longitud() : null);

        ResponseEntity<?> validacion = validarHito(request);
        if (validacion != null) {
            log.warn("<<< 400 datos GPS faltantes para {}", id);
            return validacion;
        }

        ServicioDemo actual = obtenerOCrear(id, "CONFIRMADO");
        if (!"CONFIRMADO".equals(actual.estado())) {
            log.warn("<<< 409 transicion invalida desde {} para {}", actual.estado(), id);
            return transicionInvalida(actual);
        }

        ServicioDemo actualizado = actual.conCheckIn(crearHito(actual, request));
        servicios.put(id, actualizado);
        log.info("<<< 200 {} -> EN_RUTA", id);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/check-out")
    @Operation(summary = "Registrar check-out", description = "Cambia el estado de EN_RUTA a FINALIZADO y guarda la posicion GPS de llegada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-out registrado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos GPS"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "409", description = "El servicio no esta en estado EN_RUTA")
    })
    public ResponseEntity<?> registrarCheckOut(
            @PathVariable String id,
            @RequestBody(required = false) HitoRequest request
    ) {
        log.info(">>> POST /check-out id={} lat={} lon={}", id,
                request != null ? request.latitud() : null,
                request != null ? request.longitud() : null);

        ResponseEntity<?> validacion = validarHito(request);
        if (validacion != null) {
            log.warn("<<< 400 datos GPS faltantes para {}", id);
            return validacion;
        }

        ServicioDemo actual = obtenerOCrear(id, "EN_RUTA");
        if (!"EN_RUTA".equals(actual.estado())) {
            log.warn("<<< 409 transicion invalida desde {} para {}", actual.estado(), id);
            return transicionInvalida(actual);
        }

        ServicioDemo actualizado = actual.conCheckOut(crearHito(actual, request));
        servicios.put(id, actualizado);
        log.info("<<< 200 {} -> FINALIZADO", id);
        return ResponseEntity.ok(actualizado);
    }

    private ServicioDemo obtenerOCrear(String id, String estadoInicial) {
        ServicioDemo actual = servicios.get(id);
        if (actual != null) {
            return actual;
        }
        ServicioDemo nuevo = new ServicioDemo(
                id,
                estadoInicial,
                LocalDate.now().toString(),
                LocalTime.now().plusHours(1).withSecond(0).withNano(0).toString(),
                null,
                null
        );
        servicios.put(id, nuevo);
        log.info("    (servicio {} creado automaticamente en estado {})", id, estadoInicial);
        return nuevo;
    }

    private ResponseEntity<?> validarHito(HitoRequest request) {
        if (request == null
                || request.latitud() == null
                || request.longitud() == null
                || request.direccionReferencia() == null
                || request.direccionReferencia().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Debe indicar latitud, longitud y direccionReferencia."));
        }
        return null;
    }

    private Hito crearHito(ServicioDemo servicio, HitoRequest request) {
        return new Hito(
                request.latitud(),
                request.longitud(),
                request.direccionReferencia(),
                System.currentTimeMillis(),
                calcularATiempo(servicio)
        );
    }

    private boolean calcularATiempo(ServicioDemo servicio) {
        try {
            LocalDateTime momentoPactado = LocalDateTime.of(
                    LocalDate.parse(servicio.fecha()),
                    LocalTime.parse(servicio.horaInicio())
            );
            return !LocalDateTime.now().isAfter(momentoPactado.minusMinutes(15));
        } catch (RuntimeException error) {
            return true;
        }
    }

    private ResponseEntity<ErrorResponse> transicionInvalida(ServicioDemo servicio) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Transicion invalida desde el estado " + servicio.estado()));
    }

    public record RespuestaRequest(Boolean aceptar) {
    }

    public record HitoRequest(Double latitud, Double longitud, String direccionReferencia) {
    }

    public record Hito(
            double latitud,
            double longitud,
            String direccionReferencia,
            long timestamp,
            boolean aTiempo
    ) {
    }

    public record ServicioDemo(
            String id,
            String estado,
            String fecha,
            String horaInicio,
            Hito checkIn,
            Hito checkOut
    ) {
        ServicioDemo conEstado(String nuevoEstado) {
            return new ServicioDemo(id, nuevoEstado, fecha, horaInicio, checkIn, checkOut);
        }

        ServicioDemo conCheckIn(Hito hito) {
            return new ServicioDemo(id, "EN_RUTA", fecha, horaInicio, hito, checkOut);
        }

        ServicioDemo conCheckOut(Hito hito) {
            return new ServicioDemo(id, "FINALIZADO", fecha, horaInicio, checkIn, hito);
        }
    }

    public record ErrorResponse(String mensaje) {
    }
}
