# FlagLeague Manager — Guía individual de desarrollo

## Carlos: Repositorios y persistencia simulada en memoria

**Materia:** Lenguajes Orientados a Objetos  
**Proyecto compartido:** FlagLeague Manager — Administración de una liga de Tochito Bandera  
**Responsable:** **Carlos**  
**Tecnologías:** Java 21, Maven, colecciones Java, genéricos y JUnit 5  
**Duración:** 10 días hábiles / 2 semanas  
**Rama individual:** `feature/carlos-persistencia`  
**Evaluación:** individual, 100 puntos  
**Versión:** 1.0 — instrucciones, no implementación resuelta

> **Una sola aplicación:** los repositorios que construyas serán utilizados por los servicios de Paul y el motor de Parra. No crearás una base de datos real, un servidor, un segundo proyecto Maven ni almacenamiento persistente en archivos. La simulación debe comportarse como una capa de persistencia intercambiable.

### Documentos de referencia

- [01 — Documento maestro](01-documento-maestro.md): alcance, reglas de integridad y evaluación.
- [02 — UML del dominio](02-modelo-dominio.mmd): entidades, asociaciones e identificadores.
- [03 — Contratos de integración](03-contratos-integracion.md): interfaces Repository y consultas mínimas.
- [04 — Guía de Yan](04-guia-yan-dominio.md): tipos de entidades y encapsulamiento.
- [05 — Guía de Paul](05-guia-paul-servicios.md): operaciones consumidoras del almacenamiento.

Los contratos del documento 03 se aprobarán el día 1. Si una interfaz o un método adicional se necesita, propón el cambio y acuerda su uso con todos los consumidores antes de implementarlo.

---

## 1. Objetivo individual

**Diseñar, implementar y probar** una capa de persistencia simulada que guarde, consulte, actualice y dé de baja registros deportivos en memoria mediante interfaces, clases genéricas cuando proceda, colecciones (`Map<UUID, T>`) y manejo de excepciones, manteniendo consistencia e independencia entre torneos.

### Resultado esperado

Los servicios de Paul y los formatos de Parra podrán operar con datos comunes durante toda la ejecución de la aplicación sin conocer `HashMap`, `ArrayList` ni la forma concreta del almacenamiento. Los repositorios deberán conservar sus datos mientras la aplicación esté abierta y permitir cargar un escenario de demostración reproducible.

## 2. Responsabilidades y exclusiones

**Debes desarrollar:**

1. Interfaces especializadas de los repositorios, acordadas con Yan y Paul.
2. Implementaciones `*RepositoryMemoria` para temporadas, categorías, equipos, jugadores, torneos, inscripciones y partidos.
3. Colecciones internas con identificación única y consultas especializadas.
4. Control de identidades, duplicados y política de modificaciones/bajas.
5. Copias defensivas o exposición segura de las colecciones.
6. Carga inicial `DatosDemo` y método de reinicio utilizado solamente en las pruebas.
7. Pruebas CRUD y de aislamiento de información.
8. Documentación de las decisiones de almacenamiento y del alcance de la simulación.

**No debes desarrollar:**

- Clases de dominio ni la calculadora de marcadores (Yan).
- Servicios de negocio, inscripciones o calendario (Paul).
- FXML, controladores ni navegación JavaFX (Emilio).
- Cálculo de posiciones, playoffs, campeón o arranque general (Parra).
- Conexiones a MySQL, SQLite, PostgreSQL, APIs web o archivos persistentes.

## 3. Paquetes y archivos

```text
amateur-flagleague/
├── src/main/java/com/flagleague/
│   ├── domain/repository/
│   │   ├── Repository.java            (contrato compartido)
│   │   ├── TemporadaRepository.java
│   │   ├── CategoriaRepository.java
│   │   ├── EquipoRepository.java
│   │   ├── JugadorRepository.java
│   │   ├── TorneoRepository.java
│   │   ├── InscripcionRepository.java
│   │   └── PartidoRepository.java
│   └── infrastructure/
│       ├── memory/
│       │   ├── TemporadaRepositoryMemoria.java
│       │   ├── CategoriaRepositoryMemoria.java
│       │   ├── EquipoRepositoryMemoria.java
│       │   ├── JugadorRepositoryMemoria.java
│       │   ├── TorneoRepositoryMemoria.java
│       │   ├── InscripcionRepositoryMemoria.java
│       │   └── PartidoRepositoryMemoria.java
│       └── seed/DatosDemo.java
└── src/test/java/com/flagleague/infrastructure/
    └── ...Test.java
```

