package hamburgueseria.datos;

import hamburgueseria.modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDatos {

    public List<Categoria> listar() throws DatosException {
        return consultar("SELECT id_categoria, nombre, activo FROM categoria ORDER BY nombre");
    }

    /** Para llenar el ComboBox del formulario de productos. */
    public List<Categoria> listarActivas() throws DatosException {
        return consultar("SELECT id_categoria, nombre, activo FROM categoria "
                + "WHERE activo = 1 ORDER BY nombre");
    }

    private List<Categoria> consultar(String sql) throws DatosException {
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getBoolean("activo")));
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudieron listar las categorías", e);
        }
        return lista;
    }

    public void insertar(Categoria c) throws DatosException {
        String sql = "INSERT INTO categoria (nombre, activo) VALUES (?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo guardar la categoría", e);
        }
    }

    public void actualizar(Categoria c) throws DatosException {
        String sql = "UPDATE categoria SET nombre = ?, activo = ? WHERE id_categoria = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActivo());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo actualizar la categoría", e);
        }
    }

    /** Baja lógica (el trigger trg_categoria_au deshabilita sus productos). */
    public void darDeBaja(int id) throws DatosException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE categoria SET activo = 0 WHERE id_categoria = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo dar de baja la categoría", e);
        }
    }
}
