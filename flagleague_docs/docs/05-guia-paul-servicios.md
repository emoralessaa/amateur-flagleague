# FlagLeague Manager — Guía individual de desarrollo

## Paul: Servicios, lógica de negocio y generación de calendarios

**Materia:** Lenguajes Orientados a Objetos  
**Proyecto compartido:** FlagLeague Manager — Sistema de Administración de Liga de Tochito Bandera  
**Tecnología:** Java 21, Maven y JUnit 5; sin código JavaFX en este módulo  
**Duración del proyecto completo:** 10 días hábiles / 2 semanas  
**Rama individual:** `feature/paul-servicios`  
**Responsable:** Paul  
**Evaluación:** individual, sobre 100 puntos  
**Versión de esta guía:** 1.0 — especificación para estudiantes, no solución implementada

> **Importante.** Esta guía establece exclusivamente las responsabilidades de Paul. Los cinco integrantes contribuyen al **mismo proyecto Maven y repositorio GitHub**. No debes crear un sistema independiente, duplicar clases ajenas ni entregar una aplicación de consola separada. Se describen comportamientos y contratos, **no** se proporciona la implementación de los algoritmos.

### Documentos obligatorios antes de empezar

1. [01 — Documento maestro](01-documento-maestro.md): RF-01 a RF-16, RN-01 a RN-14, plan y alcance.
2. [02 — Modelo UML](02-modelo-dominio.mmd): entidades, atributos, estados y relaciones.
3. [03 — Contratos de integración](03-contratos-integracion.md): firmas mínimas para servicios y repositorios.
4. [04 — Guía de Yan](04-guia-yan-dominio.md): límites del dominio, calculadora de marcadores y acuerdos pendientes.

**Jerarquía de acuerdos:** los documentos 01, 02 y 03 son la base compartida. El archivo 03 define **firmas objetivo todavía sujetas a aprobación del equipo en el día 1**. Esta guía no autoriza cambiar silenciosamente esas firmas. Cualquier conflicto se documenta, se propone y se aprueba mediante Pull Request antes de programar consumidores.

---

## 1. Objetivo individual

**Diseñar, implementar y probar los servicios de aplicación** que administran temporadas, categorías, equipos, jugadores, torneos, inscripciones y partidos; y construir un **generador de calendarios todos-contra-todos de una vuelta**, aplicando interfaces, encapsulamiento, validaciones de negocio, colecciones, excepciones e inyección de dependencias por constructor.

### Resultado esperado

El módulo debe ofrecer operaciones reutilizables desde JavaFX (Emilio) y desde el motor de competencias (Parra). Las operaciones consultarán y modificarán datos **exclusivamente mediante interfaces de repositorio** (Carlos) y utilizarán entidades y reglas del dominio (Yan).

El alumno debe poder demostrar, sin abrir JavaFX, un ciclo completo: crear un torneo, inscribir equipos elegibles, generar una jornada de liga, programar e iniciar partidos, registrar anotaciones válidas y finalizar encuentros.

## 2. Responsabilidades y límites

**Sí debes desarrollar:**

- Clases de servicios para administrar **temporadas, categorías, equipos, jugadores, torneos, inscripciones y partidos**.
- Operaciones de creación, consulta y modificación conforme a RF-01 a RF-09. Operaciones de baja **controlada** cuando no se altere el historial deportivo.
- Validación **entre entidades**: existencia, compatibilidad categoría/rama/edad/modalidad, tamaño de plantilla, duplicados, inscripción y pertenencia a torneo.
- Generación automática de enfrentamientos de liga regular mediante *round-robin* de una vuelta, para **4 a 8 equipos**, con descansos si el total es impar.
- Programación manual de encuentros y reprogramación de fecha/hora de partidos no iniciados.
- Coordinación de estados `PROGRAMADO → EN_CURSO → FINALIZADO` y de los eventos de anotación, delegando el cálculo al módulo de dominio.
- Manejo de errores con excepciones útiles y devolución de información a la UI mediante servicios públicos.
- Pruebas automatizadas aisladas con repositorios de prueba (fakes), documentación de decisiones y evidencia GitHub.

**No debes desarrollar:**

- Las entidades, enumeraciones, excepciones propias del dominio ni el algoritmo de puntos por anotación (Yan).
- Pantallas, FXML o controladores JavaFX (Emilio).
- `EquipoRepositoryMemoria`, otras implementaciones reales de los repositorios, `DatosDemo` o acceso a BD/archivos (Carlos).
- Tabla de posiciones, desempates de clasificación, generación de cuadros eliminatorios, playoffs, clasificación de semifinalistas o proclamación de campeón (Parra).
- `AppBootstrap` ni la configuración integral de Maven y CI (Parra con apoyo del equipo).

