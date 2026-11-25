# FlagLeague Manager — Guía individual de desarrollo

## Emilio: Interfaz gráfica JavaFX y experiencia de uso

**Materia:** Lenguajes Orientados a Objetos  
**Proyecto compartido:** FlagLeague Manager — Administración de una liga de Tochito Bandera  
**Responsable:** **Emilio**  
**Tecnologías:** Java 21, JavaFX (FXML y controladores), Maven, CSS y JUnit 5  
**Duración:** 10 días hábiles / 2 semanas  
**Rama personal:** `feature/emilio-javafx`  
**Evaluación:** individual, 100 puntos  
**Versión:** 1.0 — documento de trabajo, **no contiene la solución**

> **Un sistema, no cinco proyectos.** Tu responsabilidad es la capa visual del mismo proyecto Maven que desarrollan Yan, Paul, Carlos y Parra. No crearás un proyecto JavaFX independiente ni incorporarás en los controladores las reglas deportivas. El hecho de que tus pantallas dependan de los servicios de otro compañero no te exime de entregar código, pruebas y evidencias propias.

### Referencias obligatorias

- [01 — Documento maestro](01-documento-maestro.md): requisitos, casos de uso y reglas de negocio.
- [02 — UML del dominio](02-modelo-dominio.mmd): clases, estados y relaciones.
- [03 — Contratos de integración](03-contratos-integracion.md): operaciones disponibles y dependencias.
- [04 — Guía de Yan](04-guia-yan-dominio.md): datos y reglas que representa el modelo.
- [05 — Guía de Paul](05-guia-paul-servicios.md): servicios a consumir.

**Las firmas publicadas en 03 son propuestas a aprobar el día 1.** Si un método que necesitas no está definido, solicítalo a Paul o Parra y documenta el acuerdo; no inventes una firma pública unilateralmente.

---

## 1. Objetivo individual

**Diseñar, implementar, conectar y probar** una interfaz gráfica de escritorio para consultar y administrar temporadas, categorías, equipos, jugadores, torneos, inscripciones, jornadas, partidos, anotaciones, posiciones y campeonatos, empleando JavaFX y separando adecuadamente las responsabilidades de vista, controlador y servicios.

### Resultado esperado

El usuario podrá completar el recorrido principal de la liga sin utilizar la consola ni editar variables a mano: registrar equipos, crear un torneo, inscribir participantes, generar calendario, capturar anotaciones, consultar posiciones y visualizar al campeón. Los mensajes de error serán comprensibles y la aplicación no se cerrará ante una validación de negocio.

## 2. Responsabilidades y límites

**Debes programar:**

1. Estructura de navegación y pantalla inicial.
2. Seis vistas en FXML y sus controladores.
3. Formularios con validación de entradas, controles JavaFX y mensajes de retroalimentación.
4. Adaptación de entidades o DTO a filas para `TableView`, sin modificar el dominio.
5. Conexión con servicios de Paul y Parra mediante dependencias inyectadas por el arranque de Parra.
6. Actualización visual tras crear, editar, consultar o finalizar operaciones.
7. Pruebas de lógica de presentación que no requieran una ventana y guion de pruebas manuales de la interfaz.
8. Documentación de errores y capturas de la demostración.

**No debes programar:**

- Entidades o calculadora de marcador (Yan).
- CRUD y generación de round-robin (Paul).
- `HashMap` de repositorios ni datos demo (Carlos).
- Posiciones, playoffs, campeón o composición de dependencias (`AppBootstrap`) (Parra).
- Cálculos deportivos, puntuaciones, validación de elegibilidad ni SQL dentro del controlador.

## 3. Ubicación del código y propiedad de archivos

