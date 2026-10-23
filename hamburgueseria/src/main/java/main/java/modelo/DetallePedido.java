package main.java.modelo;

import java.math.BigDecimal;
import java.util.Objects;

public class DetallePedido {

    private final String articuloId;
    private final String tipoArticulo;
    private final String nombre;
    private final BigDecimal precioUnitario;
    private final int cantidad;

    public DetallePedido(ItemCarrito item) {
        Objects.requireNonNull(item, "El artículo es obligatorio");

        ArticuloMenu articulo = item.getArticulo();

        this.articuloId = articulo.getId();
        this.tipoArticulo = articulo.getClass().getSimpleName();
        this.nombre = articulo.getNombre();
        this.precioUnitario = articulo.getPrecio();
        this.cantidad = item.getCantidad();
    }

    public String getArticuloId() {
        return articuloId;
    }

    public String getTipoArticulo() {
        return tipoArticulo;
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
        return precioUnitario.multiply(
            BigDecimal.valueOf(cantidad)
        );
    }
}