> **Regla de colaboración:** Paul **sí** permite crear encuentros que solicite el motor de Parra mediante servicios comunes, pero **no** decide qué equipos avanzan o qué cruces debe tener una eliminatoria. Tampoco duplica el generador *round-robin* dentro de un formato de competencia.

## 3. Ubicación del código en el repositorio

El proyecto Maven tendrá su raíz en `amateur-flagleague/`. La carpeta `flagleague_docs` seguirá dedicada a documentos y diagramas.

```text
amateur-flagleague/
├── pom.xml
├── src/
│   ├── main/java/com/flagleague/
│   │   ├── domain/                       (Yan / contratos de Carlos)
│   │   ├── application/
│   │   │   ├── service/                  (TU RESPONSABILIDAD)
│   │   │   │   ├── TemporadaService.java
│   │   │   │   ├── CategoriaService.java
│   │   │   │   ├── EquipoService.java
│   │   │   │   ├── JugadorService.java
│   │   │   │   ├── TorneoService.java
│   │   │   │   ├── InscripcionService.java
│   │   │   │   ├── CalendarioService.java
│   │   │   │   └── PartidoService.java
│   │   │   └── dto/                      (solo si acuerdan DTO de lectura)
│   │   ├── infrastructure/               (Carlos)
│   │   ├── presentation/                 (Emilio)
│   │   └── bootstrap/                    (Parra)
│   └── test/java/com/flagleague/application/service/
│       └── ...Test.java                 (TUS PRUEBAS)
└── flagleague_docs/docs/
    ├── 01-documento-maestro.md
    ├── 02-modelo-dominio.mmd
    ├── 03-contratos-integracion.md
    ├── 04-guia-yan-dominio.md
    └── 05-guia-paul-servicios.md
```

Los nombres de servicios anteriores son la distribución propuesta en el documento maestro; pueden combinarse solamente con aprobación y sin crear una clase monolítica. No crees otro `pom.xml` dentro de documentación.

## 4. Interfaces que debes respetar

El documento 03 presenta `GestionLigaService` como **mapa funcional**. No exige que todo viva dentro de una clase. Las siguientes **firmas objetivo** deben aparecer en alguno de los servicios de la capa de aplicación, con parámetros y tipos compatibles con el contrato aprobado por el equipo:

```java
Equipo crearEquipo(String nombre);

Jugador registrarJugador(
    UUID equipoId,
    String nombre,
    LocalDate nacimiento,
    Rama rama
);

Torneo crearTorneo(
    UUID temporadaId,
    UUID categoriaId,
    Modalidad modalidad,
    FormatoTorneo formato,
    ConfiguracionTorneo configuracion
);

Inscripcion inscribirEquipo(UUID torneoId, UUID equipoId);

List<Partido> generarCalendarioLiga(UUID torneoId);

Partido programarPartido(
    UUID torneoId,
    UUID localId,
    UUID visitanteId,
    LocalDateTime fechaHora,
    int jornada,
    Etapa etapa
);

Partido iniciarPartido(UUID partidoId);

Partido registrarAnotacion(
    UUID partidoId,
    UUID equipoId,
    TipoAnotacion tipo,
    UUID touchdownOrigenId
);

Partido finalizarPartido(
    UUID partidoId,
    UUID ganadorDesempateId,
    String motivoDesempate
);
```

Las declaraciones completas de interfaz, imports, constructores y clases concretas serán diseñadas por el estudiante; no debes introducir una `GestionLigaService` gigante solo para repetir la lista anterior.

### 4.1 Servicios y sus responsabilidades mínimas

| Clase | Funciones obligatorias | Regla crítica |
|---|---|---|
| `TemporadaService` | Crear y consultar temporadas, validar existencia al crear torneo. | Las fechas de temporada deben ser coherentes. |
| `CategoriaService` | Crear y consultar categorías configurables. | Validar rama y rango de edad. |
| `EquipoService` | Crear, listar, consultar, actualizar y dar de baja de forma controlada. | No crear nombres duplicados según la política aprobada; respetar historial. |
| `JugadorService` | Registrar, listar por equipo, actualizar y desactivar jugadores. | El equipo debe existir; verificar datos recibidos. |
| `TorneoService` | Crear y consultar torneos, gestionar activación y restricciones de cambio. | Verificar que temporada y categoría existan. |
| `InscripcionService` | Inscribir equipos y consultar inscripciones de un torneo. | Validar plantilla elegible, duplicados y estado del torneo. |
| `CalendarioService` | Generar liga a una vuelta y establecer jornadas; coordinar programación manual. | No duplicar emparejamientos. |
| `PartidoService` | Programar/iniciar, capturar anotaciones, finalizar, consultar resultados. | Solo anotar en partidos `EN_CURSO`; marcador calculado, no editable. |

