package hamburgueseria.modelo;

import java.math.BigDecimal;
import java.util.Objects;

/** Línea de un pedido: conserva nombre y precio del momento de la compra. */
public class DetallePedido {

    private final String tipoArticulo;
    private final int articuloId;
    private final String nombre;
    private final BigDecimal precioUnitario;
    private final int cantidad;

    /** Crea el detalle a partir de un ítem del carrito (pedido nuevo). */
    public DetallePedido(ItemCarrito item) {
        Objects.requireNonNull(item, "El ítem es obligatorio");
        ArticuloMenu articulo = item.getArticulo();
        this.tipoArticulo = articulo.getTipo();
        this.articuloId = articulo.getId();
        this.nombre = articulo.getNombre();
        this.precioUnitario = articulo.getPrecio();
        this.cantidad = item.getCantidad();
    }

    /** Crea el detalle a partir de lo leído de la base de datos. */
    public DetallePedido(String tipoArticulo, int articuloId, String nombre,
                         BigDecimal precioUnitario, int cantidad) {
        this.tipoArticulo = tipoArticulo;
        this.articuloId = articuloId;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public String getTipoArticulo() {
        return tipoArticulo;
    }

    public int getArticuloId() {
        return articuloId;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
