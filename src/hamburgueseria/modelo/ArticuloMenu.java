package hamburgueseria.modelo;

import java.math.BigDecimal;

/**
 * Clase base de todo lo que se puede vender: {@link Producto} y {@link Combo}.
 * Los atributos son privados y se validan en los setters (encapsulamiento).
 */
public abstract class ArticuloMenu {

    private int id;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private boolean disponible;
    private boolean activo = true;

    protected ArticuloMenu(int id, String codigo, String nombre,
                           BigDecimal precio, boolean disponible) {
        this.id = id;
        setCodigo(codigo);
        setNombre(nombre);
        setPrecio(precio);
        this.disponible = disponible;
    }

    /** Tipo de artículo tal como se guarda en la base: "PRODUCTO" o "COMBO". */
    public abstract String getTipo();

    /** Texto descriptivo (cada subclase lo adapta: polimorfismo). */
    public String getDescripcion() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public final void setCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        if (codigo.trim().length() > 10) {
            throw new IllegalArgumentException("El código admite hasta 10 caracteres");
        }
        this.codigo = codigo.trim().toUpperCase();
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre admite hasta 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public final void setPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.precio = precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
