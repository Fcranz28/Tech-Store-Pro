# Segunda tarea - Desarrollador Software

Transcripción estructurada del PDF entregado: **Actividad N.º 2 de capacitación - Desarrollador Software Fullstack - Mini E-commerce Avanzado Fullstack Java + Spring Boot + Angular - Caso propuesto: «TechStore Pro»**.

## Presentación

Tienda online de accesorios tecnológicos (teclados, mouse, audífonos). El objetivo es que el desarrollador practique autenticación, carrito persistente y pedidos.

| Aspecto | Indicación del PDF |
| --- | --- |
| Nivel esperado | Junior / trainee con base sólida |
| Duración sugerida | 5 a 7 días de desarrollo + 1 día de revisión |
| Stack objetivo | Backend en Spring Boot, frontend en Angular, BD relacional |
| Entregables mínimos | Código funcional, README, endpoints y pantallas principales |

## 1. Contexto del caso

TechStore Pro vende productos tecnológicos, pero actualmente gestiona pedidos manualmente. La empresa quiere una plataforma donde los usuarios puedan registrarse, iniciar sesión, agregar productos al carrito y realizar pedidos de forma estructurada.

## 2. Objetivo de la actividad

Desarrollar una aplicación web fullstack que incluya autenticación, carrito persistente y gestión de pedidos, demostrando integración completa entre frontend y backend.

## 3. Alcance funcional

- Registro e inicio de sesión de usuarios.
- Visualizar catálogo.
- Carrito persistente.
- Confirmación de pedidos.
- Panel admin básico.

## 4. Historias de usuario

| ID | Historia |
| --- | --- |
| HU01 | Como usuario, quiero registrarme para tener una cuenta. |
| HU02 | Como usuario, quiero iniciar sesión para acceder al sistema. |
| HU03 | Como usuario, quiero ver productos disponibles. |
| HU04 | Como usuario, quiero agregar productos al carrito. |
| HU05 | Como usuario, quiero confirmar un pedido. |
| HU06 | Como admin, quiero gestionar productos. |
| HU07 | Como admin, quiero ver pedidos. |

## 5. Requerimientos backend

| ID | Requerimiento |
| --- | --- |
| RF-BE-01 | Autenticación con JWT. |
| RF-BE-02 | CRUD de productos. |
| RF-BE-03 | Gestión de carrito. |
| RF-BE-04 | Registro de pedidos. |
| RF-BE-05 | Validación de stock. |

## 6. Requerimientos frontend

| ID | Requerimiento |
| --- | --- |
| RF-FE-01 | Login y registro. |
| RF-FE-02 | Catálogo. |
| RF-FE-03 | Carrito. |
| RF-FE-04 | Confirmación de compra. |
| RF-FE-05 | Protección de rutas. |

## 7. Requerimientos no funcionales

Estructura limpia, uso de capas en backend, separación clara en frontend y validaciones básicas.

## 8. Modelo de datos sugerido

Usuario, Producto, Carrito, Pedido y DetallePedido.

## 9. Endpoints sugeridos

- `/auth/login`
- `/auth/register`
- `/productos`
- `/carrito`
- `/pedidos`

## 10. Criterios de aceptación

Login funcional, carrito persistente, pedido válido e integración completa.

## Nota de alcance de esta entrega

Este archivo recoge el enunciado. El proyecto implementa registro, login, catálogo, carrito persistente, confirmación de pedidos con validación de stock y panel admin para productos y pedidos. No incluye cobros, envíos ni reportes porque no forman parte del alcance indicado.
