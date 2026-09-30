# Especificación funcional — App de administración financiera con mayordomía cristiana

## 1. Visión general

**Principio rector:** todo lo que administramos pertenece a Dios; nosotros somos mayordomos, no dueños (1 Corintios 4:2, Salmo 24:1). La app no es "un Excel con versículos" — el principio de mayordomía se traduce en **funciones concretas** (el diezmo se aparta antes de mostrar el "saldo disponible", el sábado tiene un tratamiento distinto al resto de la semana, etc.), no solo en textos decorativos.

**Hereda del análisis del md anterior:**
- Una sola fuente de captura (todo pasa por `Movimientos`; el resto de pantallas son vistas derivadas, no vuelven a pedir el dato).
- Presupuestado vs. Real con % de variación (no solo "cuánto gasté", sino "cuánto me desvié").
- Semáforo visual único como resumen de salud financiera (en vez de una tabla de ratios).
- Categorías editables por el usuario, no ancladas a una lista fija.

**Nombre sugerido (opcional, tú decides):** *Primicias*, *Fiel*, o *Mayordomía* — los tres comunican el concepto sin sonar a app genérica de finanzas. Uso "la app" en el resto del documento.

---

## 2. Arquitectura de navegación

```
┌─────────────────────────────────────────────────┐
│                  BOTTOM TAB BAR                  │
│  Inicio | Movimientos | Diezmos | Metas | Reportes│
└─────────────────────────────────────────────────┘

Fuera del tab bar (acceso por ícono o desde una tarjeta):
  - Onboarding (solo primera vez)
  - Configuración inicial / wizard (solo primera vez)
  - Perfil y Configuración (ícono, esquina superior)
  - Notificaciones (ícono campana)
  - Reflexión semanal de mayordomía (tarjeta en Inicio, viernes/sábado)
  - Detalle de transacción / meta / deuda / categoría (drill-down desde cualquier lista)
```

5 pestañas es el límite recomendado para no complicar la navegación (patrón estándar en apps financieras tipo YNAB/Mint). Todo lo demás cuelga de una de esas 5, nunca es una sexta pestaña.

---

## 3. Flujo de datos entre pantallas (cómo se relacionan)

```
Onboarding ──► Configuración inicial (wizard) ──► Inicio

                    ┌────────────────────────────────────┐
                    │            MOVIMIENTOS              │
                    │   (única fuente de captura: todo    │
                    │  ingreso, gasto, pago de deuda o     │
                    │      aporte a meta se registra aquí) │
                    └───────────────┬──────────────────────┘
                                    │  dispara cálculos automáticos
             ┌──────────────┬──────┴───────┬───────────────┐
             ▼              ▼              ▼               ▼
          INICIO        DIEZMOS         METAS          REPORTES
      (resumen y      (aparta % del   (Ahorros y      (ratios,
       accesos          ingreso,       Deudas —        semáforo,
       directos)        historial)     segmentado)     presup. vs real)
```

**Regla de diseño:** ninguna pantalla fuera de `Movimientos` tiene un formulario de "agregar ingreso/gasto" propio. Si necesitas registrar un pago de deuda desde la pantalla `Metas`, el botón te lleva al formulario de `Movimientos` con el tipo y la categoría pre-seleccionados — no duplica el formulario.

---

## 4. Pantallas — detalle

### 4.1 Onboarding (solo primer uso)

**Propósito:** presentar el enfoque de mayordomía antes de pedir datos, para que el usuario entienda el "por qué" antes del "cómo".

**Funcionalidad:**
- 3 pantallas deslizables: (1) "Todo lo que tienes es un préstamo de Dios" (2) "Organiza con orden, no con culpa" (3) "El sábado también descansa tus finanzas"
- Botón "Comenzar" → Configuración inicial

**Relación:** única entrada al flujo; no se vuelve a mostrar (salvo que el usuario la busque en Configuración).

**Principio bíblico:** mayordomía como marco general (1 Corintios 4:2) — se presenta como identidad, no como regla.

---

### 4.2 Configuración inicial (wizard, 4 pasos)

**Propósito:** capturar lo mínimo para que el dashboard tenga sentido desde el primer uso.

**Funcionalidad — paso a paso:**
1. **Datos básicos:** nombre/familia, moneda, ¿individual o familiar? (si es familiar, agregar miembros)
2. **Ingresos:** fuente(s) de ingreso y monto estimado mensual
3. **Mayordomía:** % de diezmo (default 10%, editable), ¿ofrenda adicional? (checkbox + %), día de pago habitual del diezmo
4. **Categorías de gasto:** lista editable con las categorías más comunes ya sugeridas (vivienda, alimentación, transporte, salud, educación, deudas, ocio) — el usuario borra/agrega según su caso