**Alcance de CRUD:** RF-03 y RF-04 exigen modificaciones y bajas controladas. Los contratos exactos de operaciones complementarias como `actualizarEquipo`, `desactivarJugador`, `listarTemporadas` o `reprogramarPartido` **no están fijados en 03**. Proponlos con el resto del equipo durante el día 1 y registra sus firmas aprobadas. No los presentes como si ya existieran.

### 4.2 Inyección de dependencias

- Cada servicio recibe sus repositorios e implementaciones de reglas a través de **interfaces**, preferentemente en el constructor.
- El servicio no debe conocer `EquipoRepositoryMemoria`, `HashMap` del almacenamiento ni `AppBootstrap`.
- Las excepciones se propagan hacia una capa de presentación capaz de informar el error; no muestres cuadros de diálogo dentro del servicio.
- No compartas estado mediante variables estáticas globales ni reconstruyas repositorios en cada método.

**Ejercicio de diseño:** dibuja un diagrama de dependencias de `InscripcionService`, `CalendarioService` y `PartidoService` identificando qué repositorios y reglas consume cada uno. Justifica cómo tus constructores permiten reemplazar un repositorio real por un *fake* durante las pruebas.

## 5. Casos de uso y reglas específicas

### CU-01. Administrar temporadas, categorías, equipos y jugadores

**Entrada:** datos mínimos de cada entidad y el ID de la entidad relacionada cuando exista.  
**Salida:** entidades consultables mediante servicios; errores controlados ante datos incorrectos.

Tareas:

1. Consultar los contratos de Yan para construir entidades sin omitir validaciones intrínsecas.
2. Crear operaciones para registrar y consultar temporadas y categorías.
3. Implementar creación, listado, búsqueda, modificación y baja controlada de equipos.
4. Implementar registro de jugadores asociado a un `equipoId` existente.
5. Permitir consultar la plantilla de un equipo y desactivar jugadores sin afectar estadísticas históricas.
6. Decidir con Carlos cómo validar nombres duplicados, operaciones de actualización e integridad referencial.

**Criterios de aceptación:** no se registran jugadores en equipos inexistentes; no se da de baja físicamente una entidad referenciada por historial; los cambios se observan desde cualquier consumidor del mismo repositorio compartido.

### CU-02. Crear torneos configurables

**Entrada:** `temporadaId`, `categoriaId`, `modalidad`, `formato` y `ConfiguracionTorneo`.  
**Salida:** torneo en estado `BORRADOR` con sus configuraciones válidas.

Tareas:

1. Comprobar que temporada y categoría existan.
2. Validar modalidad `CINCO` o `SIETE`, y formato `LIGA`, `ELIMINACION` o `LIGA_PLAYOFFS`.
3. Guardar el torneo mediante su repositorio.
4. Garantizar que se pueda crear otro torneo en una temporada o categoría diferente sin mezclar inscripciones ni partidos.
5. Restringir cambios incompatibles con un torneo ya iniciado, según RN-12 y el estado del dominio.

**Criterio de aceptación:** se mantienen dos torneos independientes, uno de 5 vs. 5 y otro de 7 vs. 7. Las configuraciones de clasificación y anotación no afectan a otros torneos.

### CU-03. Inscribir equipos con verificación deportiva

**Entrada:** `torneoId` y `equipoId`.  
**Salida:** una `Inscripcion` válida, o excepción de negocio sin escritura parcial.

Proceso obligatorio:

1. Verificar existencia del torneo y del equipo.
2. Comprobar que el torneo esté en `BORRADOR` para nuevas inscripciones (política de v1.0 propuesta; aprobar en día 1).
3. Confirmar que no exista la inscripción del mismo equipo en ese torneo.
4. Consultar la plantilla a través de `JugadorRepository.buscarPorEquipo(equipoId)`.
5. Contar **solo jugadores activos y elegibles** por categoría y fecha de referencia.
6. Exigir mínimo **5** elegibles para `CINCO` o **7** para `SIETE`; no exigir máximos adicionales no definidos.
7. Construir y guardar `Inscripcion` con referencia al torneo y al equipo.

**Elegibilidad:** la edad se calcula al inicio del torneo (RN-02). La rama de jugador debe ser compatible con la categoría varonil/femenil; para `MIXTA` no se exige proporción de ramas. No inventar una cuota mixta. Si hay dudas sobre la representación del registro de rama, acuerda con Yan antes de codificar.

**Ejemplos:**

