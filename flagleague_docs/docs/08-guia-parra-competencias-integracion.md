# FlagLeague Manager — Guía individual de desarrollo

## Parra: Motor de competencias, clasificación, playoffs e integración general

**Materia:** Lenguajes Orientados a Objetos  
**Proyecto compartido:** FlagLeague Manager — Administración de una liga de Tochito Bandera  
**Responsable:** **Parra**  
**Tecnologías:** Java 21, Maven, JUnit 5, JavaFX (integración), GitHub Actions  
**Duración:** 10 días hábiles / 2 semanas  
**Rama individual:** `feature/parra-competencias-integracion`  
**Evaluación:** individual, 100 puntos  
**Complejidad asignada:** **muy alta**, junto con Paul  
**Versión:** 1.0 — guía de implementación, no solución programada

> **Este es un módulo de desarrollo, no un puesto administrativo.** Debes escribir el motor que interpreta formatos de competencia, calcula posiciones, crea rondas eliminatorias y determina campeones. Además coordinarás el arranque compartido y la integración continua del proyecto. Cada compañero conserva la autoría y responsabilidad de sus propios módulos.

### Documentos obligatorios

- [01 — Documento maestro](01-documento-maestro.md): criterios de aceptación, reglas RN-08 a RN-14, alcance por modalidad.
- [02 — UML](02-modelo-dominio.mmd): `Partido`, `Torneo`, `FilaPosiciones`, `FormatoCompetencia`.
- [03 — Contratos de integración](03-contratos-integracion.md): firmas obligatorias y estrategia de formatos.
- [04 — Yan](04-guia-yan-dominio.md): entidades, anotaciones y `CalculadoraMarcador`.
- [05 — Paul](05-guia-paul-servicios.md): programación de partidos y round-robin.
- [06 — Emilio](06-guia-emilio-javafx.md): datos que requieren las pantallas.
- [07 — Carlos](07-guia-carlos-persistencia.md): repositorios y carga demo.

**Atención:** el documento 03 contiene contratos objetivo a aprobar entre todos en el día 1. Si encuentras un caso que no pueda resolverse con esas firmas, documenta una propuesta y obtén acuerdo antes de cambiar interfaces compartidas.

---

## 1. Objetivo individual

**Diseñar, implementar y probar** un motor orientado a objetos que soporte los formatos **liga regular, eliminación directa y liga con playoffs**, calcule tablas de posiciones configurables, resuelva las fases de competencia y determine al campeón, además de construir un arranque reproducible y automatizar las pruebas de integración de la aplicación común.

### Resultado esperado

El motor deberá funcionar sobre entidades y repositorios compartidos, sin conectarse a una base de datos ni usar JavaFX para sus cálculos. Debe ser posible demostrar un campeonato completo, desde la fase inicial hasta el campeón, tanto en 5 vs. 5 como en 7 vs. 7, sin mezclar información entre torneos.

## 2. Responsabilidades y límites

**Sí debes programar:**

1. Interfaz `FormatoCompetencia` y sus tres estrategias.
2. `ClasificacionService` y la implementación del cálculo de posiciones.
3. Lógica de clasificación a playoffs, emparejamientos por siembra y progresión de rondas.
4. Verificaciones de resultados pendientes, eliminación duplicada, empates y reglas de campeón.
5. `AppBootstrap` o composición de dependencias equivalente, una sola vez por ejecución.
6. Conexión del arranque con JavaFX y los servicios compartidos, coordinando el archivo `MainApp` con Emilio.
7. Pruebas unitarias del motor, pruebas de integración entre módulos y configuración de ejecución de `mvn test` en GitHub Actions.
8. Reporte de integración, análisis de incidencias y evidencia de PR revisados.

**No debes programar:**

- Las entidades o la calculadora del marcador por eventos (Yan).
- El algoritmo *round-robin*, CRUD ni captura de anotaciones (Paul).
- Las pantallas y controladores (Emilio).
- Las implementaciones `Map<UUID,T>` y `DatosDemo` (Carlos).
- Una segunda aplicación separada ni estructuras de datos duplicadas para almacenar torneos.

## 3. Paquetes y clases de tu propiedad

