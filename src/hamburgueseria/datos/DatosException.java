package hamburgueseria.datos;

import java.sql.SQLException;

/** Error de la capa de datos con un mensaje apto para mostrar al usuario. */
public class DatosException extends Exception {

    public DatosException(String mensaje) {
        super(mensaje);
    }

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Traduce los errores SQL más comunes a mensajes entendibles. */
    public static DatosException desde(String contexto, SQLException e) {
        String detalle;
        switch (e.getErrorCode()) {
            case 1062:
                detalle = "Ya existe un registro con ese valor único (código, DNI o nombre).";
                break;
            case 1451:
                detalle = "No se puede eliminar: hay registros que dependen de este.";
                break;
            case 1452:
                detalle = "La referencia indicada no existe.";
                break;
            default:
                // 45000 = SIGNAL de un trigger o stored procedure con regla de negocio
                detalle = "45000".equals(e.getSQLState())
                        ? e.getMessage()
                        : "Error de base de datos: " + e.getMessage();
        }
        return new DatosException(contexto + ": " + detalle, e);
    }
}
