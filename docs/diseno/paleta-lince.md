# Paleta de colores Lince

Esta paleta es la fuente de referencia visual de PM Tours. Los valores viven en
`proyecto/app/src/main/java/com/pminteractive/pmtours/ui/theme/Color.kt`.

## Reglas de uso

- El rojo se reserva para errores, cancelaciones y rechazos.
- Un día bloqueado no es un error: se representa en gris pizarra oscuro con candado.
- Un día con servicio se representa en azul informativo.
- Un día disponible se representa en cyan corporativo.
- No usar códigos hexadecimales directamente en pantallas o componentes.
- No depender solo del color: acompañar con texto o icono cuando corresponda.

## Base

| Token | Hex | Uso |
|---|---|---|
| `LinceBackground` | `#0A1929` | Fondo general |
| `LinceSurface` | `#132F4C` | Tarjetas, campos y superficies |
| `LinceSurfaceBorder` | `#1E4976` | Bordes y separadores |
| `LincePrimary` | `#00A3E0` | Acción principal y disponibilidad |
| `LincePrimaryPressed` | `#008CC2` | Estado presionado del primario |
| `LinceTextPrimary` | `#FFFFFF` | Texto principal |
| `LinceTextSecondary` | `#94A3B8` | Texto secundario y ayudas |
| `LinceWarning` | `#F59E0B` | Advertencias y restricciones |
| `LinceError` | `#FF5252` | Errores de validación |
| `LinceErrorContainer` | `#26FF5252` | Fondo tenue de error |

## Estados de servicio

| Estado | Fondo | Texto | Etiqueta |
|---|---|---|---|
| Asignado | `LincePendingSoft` `#FFE5D0` | `LincePending` `#B45309` | Pendiente |
| Confirmado | `LinceSuccessSoft` `#D1F2D9` | `LinceSuccess` `#15803D` | Confirmado |
| En ruta | `LinceServiceSoft` `#E0F2FE` | `LinceService` `#0369A1` | En ruta |
| Finalizado | `LinceFinishedSoft` `#334155` | `LinceFinished` `#94A3B8` | Finalizado |
| Rechazado | `LinceDangerSoft` `#FFE4E6` | `LinceDanger` `#E11D48` | Rechazado |
| Cancelado | `LinceDangerSoft` `#FFE4E6` | `LinceDanger` `#E11D48` | Cancelado |

El mapeo central está en `ui/theme/EstadoColors.kt` y se consume mediante
`EstadoServicio.estiloVisual()`.

## Disponibilidad del calendario

| Estado | Fondo | Texto / marca |
|---|---|---|
| Disponible | `LinceSurface` | `LincePrimary` |
| Con servicio | `LinceServiceSoft` | `LinceService` + punto |
| Bloqueado | `LinceBlockedStrong` `#334155` | `LinceBlockedSoft` `#E2E8F0` + candado |

Borde de celda bloqueada: `LinceBlockedBorder` `#94A3B8`.

Leyenda visible en la pantalla de calendario:

- Punto cyan: Disponible.
- Punto azul: Con servicio.
- Candado gris claro: Bloqueado.

## Roles

| Rol | Fondo | Texto |
|---|---|---|
| Administrador | `LinceRoleAdminSoft` | `LinceRoleAdmin` |
| Coordinador | `LinceRoleCoordinatorSoft` `#EDE9FE` | `LinceRoleCoordinator` `#6D28D9` |
| Driver | `LinceRoleDriverSoft` | `LinceRoleDriver` |
| Guía | `LinceRoleGuideSoft` | `LinceRoleGuide` |

## Ruta y estados especiales

| Token | Hex | Uso |
|---|---|---|
| `LinceRouteStart` | `#1E3A8A` | Inicio de ruta |
| `LinceRouteEnd` | `#EA580C` | Fin de ruta |
| `LinceRouteSurface` | `#E2E8F0` | Fondo del gráfico de ruta |
| `LinceFinishedSurface` | `#1E293B` | Fondo de servicio completado |
| `LinceSuccessAccent` | `#10B981` | Éxito y check-in a tiempo |
| `LinceSuccessContainer` | `#2610B981` | Fondo tenue de éxito |

## Implementación

- `ui/theme/Color.kt`: definición de todos los tokens.
- `ui/theme/EstadoColors.kt`: colores y etiquetas por estado de servicio.
- `ui/theme/Theme.kt`: integración con Material 3.
- `docs/diseno/paleta-lince.svg`: referencia visual exportable.