- Torneo `CINCO`: equipo con cinco jugadores activos y elegibles → inscripción permitida.
- Torneo `SIETE`: mismo equipo con cinco elegibles → inscripción rechazada.
- Equipo ya inscrito: repetir la operación → excepción de negocio, sin segundo registro.
- Cinco jugadores totales, pero uno inactivo → inscripción rechazada para `CINCO`.
- Jugador fuera del rango de edad → no cuenta como elegible.

### CU-04. Generación automática de calendario de liga

**Entrada:** `UUID torneoId` con equipos inscritos.  
**Salida:** `List<Partido>` correspondiente a una vuelta todos contra todos, agrupada en jornadas.

Implementa un **algoritmo round-robin**, sin copiar su solución de un compañero. Se recomienda el método circular; documenta su principio, complejidad básica y las pruebas que demostrarán su corrección.

**Condiciones obligatorias:**

1. Generación exclusivamente de la etapa `LIGA`, no de llaves de eliminación directa.
2. Ligas de **4 a 8 equipos inscritos**, según documento maestro.
3. Cada par no ordenado de equipos aparece una sola vez en la temporada regular del torneo.
4. Cada equipo juega como máximo **un encuentro por jornada**.
5. Cuando `n` es impar, hay exactamente **un descanso por jornada**; el descanso no es un `Partido` persistido.
6. Total de partidos: `n × (n − 1) / 2`.
7. Con `n` par: `n − 1` jornadas; con `n` impar: `n` jornadas.
8. Los `Partido` generados están inicialmente `PROGRAMADO` y pertenecen al mismo torneo.
9. Evita sobreescribir fechas, estados y marcadores de enfrentamientos preexistentes. Ante calendario ya generado, aplica la política común aprobada (rechazar repetición o devolver el existente de manera idempotente).
10. La operación no debe modificar las inscripciones ni generar partidas virtuales para *bye*.

**Tabla de aceptación del generador:**

| Equipos | Partidos esperados | Jornadas esperadas | Descansos |
|---:|---:|---:|---|
| 4 | 6 | 3 | 0 |
| 5 | 10 | 5 | 1 por jornada |
| 6 | 15 | 5 | 0 |
| 7 | 21 | 7 | 1 por jornada |
| 8 | 28 | 7 | 0 |

**Ejemplo verificable (sin imponer orden específico):** cuatro equipos A, B, C y D deben producir exactamente los seis pares `{A,B}`, `{A,C}`, `{A,D}`, `{B,C}`, `{B,D}`, `{C,D}`, distribuidos en tres jornadas sin que se repita un equipo en la misma jornada.

**Fechas y horarios:** la creación automática de *enfrentamientos y jornadas* no equivale necesariamente a asignar una cancha ni un horario. Si `Partido.fechaHora` es obligatorio en el modelo de Yan, definan y documenten en el día 1 cómo se proporcionarán fechas sugeridas; si se admite temporalmente `null` para partidos aún no agendados, también debe quedar explícitamente aprobado. No agregues valores ficticios ni rompas constructores para resolverlo unilateralmente.

### CU-05. Programación manual y reprogramación

**Entrada:** torneo, dos equipos, fecha/hora, jornada y etapa.  
**Salida:** partido `PROGRAMADO` válido y consultable.

Validar:

- Torneo existente y sin cierre.
- Local y visitante distintos; ambos inscritos en **ese** torneo.
- Fecha/hora y jornada coherentes, conforme al contrato de dominio.
- No repetir el mismo encuentro dentro de la misma jornada y etapa (RN-04).
- En una **liga de una vuelta**, no repetir el mismo enfrentamiento en otra jornada de la fase de liga.
- No programar dos encuentros para el mismo equipo en una sola jornada de liga.
- No alterar eventos o resultados de un partido ya iniciado al reprogramar.

**Límite con Parra:** el motor de competencia solicita encuentros para `CUARTOS`, `SEMIFINAL` o `FINAL`; la decisión de emparejamientos y momento de crear la ronda pertenece al Parra. `PartidoService` o `CalendarioService` solo validan y registran esos encuentros.

### CU-06. Iniciar partido

**Entrada:** `UUID partidoId`.  
**Salida:** partido actualizado a `EN_CURSO`.

Pasos:

1. Consultar el partido y su torneo.
2. Comprobar que el partido esté `PROGRAMADO`.
3. Comprobar que el torneo esté en estado que permita jugar (en la versión compartida, `EN_CURSO`).
4. Aplicar la transición de estado definida por Yan.
5. Guardar el partido y devolver el resultado.

**Rechazos:** ID inexistente, partido ya iniciado/finalizado, torneo finalizado o transición inválida.

