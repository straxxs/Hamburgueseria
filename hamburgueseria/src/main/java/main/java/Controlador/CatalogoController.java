package main.java.Controlador;

import main.java.Servicio.CatalogoService;
import main.java.modelo.Producto;
import main.java.modelo.Combo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/productos")
    public List<Producto> listarProductos() {
        return catalogoService.listarProductos();
    }

    @GetMapping("/combos")
    public List<Combo> listarCombos() {
        return catalogoService.listarCombos();
    }
}