package hamburgueseria.servicio;

import hamburgueseria.datos.CategoriaDatos;
import hamburgueseria.datos.ComboDatos;
import hamburgueseria.datos.DatosException;
import hamburgueseria.datos.ProductoDatos;
import hamburgueseria.modelo.ArticuloMenu;
import hamburgueseria.modelo.Categoria;
import hamburgueseria.modelo.Combo;
import hamburgueseria.modelo.Producto;

import java.util.ArrayList;
import java.util.List;

/** Reglas de negocio del catálogo: categorías, productos y combos. */
public class CatalogoService {

    private final CategoriaDatos categoriaDatos = new CategoriaDatos();
    private final ProductoDatos productoDatos = new ProductoDatos();
    private final ComboDatos comboDatos = new ComboDatos();

    // ---------- Categorías ----------

    public List<Categoria> listarCategorias() throws DatosException {
        return categoriaDatos.listar();
    }

    public List<Categoria> listarCategoriasActivas() throws DatosException {
        return categoriaDatos.listarActivas();
    }

    public void guardarCategoria(Categoria c) throws DatosException {
        if (c.getId() == 0) {
            categoriaDatos.insertar(c);
        } else {
            categoriaDatos.actualizar(c);
        }
    }

    public void eliminarCategoria(Categoria c) throws DatosException {
        categoriaDatos.darDeBaja(c.getId());
    }

    // ---------- Productos ----------

    public List<Producto> listarProductos() throws DatosException {
        return productoDatos.listar();
    }

    public List<Producto> listarProductosDisponibles() throws DatosException {
        return productoDatos.listarDisponibles();
    }

    public void guardarProducto(Producto p) throws DatosException {
        if (!p.getCategoria().isActivo()) {
            throw new IllegalArgumentException("La categoría seleccionada está dada de baja");
        }
        if (p.getId() == 0) {
            productoDatos.insertar(p);
        } else {
            productoDatos.actualizar(p);
        }
    }

    public void eliminarProducto(Producto p) throws DatosException {
        productoDatos.darDeBaja(p.getId());
    }

    // ---------- Combos ----------

    public List<Combo> listarCombos() throws DatosException {
        return comboDatos.listar();
    }

    public void guardarCombo(Combo c) throws DatosException {
        if (c.getItems().isEmpty()) {
            throw new IllegalArgumentException("El combo debe incluir al menos un producto");
        }
        if (c.getId() == 0) {
            comboDatos.insertar(c);
        } else {
            comboDatos.actualizar(c);
        }
    }

    public void eliminarCombo(Combo c) throws DatosException {
        comboDatos.darDeBaja(c.getId());
    }

    // ---------- Para armar pedidos ----------

    /** Productos y combos vendibles, juntos (polimorfismo: todos son ArticuloMenu). */
    public List<ArticuloMenu> listarArticulosVendibles() throws DatosException {
        List<ArticuloMenu> articulos = new ArrayList<>();
        articulos.addAll(comboDatos.listarDisponibles());
        articulos.addAll(productoDatos.listarDisponibles());
        return articulos;
    }
}
