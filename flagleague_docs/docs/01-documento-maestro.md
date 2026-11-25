# FlagLeague Manager — Documento maestro de especificación (v1.0)

**Asignatura:** Lenguajes Orientados a Objetos  
**Lenguaje:** Java 21  
**Interfaz:** JavaFX (FXML + controladores)  
**Construcción:** Maven  
**IDE:** Visual Studio Code  
**Trabajo colaborativo:** Git/GitHub, cinco ramas individuales  
**Duración:** 10 días hábiles (dos semanas)  
**Persistencia:** simulada en memoria, sin motor de base de datos ni archivos obligatorios

> **Propósito académico.** Desarrollar una única aplicación de escritorio que administre ligas de Tochito Bandera, con responsabilidades técnicas individuales y una integración común. Todos los estudiantes deberán demostrar POO, buenas prácticas, pruebas y manejo correcto de dependencias.

## 1. Objetivo general

Diseñar, implementar, integrar y probar un sistema de administración de temporadas, categorías, equipos, jugadores, torneos, calendarios, anotaciones, resultados, clasificaciones y campeonatos para modalidades de Tochito Bandera 5 vs. 5 y 7 vs. 7, empleando una arquitectura por capas e interfaces de repositorio con implementación en memoria.

## 2. Alcance obligatorio y límites

| Área | Obligatorio para v1.0 | Fuera de alcance |
|---|---|---|
| Configuración | Varias temporadas; torneos independientes; categorías con rama y edades; modalidades 5/7 | Multiempresa, sedes, cobros |
| Personas | CRUD de equipos y jugadores; asignación de jugadores a equipos | Cuentas, contraseñas, control de acceso |
| Torneos | Liga regular, eliminación directa, liga con playoffs | Grupos múltiples, doble eliminación |
| Calendario | Captura manual, generación round-robin a una vuelta | Optimización compleja de canchas y árbitros |
| Marcador | Touchdown, conversiones 1/2, safety; cálculo por eventos | Simulación de jugadas, cronómetro |
| Resultados | Inicio/finalización de partidos; tabla de posiciones; campeón | Estadísticas por atleta, sanciones |
| Datos | Repositorios en memoria y carga demo | MySQL, PostgreSQL, CSV, API web |
| Calidad | Pruebas unitarias, integración, reportes técnicos y UML | Despliegue instalador multiplataforma |

**Formatos permitidos para esta entrega:**
- Liga de una vuelta con 4 a 8 equipos; permite cantidad impar mediante descansos (*bye*).
- Eliminación directa con exactamente 4 u 8 equipos.
- Liga con playoffs para exactamente los 4 primeros; requiere al menos 4 equipos.
- Los calendarios de liga se pueden generar automáticamente o registrar manualmente; las eliminatorias se generan por ronda una vez que se conocen los participantes.
- El sistema debe soportar simultáneamente torneos de 5 vs. 5 y 7 vs. 7, aunque una competencia solo tiene una modalidad.

## 3. Conceptos de POO por demostrar

Clases, instancias, constructores, encapsulamiento, métodos de instancia, composición, enumeraciones, abstracción, interfaces, polimorfismo de estrategias de competición, genéricos en repositorios, excepciones de dominio, colecciones, principios SOLID aplicables, inyección de dependencias manual y pruebas de unidad/integración.

No se exige crear herencia artificial en las entidades si una interfaz/estrategia expresa mejor el concepto. Para demostrar herencia de manera legítima, se puede usar una excepción base `ReglaNegocioException` con subclases especializadas; el principal polimorfismo estará en `FormatoCompetencia`.

## 4. Requisitos funcionales