```text
amateur-flagleague/
├── pom.xml                            (configuración Maven compartida)
├── .github/workflows/
│   └── java-ci.yml                    (Parra)
├── src/main/java/com/flagleague/
│   ├── MainApp.java                   (integración con Emilio)
│   ├── bootstrap/
│   │   └── AppBootstrap.java          (Parra)
│   ├── application/competition/
│   │   ├── FormatoCompetencia.java    (interfaz)
│   │   ├── FormatoLiga.java
│   │   ├── FormatoEliminacion.java
│   │   ├── FormatoLigaPlayoffs.java
│   │   ├── ClasificacionService.java  (interfaz)
│   │   └── ...                        (implementación y apoyos)
│   ├── application/service/           (Paul)
│   ├── domain/                        (Yan + contratos Carlos)
│   ├── infrastructure/                (Carlos)
│   └── presentation/                  (Emilio)
├── src/test/java/com/flagleague/
│   ├── application/competition/       (pruebas del motor, Parra)
│   └── integration/                   (pruebas entre módulos, Parra)
└── flagleague_docs/
    └── docs/                          (documentación común)
```

No modifiques las clases de otros compañeros únicamente para acomodar tu implementación. Si un contrato es insuficiente, abre un PR que explique el caso de uso, el impacto y las pruebas que se añadirían.

## 4. Contratos que debes implementar

### 4.1 Estrategia de formato

```java
public interface FormatoCompetencia {
    List<Partido> generarPrimeraFase(UUID torneoId);
    List<Partido> generarSiguienteFase(UUID torneoId);
    Optional<UUID> obtenerCampeon(UUID torneoId);
}
```

Implementaciones obligatorias:

- `FormatoLiga` — liga de una vuelta y campeón por clasificación.
- `FormatoEliminacion` — bracket eliminatorio de cuatro u ocho equipos.
- `FormatoLigaPlayoffs` — liga de una vuelta, cuatro clasificados, semifinales y final.

Las estrategias comparten contrato, pero su comportamiento debe cambiar según el formato. **Esto es polimorfismo real**; no se aceptará un `switch` gigantesco en la interfaz gráfica que implemente por su cuenta todas las reglas.

### 4.2 Clasificación

```java
public interface ClasificacionService {
    List<FilaPosiciones> calcular(UUID torneoId);
}
```

`FilaPosiciones` pertenece al dominio de Yan. El motor la construirá a partir de partidos `FINALIZADO` del mismo torneo y los marcadores calculados por el componente de Yan. No debe consultar directamente ningún controlador JavaFX.

### 4.3 Contratos externos necesarios

Consume las operaciones acordadas con Paul para programar encuentros y generar calendarios. Consulta partidos, inscripciones, torneos, categorías y reglamentos mediante las interfaces de repositorio definidas con Carlos. Si alguna operación no existe en el contrato 03 (por ejemplo, actualizar estado del torneo, recuperar anotaciones por partido o establecer una siembra), debe **definirse durante el día 1**, incluirse en el contrato común y probarse.

## 5. Motor de clasificación: requisitos matemáticos y funcionales

### 5.1 Cálculo por torneo

Para cada equipo inscrito en el torneo seleccionado:

1. Inicializa jugados (`PJ`), ganados (`PG`), empatados (`PE`), perdidos (`PP`), puntos a favor (`PF`), puntos en contra (`PC`) y puntos de tabla (`PTS`) en cero.
2. Obtén **solo** los partidos de la etapa `LIGA` que estén `FINALIZADO`.
3. Usa el marcador calculado desde anotaciones (`CalculadoraMarcador` de Yan); un ganador por desempate externo **no cambia** los puntos anotados.
4. Para cada partido, actualiza las filas de sus dos equipos inscritos.
5. Aplica la configuración del torneo para victoria, empate y derrota; valor predeterminado `3/1/0`.
6. Calcula diferencia de puntos (`DIF = PF - PC`) y ordena conforme a los criterios configurados en `ConfiguracionTorneo`.
7. No incluyas encuentros `SEMIFINAL`, `FINAL` ni `CUARTOS` en la clasificación de liga regular.

**Invariante importante:** para una tabla completa, la suma de `PF` debe ser igual a la suma de `PC` en los partidos contados, porque cada punto de un equipo es un punto recibido por su rival. Además, la suma de `PJ` será dos veces el número de encuentros finalizados considerados.

### 5.2 Configuración de la clasificación

