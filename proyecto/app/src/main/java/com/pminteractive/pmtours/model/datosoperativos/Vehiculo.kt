package com.pminteractive.pmtours.model.datosoperativos

// Modelo para el catálogo de autos de cada transportista
data class Vehiculo(
    val id: String,
    val modelo: String,          // Ej: "Mercedes-Benz Sprinter", "Hyundai H1", "Toyota Rav4"
    val patente: String,         // Ej: "ABCD-12"
    val capacidadPasajeros: Int, // Ej: 12, 4
    val tipo: String = "Van"     // Ej: "Van", "Minibús", "Sedán", "SUV"
)