| ID | Requisito | Evidencia mínima |
|---|---|---|
| RF-01 | Crear y consultar temporadas | Se visualizan dos temporadas independientes |
| RF-02 | Crear categorías de rama VARONIL/FEMENIL/MIXTA y rango de edad | Validación de edad mínima/máxima |
| RF-03 | Crear, modificar, consultar y retirar equipos cuando no afecte historial | CRUD validado |
| RF-04 | Crear, consultar, modificar y asignar jugadores a equipos | Listado por equipo |
| RF-05 | Crear torneos con temporada, categoría, modalidad y formato | Tres tipos de torneo |
| RF-06 | Inscribir equipos elegibles y evitar duplicados | Rechazo de inscripción repetida |
| RF-07 | Generar todos-contra-todos a una vuelta, sin duplicados | Para 4 equipos: 6 encuentros en 3 jornadas |
| RF-08 | Programar manualmente fechas y horas de partidos | Crear/editar programación sin afectar resultados |
| RF-09 | Iniciar partido y registrar anotaciones por equipo | Validación del equipo anotador |
| RF-10 | Calcular marcador desde eventos | Ejemplo: 2 TD + conversión 2 = 14 puntos |
| RF-11 | Finalizar partidos y calcular posiciones | Clasificación actualizada únicamente con resultados cerrados |
| RF-12 | Configurar puntos por victoria/empate/derrota y orden de desempates | Recalcular tabla al cambiar reglas permitidas |
| RF-13 | Generar fases de eliminación (4 u 8) y playoffs (4) | Enfrentamientos y ganadores correctos |
| RF-14 | Declarar campeón solamente al completar enfrentamientos obligatorios | Campeón coherente y torneo cerrado |
| RF-15 | Cargar datos demo desde repositorios de memoria | Dos torneos de distintas modalidades |
| RF-16 | Mostrar operaciones clave desde interfaz JavaFX | Navegación sin fallos y acciones conectadas a servicios |

## 5. Modelo conceptual y reglas de negocio

### 5.1 Identificadores y relaciones

Todas las entidades persistibles tendrán `UUID id`. Las relaciones entre entidades se referencian mediante UUID para evitar ciclos de objetos difíciles de mantener: `Torneo` contiene `temporadaId` y `categoriaId`; `Jugador` contiene `equipoId`; `Partido` contiene `torneoId`, `equipoLocalId` y `equipoVisitanteId`; `Anotacion` contiene `partidoId` y `equipoId`.

Un `Equipo` existe independientemente de un torneo; su presencia en cada torneo se representa con `Inscripcion`. Las clasificaciones y los marcadores son **datos calculados**, no tablas ni repositorios editables.

### 5.2 Reglas obligatorias

- **RN-01:** un torneo pertenece a una sola temporada, categoría y modalidad; sus partidos e inscripciones pertenecen a ese mismo torneo.
- **RN-02:** al inscribir, la plantilla debe tener por lo menos 5 integrantes elegibles para `CINCO` o 7 para `SIETE`. La edad se verifica en la fecha de inicio del torneo. La rama se valida frente a la categoría; en MIXTA no se exige una proporción específica por falta de definición reglamentaria.
- **RN-03:** no se permite una segunda inscripción del mismo `equipoId` en un mismo `torneoId`.
- **RN-04:** un partido enfrenta dos equipos diferentes e inscritos; no puede existir el mismo enfrentamiento duplicado en la misma jornada/fase.
- **RN-05:** solamente los partidos `EN_CURSO` reciben anotaciones. Los partidos `FINALIZADO` no aceptan modificaciones en v1.0.
- **RN-06:** `marcadorLocal` y `marcadorVisitante` son calculados por `CalculadoraMarcador`, jamás editados directamente.
- **RN-07:** una conversión debe hacer referencia a un touchdown previo del mismo equipo y partido; cada touchdown admite como máximo una conversión. Los safeties se registran sin touchdown asociado.
- **RN-08:** los partidos finalizados alimentan la tabla. La liga usa puntos por victoria/empate/derrota configurables (por defecto 3/1/0).
- **RN-09:** el torneo define una lista ordenada sin duplicados de criterios de desempate: diferencia de puntos, puntos a favor y victorias. Si todos empatan, la clasificación marca `DESEMPATE_PENDIENTE` para los puestos decisivos; el nombre solo se usa para mostrar un orden visual estable, nunca para adjudicar un campeón.
- **RN-10:** el ganador de una fase eliminatoria debe ser inequívoco. Si el marcador termina empatado, se selecciona manualmente el ganador de un desempate disputado fuera del sistema, dejando una justificación obligatoria; no se simula el desempate.
- **RN-11:** el campeón de una liga regular solo puede resolverse cuando todos sus encuentros estén finalizados y no existan empates irresueltos en el primer puesto. En eliminación o playoffs, únicamente al finalizar la final.
- **RN-12:** la configuración de puntos por anotación no puede modificarse después de registrar la primera anotación en un torneo. La configuración de clasificación no puede cambiarse tras cerrar el torneo.
- **RN-13:** cada torneo es independiente; los resultados de otro torneo o temporada nunca afectan sus posiciones.
- **RN-14:** no se eliminan de manera física equipos, torneos o jugadores referenciados por partidos históricos; se utilizan estados inactivos o se rechaza la operación.

### 5.3 Tipos de anotaciones

