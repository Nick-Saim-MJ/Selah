# Reporte de mayordomía integral — Tiempo, Talento, Tesoro, Templo

La mayordomía adventista abarca toda la vida, no solo el dinero. El reporte combina las cuatro dimensiones en
puntajes 0–100 y un semáforo, con historial en `reporte_mayordomia` (snapshot + `version_algoritmo`).

| Dimensión | Qué mide | Fuente de datos |
|---|---|---|
| **Tiempo** | Culto personal, lección de Escuela Sabática, culto familiar, asistencia a la iglesia, sábado guardado | Hábitos en la app y/o API de otro equipo |
| **Talento** | Servicio en ministerios, obra misionera, desarrollo de dones, ayuda a la comunidad | Hábitos en la app y/o API de otro equipo |
| **Tesoro** | Fidelidad en el diezmo, semáforo de gasto, ahorro, salud de deuda | **Calculado** desde las finanzas (sin registro manual) |
| **Templo** | Los 8 remedios naturales: agua, ejercicio, descanso, sol, aire puro, temperancia, alimentación, confianza en Dios | Hábitos en la app y/o API de otro equipo (ej. módulo de salud) |

Catálogo inicial (18 hábitos con meta y referencia bíblica): migración `V7__datos_semilla.sql`.

## Algoritmo v1.0 (`reportes/domain/service/CalculadoraPuntajes`)

**Tesoro** (solo si hubo ingresos en el periodo):

| Componente | Peso | Cálculo |
|---|---|---|
| Fidelidad en el diezmo | 40% | entregado ÷ apartado × 100 (100 si no hubo apartado) |
| Semáforo de gasto | 25% | (gastos + pagos de deuda) ÷ ingresos: < 70% → 100 · 70–90% → 60 · > 90% → 20 |
| Ahorro | 20% | aportes a metas ÷ ingresos, meta 10% → tope 100 |
| Salud de deuda | 15% | cuotas ÷ ingresos ≤ 40% → 100; luego −5 por cada punto sobre 40% |

**Tiempo, Talento, Templo:** promedio del cumplimiento de cada hábito con meta en el periodo
(`valorRegistrado ÷ metaPeriodo`, tope 100% por hábito, para no compensar un área con exceso en otra).
Se combinan registros locales y de APIs externas (`MetricasDimensionPort`).

**Global:** promedio simple de las dimensiones con datos (una dimensión sin datos no penaliza).
**Semáforo 4T:** ≥ 75 verde · 50–74 amarillo · < 50 rojo.

Semáforo financiero de Inicio (independiente): % de gasto + deuda sobre ingreso, verde < 70%, amarillo 70–90%, rojo > 90%.

> Cambiar pesos o umbrales = subir `CalculadoraPuntajes.VERSION` para no mezclar reportes históricos calculados distinto.

## Cómo se genera y quién lo ve

- `GET /api/v1/reportes/mayordomia?periodo=YYYY-MM` calcula y guarda el snapshot; un job diario (`selah.reportes.cron`) lo regenera para todos.
- **Hermano / pastor:** ven su propio reporte completo (con montos en sus finanzas).
- **Pastor sobre su congregación** (`GET /api/v1/pastor/congregacion`, `/pastor/reportes-compartidos`):
  - *Totales anónimos*: hermanos activos, cuántos tienen reporte, % con diezmo al día y promedio de cada dimensión. Solo se muestran si hay
    al menos 3 hermanos con datos (`selah.congregacion.minimo-anonimato`); nunca hay dinero.
  - *Reportes compartidos*: solo de hermanos de su iglesia que activaron «Compartir mi reporte con mi pastor»: nombre, puntajes 4T,
    semáforo, desglose de hábitos y **si cumplió el diezmo (sí/no)**. **Nunca montos.** El hermano puede retirar el permiso cuando quiera.
- **Administrador:** no accede a ningún reporte ni monto.

## Pendiente

- Endpoint equivalente con scope `reportes:read` para otros equipos (integraciones).
- Pesos y umbrales del algoritmo por validar con el equipo pastoral/de mayordomía antes de producción.
