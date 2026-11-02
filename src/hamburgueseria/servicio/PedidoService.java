package hamburgueseria.servicio;

import hamburgueseria.datos.DatosException;
import hamburgueseria.datos.PedidoDatos;
import hamburgueseria.modelo.Carrito;
import hamburgueseria.modelo.Cliente;
import hamburgueseria.modelo.EstadoPedido;
import hamburgueseria.modelo.Pedido;

import java.util.List;

public class PedidoService {

    private final PedidoDatos pedidoDatos = new PedidoDatos();

    /** Valida y registra el pedido (la transacción la maneja la capa de datos). */
    public Pedido registrarPedido(Cliente cliente, Carrito carrito) throws DatosException {
        Pedido pedido = new Pedido(cliente, carrito);   // valida cliente y carrito
        pedidoDatos.registrar(pedido);
        return pedido;
    }

    public List<Pedido> listar() throws DatosException {
        return pedidoDatos.listar();
    }

    public void confirmar(Pedido pedido) throws DatosException {
        pedido.confirmar();                              // valida la transición en el dominio
        pedidoDatos.cambiarEstado(pedido.getId(), EstadoPedido.CONFIRMADO);
    }

    public void cancelar(Pedido pedido) throws DatosException {
        pedido.cancelar();
        pedidoDatos.cambiarEstado(pedido.getId(), EstadoPedido.CANCELADO);
    }
}
