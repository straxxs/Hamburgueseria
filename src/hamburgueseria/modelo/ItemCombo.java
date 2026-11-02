package hamburgueseria.modelo;

import java.util.Objects;

/** Un producto incluido en un combo, con su cantidad (relación N a M). */
public class ItemCombo {

    private final Producto producto;
    private int cantidad;

    public ItemCombo(Producto producto, int cantidad) {
        this.producto = Objects.requireNonNull(producto, "El producto es obligatorio");
        setCantidad(cantidad);
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public final void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }
}
