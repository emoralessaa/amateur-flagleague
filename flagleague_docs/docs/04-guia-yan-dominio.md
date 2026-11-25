# FlagLeague Manager — Guía individual de desarrollo

## Yan: Arquitectura del dominio y modelos de Programación Orientada a Objetos

**Materia:** Lenguajes Orientados a Objetos  
**Proyecto compartido:** FlagLeague Manager — Sistema de Administración de Liga de Tochito Bandera  
**Tecnología:** Java 21, Maven y JUnit 5; sin interfaz gráfica en este módulo  
**Duración del proyecto completo:** 10 días hábiles / 2 semanas  
**Rama individual:** `feature/yan-dominio`  
**Responsable:** Yan  
**Evaluación:** individual, sobre 100 puntos

> **Importante.** Este documento especifica **solo el trabajo de Yan**. No se debe desarrollar un programa independiente ni crear un nuevo proyecto Maven. El código debe integrarse al mismo repositorio y compilación que usarán los cinco alumnos. Tampoco se proporcionan las implementaciones resueltas: el estudiante debe diseñarlas, programarlas y defenderlas.

### Documentos que debes consultar antes de empezar

1. [01 — Documento maestro](01-documento-maestro.md): alcance, requisitos funcionales, reglas de negocio y criterios generales.
2. [02 — Modelo UML](02-modelo-dominio.mmd): atributos y relaciones de entidades.
3. [03 — Contratos de integración](03-contratos-integracion.md): enumeraciones, interfaces y firmas acordadas.

**Precedencia de acuerdos:** los tres documentos forman la base del proyecto. Si detectas contradicciones, comunícalas al equipo y al docente **antes** de cambiar una firma pública. Ningún alumno debe modificar unilateralmente el UML ni los contratos compartidos después de su aprobación.

---

## 1. Objetivo individual

**Diseñar e implementar el modelo de dominio** de una liga de Tochito Bandera utilizando clases y objetos, encapsulamiento, constructores, composición, enumeraciones, excepciones e interfaces, y **programar una calculadora de marcadores basada en eventos de anotación** que pueda ser utilizada por los servicios y el motor de competencias de los demás alumnos.

### Resultado esperado

Al finalizar, Yan habrá entregado clases de dominio compilables, validadas y probadas, sin dependencias de JavaFX, repositorios en memoria ni clases de servicios. Sus compañeros podrán utilizar las entidades y la calculadora de anotaciones desde el proyecto común.

## 2. Responsabilidades y límites

**Sí debes desarrollar:**

- Entidades y objetos de valor establecidos en el UML.
- Enumeraciones de tipos de torneo, modalidad, estados y anotaciones.
- Constructores y métodos para mantener estados válidos.
- Validaciones intrínsecas de cada entidad (datos nulos, nombres vacíos, fechas incoherentes, equipos idénticos en un partido, etc.).
- Una jerarquía pequeña y útil de excepciones de dominio.
- La interfaz `CalculadoraMarcador` y una implementación que calcule el marcador desde las anotaciones.
- Pruebas unitarias JUnit para reglas del dominio y cálculo del marcador.
- Documentación UML de las clases que desarrolles y una bitácora de integración.

**No debes desarrollar:**

- Pantallas JavaFX ni controladores (Emilio).
- Operaciones CRUD, inscripción, calendario y gestión de partidos en servicios (Paul).
- Implementaciones de repositorios `Map` / `ArrayList` o carga de datos demo (Carlos).
- Cálculo de posiciones, construcción de playoffs o selección de campeón (Parra).
- Conexión real a bases de datos o archivos de persistencia.

> **Separación importante:** las validaciones intrínsecas de un objeto pertenecen a este módulo; las que requieren consultar otras entidades o repositorios —por ejemplo, comprobar que un equipo tiene 5 o 7 jugadores elegibles o que está inscrito en un torneo— pertenecen a los servicios de Paul.