Valores iniciales: `TOUCHDOWN=6`, `CONVERSION_1=1`, `CONVERSION_2=2`, `SAFETY=2`. Se almacenan en `ReglamentoAnotaciones` del torneo. El modelo administrativo simplifica otras situaciones especiales de reglamentos reales de flag football. Se deberán poder configurar valores por torneo antes de que existan anotaciones.

### 5.4 Calendario

En liga, se utiliza algoritmo circular (*round-robin*), una sola vuelta. Si el total de equipos es impar, se agrega un marcador virtual de descanso; jamás se crea un partido con el equipo virtual. El algoritmo debe producir `n(n-1)/2` encuentros. Cada jornada puede tener una fecha sugerida, modificable antes de iniciar el partido. En eliminación directa se preparan los encuentros de la siguiente ronda únicamente cuando concluye la anterior.

### 5.5 Estados

- `Torneo`: `BORRADOR`, `EN_CURSO`, `FINALIZADO`.
- `Partido`: `PROGRAMADO`, `EN_CURSO`, `FINALIZADO`.
- Etapas: `LIGA`, `CUARTOS`, `SEMIFINAL`, `FINAL`.

Los partidos se crean PROGRAMADOS; al iniciar el torneo se pueden iniciar partidos. Una vez finalizados, no se reabren en esta versión. El cierre del torneo bloquea cambios deportivos.

## 6. Arquitectura y reglas de dependencia

```text
presentation (JavaFX) --> application (servicios, estrategias)
application --> domain (entidades, reglas, interfaces repository)
infrastructure --> domain (implementación de interfaces repository)
bootstrap --> presentation, application, infrastructure
```

La capa de dominio no importa JavaFX, Maven, implementación de repositorios ni otras capas. La capa de aplicación no instancia repositorios de memoria. JavaFX invoca servicios públicos y jamás manipula directamente `Map`, `ArrayList` del almacenamiento ni calcula posiciones.

### 6.1 Paquetes recomendados

```text
com.flagleague
├── MainApp.java
├── bootstrap/AppBootstrap.java
├── domain/model/{Temporada,Categoria,Equipo,Jugador,Torneo,Inscripcion,Partido,Anotacion}.java
├── domain/model/{ConfiguracionTorneo,ReglamentoAnotaciones,FilaPosiciones}.java
├── domain/enums/{Modalidad,Rama,FormatoTorneo,TipoAnotacion,EstadoPartido,EstadoTorneo,Etapa}.java
├── domain/rules/{CalculadoraMarcador,ReglaNegocioException}.java
├── domain/repository/{Repository,EquipoRepository,JugadorRepository,TorneoRepository,PartidoRepository,TemporadaRepository,CategoriaRepository,InscripcionRepository}.java
├── application/service/{EquipoService,JugadorService,TorneoService,InscripcionService,PartidoService,CalendarioService}.java
├── application/competition/{FormatoCompetencia,FormatoLiga,FormatoEliminacion,FormatoLigaPlayoffs,ClasificacionService}.java
├── infrastructure/memory/ (implementaciones RepositoryMemoria)
├── infrastructure/seed/DatosDemo.java
└── presentation/controller/ (controladores JavaFX)
```

## 7. Responsabilidades por estudiante

| Estudiante | Responsabilidad | Código obligatorio | Evidencia específica |
|---|---|---|---|
| 1 | Dominio y cálculo de marcador | Entidades, enums, validaciones, reglamento y `CalculadoraMarcador` | UML de entidades, casos de validación y anotaciones |
| 2 | Servicios y calendario | Servicios CRUD/inscripción/partido, generación round-robin | Casos de uso, calendario 4/5 equipos, pruebas de servicios |
| 3 | Interfaz JavaFX | 6 vistas, controladores FXML, formularios, tablas y navegación | Demostración de todas las operaciones y validación visual |
| 4 | Repositorios simulados | Contratos Repository e implementaciones `Map<UUID,T>`, datos demo | Pruebas CRUD, búsqueda, duplicados y memoria aislada |
| 5 | Motor de competición + integración | Cálculo de posiciones, estrategias de formato, `AppBootstrap`, CI | Liga, brackets de 4/8, playoffs, pruebas integrales |

**Distribución de contratos:** los alumnos 1 y 4 definen juntos el formato de entidades e interfaces en el día 1; las interfaces quedan congeladas antes del desarrollo paralelo. El alumno 2 puede usar *fakes* hasta disponer de repositorios reales. El alumno 3 usa implementaciones de servicios simulados hasta completar integración. El alumno 5 desarrolla su motor independientemente usando partidos terminados de prueba.

