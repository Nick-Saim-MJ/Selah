/**
 * SelahFinance API — Clean Architecture organizada por módulos (bounded contexts).
 *
 * <pre>
 * com.selahfinance
 * ├── config/          Raíz de composición: seguridad, OpenAPI, reloj. Puede ver todos los módulos.
 * ├── shared/          Kernel compartido (Dinero, Periodo, eventos, errores). Sin lógica de módulos.
 * └── &lt;modulo&gt;/
 *     ├── domain/          Entidades, value objects, reglas y eventos. Java puro: sin Spring, JPA ni HTTP.
 *     ├── application/
 *     │   ├── port/in/     Casos de uso (API pública del módulo; otros módulos solo usan esto).
 *     │   ├── port/out/    Lo que el caso de uso necesita del exterior (repositorios, APIs externas).
 *     │   └── usecase/     Implementación de los casos de uso (orquesta dominio + puertos).
 *     └── infrastructure/
 *         ├── persistence/ Adaptadores JPA (entidades @Entity, Spring Data, mappers).
 *         ├── web/         Controladores REST + DTOs de request/response (versionados /api/v1).
 *         └── adapter/     Otros adaptadores de salida (APIs de otros equipos, otros módulos).
 * </pre>
 *
 * Regla de dependencia: web/persistence → application → domain. Nunca al revés.
 * Se verifica automáticamente con ArchUnit (ArquitecturaTest).
 */
package com.selahfinance;