## 3. Ubicación del código dentro del proyecto común

La estructura de documentación existente es `amateur-flagleague/flagleague_docs/docs/`. El código Java **no debe guardarse dentro de `flagleague_docs`**. Al preparar el proyecto Maven, el equipo usará una distribución como esta:

```text
amateur-flagleague/
├── pom.xml                                  (configuración compartida)
├── src/
│   ├── main/java/com/flagleague/
│   │   └── domain/
│   │       ├── model/                       (Yan)
│   │       ├── enums/                       (Yan)
│   │       ├── rules/                       (Yan)
│   │       └── repository/                  (contratos acordados con Carlos)
│   └── test/java/com/flagleague/domain/     (pruebas Yan)
└── flagleague_docs/docs/
    ├── 01-documento-maestro.md
    ├── 02-modelo-dominio.mmd
    ├── 03-contratos-integracion.md
    └── 04-guia-yan-dominio.md
```

No crees otro `pom.xml` dentro de `flagleague_docs`. El alumno que coordina integración configurará el proyecto Maven raíz con el equipo.

## 4. Entidades obligatorias

Implementa estas clases respetando sus atributos básicos del UML, sus tipos y las reglas enumeradas abajo. Se permiten **métodos auxiliares y atributos adicionales justificados**, siempre que no rompan los contratos públicos.

| Clase | Atributos esenciales del UML | Responsabilidad / validaciones fundamentales |
|---|---|---|
| `Temporada` | `id`, `nombre`, `inicio`, `fin` | Identificar una temporada; nombre obligatorio, `inicio` no posterior a `fin`. |
| `Categoria` | `id`, `nombre`, `rama`, `edadMin`, `edadMax` | Definir rama y rango de edad inclusivo; edades no negativas y `edadMin <= edadMax`. |
| `Equipo` | `id`, `nombre`, `activo` | Representar un equipo independiente de los torneos; nombre obligatorio. |
| `Jugador` | `id`, `equipoId`, `nombre`, `fechaNacimiento`, `ramaRegistro`, `activo` | Representar a un jugador, con fecha de nacimiento válida y equipo asignado. |
| `Torneo` | `id`, `temporadaId`, `categoriaId`, `modalidad`, `formato`, `estado` | Identificar un torneo y sus reglas; estado inicial `BORRADOR`. Asociar `ConfiguracionTorneo` y `ReglamentoAnotaciones` según el UML. |
| `Inscripcion` | `id`, `torneoId`, `equipoId`, `ordenSiembra` | Vincular un equipo con un torneo; referencias obligatorias, siembra válida si se informa. |
| `Partido` | `id`, `torneoId`, `equipoLocalId`, `equipoVisitanteId`, `fechaHora`, `jornada`, `etapa`, `estado`, `ganadorDesempateId`, `motivoDesempate` | Representar un encuentro; los equipos deben ser diferentes; estado inicial `PROGRAMADO`. |
| `Anotacion` | `id`, `partidoId`, `equipoId`, `tipo`, `touchdownOrigenId` | Registrar un evento de anotación, sin almacenar puntos fijos en el evento. |
| `ConfiguracionTorneo` | `puntosVictoria`, `puntosEmpate`, `puntosDerrota`, `criterios` | Definir puntuación en tabla y orden de desempates; evitar criterios repetidos. |
| `ReglamentoAnotaciones` | `puntosTouchdown`, `puntosConversion1`, `puntosConversion2`, `puntosSafety` | Definir puntos por evento: valores por defecto 6, 1, 2 y 2, respectivamente. |
| `Marcador` | `local`, `visitante` | Resultado **calculado**, no editable por formularios; valores no negativos. |
| `FilaPosiciones` | `equipoId`, `jugados`, `ganados`, `empatados`, `perdidos`, `puntosFavor`, `puntosContra`, `puntosTabla` | Objeto de transferencia/valor usado por el motor de Parra; no calcula ni ordena por sí solo la tabla. |