- Valores de puntos por victoria, empate y derrota son configurables por torneo.
- Los desempates son una lista ordenada sin elementos repetidos de `DIFERENCIA_PUNTOS`, `PUNTOS_FAVOR` y `VICTORIAS`.
- Los puntos de tabla (`PTS`) siempre son el criterio principal; la lista de desempates se aplica después.
- Si todos los criterios producen igualdad en posiciones decisivas, **no adjudicar** campeón ni clasificado arbitrariamente. El orden por nombre o UUID puede emplearse solo para representación visual estable.
- El sistema debe reportar «desempate pendiente» sin inventar un vencedor. Acuerda con Yan y Emilio en el día 1 cómo representar esto (por ejemplo, un resultado auxiliar o excepción de situación pendiente); **no presupongas** que `FilaPosiciones` ya contiene ese atributo.

### 5.3 Ejemplo de aceptación de clasificación

Configura tres equipos con puntuación `3/1/0` y una liga de una vuelta:

| Partido | Resultado |
|---|---|
| A vs B | A gana 12–6 |
| A vs C | Empate 6–6 |
| B vs C | B gana 8–0 |

Resultados esperados:

| Equipo | PJ | PG | PE | PP | PF | PC | DIF | PTS |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| A | 2 | 1 | 1 | 0 | 18 | 12 | +6 | 4 |
| B | 2 | 1 | 0 | 1 | 14 | 12 | +2 | 3 |
| C | 2 | 0 | 1 | 1 | 6 | 14 | −8 | 1 |

Este escenario sirve para probar el **motor de clasificación** con partidos de prueba ya finalizados; no implica agregar a la interfaz un sistema para capturar manualmente marcadores. Los resultados mostrados pueden construirse en pruebas con eventos de anotación permitidos o con dobles de la calculadora acordados.

## 6. Estrategia 1 — Liga regular

### 6.1 Primera fase

- Verifica que el torneo tenga formato `LIGA`, estado válido y entre 4 y 8 equipos participantes.
- Usa **el generador round-robin de Paul**. No implementes una segunda versión del algoritmo.
- La liga es de una sola vuelta y produce `n(n−1)/2` partidos, una pareja por enfrentamiento.
- No genera una «segunda fase»; acuerda que `generarSiguienteFase` devuelva una lista vacía sin efectos laterales o una señal documentada en el contrato. La propuesta para v1.0 es `List.of()`.
- La generación repetida de primera fase no debe duplicar partidos.

### 6.2 Campeón

- Todos los partidos de liga deben estar finalizados.
- Debe existir un primer lugar único tras aplicar los criterios de desempate configurados.
- Si falta un encuentro o persiste empate en el primer lugar, `obtenerCampeon` devuelve `Optional.empty()` y el estado correspondiente se comunica de manera verificable; no elegir ganador por nombre.
- Una vez cumplidas las condiciones, devuelve el UUID del equipo campeón.

## 7. Estrategia 2 — Eliminación directa

### 7.1 Inscripciones y siembra

- Admitir **exactamente 4 u 8 equipos**.
- Los registros `Inscripcion` incluyen `ordenSiembra`. Antes de generar el cuadro, exige siembras válidas, únicas y completas; acuerda su asignación con Paul.
- Para cuatro equipos, crear semifinales **1 vs 4** y **2 vs 3**.
- Para ocho equipos, crear cuartos **1 vs 8**, **4 vs 5**, **2 vs 7**, **3 vs 6**; esto mantiene los sembrados 1 y 2 en mitades opuestas.
- El UUID de torneo, la etapa y la relación entre inscripciones y partidos deben mantenerse íntegros.

### 7.2 Progresión de rondas

1. Al invocar `generarPrimeraFase`, crea semifinales para cuatro equipos o cuartos para ocho.
2. `generarSiguienteFase` consulta **todos** los partidos de la ronda activa.
3. Si hay algún partido pendiente, impide crear la siguiente fase con un mensaje de dominio preciso.
4. Cuando todos terminan y tienen ganador definido, genera los emparejamientos de la siguiente ronda según el cuadro inicial.
5. No avances por el mero hecho de que un partido tenga mayor puntaje si el encuentro no está formalmente `FINALIZADO`.
6. No crees una final por duplicado cuando el usuario pulse dos veces «Generar siguiente ronda».
7. No permitas inscribir nuevos equipos una vez generada la primera ronda.

