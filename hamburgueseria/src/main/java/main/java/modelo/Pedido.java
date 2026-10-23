package main.java.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Pedido {

    private final long numero;
    private final LocalDateTime fecha;
    private final List<DetallePedido> detalles;
    private final BigDecimal total;

    private EstadoPedido estado;

    public Pedido(long numero, Carrito carrito) {
        if (numero <= 0) {
            throw new IllegalArgumentException(
                "El número de pedido debe ser positivo"
            );
        }

        Objects.requireNonNull(carrito, "El carrito es obligatorio");

        if (carrito.estaVacio()) {
            throw new IllegalArgumentException(
                "No se puede crear un pedido vacío"
            );
        }

        this.numero = numero;
        this.fecha = LocalDateTime.now();

        // Copiamos los datos para conservar el pedido original.
        this.detalles = carrito.getItems()
                .stream()
                .map(DetallePedido::new)
                .toList();

        this.total = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.estado = EstadoPedido.PENDIENTE;
    }

    public long getNumero() {
        return numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void confirmar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                "Solo se puede confirmar un pedido pendiente"
            );
        }

        estado = EstadoPedido.CONFIRMADO;
    }

    public void cancelar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                "Solo se puede cancelar un pedido pendiente"
            );
        }

        estado = EstadoPedido.CANCELADO;
    }
}