Los contratos de `domain/repository` se acordarán con Yan y sus consumidores. **No agregues dependencias de infraestructura a `domain`**: una interfaz puede depender de `Equipo`, pero nunca de `EquipoRepositoryMemoria`.

## 4. Contrato obligatorio de repositorio

El contrato compartido inicial es:

```java
public interface Repository<T> {
    T guardar(T entidad);
    Optional<T> buscarPorId(UUID id);
    List<T> listar();
    void eliminar(UUID id);
}
```

Las interfaces especializadas deben incluir al menos:

```java
public interface EquipoRepository extends Repository<Equipo> {
    boolean existeNombre(String nombre);
}

public interface JugadorRepository extends Repository<Jugador> {
    List<Jugador> buscarPorEquipo(UUID equipoId);
}

public interface PartidoRepository extends Repository<Partido> {
    List<Partido> buscarPorTorneo(UUID torneoId);
}

public interface InscripcionRepository extends Repository<Inscripcion> {
    List<Inscripcion> buscarPorTorneo(UUID torneoId);
    boolean existe(UUID torneoId, UUID equipoId);
}
```

Las interfaces `TemporadaRepository`, `CategoriaRepository` y `TorneoRepository` extenderán `Repository<T>`. Cuando un servicio requiera búsqueda por referencia foránea, define un método específico **solo si se aprueba y documenta**. Los fragmentos anteriores son contratos de diseño, **no implementaciones**.

### 4.1 Política del método `guardar`

- Crea un registro cuando su UUID todavía no existe.
- Actualiza el registro correspondiente cuando su UUID ya existe; mantiene la misma identidad.
- No permite entidades nulas ni UUID nulos.
- No sobrescribe silenciosamente una entidad distinta con el mismo UUID si incumple las reglas acordadas.
- Las validaciones de *negocio* (plantilla elegible, estado del torneo, formato) pertenecen a Paul o Parra. La persistencia valida identidad, unicidad e integridad de referencias cuando el contrato así lo exige.

**Pregunta de diseño:** ¿cómo distinguirás una actualización legítima de una creación que reutiliza un UUID? Explica la convención que el equipo acuerde en el día 1 y cómo la probarás.

### 4.2 Política de eliminación

Una entidad sin referencias puede eliminarse físicamente si el contrato lo admite. Si un jugador, equipo o torneo forma parte de registros históricos, la eliminación se rechaza o se ejecuta como desactivación a través del servicio correspondiente. **No permitas borrar silenciosamente resultados ni dejar registros huérfanos.**

Como una baja puede depender de otros repositorios, define con Paul **quién verifica las referencias**. Una opción sencilla es que el servicio consulte las dependencias y decida desactivar; no introduzcas un motor de integridad referencial no requerido en dos semanas.

### 4.3 Consultas y aislamiento

- `buscarPorId` devuelve `Optional.empty()` si no existe.
- `listar()` devuelve una colección independiente de la colección interna.
- Modificar una lista devuelta por `listar()` **no debe** alterar el número de registros guardados.
- Si las entidades son mutables, aclara que copiar la lista **no protege** de cambios al contenido; acuerda objetos inmutables, instantáneas/DTOs o encapsulamiento que impida modificaciones accidentales.
- `buscarPorTorneo(torneoId)` no devuelve partidos de otros torneos.
- `buscarPorEquipo(equipoId)` no mezcla jugadores de equipos diferentes.
- Se recomienda `LinkedHashMap` si interesa presentar listados en orden estable, sin depender de ese orden para la clasificación deportiva.

### 4.4 Duplicados y referencias