### CU-07. Registrar anotación y obtener marcador

**Entrada:** partido, equipo anotador, tipo de evento y posible `touchdownOrigenId`.  
**Salida:** evento guardado de manera coherente y `Partido` de retorno, según contrato 03.

Secuencia:

1. Obtener el partido y comprobar que esté `EN_CURSO`.
2. Verificar que el `equipoId` corresponda al local o visitante.
3. Consultar las anotaciones actuales del partido.
4. Construir el evento con la clase `Anotacion` de Yan.
5. Validar el **conjunto completo de eventos** utilizando `CalculadoraMarcador` (Yan), que verificará relación a touchdowns y duplicados de conversión.
6. Almacenar el nuevo evento mediante el mecanismo de anotaciones aprobado, sin guardar un marcador editable.
7. Devolver el partido consultable y permitir que la UI obtenga su marcador calculado por un método de consulta acordado.

**Ejemplo de aceptación:** dos touchdowns y una conversión de dos puntos de un mismo equipo producen **14 puntos**, sin edición manual del total.

**Error que debes evitar:** el contrato 03 define `registrarAnotacion(...)` con retorno `Partido`, pero una conversión necesita conocer el `UUID` del touchdown precedente. Coordina con los compañeros Yan, Emilio y Carlos cómo se **listan/consultan anotaciones con su ID**; no cambies el retorno del método en secreto.

### CU-08. Finalizar partido

**Entrada:** `partidoId`, `ganadorDesempateId` opcional y `motivoDesempate` opcional.  
**Salida:** partido `FINALIZADO` correctamente guardado.

Reglas:

1. Solo finalizar un partido `EN_CURSO`.
2. Calcular el marcador desde las anotaciones del partido.
3. En partidos de etapa `LIGA`, permitir empate ordinario; no inventar ganador de desempate.
4. En encuentros eliminatorios (`CUARTOS`, `SEMIFINAL`, `FINAL`), si existe empate, exigir el ID de uno de los dos equipos como ganador de desempate **y una justificación no vacía**.
5. En encuentros eliminatorios sin empate, el ganador es el de mayor puntuación; rechazar una selección manual contradictoria.
6. Aplicar la transición de estado y guardar.
7. No actualizar aquí la tabla de posiciones ni determinar el campeón: ese cálculo corresponde al Parra.

**Regla de integridad:** una excepción no debe dejar el partido marcado como finalizado a medias ni con anotaciones inconsistentes. Evita mutar entidades guardadas antes de concluir todas las validaciones; acuerda con Carlos cómo lograrlo con repositorios en memoria.

## 6. Contratos por resolver en el día 1 (NO asumir que ya están aprobados)

Los documentos base contienen puntos abiertos. Debes registrarlos en `docs/contratos.md` o actualizar `03-contratos-integracion.md` mediante el mecanismo acordado, con la aprobación de los consumidores.

| Tema pendiente | Por qué importa | Responsables de acordarlo |
|---|---|---|
| **Fecha de inicio del torneo** | RN-02 requiere calcular edad al inicio, pero el UML no expone `Torneo.fechaInicio`. | 1, 2, 4 y 5 |
| **`CalculadoraMarcador` interfaz/clase** | UML 02 muestra clase y contrato 03 muestra interfaz. Para implementar, seguir provisionalmente 03, tal como guía 04, y actualizar UML al aprobarse. | 1, 2 y 5 |
| **Eventos de anotación** | No existe `AnotacionRepository` explícito en 03 ni está definido si `Partido` guarda internamente los eventos. | 1, 2 y 4 |
| **Consulta de anotaciones/ID de touchdown** | `registrarAnotacion` retorna `Partido`, pero la UI necesita seleccionar el TD de origen de una conversión. | 1, 2 y 3 |
| **Asignación de fechas a calendario automático** | `Partido.fechaHora` está en UML; no se fija si puede quedar pendiente. | 1, 2 y 3 |
| **Operaciones extra de CRUD y consulta** | El archivo 03 solo enumera métodos mínimos, no todas las operaciones exigidas por RF-01 a RF-09. | 2, 3 y 4 |
| **Cuándo aceptar nuevas inscripciones** | Se propone solo `BORRADOR`, pero no aparece formulado literalmente en RN-01 a RN-14. | 2 y 5 |
| **Orden y gestión de siembra** | `Inscripcion.ordenSiembra` existe; cómo lo obtiene un formato eliminatorio debe ser estable. | 2 y 5 |
| **Reglas de edición con colecciones en memoria** | Referencias mutables pueden causar cambios parciales ante error. | 1, 2 y 4 |
| **Desempate pendiente de tabla** | RN-09 exige marcarlo, pero `FilaPosiciones` del UML no tiene atributo correspondiente. | 1, 2 y 5 |

