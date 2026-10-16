package com.pminteractive.pmtours.model.datosoperativos

data class DatosBancarios(
    val banco: String,          // Ej: "BancoEstado", "Banco de Chile", "Santander"
    val tipoCuenta: String,     // Ej: "Cuenta Corriente", "Cuenta RUT"
    val numeroCuenta: String,
    val titular: String,
    val rutTitular: String
)