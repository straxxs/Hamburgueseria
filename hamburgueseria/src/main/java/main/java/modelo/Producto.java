package main.java.modelo;

import java.math.BigDecimal;

public class Producto extends ArticuloMenu {

    public Producto(
            String id,
            String nombre,
            BigDecimal precio,
            boolean disponible) {

        super(id, nombre, precio, disponible);
    }
}