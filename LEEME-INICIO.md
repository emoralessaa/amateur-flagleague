# FlagLeague Manager — Base para comenzar a programar

## ¡ATENCIÓN! Cómo colocar estos archivos

Ya existe el repositorio **`amateur-flagleague`** y dentro está **`flagleague_docs/`** con los documentos 01–08.

**Extrae el contenido de este ZIP directamente dentro de `amateur-flagleague/`.** No crees una nueva carpeta `amateur-flagleague` dentro de la existente y no reemplaces ni borres `flagleague_docs/`.

La raíz del proyecto deberá contener **`pom.xml`**, **`src/`**, **`.gitignore`**, **`.github/`** y **`flagleague_docs/`** al mismo nivel.

## Requisitos en Windows

1. Instala **JDK 21** y verifica en una terminal con `java -version`.
2. Instala **Apache Maven 3.9 o superior** y verifica con `mvn -version` (debe mostrar Java 21).
3. En Visual Studio Code instala **Extension Pack for Java** (Microsoft). Este paquete incluye soporte para Maven.
4. Abre desde VS Code la **carpeta raíz `amateur-flagleague`**, no solamente `flagleague_docs/`.

Maven gestionará automáticamente los módulos JavaFX (`controls` y `fxml`) especificados en `pom.xml`; no hace falta descargar por separado el SDK JavaFX para esta configuración. La primera compilación requiere conexión a internet para resolver dependencias.

## Primeras pruebas (terminal en la raíz del repositorio)

```powershell
java -version
mvn -version
mvn clean test
mvn javafx:run
```

- `mvn clean test` debe ejecutar **2 pruebas de humo** que verifican los recursos del proyecto.
- `mvn javafx:run` debe abrir una ventana titulada **FlagLeague Manager — Base JavaFX** con mensajes de bienvenida.
- Este primer paso **no implementa aún** equipos, calendarios, resultados ni clasificaciones.

Si `mvn` no se reconoce, Maven no está instalado o no está en `PATH`. Si `java -version` no muestra la versión 21, revisa `JAVA_HOME` y la versión de JDK que está usando Maven.

## Estructura

```text
amateur-flagleague/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/flagleague/
│   │   │   ├── MainApp.java             ← pantalla temporal de arranque
│   │   │   ├── bootstrap/              ← Parra
│   │   │   ├── domain/                 ← Yan; interfaces acordadas con Carlos
│   │   │   ├── application/
│   │   │   │   ├── service/            ← Paul
│   │   │   │   └── competition/        ← Parra
│   │   │   ├── infrastructure/         ← Carlos
│   │   │   └── presentation/           ← Emilio
│   │   └── resources/
│   │       ├── fxml/dashboard.fxml     ← pantalla provisional; Emilio la sustituye
│   │       └── css/flagleague.css
│   └── test/java/com/flagleague/
├── .github/workflows/              ← Parra implementará CI
├── .gitignore
├── .vscode/extensions.json
└── flagleague_docs/                ← SE CONSERVA; documentos 01–08
```

Los archivos `.gitkeep` sirven para que Git incluya carpetas todavía vacías. No son clases Java.

## Responsabilidades y colaboración

| Persona | Módulo | Rama acordada |
|---|---|---|
| Yan | Entidades, enumeraciones y marcadores | `feature/yan-dominio` |
| Paul | Servicios, inscripción, calendario y partidos | `feature/paul-servicios` |
| Emilio | Seis vistas JavaFX, controladores y navegación | `feature/emilio-javafx` |
| Carlos | Implementaciones Repository, datos demo | `feature/carlos-persistencia` |
| Parra | Estrategias, posiciones, playoffs, bootstrap y CI | `feature/parra-competencias-integracion` |

**Este starter intencionalmente no implementa los modelos, repositorios ni servicios.** Los cinco deben programarlos y probarlos según sus guías. `MainApp` y la vista inicial son provisionales para verificar que JavaFX/FXML arrancan; Emilio y Parra coordinarán su reemplazo.

## Acuerdos del primer día (obligatorios antes de desarrollar en paralelo)

El documento `flagleague_docs/docs/03-contratos-integracion.md` señala que sus firmas son un borrador. Deben aprobar y dejar por escrito, al menos:

1. Constructores y getters del modelo que implementa Yan; identidad y mutabilidad de entidades.
2. Quién guarda los eventos de `Anotacion` y cómo se recuperan por partido, incluido su UUID para conversiones.
3. Cómo se expresa un desempate pendiente de clasificación y cómo se resuelve.
4. Cómo obtendrán los servicios la fecha inicial del torneo para validar edades.
5. El comportamiento de `Repository.guardar`, las excepciones y el bloqueo de eliminaciones con historial.
6. La manera en que Parra entrega dependencias a los controladores de Emilio.

Después de acordarlos, actualizar `03-contratos-integracion.md` y abrir Pull Requests para cualquier cambio de firma.

## Subir a GitHub sin perder la documentación

Revisa en VS Code la rama actual. Crea una rama de configuración si tu repositorio ya tiene `main` protegido:

```powershell
git switch -c setup/base-maven-javafx
git add pom.xml src .gitignore .editorconfig .vscode .github LEEME-INICIO.md
git commit -m "Configurar proyecto base Java 21 Maven JavaFX"
git push -u origin setup/base-maven-javafx
```

Luego crea un Pull Request hacia `main` desde GitHub. Si tu equipo ya usa otra rama de integración, ajústalo antes de continuar. No hagas `force push`.

## Seguimiento académico

**Día 1:** instalar herramientas, abrir ventana y pasar pruebas de humo; aprobar contratos. **Día 2:** agregar primeras clases/servicios e integrar sin romper compilación. Los módulos deportivos no vienen resueltos.