Implementa consultas específicas para detectar equipos con nombres iguales según la política de normalización acordada (por ejemplo, espacios iniciales/finales y mayúsculas). El servicio de Paul decide si crear el equipo o rechazar el duplicado. Una inscripción con el mismo `torneoId` y `equipoId` no puede registrarse dos veces. Valida que los métodos de consulta reciban identificadores válidos.

## 5. Datos de demostración (`DatosDemo`)

Construye una clase de inicialización de ejemplo que cargue **una sola vez por ejecución** un conjunto de registros coherentes:

| Conjunto | Datos sugeridos | Reglas |
|---|---|---|
| Temporadas | 1 | Fechas válidas. |
| Categorías | 2 | Una varonil y una mixta, con edades configuradas. |
| Equipos | 8 | Cuatro para cada torneo de demostración, con nombres distinguibles. |
| Jugadores | Mínimo 48 | 4 equipos con 5 elegibles y 4 con 7 elegibles = 48. |
| Torneos | 2 | Liga con playoffs 5 vs. 5; eliminación 7 vs. 7. |
| Inscripciones | 8 | Cuatro equipos por torneo, sin repetir equipos en un mismo torneo. |
| Partidos | Opcionalmente algunos programados | Los resultados deben ser consistentes si se incluyen. |

**Importante:** la inicialización acordará con Paul y Parra cómo se registran las entidades. Si `DatosDemo` utiliza servicios, evitará duplicar reglas de negocio; si utiliza repositorios directamente por motivos de arranque, deberá crear datos completos y válidos. Nunca cargar al abrir cada pantalla.

La carga demo deberá ser determinista: mismos nombres, torneos y relaciones en cada ejecución de prueba. Se pueden generar UUID nuevos al iniciar, pero dentro de cada corrida las referencias deben ser coherentes; para pruebas repetibles se admiten UUID constantes de muestra bien delimitados.

## 6. Colaboración y pruebas con dobles

**Con Yan:** acordar el día 1 los constructores, getters y semántica de identidad de las clases. No agregues `setId()` u otros métodos públicos a las entidades sin su aprobación.

**Con Paul:** facilitar instancias de repositorios que satisfagan el contrato. Paul desarrollará sus servicios con interfaces, no construyendo tus implementaciones concretas. Prepara repositorios mínimos compilables desde el día 2 aunque todavía no contengan toda la lógica.

**Con Parra:** entrega las mismas instancias de repositorios para el arranque y las pruebas de competición. `DatosDemo` debe ejecutarse desde `AppBootstrap`, no desde cada repositorio por separado.

**Con Emilio:** la interfaz gráfica no llamará directamente a tus clases. Si una tabla necesita un nuevo filtro, debes acordar con Paul el servicio público que lo expondrá.

## 7. Cronograma individual

| Día | Actividad | Evidencia |
|---|---|---|
| **1** | Revisar entidades, interfaces y políticas de UUID/copia/baja. | Contratos aprobados con Yan y Paul. |
| **2** | Estructura en memoria y primeras interfaces/implementaciones vacías. | `mvn test` compila y primer PR. |
| **3** | CRUD de temporada, categoría, equipo y jugador. | Pruebas guardar/listar/buscar/actualizar. |
| **4** | CRUD y consultas por torneo de inscripciones y partidos. | Consultas por referencia y duplicados. |
| **5** | Control intermedio individual. | Demostración de un flujo persistido en memoria. |
| **6** | Casos de bajas, inmutabilidad/copia e integridad. | Pruebas de referencias y modificaciones externas. |
| **7** | `DatosDemo` de dos torneos y conexión con Paul/Parra. | Escenarios integrados en memoria. |
| **8** | Congelamiento funcional; correcciones y regresión. | Reporte de fallos. |
| **9** | Documentación, pruebas y revisión de PR final. | `mvn test` sin fallos. |
| **10** | Demostración y defensa individual. | Presentación del diseño de repositorios. |

## 8. Pruebas unitarias obligatorias

Implementa como mínimo **14 casos automatizados** de almacenamiento independiente de JavaFX:

