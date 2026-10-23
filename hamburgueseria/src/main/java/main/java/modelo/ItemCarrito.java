package main.java.modelo;

import java.math.BigDecimal;
import java.util.Objects;

public class ItemCarrito {

    private final ArticuloMenu articulo;
    private int cantidad;

    public ItemCarrito(ArticuloMenu articulo, int cantidad) {
        this.articulo = Objects.requireNonNull(articulo);

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                "La cantidad debe ser mayor que cero"
            );
        }

        this.cantidad = cantidad;
    }

    public ArticuloMenu getArticulo() {
        return articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getSubtotal() {
        return articulo.getPrecio()
                .multiply(BigDecimal.valueOf(cantidad));
    }

    void actualizarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                "La cantidad debe ser mayor que cero"
            );
        }

        this.cantidad = cantidad;
    }
}