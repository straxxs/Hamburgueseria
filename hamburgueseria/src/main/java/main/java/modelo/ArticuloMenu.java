package main.java.modelo;
import java.math.BigDecimal;

public abstract class ArticuloMenu {

    private final String id;
    private final String nombre;
    private final BigDecimal precio;
    private final boolean disponible;

    public ArticuloMenu(
            String id,
            String nombre,
            BigDecimal precio,
            boolean disponible) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID obligatorio");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }

        if (precio == null || precio.signum() < 0) {
            throw new IllegalArgumentException("Precio inválido");
        }

        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = disponible;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }
}