## 8. Requerimientos no funcionales

- **RNF-01:** un solo proyecto Maven ejecutable con `mvn javafx:run`.
- **RNF-02:** `mvn test` debe ejecutar la batería automatizada sin depender de JavaFX en pantalla.
- **RNF-03:** manejar errores de entrada mediante mensajes entendibles, sin mostrar trazas al usuario final.
- **RNF-04:** mantener nombres en español y estilo consistente en clases, métodos y atributos (sin espacios ni acentos en identificadores Java).
- **RNF-05:** no acceder a BD, internet ni servicios externos durante la ejecución.
- **RNF-06:** preservar la compilación después de cada integración a `main`.
- **RNF-07:** los componentes del equipo deben aceptar dependencias por constructor, evitando variables estáticas globales para compartir repositorios.

## 9. Plan de integración en dos semanas

| Día | Hito común | Evidencia |
|---|---|---|
| 1 | Repositorio, Maven, UML, contrato común, asignaciones | Proyecto abre y compila; interfaces aprobadas |
| 2 | Entidades base, repositorios vacíos, stubs de servicios, JavaFX base | Primer `mvn test`; aplicación abre |
| 3 | Primera implementación individual | Pull request funcional por alumno |
| 4 | CRUD, inscripción, calendario, marcador, primera UI | Pruebas de dominio y servicios |
| 5 | **Control intermedio individual** | Cada alumno demuestra código propio y una integración |
| 6 | Clasificación, eliminatorias y navegación completa | Flujo liga regular funcional |
| 7 | Integración de los 3 formatos y 2 modalidades | Flujo total con datos demo |
| 8 | Congelamiento funcional | Sin funciones nuevas; solo correcciones |
| 9 | Pruebas finales, UML actualizado y reporte | `mvn test` sin fallos, checklist firmado |
| 10 | Demostración, defensa y entrega | Programa, repositorio, pruebas y bitácora |

## 10. Criterios de aceptación integral

**Escenario A — Liga con playoffs 5 vs. 5.** Crear temporada, categoría y torneo; registrar cuatro equipos con 5 jugadores cada uno; generar 6 partidos de liga en 3 jornadas; registrar anotaciones y finalizar partidos; calcular clasificación; generar semifinales 1.º vs. 4.º y 2.º vs. 3.º; completar final y obtener campeón.

**Escenario B — Eliminación directa 7 vs. 7.** Crear torneo independiente con cuatro equipos de al menos 7 integrantes elegibles; generar semifinales; registrar resultados; crear final; registrar un empate y documentar el ganador por desempate externo; obtener campeón.

**Escenario C — Aislamiento.** Comprobar que agregar partidos al torneo B no altera la tabla o el campeón del torneo A.

**Escenario D — Rechazos.** Intentar duplicar inscripción, registrar anotación de equipo ajeno, comenzar un partido ya finalizado, inscribir un plantel incompleto, declarar campeón con resultados pendientes y programar un encuentro de un equipo contra sí mismo. Cada operación debe rechazar la solicitud sin corromper datos.

## 11. Evaluación individual (100 puntos)

| Criterio | Puntos |
|---|---:|
| Funcionamiento demostrable de su módulo | 35 |
| POO y diseño técnico | 20 |
| Pruebas apropiadas al módulo | 15 |
| Integración y cumplimiento de contratos | 15 |
| Evidencia GitHub (PR, revisiones, documentación de cambios) | 10 |
| Explicación/defensa individual | 5 |
| **Total** | **100** |

**Regla de justicia:** se evalúan módulos de manera individual; una falla originada exclusivamente en otro módulo no invalida el código correctamente probado del estudiante afectado. El número de *commits* no es una medida automática de la calidad de la aportación. Para interfaz JavaFX se admiten pruebas manuales documentadas además de pruebas unitarias de controlador cuando corresponda.

## 12. Entregables finales

1. Repositorio GitHub completo, historial de ramas y *pull requests*.
2. Proyecto Maven ejecutable y código de los cinco módulos.
3. UML final y archivo de contratos actualizado.
4. Documento de pruebas: automáticas y manuales, resultados y correcciones.
5. Bitácora de integración: problemas, responsables, soluciones y fechas.
6. Demostración funcional y defensa individual.

**Definición de terminado:** compila, ejecuta, satisface los escenarios A–D, no existen pruebas fallidas conocidas y cada estudiante puede justificar su contribución.
