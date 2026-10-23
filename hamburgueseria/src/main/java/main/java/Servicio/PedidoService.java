package main.java.Servicio;

import main.java.dto.ItemPedidoRequest;
import main.java.modelo.ArticuloMenu;
import main.java.modelo.Carrito;
import main.java.modelo.Pedido;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    private final CatalogoService catalogoService;

    private final Map<Long, Pedido> pedidos =
            new LinkedHashMap<>();

    private long siguienteNumero = 1;

    public PedidoService(CatalogoService catalogoService) {
        this.catalogoService = Objects.requireNonNull(
                catalogoService
        );
    }

    public Pedido crearPedido(
            List<ItemPedidoRequest> articulosSolicitados) {

        if (articulosSolicitados == null
                || articulosSolicitados.isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido debe tener al menos un artículo"
            );
        }

        Carrito carrito = new Carrito();

        for (ItemPedidoRequest solicitud : articulosSolicitados) {

            if (solicitud == null) {
                throw new IllegalArgumentException(
                        "Hay un artículo inválido en la solicitud"
                );
            }

            if (solicitud.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero"
                );
            }

            ArticuloMenu articulo =
                    catalogoService.buscarArticulo(
                            solicitud.getTipo(),
                            solicitud.getId()
                    );

            carrito.agregar(
                    articulo,
                    solicitud.getCantidad()
            );
        }

        // El pedido recibe una copia de los artículos y sus precios.
        Pedido pedido = new Pedido(siguienteNumero, carrito);

        pedidos.put(siguienteNumero, pedido);
        siguienteNumero++;

        return pedido;
    }

    public Pedido buscarPedido(long numero) {
        Pedido pedido = pedidos.get(numero);

        if (pedido == null) {
            throw new IllegalArgumentException(
                    "No existe el pedido número " + numero
            );
        }

        return pedido;
    }

    public List<Pedido> listarPedidos() {
        return List.copyOf(pedidos.values());
    }

    public Pedido confirmarPedido(long numero) {
        Pedido pedido = buscarPedido(numero);
        pedido.confirmar();

        return pedido;
    }

    public Pedido cancelarPedido(long numero) {
        Pedido pedido = buscarPedido(numero);
        pedido.cancelar();

        return pedido;
    }
}