package hamburgueseria.datos;

import hamburgueseria.modelo.Combo;
import hamburgueseria.modelo.ItemCombo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComboDatos {

    public List<Combo> listar() throws DatosException {
        return consultar(false);
    }

    /** Combos que se pueden vender. */
    public List<Combo> listarDisponibles() throws DatosException {
        return consultar(true);
    }

    private List<Combo> consultar(boolean soloDisponibles) throws DatosException {
        String filtro = soloDisponibles ? " WHERE activo = 1 AND disponible = 1" : "";
        String sqlCombos = "SELECT id_combo, codigo, nombre, precio, disponible, activo "
                + "FROM combo" + filtro + " ORDER BY codigo";
        String sqlItems = "SELECT cp.id_combo, cp.cantidad, " + ProductoDatos.COLUMNAS + " "
                + "FROM combo_producto cp "
                + "JOIN producto p ON p.id_producto = cp.id_producto "
                + "JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "ORDER BY cp.id_combo, p.codigo";

        Map<Integer, Combo> combos = new LinkedHashMap<>();

        try (Connection cn = Conexion.obtener()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlCombos);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Combo combo = new Combo(
                            rs.getInt("id_combo"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getBigDecimal("precio"),
                            rs.getBoolean("disponible"));
                    combo.setActivo(rs.getBoolean("activo"));
                    combos.put(combo.getId(), combo);
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlItems);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Combo combo = combos.get(rs.getInt("id_combo"));
                    if (combo != null) {
                        combo.agregarItem(ProductoDatos.mapear(rs), rs.getInt("cantidad"));
                    }
                }
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudieron listar los combos", e);
        }
        return new ArrayList<>(combos.values());
    }

    /** Inserta el combo y sus productos en una única transacción. */
    public void insertar(Combo combo) throws DatosException {
        String sql = "INSERT INTO combo (codigo, nombre, precio, disponible, activo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, combo.getCodigo());
                    ps.setString(2, combo.getNombre());
                    ps.setBigDecimal(3, combo.getPrecio());
                    ps.setBoolean(4, combo.isDisponible());
                    ps.setBoolean(5, combo.isActivo());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            combo.setId(keys.getInt(1));
                        }
                    }
                }
                insertarItems(cn, combo);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                combo.setId(0);
                throw e;
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo guardar el combo", e);
        }
    }

    /** Actualiza el combo y reemplaza su composición en una única transacción. */
    public void actualizar(Combo combo) throws DatosException {
        String sql = "UPDATE combo SET codigo = ?, nombre = ?, precio = ?, "
                + "disponible = ?, activo = ? WHERE id_combo = ?";

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, combo.getCodigo());
                    ps.setString(2, combo.getNombre());
                    ps.setBigDecimal(3, combo.getPrecio());
                    ps.setBoolean(4, combo.isDisponible());
                    ps.setBoolean(5, combo.isActivo());
                    ps.setInt(6, combo.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "DELETE FROM combo_producto WHERE id_combo = ?")) {
                    ps.setInt(1, combo.getId());
                    ps.executeUpdate();
                }
                insertarItems(cn, combo);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo actualizar el combo", e);
        }
    }

    private void insertarItems(Connection cn, Combo combo) throws SQLException {
        String sql = "INSERT INTO combo_producto (id_combo, id_producto, cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (ItemCombo item : combo.getItems()) {
                ps.setInt(1, combo.getId());
                ps.setInt(2, item.getProducto().getId());
                ps.setInt(3, item.getCantidad());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void darDeBaja(int id) throws DatosException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE combo SET activo = 0, disponible = 0 WHERE id_combo = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo dar de baja el combo", e);
        }
    }
}
