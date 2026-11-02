package hamburgueseria.datos;

import hamburgueseria.modelo.Cliente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDatos {

    /** Columnas que arman un Cliente; la reutiliza PedidoDatos. */
    static final String COLUMNAS =
            "cl.id_cliente, cl.dni, cl.nombre, cl.apellido, cl.telefono, "
            + "cl.email, cl.fecha_nacimiento, cl.activo";

    /** Mapeo manual fila -> objeto de dominio. */
    static Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("dni"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getDate("fecha_nacimiento").toLocalDate(),
                rs.getBoolean("activo"));
    }

    public List<Cliente> listar() throws DatosException {
        return consultar("SELECT " + COLUMNAS + " FROM cliente cl ORDER BY cl.apellido, cl.nombre");
    }

    public List<Cliente> listarActivos() throws DatosException {
        return consultar("SELECT " + COLUMNAS + " FROM cliente cl "
                + "WHERE cl.activo = 1 ORDER BY cl.apellido, cl.nombre");
    }

    private List<Cliente> consultar(String sql) throws DatosException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudieron listar los clientes", e);
        }
        return lista;
    }

    public void insertar(Cliente c) throws DatosException {
        String sql = "INSERT INTO cliente "
                + "(dni, nombre, apellido, telefono, email, fecha_nacimiento, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            cargarParametros(ps, c);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo guardar el cliente", e);
        }
    }

    public void actualizar(Cliente c) throws DatosException {
        String sql = "UPDATE cliente SET dni = ?, nombre = ?, apellido = ?, telefono = ?, "
                + "email = ?, fecha_nacimiento = ?, activo = ? WHERE id_cliente = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            cargarParametros(ps, c);
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo actualizar el cliente", e);
        }
    }

    private void cargarParametros(PreparedStatement ps, Cliente c) throws SQLException {
        ps.setString(1, c.getDni());
        ps.setString(2, c.getNombre());
        ps.setString(3, c.getApellido());
        ps.setString(4, c.getTelefono());
        ps.setString(5, c.getEmail());
        ps.setDate(6, Date.valueOf(c.getFechaNacimiento()));
        ps.setBoolean(7, c.isActivo());
    }

    /** Baja lógica: conserva el historial de pedidos del cliente. */
    public void darDeBaja(int id) throws DatosException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE cliente SET activo = 0 WHERE id_cliente = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo dar de baja el cliente", e);
        }
    }
}