**Relación:** al terminar, escribe la configuración base y navega a `Inicio`. Los valores de este wizard son editables después desde `Configuración`, no son fijos.

**Principio bíblico:** planificar antes de actuar (Lucas 14:28) — se calcula el "costo" (categorías, diezmo) antes de empezar a registrar movimientos.

---

### 4.3 Inicio (Dashboard)

**Propósito:** vista de 10 segundos — "¿cómo estoy este mes?" — sin tener que entrar a otra pantalla.

**Funcionalidad — tarjetas, de arriba hacia abajo:**
1. **Semáforo del mes** (verde/amarillo/rojo) + una frase corta del veredicto (igual que el modelo SBS: "Tu nivel de gasto es adecuado y te permite ahorrar")
2. **Saldo disponible real** — importante: este número **ya tiene el diezmo y la ofrenda restados**, no es el ingreso bruto. Debajo, en texto pequeño: "Diezmo y ofrenda ya apartados: S/ XXX"
3. **Ingresos vs. Gastos del mes** (barra simple, no gráfico complejo)
4. **Diezmo pendiente de este mes** (si no se ha registrado el pago) → tap lleva a `Diezmos`
5. **Meta principal** (la que el usuario marcó como prioritaria) con barra de progreso → tap lleva a `Metas`
6. **Tarjeta de Reflexión semanal** — aparece solo viernes en la tarde y sábado (ver 4.9)

**Relación:** es el punto central; cada tarjeta es un atajo a su pantalla correspondiente, nunca duplica el dato ni permite editarlo ahí mismo.

**Principio bíblico:** conocer bien el estado de tus recursos antes de decidir (Proverbios 27:23-24) — de un vistazo, sabes dónde estás.

---

### 4.4 Movimientos (fuente única de captura)

**Propósito:** registrar y consultar **todo** movimiento de dinero — es el corazón operativo de la app.

**Funcionalidad:**
- Botón flotante `+` → selector de tipo: `Ingreso` | `Gasto` | `Pago de deuda` | `Aporte a meta`
- **Al registrar un Ingreso:** el formulario calcula automáticamente el diezmo (%) y la ofrenda (si aplica) y los muestra como líneas informativas antes de guardar — el usuario ve "de estos S/ 1000, S/ 100 son diezmo" *antes* de confirmar, no después
- **Al registrar un Gasto:** categoría (de la lista configurada), monto, fecha, ¿presupuestado o imprevisto? — si la categoría ya tiene presupuesto asignado, muestra en tiempo real "te quedan S/ XX de esta categoría este mes"
- Lista de movimientos con filtro por tipo/categoría/fecha, buscador simple
- Tap en un movimiento → detalle con opción de editar/eliminar

**Relación:** todo lo que aquí se guarda dispara actualización automática de `Inicio`, `Diezmos`, `Metas` y `Reportes`. Ninguna otra pantalla escribe datos nuevos, solo lee de aquí.

**Principio bíblico:** fidelidad en lo poco (Lucas 16:10) — el detalle exhaustivo de cada movimiento, no solo el total.

---

### 4.5 Diezmos y Ofrendas

**Propósito:** separar visual y funcionalmente el dinero "ya apartado" del "disponible para gastar" — esta pantalla es la diferenciadora del producto frente a cualquier app financiera genérica.

**Funcionalidad:**
- Monto acumulado del mes (calculado automáticamente desde los ingresos registrados en `Movimientos`)
- Botón "Marcar como entregado" — cuando el usuario efectivamente da el diezmo/ofrenda, lo confirma aquí (esto no es solo contable: refuerza el hábito)
- Historial mensual/anual: cumplimiento por mes (útil para ver constancia a lo largo del año, no solo el mes actual)
- Sección opcional de "Ofrendas específicas" (proyecto especial, misión, etc.) que el usuario puede agregar manualmente además del % automático

**Relación:** se alimenta de `Movimientos` (ingresos), no tiene formulario propio de captura de ingresos — solo el de "confirmar entrega" y el de "ofrenda específica" (que internamente crea un registro tipo `Ofrenda` en `Movimientos`).

**Principio bíblico:** primicias — lo primero para Dios, no el sobrante (Proverbios 3:9, Malaquías 3:10). Funcionalmente: el diezmo se calcula y se muestra **antes** de que el usuario vea cuánto tiene "para gastar", nunca después.

---

### 4.6 Metas (segmentado: Ahorros | Deudas)

