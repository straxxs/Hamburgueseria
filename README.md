# Hamburguesería - Sistema de gestión

Trabajo Práctico Final: aplicación de escritorio para administrar una hamburguesería.
Hecha con **Java + Swing** (ventanas), **JDBC** (conexión a la base) y **MariaDB / MySQL** (se usa el de XAMPP),
con programación orientada a objetos y 3 capas.

## Qué hace

| Módulo | Descripción |
|---|---|
| **Pedidos** | Se elige un cliente, se arma el pedido con productos y combos y se registra. Luego se puede confirmar o cancelar. |
| **Clientes** | ABM con validación de DNI, email y fecha de nacimiento. |
| **Productos** | ABM; la categoría se elige de un desplegable cargado desde la base. |
| **Combos** | ABM; cada combo se compone de varios productos. |
| **Categorías** | ABM (baja lógica). |

Todas las tablas de la pantalla tienen **buscador en tiempo real**.

## Estructura del proyecto

```
hamburgueseria/
├── src/hamburgueseria/
│   ├── Main.java       Punto de entrada (el que se ejecuta)
│   ├── gui/            Capa de presentación: ventanas y formularios (Swing)
│   ├── servicio/       Capa de lógica de negocio
│   ├── modelo/         Clases del dominio (herencia ArticuloMenu -> Producto / Combo)
│   └── datos/          Capa de acceso a datos: consultas SQL a la base de datos
├── lib/                Acá va el driver de MariaDB (.jar)
├── sql/script_bd.sql   Script de la base de datos
└── docs/diagramas.md   Diagrama de clases y diagrama entidad-relación
```

## Cómo ejecutarlo

### 1. Requisitos
- JDK 17 o superior
- XAMPP (con el módulo MySQL, que usa MariaDB) o cualquier MariaDB 10.2 / MySQL 8.0.16 o superior
- El driver de MariaDB (`mariadb-java-client`, un archivo `.jar`)

### 2. Crear la base de datos (XAMPP)
1. Abrir el **Panel de control de XAMPP** y hacer clic en **Start** en el módulo **MySQL**.
2. Entrar en <http://localhost/phpmyadmin>.
3. Ir a la pestaña **Importar** → *Elegir archivo* → seleccionar `sql/script_bd.sql` → **Importar** (al final de la página).

Eso crea la base `hamburgueseria`, las tablas, los procedimientos, los triggers y datos de prueba.

### 3. Poner el driver
1. Descargarlo de <https://central.sonatype.com/artifact/org.mariadb.jdbc/mariadb-java-client/overview> (el archivo `.jar`).
2. Copiar el archivo `mariadb-java-client-3.x.x.jar` a la carpeta `lib/`.
3. Agregarlo como librería del proyecto:
   - **Eclipse:** clic derecho en el proyecto → *Build Path* → *Add External Archives...* → elegir el `.jar`.
   - **NetBeans:** clic derecho en *Libraries* → *Add JAR/Folder...* → elegir el `.jar`.
   - **IntelliJ IDEA:** *File* → *Project Structure* → *Libraries* → **+** → *Java* → elegir el `.jar`.
   - **VS Code:** en la vista *Java Projects* → *Referenced Libraries* → **+** → elegir el `.jar`.

### 4. Configurar usuario y contraseña
En XAMPP el usuario es `root` y **no tiene contraseña**, y así viene ya configurado.
Si tu base tiene otra contraseña, abrir `src/hamburgueseria/datos/Conexion.java` y cambiar `USUARIO` y `CLAVE`.

### 5. Ejecutar
Ejecutar la clase `Main` (`src/hamburgueseria/Main.java`) con el botón *Run* del programa.

### Sin programa (por consola)
Parado en la carpeta del proyecto, con el `.jar` ya copiado en `lib/`:

```bash
# Windows
javac -encoding UTF-8 -cp "lib/*" -d bin src/hamburgueseria/*.java src/hamburgueseria/*/*.java
java -cp "bin;lib/*" hamburgueseria.Main

# Mac / Linux
javac -encoding UTF-8 -cp "lib/*" -d bin src/hamburgueseria/*.java src/hamburgueseria/*/*.java
java -cp "bin:lib/*" hamburgueseria.Main
```

## Detalles de diseño

- **Herencia y polimorfismo:** `ArticuloMenu` (abstracta) → `Producto` y `Combo`.
- **Encapsulamiento:** atributos privados con getters/setters que validan.
- **La pantalla nunca lee datos crudos de la base:** las clases de `datos` convierten cada fila en objetos (`Producto`, `Cliente`...) y recién ahí llegan a la pantalla.
- **Excepciones:** `DatosException` para errores de base de datos y `try-catch` para mostrar mensajes sin cerrar la aplicación.
- **Base de datos:** 7 tablas relacionadas, 3 procedimientos almacenados, 4 triggers y transacciones (ver `docs/diagramas.md`).

## Entregables

- `sql/script_bd.sql`: base, tablas, restricciones, procedimientos, triggers y datos de prueba.
- `docs/diagramas.md`: diagrama de clases (UML) y diagrama entidad-relación (DER).
- Código fuente en `src/`.