**Entrega del acuerdo:** una tabla de decisiones aprobadas, fecha, responsables y cambios necesarios en UML/contratos. Sin acuerdos, implementa solamente el contrato ya existente y **no** agregues firmas públicas incompatibles.

## 7. Integración con los demás alumnos

| Compañero | De quién dependes o quién depende de ti | Compromiso específico |
|---|---|---|
| **Yan — Dominio** | **Dependes** de entidades, estados, calculadora y excepciones. | No modificar sus modelos unilateralmente; comunicar validaciones requeridas y probar transiciones. |
| **Emilio — JavaFX** | **Depende de ti** para operaciones de usuario. | Publicar interfaces estables y ejemplos de entradas/salidas; atender errores funcionales sin agregar JavaFX a servicios. |
| **Carlos — Persistencia** | **Dependes** de los contratos Repository y sus implementaciones al integrar. | Consumir interfaces; utilizar *fakes* de prueba mientras se terminan repositorios reales. |
| **Parra — Competencias** | **Depende de ti** para calendario regular y operaciones de partidos. | Proporcionar generación de liga única y programación reutilizable; no implementar clasificación ni playoffs. |

### Contrato de colaboración al cierre del día 2

- Las firmas públicas mínimas de servicios están aprobadas.
- Las clases base de Yan y los repositorios **como interfaces** están disponibles para compilar.
- Tu código compila con dependencias inyectadas y al menos un test pasa sin abrir JavaFX.
- Has compartido con los Emilio y Parra los métodos que usarán, incluyendo errores esperados.
- Se ha registrado la política para anotaciones y la forma de consultar touchdowns.

## 8. Etapas y cronograma individual

### Etapa A — Diseño y primera integración (días 1 y 2)

1. Leer documentos 01–04 e identificar RF y RN correspondientes a servicios.
2. Acordar firmas de las ocho clases de servicios y sus dependencias.
3. Resolver o registrar las decisiones pendientes de la sección 6 con el equipo.
4. Crear rama `feature/paul-servicios` a partir del proyecto Maven compartido.
5. Construir esqueletos de servicios **compilables**, con sus constructores y contratos; no introducir código ficticio que simule éxito permanentemente.
6. Preparar *fakes* o *stubs* en `src/test` para probar sin requerir los repositorios reales.
7. Enviar un Pull Request pequeño y solicitar revisión.

**Evidencia al día 2:** servicios visibles, dependencias correctas, al menos un test ejecutable con `mvn test` y ninguna importación de JavaFX.

### Etapa B — Operaciones esenciales (días 3 y 4)

1. Terminar equipos, jugadores, temporadas, categorías y torneos.
2. Programar inscripción y validación de elegibilidad.
3. Implementar generación de calendario regular para 4 y 5 equipos, ampliando a 6–8.
4. Implementar `programarPartido` y validación contra duplicados.
5. Integrar primeras pruebas unitarias de servicios y *round-robin*.

**Evidencia al día 4:** CRUD principal e inscripciones funcionales; calendario de 4 y 5 equipos pasa pruebas.

### Etapa C — Control intermedio (día 5)

1. Demostrar un torneo de cuatro equipos con seis partidos en tres jornadas.
2. Demostrar una inscripción rechazada por plantilla incompleta.
3. Explicar cómo tus servicios dependen de interfaces y por qué no usan `new EquipoRepositoryMemoria()`.
4. Mostrar código propio integrado y un Pull Request revisado.

### Etapa D — Partidos e integración (días 6 y 7)

1. Integrar inicio, anotaciones y finalización de partidos con reglas de Yan.
2. Garantizar que el motor de Parra pueda consultar partidos finalizados y generar cruces por sus servicios públicos.
3. Conectar las operaciones con los formularios de Emilio.
4. Corregir defectos de integración y añadir regresiones.
5. Probar aislamiento entre torneos.

### Etapa E — Cierre y defensa (días 8, 9 y 10)

1. **Día 8:** no introducir funcionalidades nuevas; corregir fallas reportadas.
2. **Día 9:** ejecutar `mvn test`, actualizar contratos/documentación aprobados, entregar evidencia de pruebas e integración.
3. **Día 10:** demostrar una operación completa desde JavaFX y defender individualmente tu código.

## 9. Pruebas automáticas mínimas

Escribe **al menos 16 pruebas JUnit independientes**, con nombres descriptivos, datos aislados y resultados verificables. Las siguientes forman el mínimo obligatorio:

