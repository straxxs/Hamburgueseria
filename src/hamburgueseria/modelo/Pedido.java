package hamburgueseria.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {

    private int id;
    private final Cliente cliente;
    private final LocalDateTime fecha;
    private EstadoPedido estado;
    private BigDecimal total;
    private final List<DetallePedido> detalles = new ArrayList<>();

    /** Pedido nuevo: copia los datos del carrito (aún sin id). */
    public Pedido(Cliente cliente, Carrito carrito) {
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
        Objects.requireNonNull(carrito, "El carrito es obligatorio");

        if (carrito.estaVacio()) {
            throw new IllegalArgumentException("No se puede crear un pedido vacío");
        }
        if (!cliente.isActivo()) {
            throw new IllegalArgumentException("El cliente está dado de baja");
        }

        for (ItemCarrito item : carrito.getItems()) {
            detalles.add(new DetallePedido(item));
        }
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
        this.total = carrito.getTotal();
    }

    /** Pedido leído de la base de datos. */
    public Pedido(int id, Cliente cliente, LocalDateTime fecha,
                  EstadoPedido estado, BigDecimal total) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<DetallePedido> getDetalles() {
        return List.copyOf(detalles);
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
    }

    public void confirmar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar un pedido pendiente");
        }
        estado = EstadoPedido.CONFIRMADO;
    }

    public void cancelar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede cancelar un pedido pendiente");
        }
        estado = EstadoPedido.CANCELADO;
    }
}
