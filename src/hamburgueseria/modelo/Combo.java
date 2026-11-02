package hamburgueseria.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Combo extends ArticuloMenu {

    private final List<ItemCombo> items = new ArrayList<>();

    public Combo(int id, String codigo, String nombre, BigDecimal precio, boolean disponible) {
        super(id, codigo, nombre, precio, disponible);
    }

    @Override
    public String getTipo() {
        return "COMBO";
    }

    /** Ej.: "1x Hamburguesa clásica + 1x Papas chicas + 1x Gaseosa chica". */
    @Override
    public String getDescripcion() {
        return items.stream()
                .map(i -> i.getCantidad() + "x " + i.getProducto().getNombre())
                .collect(Collectors.joining(" + "));
    }

    public List<ItemCombo> getItems() {
        return List.copyOf(items);
    }

    public void agregarItem(Producto producto, int cantidad) {
        for (ItemCombo existente : items) {
            if (existente.getProducto().getId() == producto.getId()
                    && producto.getId() != 0) {
                existente.setCantidad(existente.getCantidad() + cantidad);
                return;
            }
        }
        items.add(new ItemCombo(producto, cantidad));
    }

    public void quitarItem(ItemCombo item) {
        items.remove(item);
    }

    public void vaciarItems() {
        items.clear();
    }
}
