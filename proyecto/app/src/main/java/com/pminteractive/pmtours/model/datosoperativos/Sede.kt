/**
 * OBJETIVO DEL SCRIPT:
 * Definir el catálogo oficial de sedes y oficinas operativas de Southbound / PM Tours.
 *
 * RESUMEN:
 * Enumera las bases operativas vinculando su nombre geográfico con el código de oficina
 * estándar utilizado en la planilla y en la API del sistema.
 *
 * DETALLE:
 * - Define 'codigo' con la nomenclatura de la API: SCL (Santiago), PUX (Puerto Varas) y PUQ (Punta Arenas).
 * - Facilita que los filtros de las consultas coincidan directamente con los parámetros del servidor.
 */
package com.pminteractive.pmtours.model.datosoperativos

enum class Sede(val codigo: String, val nombreCiudad: String) {
    SANTIAGO("SCL", "Santiago"),
    PUERTO_VARAS("PUX", "Puerto Varas"),
    PUNTA_ARENAS("PUQ", "Punta Arenas")
}