### Reglas de diseño para estas clases

1. Emplea atributos `private` y expón únicamente las operaciones necesarias; no habilites setters públicos irrestrictos para todo.
2. Los identificadores de entidades persistibles serán `UUID`. No uses índices de lista como identificador.
3. Las relaciones entre entidades se representarán con identificadores (`equipoId`, `torneoId`, etc.), como indica el documento maestro.
4. Para nombres, elimina espacios al inicio y al final y rechaza cadenas vacías.
5. Rechaza parámetros obligatorios nulos con una excepción de dominio comprensible.
6. Protege colecciones mutables mediante copias o vistas no modificables; un consumidor no debe poder alterar silenciosamente la configuración interna.
7. No incluyas `System.out.println`, ventanas JavaFX ni llamadas de almacenamiento en el dominio.
8. Define la identidad lógica de entidades por `UUID id`; si implementas `equals()` y `hashCode()`, hazlos coherentes entre sí y documenta el criterio.
9. Evita herencia artificial solo para demostrar el concepto: los usos legítimos de interfaces y excepciones son preferibles.

## 5. Enumeraciones obligatorias

Implementa las enumeraciones que aparecen en el contrato de integración:

```java
public enum Modalidad { CINCO, SIETE }
public enum Rama { VARONIL, FEMENIL, MIXTA }
public enum FormatoTorneo { LIGA, ELIMINACION, LIGA_PLAYOFFS }
public enum EstadoPartido { PROGRAMADO, EN_CURSO, FINALIZADO }
public enum EstadoTorneo { BORRADOR, EN_CURSO, FINALIZADO }
public enum Etapa { LIGA, CUARTOS, SEMIFINAL, FINAL }
public enum TipoAnotacion { TOUCHDOWN, CONVERSION_1, CONVERSION_2, SAFETY }
public enum CriterioDesempate { DIFERENCIA_PUNTOS, PUNTOS_FAVOR, VICTORIAS }
```

Colócalas en `com.flagleague.domain.enums`, en archivos `.java` independientes. No agregues tipos incompatibles sin acuerdo previo.

## 6. Contrato de la calculadora de marcador

El contrato aprobado para implementar es el del **archivo 03**:

```java
package com.flagleague.domain.rules;

import java.util.List;
import com.flagleague.domain.model.*;

public interface CalculadoraMarcador {
    Marcador calcular(
        Partido partido,
        List<Anotacion> anotaciones,
        ReglamentoAnotaciones reglamento
    );
}
```

Crea una implementación concreta denominada `CalculadoraMarcadorPorEventos`, que implemente esta interfaz. **No entregues aquí el algoritmo escrito por el docente**: diseña y programa tú el cálculo.

### Condiciones que debe verificar la calculadora

- Cada anotación corresponde al `id` del partido recibido.
- El equipo anotador es el local o el visitante del mismo encuentro.
- Para cada tipo, el valor en puntos se obtiene de `ReglamentoAnotaciones`; no codifiques 6, 1, 2 o 2 dentro del algoritmo.
- Las anotaciones de tipo `TOUCHDOWN` y `SAFETY` no deben asociar `touchdownOrigenId`.
- Las conversiones `CONVERSION_1` y `CONVERSION_2` deben referirse a un touchdown existente **en el mismo partido y del mismo equipo**.
- Un touchdown puede tener **como máximo una conversión** vinculada.
- El resultado debe devolverse como `Marcador` sin persistirlo ni modificar el objeto `Partido`.
- Ante eventos inválidos, el cálculo debe rechazar la operación con una excepción de negocio entendible.

### Ejemplo de aceptación

En un partido entre `Águilas` (local) y `Tigres` (visitante):

1. Águilas registra un touchdown: +6.
2. Águilas registra otro touchdown: +6.
3. Águilas registra una conversión de dos puntos asociada al segundo touchdown: +2.
4. Tigres registra un safety: +2.

