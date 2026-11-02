package hamburgueseria.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Única clase que sabe cómo conectarse a la base de datos (MariaDB de XAMPP).
 * Si tu usuario o contraseña son otros, cambialos acá abajo.
 */
public final class Conexion {

    // ====== CAMBIAR ACÁ SI HACE FALTA ======
    private static final String SERVIDOR = "localhost";
    private static final String PUERTO = "3306";
    private static final String BASE_DE_DATOS = "hamburgueseria";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";   // XAMPP viene sin contraseña
    // =======================================

    private static final String URL = "jdbc:mariadb://" + SERVIDOR + ":" + PUERTO + "/"
            + BASE_DE_DATOS;

    private Conexion() {
    }

    public static Connection obtener() throws DatosException {
        try {
            return DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (SQLException e) {
            throw new DatosException("No se pudo conectar a la base de datos ("
                    + e.getMessage() + "). Revisá que MySQL esté iniciado en XAMPP, "
                    + "que hayas importado script_bd.sql y que el usuario y la "
                    + "contraseña de la clase Conexion sean los tuyos.", e);
        }
    }
}