### 7.3 Empates y desempate externo

Si un partido eliminatorio termina empatado:

- El resultado conserva su marcador de anotaciones.
- Se registra un `ganadorDesempateId` que debe pertenecer a uno de los dos equipos.
- `motivoDesempate` debe contener una explicación no vacía.
- Sin esos campos válidos, el motor no puede usar el partido para generar la siguiente ronda.
- **No simules** un volado, penales o desempate adicional dentro de la versión 1.0.

### 7.4 Campeón

Solo existe cuando la **final** está finalizada y tiene un ganador válido. No debe retornarse el mejor sembrado ni el equipo con más puntos acumulados si el torneo aún no concluye.

## 8. Estrategia 3 — Liga con playoffs

1. Requiere al menos cuatro equipos válidos, conforme al alcance de liga (entre 4 y 8).
2. La primera fase es una liga de una vuelta **generada por el servicio de Paul**, sin duplicar código.
3. Para crear semifinales, **todos los partidos de la etapa LIGA** deben estar finalizados.
4. La clasificación no debe tener empate irresuelto que afecte los cuatro clasificados o su orden de siembra.
5. Se clasifican primero, segundo, tercero y cuarto; semifinales **1 vs 4** y **2 vs 3**.
6. Genera la final con los vencedores de semifinales. No se jugará partido por tercer lugar en esta versión.
7. La final debe estar terminada para devolver el UUID del campeón.
8. La repetición de cualquier operación de generación debe ser segura frente a duplicados.

**No mezcles** partidos de semifinal/final con la tabla de temporada regular. La tabla será una fotografía de la fase LIGA; los playoffs son un flujo adicional del mismo torneo.

## 9. Integración: `AppBootstrap`

Tu responsabilidad incluye programar un **único punto de composición** que cree y conecte los componentes. No se trata de acceder globalmente a todo mediante variables estáticas.

### 9.1 Orden sugerido de inicialización

1. Construir una instancia de cada repositorio en memoria proporcionado por Carlos.
2. Crear instancias de las reglas y calculadora de Yan.
3. Crear los servicios de Paul inyectando las interfaces de repositorios y reglas correspondientes.
4. Crear el servicio de clasificación y las tres estrategias de competencia con dependencias explícitas.
5. Cargar `DatosDemo` una sola vez en la ejecución.
6. Entregar los servicios a los controladores JavaFX de Emilio a través de la factoría acordada.
7. Abrir una única ventana principal con el dashboard.

### 9.2 Pruebas esenciales de composición

- Registrar un equipo mediante `EquipoService` y leerlo mediante otro servicio debe producir el mismo registro.
- Los datos demo no deben duplicarse al volver a abrir una vista.
- `FormatoLigaPlayoffs` debe usar la misma colección de partidos que `PartidoService`.
- Las interfaces deben permitir sustituir los repositorios de Carlos por dobles de prueba.
- El motor debe poder probarse con `mvn test` **sin iniciar JavaFX**.

### 9.3 Maven y ejecución

Coordina con Emilio la configuración común de Maven y el punto de inicio. Los resultados mínimos son:

```bash
mvn clean test
mvn javafx:run
```

No añadas scripts que dependan exclusivamente de tu computadora. Documenta la versión del JDK utilizada y las instrucciones reproducibles de arranque en Windows con Visual Studio Code.

## 10. Integración continua y GitHub

Configura un flujo de **GitHub Actions** que, en *push* y *pull request* hacia `main`, utilice Java 21 y ejecute `mvn --batch-mode clean test`. El objetivo es impedir que una integración rompa las pruebas existentes.

**Política de colaboración:**

- Ramas: `feature/yan-dominio`, `feature/paul-servicios`, `feature/emilio-javafx`, `feature/carlos-persistencia` y `feature/parra-competencias-integracion`.
- Cada integrante crea y presenta PR de sus propios cambios.
- Una persona diferente revisa el PR antes de integrarlo.
- Ningún módulo cambia una firma pública acordada sin aprobación del módulo consumidor.
- No se mide la calidad de cada alumno por cantidad de commits; las pruebas y las contribuciones verificables son prioridad.
- Parra coordina la integración, pero **no es responsable de corregir el código de todos**.