**Marcador esperado: Águilas 14 — Tigres 2.**

La calculadora debe producir el mismo resultado independientemente del orden en que se suministre la lista de eventos, siempre que las referencias a los touchdowns sean válidas.

## 7. Excepciones y transiciones de estado

Crea una excepción base `ReglaNegocioException` y, si agregan claridad, subclases como `DatoInvalidoException` o `AnotacionInvalidaException`. No abuses de una clase de excepción por cada campo; la jerarquía debe ser pequeña y justificable.

Las transiciones de estado de un partido deberán permitir únicamente:

```text
PROGRAMADO -> EN_CURSO -> FINALIZADO
```

No se permite regresar de `FINALIZADO` a un estado anterior en v1.0. Los métodos de dominio deben impedir un estado imposible; **iniciar y finalizar el partido mediante servicios es tarea de Paul**. Coordina las firmas de transición que el servicio utilizará.

En torneos, las transiciones permitidas serán:

```text
BORRADOR -> EN_CURSO -> FINALIZADO
```

La condición de **cuándo** existe campeón o cuáles partidos faltan la determina el motor de Parra; las entidades solo protegen sus invariantes.

## 8. Integración con los otros alumnos

| Compañero | Necesita de tu módulo | Acuerdo obligatorio |
|---|---|---|
| Paul — Servicios | `Equipo`, `Jugador`, `Torneo`, `Partido`, `Anotacion`, estados y calculadora | Confirmar constructores, firmas públicas, excepciones y métodos de transición. |
| Emilio — JavaFX | Nombres, tipos, estados y datos mostrables | No incluir código JavaFX en modelos; aportar información consultable mediante métodos. |
| Carlos — Persistencia | Identificadores UUID, entidades y getters para guardar/buscar | Acordar cómo leer el `id` y cómo representar entidades sin romper encapsulamiento. |
| Parra — Competencias | `Partido`, `Marcador`, `FilaPosiciones`, configuraciones, etapas y estados | Acordar manejo de desempates externos y objetos de resultados calculados. |

**Punto de control común:** antes de cerrar el día 2, Yan debe entregar clases base y enums compilables para que los demás módulos puedan importar tipos reales, aunque ciertas reglas todavía estén en desarrollo.

### Tres decisiones que el equipo debe resolver en el día 1

Estas dudas ya existen en la documentación inicial y **no deben resolverse unilateralmente**:

1. **Interfaz vs. clase de marcador:** el archivo 02 dibuja `CalculadoraMarcador` como clase, mientras que el archivo 03 la define como interfaz. Para programar, se tomará provisionalmente el **contrato 03**, con una implementación `CalculadoraMarcadorPorEventos`. Después se actualizará el UML mediante Pull Request.
2. **Fecha inicial del torneo:** RN-02 pide calcular la edad al inicio del torneo, pero el UML actual solo explicita fechas en `Temporada`. El equipo debe acordar si se agregará `fechaInicio` a `Torneo` o si se adoptará formalmente otra fecha de referencia; documentarlo antes de implementar elegibilidad.
3. **Estado de desempate pendiente:** RN-09 menciona `DESEMPATE_PENDIENTE`, pero `FilaPosiciones` no incluye todavía un atributo para señalarlo. El Parra debe acordar con Yan cómo representarlo sin alterar a escondidas el contrato de tabla.

## 9. Actividades de desarrollo por etapas

### Etapa A — Diseño y coordinación (días 1 y 2)

1. Leer los tres documentos comunes y enumerar todas las clases y tipos que debes crear.
2. Confirmar con Carlos cómo expondrán las entidades sus identificadores a los repositorios.
3. Confirmar con Paul el flujo de estados y las excepciones esperadas.
4. Confirmar con Parra la representación de `Marcador`, `FilaPosiciones` y las reglas configurables.
5. Crear tu rama individual desde el proyecto Maven compartido, una vez que la base esté lista.
6. Programar los ocho enums y las entidades mínimas para permitir compilación.
7. Abrir un primer Pull Request pequeño con modelos, pruebas iniciales y documentación de decisiones.