```text
amateur-flagleague/
├── pom.xml                            (común; Parra coordina cambios)
├── src/main/java/com/flagleague/
│   ├── MainApp.java                   (acuerdo con Parra)
│   └── presentation/
│       ├── controller/
│       │   ├── DashboardController.java
│       │   ├── EquiposController.java
│       │   ├── TorneosController.java
│       │   ├── CalendarioController.java
│       │   ├── ResultadosController.java
│       │   └── PosicionesController.java
│       ├── navigation/                (opcional: gestor de navegación)
│       ├── viewmodel/                 (clases de presentación)
│       └── validation/                (validaciones de formulario)
├── src/main/resources/
│   ├── fxml/
│   │   ├── dashboard.fxml
│   │   ├── equipos.fxml
│   │   ├── torneos.fxml
│   │   ├── calendario.fxml
│   │   ├── resultados.fxml
│   │   └── posiciones.fxml
│   └── css/flagleague.css
└── src/test/java/com/flagleague/presentation/
    └── ...Test.java
```

Los nombres son referencia de integración. Si el equipo necesita modificar alguno, debe hacerlo de forma coordinada. Evita duplicar `MainApp` o `AppBootstrap`.

## 4. Requisitos de las seis vistas

| Vista | Elementos mínimos | Operaciones requeridas |
|---|---|---|
| **1. Inicio / Dashboard** | Selector de temporada y torneo, indicadores y navegación | Mostrar torneo seleccionado, estado, número de equipos y partidos; navegar a las otras vistas. |
| **2. Equipos y jugadores** | `TableView`, formulario de equipo, subtabla de plantilla | Alta, consulta, edición y baja controlada de equipo; registro y consulta de jugador. |
| **3. Torneos** | Selectores de temporada, categoría, modalidad y formato; tabla de inscritos | Crear torneo y registrar equipos elegibles; mostrar motivo cuando la inscripción es rechazada. |
| **4. Calendario** | Tabla de jornadas, equipos, etapa, fecha y estado | Generar jornada automática, programar partido manual y reprogramar antes del inicio. |
| **5. Resultados** | Selector de partido, marcador de solo lectura, lista de eventos | Iniciar partido, registrar TD, conversión y safety, finalizar y documentar desempate externo cuando aplique. |
| **6. Posiciones** | Tabla de resultados, estado de clasificación y campeón | Consultar tabla según torneo; mostrar semifinales, final y campeón cuando correspondan. |

### 4.1 Navegación

- Un único `Stage` principal y un contenedor de navegación coherente.
- Las vistas pueden cargarse al cambiar de módulo; no crear seis ventanas independientes como aplicación principal.
- Mostrar claramente temporada y torneo seleccionados para no mezclar datos.
- Permitir volver a la pantalla de inicio desde cualquier vista.
- No realizar cambios de datos al abrir una vista: la consulta no debe tener efectos laterales.

### 4.2 Controles y entradas

- Emplear `TableView` para equipos, jugadores, partidos y clasificación.
- Emplear `ComboBox` con opciones del dominio para modalidades, etapas, tipos de anotación y formatos.
- Emplear `DatePicker` y un control sencillo para hora, realizando conversión a `LocalDateTime`.
- No permitir capturar manualmente el marcador numérico: será calculado por la lógica compartida.
- No habilitar botones de anotación para partidos `PROGRAMADO` o `FINALIZADO`.
- Ante un error, mostrar un `Alert` con mensaje útil; la excepción de dominio no debe cerrar la aplicación.
- Antes de bajas o cambios irreversibles, solicitar confirmación.

### 4.3 Errores y estados vacíos

Diseña cómo se verá cada vista cuando todavía no existen datos. Debe mostrar indicaciones como «No hay equipos registrados» o «Selecciona un torneo». Valida campos vacíos, fecha/hora inválida, equipo no seleccionado, torneo sin inscritos y acciones fuera de estado. Diferencia entre *error de captura* y *operación rechazada por reglas deportivas*.

## 5. Comunicación con los demás módulos

**Con Paul:** consume los servicios aprobados para equipos, jugadores, temporadas, torneos, inscripciones y partidos. Una vista **no puede** crear `new EquipoRepositoryMemoria()` ni manejar el `Map` directamente.

