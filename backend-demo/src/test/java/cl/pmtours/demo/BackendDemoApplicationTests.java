package cl.pmtours.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ejecutaFlujoDeComandos() throws Exception {
        mockMvc.perform(patch("/api/v1/servicios/SRV-000245/respuesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceptar\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        mockMvc.perform(post("/api/v1/servicios/SRV-000251/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitud\":-33.39,\"longitud\":-70.79,\"direccionReferencia\":\"Aeropuerto SCL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_RUTA"))
                .andExpect(jsonPath("$.checkIn.aTiempo").value(true));

        mockMvc.perform(post("/api/v1/servicios/SRV-000251/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitud\":-33.43,\"longitud\":-70.65,\"direccionReferencia\":\"Hotel Lastarria\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));

        mockMvc.perform(post("/api/v1/servicios/SRV-000257/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitud\":-33.43,\"longitud\":-70.65,\"direccionReferencia\":\"Hotel Lastarria\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void autoCreaServicioDesconocidoYLista() throws Exception {
        mockMvc.perform(patch("/api/v1/servicios/SRV-NUEVO-01/respuesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceptar\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        mockMvc.perform(get("/api/v1/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SRV-NUEVO-01.estado").value("CONFIRMADO"));
    }
}