| ID | Escenario | Resultado esperado |
|---|---|---|
| A2-T01 | Registrar jugador con `equipoId` inexistente | Operación rechazada sin guardar jugador. |
| A2-T02 | Crear torneo con `categoriaId` o `temporadaId` inexistente | Operación rechazada sin guardar torneo. |
| A2-T03 | Inscribir equipo con cinco elegibles en torneo `CINCO` | Inscripción creada. |
| A2-T04 | Inscribir equipo con solo cinco elegibles en torneo `SIETE` | Inscripción rechazada. |
| A2-T05 | Inscribir dos veces mismo equipo en un torneo | Segunda inscripción rechazada. |
| A2-T06 | Plantel con un jugador inactivo o fuera de edad | No se cuenta como elegible; se rechaza si queda bajo mínimo. |
| A2-T07 | Generar liga de cuatro equipos | Seis partidos en tres jornadas, sin pares repetidos. |
| A2-T08 | Generar liga de cinco equipos | Diez partidos en cinco jornadas; un descanso por jornada. |
| A2-T09 | Generar liga de ocho equipos | Veintiocho partidos en siete jornadas, sin repeticiones. |
| A2-T10 | Intentar generar un calendario que ya existe | Se cumple política documentada sin duplicar ni sobrescribir partidos. |
| A2-T11 | Programar partido de equipo contra sí mismo | Operación rechazada. |
| A2-T12 | Programar partido con equipo no inscrito | Operación rechazada. |
| A2-T13 | Registrar touchdown en partido `PROGRAMADO` | Operación rechazada. |
| A2-T14 | Registrar dos TD y una conversión de dos | Marcador calculado de 14 para el equipo correcto. |
| A2-T15 | Finalizar partido eliminatorio empatado sin ganador externo | Operación rechazada; partido no finalizado. |
| A2-T16 | Finalizar partido empatado en etapa `LIGA` | Partido finalizado válidamente como empate. |

**Pruebas recomendadas adicionales:** `iniciarPartido` dos veces, anotar con equipo ajeno, conversión con touchdown no existente, reprogramación de partido finalizado, bloqueo de edición de reglamento, integración con `ClasificacionService`, ausencia de datos cruzados entre torneos y mantenimiento de historial tras baja controlada.

**Alcance del test:** Yan comprueba que la calculadora suma correctamente los eventos; Paul debe comprobar que **sus servicios llaman a esa calculadora, validan estados y persisten coherentemente los datos**. Una prueba no sustituye a la otra.

### Organización recomendada de pruebas

```text
src/test/java/com/flagleague/application/service/
├── EquipoServiceTest.java
├── JugadorServiceTest.java
├── TorneoServiceTest.java
├── InscripcionServiceTest.java
├── CalendarioServiceTest.java
└── PartidoServiceTest.java
```

Utiliza instancias nuevas de fakes en cada prueba; no reutilices un estado global mutable entre pruebas.

## 10. Ejemplos de aceptación funcional

### Escenario A — Liga de 4 equipos

**Datos:** un torneo `LIGA` con cuatro equipos inscritos y suficientes jugadores elegibles.  
**Acción:** invocar `generarCalendarioLiga(torneoId)`.  
**Esperado:** seis partidos, tres jornadas, cada pareja exactamente una vez y un partido por equipo en cada jornada.

### Escenario B — Liga con 5 equipos

**Datos:** un torneo `LIGA` con cinco equipos inscritos.  
**Acción:** generar calendario.  
**Esperado:** diez partidos, cinco jornadas, cada equipo juega cuatro encuentros y descansa una jornada.

### Escenario C — Registro de anotaciones

**Datos:** partido `EN_CURSO`, dos equipos inscritos, reglamento 6/1/2/2.  
**Acción:** registrar dos touchdowns del local y vincular conversión de dos a uno de esos touchdowns.  
**Esperado:** marcador 14–0 consultable sin un setter de marcador.

### Escenario D — Eliminación y desempate

**Datos:** semifinal generada por Parra; marcador empatado al terminar.  
**Acción:** finalizar sin desempate documentado.  
**Esperado:** error, sin transicionar a `FINALIZADO`. Al indicar uno de los dos equipos y justificación no vacía, se permite finalizar conforme a la regla común.

### Escenario E — Aislamiento

**Datos:** dos torneos independientes, uno `CINCO` y otro `SIETE`.  
**Acción:** generar encuentros y registrar anotaciones solo en el primero.  
**Esperado:** ningún partido ni evento del segundo torneo se altera.

## 11. Entregables individuales