**Con Parra:** consume `ClasificacionService.calcular(torneoId)` y operaciones del formato de competición para visualizar rondas y campeón; **no recalcules** puntos, diferencia ni posiciones desde las filas de la tabla.

**Con Yan:** utiliza entidades, enums y los estados disponibles. No modifiques los objetos de dominio para resolver una limitación visual sin acordar el cambio.

**Con Carlos:** interactúa indirectamente mediante servicios. Solo solicita ajustes a los repositorios si una consulta requerida no está contemplada en el contrato compartido.

**Con Parra, en el arranque:** acuerden una factoría de controladores (`FXMLLoader.setControllerFactory(...)` u otro mecanismo documentado) para recibir dependencias por constructor. Eviten que los controladores construyan servicios o almacenes por cuenta propia.

### Ejemplo de contrato de vista — no es solución

```java
// Operaciones conceptuales que debe ofrecer la vista de resultados:
void mostrarPartido(UUID partidoId);
void actualizarMarcador();
void informarError(String mensaje);
```

Los nombres concretos, eventos FXML y mecanismos de vinculación son decisiones que debes implementar y explicar.

## 6. Desarrollo por etapas

### Días 1–2. Navegación y contratos

1. Revisa UML y firmas objetivo con el equipo.
2. Crea la estructura `presentation/` y seis FXML con controladores vacíos.
3. Configura el arranque JavaFX con Parra: una única aplicación que abre.
4. Define qué servicio inyectará cada controlador y documenta sus dependencias.
5. Emplea dobles de servicios o adaptadores temporales acordados para no esperar a Paul.
6. Entrega un PR que compile y muestre navegación entre dos vistas.

**Evidencia:** captura de pantalla de JavaFX abierto, estructura de vistas y PR revisado.

### Días 3–4. Formularios y consultas

1. Construye vistas de equipos/jugadores y torneos.
2. Añade `TableView`, formularios y manejo de eventos.
3. Conecta los casos de uso ya disponibles en los servicios.
4. Documenta entradas inválidas y mensajes visibles.
5. Evita dependencias directas de infraestructura.

**Evidencia:** altas y consultas visibles y al menos dos errores controlados.

### Día 5. Primera demostración individual

Presenta navegación, registro de equipo y creación de torneo o inscripción, usando servicios reales o dobles declarados claramente. Identifica qué queda pendiente por dependencia de otros módulos.

### Días 6–7. Calendario, resultados y posiciones

1. Implementa la vista de jornadas y fechas.
2. Implementa el registro de anotaciones y la visualización automática del marcador.
3. Integra las tablas de posiciones y el campeón proporcionados por Parra.
4. Refresca vistas al cambiar de torneo o al terminar encuentros.
5. Verifica que el menú de resultados no habilite acciones fuera de estado.

### Días 8–9. Robustez, pruebas y documentación

Congela funcionalidades, elimina componentes de prueba que ya tengan implementaciones reales, corrige errores, prueba las seis vistas y actualiza capturas. Ejecuta el flujo integral con datos de Carlos.

### Día 10. Demostración y defensa

Participa en la demostración general y explica individualmente un controlador, su inyección de dependencias y el manejo de excepciones.

## 7. Casos mínimos de prueba

### Pruebas de presentación (automatizables cuando no requieren `Stage`)

| ID | Caso | Resultado esperado |
|---|---|---|
| E-01 | Validar nombre de equipo vacío | Error legible, sin llamar al servicio. |
| E-02 | Convertir fecha/hora capturada | `LocalDateTime` válido o rechazo controlado. |
| E-03 | Seleccionar otra temporada/torneo | Datos de vista correspondientes al nuevo contexto. |
| E-04 | Formatear un marcador 14 a 6 | Texto correcto sin recalcular puntos. |
| E-05 | Traducir una excepción de negocio | Mensaje comprensible sin traza técnica. |
| E-06 | Estado partido `FINALIZADO` | Acciones de anotación y finalización deshabilitadas. |
| E-07 | Sin equipos inscritos | Tabla vacía y aviso de estado vacío. |
| E-08 | Ordenar/mostrar filas de clasificación | Se preserva el orden entregado por `ClasificacionService`. |