**Evidencia al día 2:** todos los enums y clases base esenciales están disponibles para que los compañeros compilen sus módulos. No se exige todavía que todos los comportamientos estén terminados.

### Etapa B — Implementación y pruebas (días 3 y 4)

1. Terminar invariantes y métodos de las entidades.
2. Implementar `ReglamentoAnotaciones` y `ConfiguracionTorneo`.
3. Crear `Marcador` y la interfaz `CalculadoraMarcador`.
4. Implementar `CalculadoraMarcadorPorEventos`.
5. Probar touchdowns, conversiones y safeties, tanto válidos como inválidos.
6. Resolver observaciones del Pull Request y entregar un segundo cambio funcional.

**Evidencia al día 4:** `mvn test` ejecuta exitosamente las pruebas de dominio sin iniciar JavaFX.

### Etapa C — Primera demostración (día 5)

1. Mostrar un caso de cálculo de marcador con dos equipos.
2. Demostrar al menos un rechazo por anotación inválida.
3. Explicar un ejemplo de encapsulamiento, un contrato de interfaz y una validación.
4. Mostrar evidencia de una integración con otro módulo.

### Etapa D — Integración y robustez (días 6 y 7)

1. Apoyar la integración de entidades con servicios, repositorios y competencias.
2. Corregir incompatibilidades detectadas en pruebas, sin romper firmas públicas.
3. Añadir pruebas de regresión para los errores encontrados.
4. Verificar que los eventos de un torneo no alteran el resultado de otro.

### Etapa E — Calidad y entrega (días 8 a 10)

1. Día 8: congelar nuevas funcionalidades; solo corregir defectos.
2. Día 9: actualizar UML y documentación, ejecutar `mvn test`, dejar evidencia y cerrar Pull Requests.
3. Día 10: defender tu código y demostrar la calculadora integrada en la aplicación final.

## 10. Pruebas unitarias mínimas

Implementa pruebas JUnit con nombres que expliquen el comportamiento; puedes organizar las pruebas en `CalculadoraMarcadorPorEventosTest`, `PartidoTest`, `CategoriaTest` y otras clases equivalentes.

| ID | Prueba | Resultado esperado |
|---|---|---|
| A1-T01 | 2 touchdowns y una conversión de 2 del mismo equipo | Suma 14 puntos para ese equipo. |
| A1-T02 | Conversión sin touchdown de origen | Se rechaza. |
| A1-T03 | Dos conversiones vinculadas al mismo touchdown | Se rechaza la segunda asociación / cálculo. |
| A1-T04 | Conversión vinculada a touchdown de otro equipo | Se rechaza. |
| A1-T05 | Anotación con `partidoId` ajeno | Se rechaza. |
| A1-T06 | Anotación de un equipo que no participa en el partido | Se rechaza. |
| A1-T07 | Safety del visitante | Incrementa el marcador visitante según el reglamento. |
| A1-T08 | Reglamento personalizado previo al inicio | Los puntos reflejan los valores configurados. |
| A1-T09 | Construir un partido con el mismo local y visitante | Se rechaza. |
| A1-T10 | Intentar modificar un partido finalizado | Se rechaza. |
| A1-T11 | Categoría con edad mínima superior a máxima | Se rechaza. |
| A1-T12 | Nombre vacío o fecha incoherente de temporada | Se rechaza. |

**Mínimo académico:** implementa al menos las 12 verificaciones anteriores; un único test que agrupe todos los casos no cuenta como 12 pruebas independientes. Puedes agregar pruebas adicionales.

**Casos cruzados:** la elegibilidad de plantillas, inscripción doble y cálculo de standings serán probados por los Paul y Parra; tu participación será proporcionar datos consistentes para esas pruebas.

