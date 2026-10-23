package main.java.Controlador;

import main.java.Servicio.PedidoService;
import main.java.dto.ItemPedidoRequest;
import main.java.modelo.Pedido;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Pedido> crearPedido(
            @RequestBody List<ItemPedidoRequest> articulos) {

        Pedido pedido = pedidoService.crearPedido(articulos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pedido);
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{numero}")
    public Pedido buscarPedido(@PathVariable long numero) {
        return pedidoService.buscarPedido(numero);
    }

    @PatchMapping("/{numero}/confirmar")
    public Pedido confirmarPedido(@PathVariable long numero) {
        return pedidoService.confirmarPedido(numero);
    }

    @PatchMapping("/{numero}/cancelar")
    public Pedido cancelarPedido(@PathVariable long numero) {
        return pedidoService.cancelarPedido(numero);
    }
}