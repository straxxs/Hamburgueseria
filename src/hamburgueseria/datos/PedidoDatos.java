package hamburgueseria.datos;

import hamburgueseria.modelo.DetallePedido;
import hamburgueseria.modelo.EstadoPedido;
import hamburgueseria.modelo.Pedido;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoDatos {

    /**
     * Registra el pedido completo (cabecera + líneas) en UNA transacción,
     * usando los stored procedures. Si algo falla, no queda nada guardado.
     */
    public void registrar(Pedido pedido) throws DatosException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idPedido;
                try (CallableStatement cs = cn.prepareCall("{CALL sp_crear_pedido(?, ?)}")) {
                    cs.setInt(1, pedido.getCliente().getId());
                    cs.registerOutParameter(2, Types.INTEGER);
                    cs.execute();
                    idPedido = cs.getInt(2);
                }

                try (CallableStatement cs = cn.prepareCall("{CALL sp_agregar_detalle(?, ?, ?, ?)}")) {
                    for (DetallePedido d : pedido.getDetalles()) {
                        cs.setInt(1, idPedido);
                        cs.setString(2, d.getTipoArticulo());
                        cs.setInt(3, d.getArticuloId());
                        cs.setInt(4, d.getCantidad());
                        cs.execute();
                    }
                }

                cn.commit();
                pedido.setId(idPedido);
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo registrar el pedido", e);
        }
    }

    public void cambiarEstado(int idPedido, EstadoPedido nuevoEstado) throws DatosException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try (CallableStatement cs = cn.prepareCall("{CALL sp_cambiar_estado_pedido(?, ?)}")) {
                cs.setInt(1, idPedido);
                cs.setString(2, nuevoEstado.name());
                cs.execute();
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudo cambiar el estado del pedido", e);
        }
    }

    /** Lista todos los pedidos (del más nuevo al más viejo) con sus detalles. */
    public List<Pedido> listar() throws DatosException {
        String sqlPedidos = "SELECT p.id_pedido, p.fecha, p.estado, p.total, "
                + ClienteDatos.COLUMNAS + " "
                + "FROM pedido p JOIN cliente cl ON cl.id_cliente = p.id_cliente "
                + "ORDER BY p.id_pedido DESC";
        String sqlDetalles = "SELECT id_pedido, tipo_articulo, "
                + "COALESCE(id_producto, id_combo) AS id_articulo, "
                + "nombre_articulo, precio_unitario, cantidad "
                + "FROM detalle_pedido ORDER BY id_pedido, id_detalle";

        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();

        try (Connection cn = Conexion.obtener()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlPedidos);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido pedido = new Pedido(
                            rs.getInt("id_pedido"),
                            ClienteDatos.mapear(rs),
                            rs.getTimestamp("fecha").toLocalDateTime(),
                            EstadoPedido.valueOf(rs.getString("estado")),
                            rs.getBigDecimal("total"));
                    pedidos.put(pedido.getId(), pedido);
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlDetalles);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido pedido = pedidos.get(rs.getInt("id_pedido"));
                    if (pedido != null) {
                        pedido.agregarDetalle(new DetallePedido(
                                rs.getString("tipo_articulo"),
                                rs.getInt("id_articulo"),
                                rs.getString("nombre_articulo"),
                                rs.getBigDecimal("precio_unitario"),
                                rs.getInt("cantidad")));
                    }
                }
            }
        } catch (SQLException e) {
            throw DatosException.desde("No se pudieron listar los pedidos", e);
        }
        return new ArrayList<>(pedidos.values());
    }
}