**Propósito:** una sola pestaña para todo lo que es "dinero comprometido a futuro" — ahorrar y pagar deudas son la misma lógica (apartar dinero con un objetivo), así que comparten pantalla con un selector superior.

**Sub-vista "Ahorros":**
- Lista de metas (fondo de emergencia, meta específica, libre) con barra de progreso
- Cada meta tiene: nombre, monto objetivo, monto actual, fecha estimada
- Botón "Aportar" → lleva a `Movimientos` con tipo `Aporte a meta` pre-seleccionado

**Sub-vista "Deudas":**
- Lista de deudas con: saldo pendiente, tasa, cuota mensual, meses restantes (calculado, no tipeado)
- Botón "Registrar pago" → `Movimientos` con tipo `Pago de deuda` pre-seleccionado
- Indicador simple si el total de cuotas mensuales supera el 40% del ingreso (alerta de sobreendeudamiento, tomado del modelo SBS)

**Relación:** ambas sub-vistas leen de `Movimientos`; los formularios de crear una meta o una deuda nueva sí viven aquí (son configuración, no transacciones).

**Principio bíblico:**
- Ahorros → previsión prudente (Proverbios 6:6-8, la hormiga que se prepara) y contentamiento (1 Timoteo 6:6-8 — no acumular por acumular, cada meta tiene un propósito nombrado).
- Deudas → no vivir atado a lo que no se puede pagar (Romanos 13:8, Proverbios 22:7).

---

### 4.7 Reportes

**Propósito:** el análisis que responde "¿por qué estoy en amarillo/rojo?" cuando el semáforo de `Inicio` no basta.

**Funcionalidad:**
- Presupuestado vs. Real por categoría (barra doble, con % de variación) — la mejora #3 del análisis anterior
- Gasto por categoría (torta o barras simples, no más de 8-9 categorías visibles)
- Ratios clave (heredados del modelo SBS, pero solo 3, no 10): % de gasto sobre ingreso, % de deuda sobre ingreso, % de ahorro sobre ingreso
- Semáforo detallado: qué categoría específica está empujando el color (ej. "tu semáforo está en amarillo por Transporte, que superó el presupuesto en 35%")
- Filtro por mes/rango de meses

**Relación:** solo lectura, no tiene formularios; todo dato viene de `Movimientos`, `Metas` y `Diezmos`.

**Principio bíblico:** mayordomía fiel exige revisión periódica, no solo buena intención (Proverbios 27:23-24 aplicado como hábito recurrente, no como acción única).

---

### 4.8 Configuración / Perfil

**Propósito:** todo lo que se definió en el wizard, editable después.

**Funcionalidad:**
- Editar % de diezmo/ofrenda, categorías de gasto, miembros de familia, moneda
- Notificaciones: recordatorio de diezmo, recordatorio de pago de deudas, resumen semanal
- **"Modo Sábado"** (on/off, activado por defecto): entre el viernes al atardecer y el sábado al atardecer, la app **no envía recordatorios de gasto ni promueve registrar compras** — no bloquea el registro de datos (si el usuario necesita anotar algo, puede), pero prioriza mostrar la tarjeta de Reflexión en vez de notificaciones de consumo
- Exportar datos (CSV) — útil dado tu perfil de BI, aunque es "nice to have", no MVP

**Relación:** accesible desde ícono en `Inicio`; cambios aquí afectan el comportamiento de todas las demás pantallas (no crea datos nuevos).

**Principio bíblico:** el sábado como reposo integral, incluido lo financiero (Éxodo 20:8-11) — es la única función de la app que **restringe** comportamiento en vez de solo mostrar información, y es intencional.

---

### 4.9 Reflexión semanal de mayordomía

**Propósito:** cerrar el ciclo semanal con una revisión breve, no solo números — conecta el hábito financiero con el ciclo del sábado.

**Funcionalidad:**
- Aparece como tarjeta en `Inicio` viernes en la tarde y sábado
- Resumen de la semana: cuánto entró, cuánto salió, si el diezmo fue entregado
- Una pregunta de reflexión corta (rotativa, banco de 8-10 preguntas: "¿Esta semana gasté por necesidad o por impulso?", "¿Aparté lo de Dios antes que lo mío?") — sin obligar a responder, es opcional
- No hay input de datos financieros aquí, es solo lectura + reflexión

**Relación:** solo lee de `Movimientos` y `Diezmos`; no es una pantalla que el usuario busca activamente, aparece contextualmente.

**Principio bíblico:** el sábado como espacio de evaluación y descanso (Levítico 23:3) — el único momento de la app diseñado para *no* actuar, solo revisar.