## 11. Entregables individuales

1. Código fuente de todas las entidades, enums, excepciones y calculadora de marcadores asignados.
2. Pruebas JUnit y evidencia de su ejecución con `mvn test`.
3. Diagrama UML de dominio actualizado conforme a lo implementado, compatible con el UML general.
4. Registro de decisiones técnicas (máximo 1–2 páginas en Markdown): validaciones, inmutabilidad, excepciones y cambios aprobados.
5. Evidencia de colaboración: al menos un Pull Request revisado por otro alumno y una revisión realizada a un compañero.
6. Descripción breve de cómo se usan tus clases desde los módulos de Paul, Carlos y Parra.
7. Participación en la demostración final, explicando tu código y su integración.

No se aceptará como entrega final un proyecto independiente que solo funcione fuera del repositorio principal.

## 12. Rúbrica individual — 100 puntos

| Criterio | Evidencia de evaluación | Puntos |
|---|---|---:|
| Funcionamiento del módulo | Entidades correctas, estados coherentes, cálculos de anotaciones y manejo de errores | 35 |
| Aplicación de POO y diseño | Encapsulamiento, objetos de valor, interfaces, composición, buenas prácticas y excepciones | 20 |
| Pruebas unitarias | Casos A1-T01 a A1-T12, independencia de pruebas y cobertura de casos inválidos | 15 |
| Integración y contratos | Compatibilidad con servicios, repositorios, tablas y aplicación global | 15 |
| Evidencia colaborativa en GitHub | Pull Requests, revisión de otro compañero, historial de cambios útil | 10 |
| Defensa individual | Explica decisiones, recorre código propio y responde preguntas | 5 |
| **Total** | | **100** |

**Criterio de justicia:** un fallo que se demuestre originado exclusivamente en otro módulo no debe anular la calificación del tuyo si sus pruebas aisladas y sus contratos funcionan correctamente. La cantidad de commits no determina por sí sola la nota.

### Preguntas posibles en la defensa individual

- ¿Por qué utilizaste UUID para relacionar las entidades en lugar de índices de `ArrayList`?
- ¿Qué regla impide registrar dos conversiones para el mismo touchdown?
- ¿Por qué el marcador se calcula y no se guarda como un atributo editable del partido?
- ¿Qué diferencia existe entre una validación intrínseca de `Partido` y una validación de `PartidoService`?
- ¿Cómo podría cambiar la implementación de `CalculadoraMarcador` sin modificar el contrato público?
- ¿Cómo protegiste los atributos y colecciones frente a modificaciones externas?

## 13. Definición de terminado

El módulo de Yan se considera terminado cuando se cumple **todo** lo siguiente:

- [ ] Las clases del dominio y los ocho enums compilan en el proyecto Maven común.
- [ ] Las entidades aplican las invariantes documentadas y rechazan datos inválidos.
- [ ] `CalculadoraMarcador` y su implementación están disponibles mediante la firma acordada.
- [ ] Se calcula correctamente un marcador usando touchdowns, conversiones y safeties.
- [ ] No se aceptan conversiones huérfanas, duplicadas o vinculadas a otro equipo.
- [ ] No se usan JavaFX ni repositorios de memoria desde las clases de dominio.
- [ ] Se ejecutan satisfactoriamente las 12 pruebas mínimas con `mvn test`.
- [ ] La rama individual está integrada mediante un Pull Request revisado.
- [ ] El UML y las decisiones de contrato están actualizados y aprobados.
- [ ] El estudiante puede explicar y demostrar su contribución sin ayuda.

---

**Nota para el docente:** la guía mantiene el alcance del Documento Maestro v1.0 y los contratos del archivo 03. Los tres puntos de discrepancia identificados en la sección 8 son acuerdos pendientes del equipo, no funcionalidades inventadas o decisiones que un alumno pueda imponer al resto.
