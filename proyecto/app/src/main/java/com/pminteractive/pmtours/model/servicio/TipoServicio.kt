/**
 * OBJETIVO DEL SCRIPT:
 * Modelar los tipos oficiales de servicios turísticos gestionados por la empresa.
 *
 * RESUMEN:
 * Clasifica la operación según la columna 'Srv' y 'Tipo' de la planilla de asignaciones,
 * vinculando el código abreviado con su descripción de negocio.
 *
 * DETALLE:
 * - TRANSFER (TR): Traslados exclusivos con transportista y vehículo.
 * - GUIDED_SERVICE (GS): Servicios guiados que exigen guía calificado en idioma requerido.
 * - EXCURSION (EX): Excursiones completas operadas por transportista con vehículo.
 */
package com.pminteractive.pmtours.model.servicio

enum class TipoServicio(val srv: String, val descripcion: String) {
    TRANSFER("TR", "Transfer"),
    GUIDED_SERVICE("GS", "Servicio Guiado"),
    EXCURSION("EX", "Excursión")
}