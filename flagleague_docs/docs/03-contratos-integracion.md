# FlagLeague Manager — Contratos de integración (borrador para aprobar en día 1)

> No es código implementado. Son firmas objetivo y acuerdos de conducta para que los cinco estudiantes puedan programar en paralelo. Las firmas deberán fijarse por escrito al terminar el día 1; cualquier modificación posterior requiere Pull Request aprobado por los módulos consumidores.

## 1. Tipos básicos

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

`UUID` identificará entidades. Se usará `LocalDate` en fechas de nacimiento/temporada y `LocalDateTime` para el horario local de partidos. No se implementarán husos horarios en v1.0.

## 2. Contrato de repositorios (alumno 4)

```java
public interface Repository<T> {
    T guardar(T entidad);           // crea o actualiza; misma identidad
    Optional<T> buscarPorId(UUID id);
    List<T> listar();                // copia defensiva de la colección
    void eliminar(UUID id);         // rechazar si hay referencias históricas
}

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

También existirán `TemporadaRepository`, `CategoriaRepository` y `TorneoRepository`. Las implementaciones de memoria deben conservar estado compartido mientras la app está abierta; no deben reconstruirse en cada llamada.

## 3. Contratos de servicios (alumno 2)

La convención es recibir comandos o parámetros mínimos y devolver entidades/DTOs de solo lectura para la interfaz. Propuesta de operaciones:

```java
public interface GestionLigaService {
    Equipo crearEquipo(String nombre);
    Jugador registrarJugador(UUID equipoId, String nombre,
                            LocalDate nacimiento, Rama rama);
    Torneo crearTorneo(UUID temporadaId, UUID categoriaId,
                      Modalidad modalidad, FormatoTorneo formato,
                      ConfiguracionTorneo configuracion);
    Inscripcion inscribirEquipo(UUID torneoId, UUID equipoId);
    List<Partido> generarCalendarioLiga(UUID torneoId);
    Partido programarPartido(UUID torneoId, UUID localId,
                            UUID visitanteId, LocalDateTime fechaHora,
                            int jornada, Etapa etapa);
    Partido iniciarPartido(UUID partidoId);
    Partido registrarAnotacion(UUID partidoId, UUID equipoId,
                              TipoAnotacion tipo, UUID touchdownOrigenId);
    Partido finalizarPartido(UUID partidoId,
                            UUID ganadorDesempateId,
                            String motivoDesempate);
}
```

En la implementación final se recomienda dividir `GestionLigaService` por responsabilidad (`EquipoService`, `TorneoService`, `InscripcionService`, `PartidoService`, `CalendarioService`) para evitar una clase gigante. La interfaz anterior es solo un mapa de métodos mínimos, **no** una instrucción para implementar todo en una clase.

## 4. Motor de competición (alumno 5)

```java
public interface FormatoCompetencia {
    List<Partido> generarPrimeraFase(UUID torneoId);
    List<Partido> generarSiguienteFase(UUID torneoId);
    Optional<UUID> obtenerCampeon(UUID torneoId);
}

public interface ClasificacionService {
    List<FilaPosiciones> calcular(UUID torneoId);
}
```

- `FormatoLiga`: delega la generación round-robin al `CalendarioService` del alumno 2; sin siguiente fase; campeón cuando todos los encuentros concluyan y el primer puesto no tenga empate irresuelto.
- `FormatoEliminacion`: primera fase de 4 u 8; semifinal o cuartos; siguiente ronda al terminar la anterior; campeón cuando termine la final.
- `FormatoLigaPlayoffs`: fase regular generada por el mismo `CalendarioService` (sin duplicar algoritmos); clasifican cuatro; semifinal 1-4 y 2-3; final con ganadores. Para generar las semifinales deben terminar todos los encuentros de liga y resolverse cualquier empate en los lugares de clasificación.

## 5. Contrato de calculadora (alumno 1)

```java
public interface CalculadoraMarcador {
    Marcador calcular(Partido partido, List<Anotacion> anotaciones,
                     ReglamentoAnotaciones reglamento);
}
```

La calculadora no guarda resultados ni conoce JavaFX. Valida que las anotaciones correspondan al partido/equipo y suma puntos según el reglamento entregado. La aplicación bloquea la modificación del reglamento cuando ya existen eventos.

## 6. Contrato JavaFX (alumno 3)

- Controladores obtienen servicios por constructor o factoría controlada por `AppBootstrap`; evitar `new EquipoRepositoryMemoria()` dentro de controladores.
- `DashboardController`: resumen del torneo seleccionado.
- `EquiposController`: listado/equipo/plantel.
- `TorneosController`: temporada, categoría, modalidad, formato, inscripción.
- `CalendarioController`: tabla, fecha, hora y generación.
- `ResultadosController`: registrar TD, conversión, safety, iniciar/finalizar.
- `PosicionesController`: tabla y campeón.
- La interfaz muestra fallas mediante alertas con mensajes comprensibles; no calcula standings ni guarda en `Map`.

## 7. Integración/arranque (alumno 5)

`AppBootstrap` instancia una sola vez el conjunto de repositorios, inyecta las mismas instancias en servicios y formatos y entrega estos servicios a la aplicación JavaFX. `DatosDemo` se ejecuta una sola vez y carga dos competencias independientes.

## 8. Pruebas contractuales mínimas

| ID | Prueba | Esperado | Responsable principal |
|---|---|---|---|
| T01 | 2 TD + conversión 2 | 14 puntos | 1 |
| T02 | Conversión sin TD | Excepción de negocio | 1 |
| T03 | Cuatro equipos round-robin | 6 partidos, 3 jornadas | 2 |
| T04 | Cinco equipos round-robin | 10 partidos, 5 jornadas y descanso por fecha | 2 |
| T05 | Doble inscripción | Rechazada | 2 |
| T06 | Guardar/buscar equipo UUID | Mismo registro lógico | 4 |
| T07 | `listar()` mutable desde fuera | No altera almacén interno | 4 |
| T08 | Repositorios compartidos | Una operación de servicio se refleja en UI | 3 y 5 |
| T09 | Tabla 3/1/0 | Puntos y orden correctos | 5 |
| T10 | Eliminatoria 4/8 | Rondas y final correctas | 5 |
| T11 | Empate en partido eliminatorio | Exige desempate externo | 5 |
| T12 | Aislamiento entre torneos | Sin resultados cruzados | 5 |
| T13 | Error de formulario | Mensaje visible sin cerrar la aplicación | 3 |

## 9. Flujo GitHub

```text
main (protegida)
feature/alumno-1-dominio
feature/alumno-2-servicios
feature/alumno-3-javafx
feature/alumno-4-persistencia
feature/alumno-5-competencias-integracion
```

Cada cambio: actualizar rama → implementar → pruebas → push → Pull Request → revisión por otro alumno → merge. No modificar contratos públicos sin discusión y aprobación. Mantener PR pequeños. Registrar evidencia propia: qué clases creó/modificó, pruebas, capturas y problemas resueltos.
