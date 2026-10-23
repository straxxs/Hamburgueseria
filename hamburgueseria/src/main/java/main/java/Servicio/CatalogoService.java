package main.java.Servicio;

import main.java.modelo.ArticuloMenu;
import main.java.modelo.Combo;
import main.java.modelo.Producto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CatalogoService {

    private final Map<String, Producto> productos =
            new LinkedHashMap<>();

    private final Map<String, Combo> combos =
            new LinkedHashMap<>();

    public CatalogoService() {
        cargarProductos();
        cargarCombos();
    }

    private void cargarProductos() {
        productos.put("H001", new Producto(
                "H001", "Hamburguesa clásica",
                new BigDecimal("5000"), true
        ));

        productos.put("H002", new Producto(
                "H002", "Hamburguesa doble",
                new BigDecimal("7500"), true
        ));
        
        productos.put("H003", new Producto(
                "H003", "Hamburguesa triple",
                new BigDecimal("7500"), true
        ));

        productos.put("P001", new Producto(
                "P001", "Papas chicas",
                new BigDecimal("2000"), true
        ));

        productos.put("P002", new Producto(
                "P002", "Papas medianas",
                new BigDecimal("2500"), true
        ));
        
        productos.put("P003", new Producto(
                "P003", "Papas grandes",
                new BigDecimal("3000"), true
        ));
        
        productos.put("B001", new Producto(
                "B001", "Agua",
                new BigDecimal("1800"), true
        ));
        
        productos.put("B002", new Producto(
                "B002", "Gaseosa chica",
                new BigDecimal("2200"), true
        ));
        
        productos.put("B003", new Producto(
                "B003", "Gaseosa mediana",
                new BigDecimal("2500"), true
        ));
        
        productos.put("B004", new Producto(
                "B004", "Gaseosa grande",
                new BigDecimal("2800"), true
        ));
    }

    private void cargarCombos() {
        combos.put("C001", new Combo(
                "C001",
                "Combo hamburguesa simple chico",
                new BigDecimal("8500"),
                true,
                List.of(
                        productos.get("H001"),
                        productos.get("P001"),
                        productos.get("B002")
                )
        ));

        combos.put("C002", new Combo(
                "C002",
                "Combo hamburguesa simple mediano",
                new BigDecimal("9500"),
                true,
                List.of(
                        productos.get("H001"),
                        productos.get("P002"),
                        productos.get("B003")
                )
        ));
        
        combos.put("C003", new Combo(
                "C003",
                "Combo hamburguesa simple grande",
                new BigDecimal("11000"),
                true,
                List.of(
                        productos.get("H001"),
                        productos.get("P003"),
                        productos.get("B004")
                )
        ));
    }

    public List<Producto> listarProductos() {
        return List.copyOf(productos.values());
    }

    public List<Combo> listarCombos() {
        return List.copyOf(combos.values());
    }

    public Producto buscarProducto(String id) {
        Producto producto = productos.get(id);

        if (producto == null) {
            throw new IllegalArgumentException(
                    "No existe el producto: " + id
            );
        }

        return producto;
    }

    public Combo buscarCombo(String id) {
        Combo combo = combos.get(id);

        if (combo == null) {
            throw new IllegalArgumentException(
                    "No existe el combo: " + id
            );
        }

        return combo;
    }

    public ArticuloMenu buscarArticulo(
            String tipo, String id) {

        if (tipo == null || id == null) {
            throw new IllegalArgumentException(
                    "El tipo y el ID son obligatorios"
            );
        }

        return switch (tipo.toUpperCase(Locale.ROOT)) {
            case "PRODUCTO" -> buscarProducto(id);
            case "COMBO" -> buscarCombo(id);
            default -> throw new IllegalArgumentException(
                    "Tipo de artículo inválido: " + tipo
            );
        };
    }
}