---

## 5. Lógica de negocio transversal (cómo funciona "por debajo")

| Regla | Comportamiento |
|---|---|
| Única fuente de verdad | Todo movimiento se crea en `Movimientos`; ninguna otra pantalla tiene formulario de captura de transacciones nuevas (solo de configuración: metas, deudas, categorías) |
| Diezmo automático | Al guardar un `Ingreso`, se calcula `% diezmo` y `% ofrenda` configurados, y se muestran como líneas separadas antes de confirmar — no se resta en silencio |
| Saldo disponible | = Ingresos − Diezmo/Ofrenda − Gastos − Pagos de deuda − Aportes a metas. El diezmo se resta primero, no al final |
| Presupuesto por categoría | Cada categoría tiene un monto planeado (configurable); cada gasto nuevo actualiza en tiempo real "cuánto queda" de esa categoría |
| Semáforo | 3 colores según el % de gasto total + deuda sobre el ingreso (umbral simplificado del modelo SBS: verde <70%, amarillo 70-90%, rojo >90%) |
| Modo Sábado | Silencia notificaciones de tipo "gasto/consumo" en la ventana viernes-atardecer a sábado-atardecer; no bloquea registro manual de datos |
| Deudas → cuota | La cuota mensual de una deuda no se tipea suelta: se calcula desde saldo, tasa y plazo (evita el error de desactualizar la cuota a mano) |

---

## 6. Tabla resumen — pantalla ↔ principio ↔ función concreta

| Pantalla | Principio bíblico | Cómo se manifiesta (no solo texto) |
|---|---|---|
| Onboarding | Mayordomía (1 Cor. 4:2) | Se presenta como identidad antes de pedir datos |
| Config. inicial | Planificación (Lucas 14:28) | Wizard obliga a definir categorías y % de diezmo antes de usar la app |
| Inicio | Conocimiento del estado (Prov. 27:23-24) | Semáforo + saldo ya neto de diezmo, visible en 10 segundos |
| Movimientos | Fidelidad en lo poco (Lucas 16:10) | Registro detallado por transacción, fuente única |
| Diezmos | Primicias (Prov. 3:9, Mal. 3:10) | El diezmo se calcula y muestra **antes** del saldo disponible |
| Metas (Ahorros) | Previsión y contentamiento (Prov. 6:6-8, 1 Tim. 6:6-8) | Cada ahorro tiene nombre/propósito, no es "dinero suelto" |
| Metas (Deudas) | No esclavitud de deudas (Rom. 13:8) | Alerta automática si las cuotas superan 40% del ingreso |
| Reportes | Revisión periódica (Prov. 27:23-24) | Presupuestado vs. real, no solo el total gastado |
| Configuración | Reposo del sábado (Éx. 20:8-11) | Modo Sábado restringe notificaciones de consumo, única función que "frena" en vez de informar |
| Reflexión semanal | Sábado como evaluación (Lev. 23:3) | Única pantalla sin input financiero, solo revisión y pregunta reflexiva |

---

## 7. Alcance del prototipo (MVP vs. futuro)

**Para el prototipo v1, prioriza:**
- Onboarding + Configuración inicial
- Inicio
- Movimientos (con cálculo automático de diezmo/ofrenda)
- Diezmos (vista simple: acumulado del mes + botón confirmar entrega)
- Metas (solo Ahorros; Deudas puede ser v2 si el tiempo aprieta)
- Reportes (solo semáforo + presupuestado vs real; los ratios detallados pueden ser v2)

**Deja para después (no bloquea el prototipo):**
- Modo Sábado automático (para el prototipo puede ser un toggle visual sin lógica de horario real)
- Reflexión semanal con banco de preguntas rotativo
- Exportar CSV
- Multi-usuario/familia con roles

---

## 8. Modelo de datos sugerido (referencia rápida)

```
Usuario/Familia
Transaccion   { tipo: ingreso|gasto|diezmo|ofrenda|pago_deuda|aporte_meta,
                monto, categoria, fecha, presupuestado: bool }
Categoria     { nombre, monto_presupuestado_mensual }
Meta          { nombre, monto_objetivo, monto_actual, tipo: emergencia|especifica|libre }
Deuda         { nombre, saldo, tasa, cuota_mensual, meses_restantes }
ConfigMayordomia { pct_diezmo, pct_ofrenda, dia_pago_habitual }
```

Todo lo que ves en las pantallas es una vista filtrada/agregada de estas 6 entidades — ninguna pantalla necesita su propia tabla de datos.