### Pruebas manuales obligatorias documentadas

1. La aplicación arranca y muestra el dashboard.
2. Se navega de inicio a equipos, torneos, calendario, resultados y posiciones.
3. Se registra un equipo y el nuevo registro aparece inmediatamente.
4. Se crea un torneo 5 vs. 5, se inscriben equipos y se muestra confirmación.
5. Se genera un calendario, se edita una fecha y se muestra la jornada.
6. Se inicia un partido y se registran anotaciones; el marcador se actualiza correctamente.
7. Se intenta anotar un partido finalizado; aparece un rechazo legible.
8. Se consultan clasificación, semifinales, final y campeón tras la ejecución del flujo.
9. Se cambia a un torneo 7 vs. 7 y los datos no se mezclan.
10. Se cierra y reabre la app: se explica que los datos no persisten por diseño.

Registra para cada prueba: fecha, pasos, resultado esperado, resultado observado, evidencia (captura) y estado (aprobada/fallida/corregida). No marques una prueba como aprobada si faltaba integrar el servicio real.

## 8. Entregables individuales

- Código Java de controladores, clases de navegación, validadores y adaptadores propios.
- Seis FXML y archivo CSS, sin recursos privados o de pago.
- Diagrama sencillo de navegación con pantallas y operaciones.
- Registro de dependencias públicas de cada controlador.
- Ocho pruebas verificables de presentación y diez pruebas manuales documentadas.
- Capturas del flujo completo y una descripción de las decisiones de UX.
- Rama `feature/emilio-javafx`, commits y PR propios revisados.
- Apartado individual en el reporte final: dificultades, decisiones y soluciones.

## 9. Rúbrica individual — 100 puntos

| Criterio | Puntos | Evidencia |
|---|---:|---|
| Funcionamiento del módulo visual | **35** | Seis vistas, navegación, consultas, formularios, validaciones y actualizaciones. |
| Diseño y POO | **20** | Controladores desacoplados, responsabilidades claras, clases de apoyo justificadas. |
| Pruebas y manejo de errores | **15** | Casos automatizables, pruebas manuales y evidencias. |
| Integración con servicios compartidos | **15** | Consume correctamente APIs de Paul/Parra; sin acceso a repositorios concretos. |
| Trabajo en GitHub | **10** | PR propios, revisiones, cambios acotados y mensajes útiles. |
| Defensa individual | **5** | Explica eventos, dependencias y errores con su propio código. |
| **Total** | **100** | |

### Preguntas para la defensa

- ¿Por qué el controlador no debe calcular la tabla de posiciones?
- ¿Cómo inyectas un servicio en un controlador que se carga mediante FXML?
- ¿Qué haces cuando un formulario es válido visualmente pero falla una regla deportiva?
- ¿Cómo garantizas que las tablas corresponden al torneo seleccionado?
- ¿Cómo probarías la presentación si no puedes abrir JavaFX en el entorno automatizado?

## 10. Definición de terminado (*Definition of Done*)

- [ ] Las seis vistas existen y son navegables.
- [ ] Las operaciones obligatorias están conectadas a servicios reales al finalizar el proyecto.
- [ ] No hay cálculos de puntuación o acceso a `Map` de persistencia en controladores.
- [ ] Los errores no cierran la aplicación y se explican claramente.
- [ ] Se probaron contextos de dos torneos independientes.
- [ ] `mvn test` pasa y `mvn javafx:run` abre la aplicación integrada.
- [ ] Existe documentación de pruebas manuales y PR revisados.
- [ ] El estudiante demuestra y explica su código.

**Recordatorio:** este documento define el trabajo de Emilio y no autoriza cambiar unilateralmente las firmas del documento 03. Cualquier discrepancia detectada durante la implementación se resuelve mediante acuerdo escrito del equipo.
