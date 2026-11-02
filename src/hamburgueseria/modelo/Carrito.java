package hamburgueseria.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Carrito en memoria: se arma en pantalla y recién al registrar se persiste. */
public class Carrito {

    private final Map<String, ItemCarrito> items = new LinkedHashMap<>();

    public void agregar(ArticuloMenu articulo, int cantidad) {
        if (articulo == null || !articulo.isDisponible() || !articulo.isActivo()) {
            throw new IllegalArgumentException("El artículo no está disponible");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        String clave = generarClave(articulo);
        ItemCarrito item = items.get(clave);

        if (item == null) {
            items.put(clave, new ItemCarrito(articulo, cantidad));
        } else {
            item.actualizarCantidad(Math.addExact(item.getCantidad(), cantidad));
        }
    }

    public void quitar(ArticuloMenu articulo) {
        items.remove(generarClave(articulo));
    }

    public Collection<ItemCarrito> getItems() {
        return new ArrayList<>(items.values());
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : items.values()) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public void vaciar() {
        items.clear();
    }

    private String generarClave(ArticuloMenu articulo) {
        return articulo.getTipo() + ":" + articulo.getId();
    }
}
