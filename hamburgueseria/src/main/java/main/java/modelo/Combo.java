package main.java.modelo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class Combo extends ArticuloMenu {

    private final List<Producto> productos;

    public Combo(
            String id,
            String nombre,
            BigDecimal precio,
            boolean disponible,
            List<Producto> productos) {

        super(id, nombre, precio, disponible);

        this.productos = List.copyOf(
            Objects.requireNonNull(productos)
        );
    }

    public List<Producto> getProductos() {
        return productos;
    }
}