| ID | Entrada/acción | Esperado |
|---|---|---|
| C-01 | Guardar equipo válido | Se guarda con UUID correcto. |
| C-02 | Buscar equipo existente | `Optional` contiene la entidad correspondiente. |
| C-03 | Buscar UUID inexistente | `Optional.empty()`. |
| C-04 | Actualizar equipo con mismo UUID | Un solo registro y datos actualizados. |
| C-05 | Guardar entidad nula | Excepción controlada. |
| C-06 | `listar()` y modificar la lista devuelta | Almacén interno no cambia. |
| C-07 | Buscar jugadores por equipo A | No se devuelven jugadores de B. |
| C-08 | Guardar dos equipos con nombres equivalentes | `existeNombre` detecta equivalencia según política acordada. |
| C-09 | Registrar inscripción A en torneo X | `existe(X,A)` devuelve `true`. |
| C-10 | Consultar inscripciones del torneo Y | No mezcla registros de X. |
| C-11 | Consultar partidos del torneo X | No mezcla los de Y. |
| C-12 | Intentar baja con referencia histórica | Rechazo o desactivación según política aprobada. |
| C-13 | Dos servicios que usan el mismo repositorio | Ven el mismo conjunto de datos durante la sesión. |
| C-14 | Ejecutar `DatosDemo` mediante arranque único | No aparecen registros duplicados. |

Agrega pruebas de límites y de error cuando identifiques riesgos adicionales. Una prueba solo se considerará correcta si verifica un resultado concreto mediante aserciones; imprimir en consola no equivale a probar.

## 9. Entregables individuales

1. Interfaces y clases concretas de los siete tipos de repositorio.
2. Diagrama breve del patrón Repository y justificación de genéricos.
3. Clase `DatosDemo` con al menos dos torneos independientes y plantillas elegibles.
4. Código de pruebas JUnit, mínimo 14 casos ejecutables.
5. Documento breve de política de `guardar`, `eliminar`, duplicados y copias defensivas.
6. Evidencia de un servicio de Paul utilizando tus repositorios.
7. PR propios revisados desde `feature/carlos-persistencia`.
8. Registro individual de decisiones, problemas y correcciones.

## 10. Rúbrica individual — 100 puntos

| Criterio | Puntos | Evidencia |
|---|---:|---|
| Funcionamiento del módulo | **35** | Operaciones CRUD, consultas, duplicados, `DatosDemo` y control de referencias. |
| Diseño POO | **20** | Interfaces, genéricos, encapsulamiento e independencia de infraestructura. |
| Pruebas | **15** | 14 casos automáticos y reporte de resultados. |
| Integración | **15** | Repositorios compartidos, servicios de Paul y arranque de Parra. |
| Trabajo GitHub | **10** | Commits, PR revisados, control de cambios de contratos. |
| Defensa individual | **5** | Explica sus decisiones sobre identidad, almacenamiento y mutabilidad. |
| **Total** | **100** | |

### Preguntas de defensa

- ¿Qué ventaja tiene `Repository<T>` sobre implementar siete sistemas de almacenamiento desde cero?
- ¿Qué problema ocurre si `listar()` entrega directamente una colección mutable interna?
- ¿Por qué una copia de `ArrayList<Equipo>` no necesariamente protege sus objetos `Equipo`?
- ¿Cómo se garantiza que todos los servicios consulten los mismos repositorios en una ejecución?
- ¿Qué debe cambiarse para sustituir la memoria por una base de datos sin modificar a Emilio o Paul?

## 11. Definición de terminado

- [ ] Siete repositorios funcionales implementan contratos aprobados.
- [ ] Las consultas no mezclan torneos ni equipos.
- [ ] Las colecciones se entregan de forma segura.
- [ ] La política de bajas y referencias históricas está documentada y probada.
- [ ] `DatosDemo` carga escenarios válidos una sola vez.
- [ ] 14 pruebas pasan con `mvn test`.
- [ ] Los repositorios son inyectables; ninguna vista crea infraestructura.
- [ ] El código está integrado mediante PR y Carlos puede defenderlo.

**El almacenamiento es intencionalmente temporal:** al cerrar la aplicación los datos desaparecen. Este comportamiento se explicará en la documentación y no constituye un error para la versión 1.0.