## 11. Cronograma individual (10 días hábiles)

| Día | Trabajo propio | Evidencia |
|---|---|---|
| **1** | Revisar modelo, reglas, tipos de torneo, siembra y contratos con Paul, Yan y Carlos. | Lista de decisiones y cambios aprobados. |
| **2** | Crear interfaces/estrategias base, `AppBootstrap` mínimo, flujo CI. | Proyecto compila, abre JavaFX y corre `mvn test`. |
| **3** | Implementar cálculo de posiciones 3/1/0 con diferentes criterios de desempate. | Pruebas de tabla y datos aislados. |
| **4** | Liga y eliminatoria de cuatro equipos; primera versión de avance de rondas. | Prueba de semifinal/final y PR. |
| **5** | **Control intermedio individual** con liga y primera eliminatoria. | Demostración de motor en tests sin UI. |
| **6** | Eliminatoria de ocho equipos y liga con playoffs. | Cuartos, semifinales, final y cuadro 1–4. |
| **7** | Integración de servicios, repositorios y vista de posiciones. | Escenario integral 5 vs. 5 y 7 vs. 7. |
| **8** | Congelamiento funcional; reforzar idempotencia, errores y empates. | Pruebas negativas y CI verde. |
| **9** | Reporte de integración, UML ajustado, regresión y documentación. | Reporte de incidencias y PR final revisado. |
| **10** | Demostración y defensa individual. | Prueba de campeonato completo y explicación de estrategias. |

## 12. Pruebas unitarias obligatorias del motor

Implementa al menos **18 pruebas automatizadas**, con aserciones concretas y sin JavaFX:

| ID | Situación | Esperado |
|---|---|---|
| P-01 | Clasificación inicial sin partidos | Filas de inscritos con valores en cero. |
| P-02 | Victoria, empate y derrota (3/1/0) | Puntos de tabla correctos. |
| P-03 | Configuración 2/1/0 | Puntuación recalculada sin editar partidos. |
| P-04 | Desempate por diferencia de puntos | Orden correcto. |
| P-05 | Desempate por puntos a favor | Orden correcto si la diferencia es igual. |
| P-06 | Partido programado | No altera tabla. |
| P-07 | Partido de semifinal | No altera tabla de liga. |
| P-08 | Partidos de otro torneo | No aparecen en tabla seleccionada. |
| P-09 | Liga con encuentros pendientes | Campeón todavía no disponible. |
| P-10 | Liga cerrada con primer lugar único | Campeón correcto. |
| P-11 | Empate irresuelto por primer lugar | No se elige campeón arbitrario. |
| P-12 | Eliminación de cuatro | Semifinales 1–4 y 2–3; final correcta. |
| P-13 | Eliminación de ocho | Cuartos, semifinales y final correctos. |
| P-14 | Intento de avance con partido pendiente | Excepción/resultado de rechazo, sin crear ronda. |
| P-15 | Empate de semifinal sin desempate externo | No permite pasar de fase. |
| P-16 | Desempate externo válido | Avanza ganador documentado sin cambiar marcador. |
| P-17 | Generar dos veces la misma ronda | No duplica partidos. |
| P-18 | Liga con playoffs | Clasifica 1–4, 2–3 y campeón tras la final. |

**Recomendadas adicionales:** duplicar siembras, ausencia de cuarta plaza, torneo 7 vs. 7 simultáneo, criterios repetidos, ganador de desempate ajeno al partido y entrega de campeón con final programada.

## 13. Pruebas de integración obligatorias

Estas pruebas son **adicionales** a las 18 pruebas del motor. Registra al menos **ocho escenarios**:

1. **Integración dominio–servicio:** Paul registra un touchdown, Yan calcula el marcador y Parra incorpora el resultado a la tabla.
2. **Repositorios compartidos:** dos servicios consultan el mismo torneo y sus equipos sin clonar el almacén.
3. **Liga de cuatro equipos:** Paul genera seis partidos; Parra clasifica y obtiene campeón cuando concluyen.
4. **Liga con playoffs:** tras los seis encuentros genera semifinales 1–4, 2–3 y final.
5. **Eliminación 7 vs. 7:** cuatro equipos, desempate externo documentado y campeón.
6. **Aislamiento:** registrar un resultado en el torneo B no modifica posiciones ni fases del torneo A.
7. **Reintentos:** invocar dos veces generación de calendario o fase no produce duplicados.
8. **Arranque completo:** carga demo una vez y los controladores de Emilio utilizan los mismos servicios.