1. **Código fuente** de los servicios y su generador de calendarios integrado al Maven común.
2. **Interfaces/firma pública** documentadas y aprobadas en los contratos de integración; ejemplos breves de uso por otros módulos, sin entregar la solución completa.
3. **Al menos 16 pruebas JUnit** independientes con evidencia de ejecución de `mvn test`.
4. **Documento de decisiones técnicas** (1–2 páginas Markdown): algoritmo de calendario, validaciones entre entidades, fechas, manejo de estados, eventos de anotación y dependencias.
5. **Diagrama sencillo de dependencias** de sus servicios con repositorios y reglas del dominio.
6. **Evidencia GitHub:** historial de cambios propio, al menos un Pull Request revisado por otro integrante y una revisión que tú realices a código de otro.
7. **Evidencia de integración:** al menos una operación invocada desde JavaFX y una utilizada por el motor de competencias.
8. **Demostración y defensa individual** en el día 10.

Un servicio que solo funciona dentro de un programa de prueba externo o requiere repositorios creados manualmente desde JavaFX **no cumple** el contrato de integración.

## 12. Rúbrica individual — 100 puntos

| Criterio | Evidencia | Puntos |
|---|---|---:|
| Funcionamiento del módulo | CRUD, inscripciones, calendario, estados y anotaciones con reglas correctas | **35** |
| POO y diseño técnico | Interfaces, separación de responsabilidades, DI, excepciones y buenas prácticas | **20** |
| Pruebas unitarias | Casos A2-T01 a A2-T16, independencia, errores y regresiones | **15** |
| Integración y contratos | Compatibilidad con dominio, repositorios, JavaFX y motor de competencias | **15** |
| Evidencia colaborativa GitHub | Pull Requests, revisiones, cambios trazables y acuerdos documentados | **10** |
| Defensa individual | Explicación de código, algoritmo de liga y validaciones | **5** |
| **Total** | | **100** |

**Criterio de justicia:** si un componente ajeno falla, presenta pruebas aisladas que demuestren el funcionamiento de tus servicios con contratos correctos. No se penaliza automáticamente a un estudiante por defectos exclusivos de otro módulo. La cantidad de commits no sustituye la evidencia técnica.

### Preguntas posibles para la defensa

- ¿Qué validación pertenece a `InscripcionService` y cuál a `Jugador` o `Categoria`? ¿Por qué?
- ¿Cómo garantizas que cada equipo se enfrente una sola vez en la liga?
- ¿Qué sucede con el calendario cuando la cantidad de equipos es impar?
- ¿Por qué recibes `EquipoRepository` en el constructor en lugar de crear `EquipoRepositoryMemoria`?
- ¿Qué evita que una inscripción duplicada quede almacenada?
- ¿Cómo permites una conversión asociada a un touchdown sin guardar directamente un marcador editable?
- ¿Qué haces si la operación de finalizar un partido falla después de validar el resultado?
- ¿Cómo compruebas que los partidos de otro torneo no aparecen en tus consultas?

## 13. Definición de terminado (*Definition of Done*)

- [ ] Los servicios residen en `com.flagleague.application.service`, dentro del único proyecto Maven.
- [ ] Todos los servicios usan interfaces Repository y dependen de objetos del dominio, no de clases de infraestructura ni JavaFX.
- [ ] Existen operaciones demostrables para temporadas, categorías, equipos, jugadores y torneos.
- [ ] La inscripción valida existencia, duplicados, categoría, modalidad y plantilla elegible.
- [ ] El algoritmo de liga genera correctamente calendarios de 4, 5, 6, 7 y 8 equipos sin enfrentamientos repetidos.
- [ ] Se pueden programar partidos manualmente sin violar inscripción ni jornadas.
- [ ] Solo se anotan puntos en partidos `EN_CURSO` y el marcador proviene de `CalculadoraMarcador`.
- [ ] La finalización permite empates ordinarios y exige desempate documentado para fases eliminatorias empatadas.
- [ ] Ninguna operación inválida deja registros duplicados, cambios parciales o resultados inconsistentes.
- [ ] Las discrepancias de contratos de la sección 6 están resueltas por acuerdo o registradas como bloqueos explícitos.
- [ ] Se ejecutan satisfactoriamente al menos las 16 pruebas de la sección 9 con `mvn test`.
- [ ] Se entregan documentación, Pull Request revisado, evidencia de integración y defensa individual.

---

**Nota para el docente.** Esta guía se elaboró tomando como referencia los archivos `01-documento-maestro.md`, `02-modelo-dominio.mmd`, `03-contratos-integracion.md` y `04-guia-yan-dominio.md` de la documentación inicial de FlagLeague Manager. Se mantuvieron las firmas conocidas del archivo 03 y se identificaron de manera explícita las decisiones todavía pendientes; no se presentaron como contratos ya aprobados. La guía no proporciona código funcional resuelto del algoritmo ni de los servicios.
