package hamburgueseria.modelo;

import java.math.BigDecimal;
import java.util.Objects;

public class Producto extends ArticuloMenu {

    private Categoria categoria;

    public Producto(int id, String codigo, String nombre, BigDecimal precio,
                    boolean disponible, Categoria categoria) {
        super(id, codigo, nombre, precio, disponible);
        setCategoria(categoria);
    }

    @Override
    public String getTipo() {
        return "PRODUCTO";
    }

    @Override
    public String getDescripcion() {
        return getNombre() + " (" + categoria.getNombre() + ")";
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public final void setCategoria(Categoria categoria) {
        this.categoria = Objects.requireNonNull(categoria, "La categoría es obligatoria");
    }
}
