package hamburgueseria.datos;

import hamburgueseria.modelo.Categoria;
import hamburgueseria.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDatos {

    /** Columnas que arma un Producto (con su categoría); la reutiliza ComboDatos. */
    static final String COLUMNAS =
            "p.id_producto, p.codigo, p.nombre, p.precio, p.disponible, p.activo, "
            + "c.id_categoria, c.nombre AS categoria_nombre, c.activo AS categoria_activo";

    static final String FROM =
            "FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria";

    /** Mapeo manual fila -> objeto de dominio. */
    static Producto mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getInt("id_categoria"),
                rs.getString("categoria_nombre"),
                rs.getBoolean("categoria_activo"));

        Producto producto = new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getBoolean("disponible"),
                categoria);
        producto.setActivo(rs.getBoolean("activo"));
        return producto;
    }

    public List<Producto> listar() throws DatosException {
        return consultar("SELECT " + COLUMNAS + " " + FROM + " ORDER BY p.codigo");
    }

    /** Productos que se pueden usar en combos y pedidos. */
    public List<Producto> listarDisponibles() throws DatosException {
        return consultar("SELECT " + COLUMNAS + " " + FROM
                + " WHERE p.activo = 1 AND p.disponible = 1 ORDER BY p.codigo");
    }

    private List<Producto> consultar(String sql) throws DatosException {
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudieron listar los productos", e);
        }
        return lista;
    }

    public void insertar(Producto p) throws DatosException {
        String sql = "INSERT INTO producto "
                + "(codigo, nombre, precio, disponible, activo, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setBoolean(4, p.isDisponible());
            ps.setBoolean(5, p.isActivo());
            ps.setInt(6, p.getCategoria().getId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo guardar el producto", e);
        }
    }

    public void actualizar(Producto p) throws DatosException {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, precio = ?, "
                + "disponible = ?, activo = ?, id_categoria = ? WHERE id_producto = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setBoolean(4, p.isDisponible());
            ps.setBoolean(5, p.isActivo());
            ps.setInt(6, p.getCategoria().getId());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo actualizar el producto", e);
        }
    }

    /** Baja lógica: se conserva para no romper pedidos y combos históricos. */
    public void darDeBaja(int id) throws DatosException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE producto SET activo = 0, disponible = 0 WHERE id_producto = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo dar de baja el producto", e);
        }
    }
}