Los escenarios 1–7 deben poder ejecutarse como pruebas automáticas. El escenario 8 puede combinar una prueba de composición automatizada con una prueba manual de apertura JavaFX documentada.

### 13.1 Matriz de incidencias

El reporte de integración incluirá una tabla con columnas: ID, fecha, reproducción, módulo origen, severidad, responsable de corrección, PR vinculado, prueba de regresión y estado. **No uses fallas ajenas como evidencia negativa del trabajo individual de otro estudiante sin aislar su origen.**

## 14. Entregables individuales

1. `ClasificacionService` implementado y probado, usando partidos finalizados del mismo torneo.
2. Tres implementaciones de `FormatoCompetencia`, con polimorfismo y sin repetir el algoritmo de calendario de Paul.
3. Generación de eliminación directa 4/8, playoffs 1–4 y 2–3, campeón y desempate externo.
4. `AppBootstrap` con dependencias compartidas y JavaFX integrado con Emilio.
5. Archivo `.github/workflows/java-ci.yml` para probar en GitHub.
6. Mínimo 18 pruebas del motor y ocho escenarios de integración.
7. Documento de diseño que explique estrategias, criterios de desempate y protección contra duplicados.
8. Reporte de integración con incidencias, decisiones y pruebas de regresión.
9. Evidencia de PR individuales revisados y de revisión de código de otros miembros.
10. Participación en demostración final y defensa individual de un formato y el arranque.

## 15. Rúbrica individual — 100 puntos

| Criterio | Puntos | Evidencia |
|---|---:|---|
| Funcionamiento del motor | **35** | Tabla, desempates, tres formatos, rondas, campeón, idempotencia. |
| Diseño y POO | **20** | Estrategia, interfaces, polimorfismo, encapsulamiento, responsabilidades claras. |
| Pruebas y casos límite | **15** | 18 unitarias, ocho escenarios de integración y errores controlados. |
| Integración técnica | **15** | `AppBootstrap`, CI, repositorios/servicios compartidos, JavaFX conectado. |
| Colaboración verificable | **10** | PR propios, revisiones, acuerdos de firmas, reporte de incidencias. |
| Defensa individual | **5** | Justifica algoritmos y explica su código y pruebas. |
| **Total** | **100** | |

### Preguntas sugeridas para la defensa

- ¿Por qué `FormatoLiga`, `FormatoEliminacion` y `FormatoLigaPlayoffs` implementan una interfaz común?
- ¿Cómo impides que el mismo botón genere dos veces una final?
- ¿Por qué un partido de semifinal no debe cambiar los puntos de la liga regular?
- ¿Cómo distingues entre empate deportivo y empate irresuelto de clasificación?
- ¿Por qué `obtenerCampeon` devuelve `Optional<UUID>` y no inventa siempre un ganador?
- ¿Cómo inyectaste las mismas instancias de repositorio a los servicios y estrategias?
- ¿Qué prueba demuestra que no mezclas resultados de dos torneos simultáneos?

## 16. Definición de terminado (*Definition of Done*)

- [ ] Tres estrategias compilables respetan `FormatoCompetencia`.
- [ ] Clasificación configurable correcta y aislada por torneo.
- [ ] El orden de criterios de desempate se respeta y no se adjudican empates irresueltos.
- [ ] Eliminatoria 4/8 y liga con playoffs generan rondas correctas.
- [ ] Repetir generaciones no duplica partidos.
- [ ] La final es condición obligatoria para declarar campeón eliminatorio.
- [ ] `AppBootstrap` crea repositorios una sola vez y conecta todos los servicios.
- [ ] GitHub Actions ejecuta `mvn clean test` sin fallos.
- [ ] Pasan 18 pruebas del motor y se documentan ocho integraciones.
- [ ] La aplicación completa abre en JavaFX y el equipo demuestra dos torneos independientes.
- [ ] Se entrega reporte de integración y defensa del código propio.

**Regla final:** la coordinación no sustituye la programación. El mérito de Parra se evaluará por la calidad verificable del motor de competencias, su composición técnica y sus pruebas, no por cuántos archivos de otros compañeros haya modificado.
