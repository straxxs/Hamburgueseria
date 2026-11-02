package hamburgueseria.gui;

import hamburgueseria.modelo.ArticuloMenu;
import hamburgueseria.modelo.Carrito;
import hamburgueseria.modelo.Cliente;
import hamburgueseria.modelo.DetallePedido;
import hamburgueseria.modelo.ItemCarrito;
import hamburgueseria.modelo.Pedido;
import hamburgueseria.servicio.CatalogoService;
import hamburgueseria.servicio.ClienteService;
import hamburgueseria.servicio.PedidoService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Módulo transaccional: relaciona Cliente + Artículos y genera un Pedido.
 * Arriba se arma el pedido (carrito); abajo se ve el historial.
 */
public class PanelPedidos extends JPanel implements Refrescable {

    private final CatalogoService catalogo = new CatalogoService();
    private final ClienteService clientes = new ClienteService();
    private final PedidoService pedidos = new PedidoService();

    private final Carrito carrito = new Carrito();

    // --- Nuevo pedido ---
    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JComboBox<ArticuloMenu> cmbArticulo = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final TablaModelo<ItemCarrito> modeloCarrito = new TablaModelo<ItemCarrito>(
            new String[]{"Artículo", "Tipo", "Precio", "Cantidad", "Subtotal"},
            i -> i.getArticulo().getNombre(), i -> i.getArticulo().getTipo(),
            i -> i.getArticulo().getPrecio(), i -> i.getCantidad(), i -> i.getSubtotal());
    private final JTable tablaCarrito = UI.crearTabla(modeloCarrito);
    private final JLabel lblTotal = new JLabel("Total: " + UI.moneda(java.math.BigDecimal.ZERO));

    // --- Historial ---
    private final TablaModelo<Pedido> modeloPedidos = new TablaModelo<Pedido>(
            new String[]{"Nº", "Fecha", "Cliente", "Estado", "Total"},
            p -> p.getId(), p -> p.getFecha(), p -> p.getCliente().getNombreCompleto(),
            p -> p.getEstado(), p -> p.getTotal());
    private final JTable tablaPedidos = UI.crearTabla(modeloPedidos);
    private final TablaModelo<DetallePedido> modeloDetalle = new TablaModelo<DetallePedido>(
            new String[]{"Artículo", "Precio", "Cant.", "Subtotal"},
            d -> d.getNombre(), d -> d.getPrecioUnitario(), d -> d.getCantidad(),
            d -> d.getSubtotal());
    private final JTable tablaDetalle = UI.crearTabla(modeloDetalle);
    private final JTextField txtBuscar = new JTextField(20);

    public PanelPedidos() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cmbArticulo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof ArticuloMenu a) {
                    String prefijo = "COMBO".equals(a.getTipo()) ? "[Combo] " : "";
                    setText(prefijo + a.getNombre() + "  -  " + UI.moneda(a.getPrecio()));
                }
                return this;
            }
        });

        JSplitPane divisor = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                crearPanelNuevoPedido(), crearPanelHistorial());
        divisor.setResizeWeight(0.5);
        divisor.setBorder(null);
        add(divisor, BorderLayout.CENTER);

        UI.conectarBuscador(txtBuscar, tablaPedidos);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });
    }

    private JPanel crearPanelNuevoPedido() {
        JButton btnAgregar = new JButton("Agregar al pedido");
        btnAgregar.addActionListener(e -> agregarAlCarrito());

        JPanel filaCliente = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaCliente.add(new JLabel("Cliente:"));
        cmbCliente.setPreferredSize(new Dimension(260, 26));
        filaCliente.add(cmbCliente);

        JPanel filaArticulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaArticulo.add(new JLabel("Artículo:"));
        cmbArticulo.setPreferredSize(new Dimension(320, 26));
        filaArticulo.add(cmbArticulo);
        filaArticulo.add(new JLabel("Cant.:"));
        filaArticulo.add(spnCantidad);
        filaArticulo.add(btnAgregar);

        JPanel seleccion = new JPanel();
        seleccion.setLayout(new BoxLayout(seleccion, BoxLayout.Y_AXIS));
        seleccion.add(filaCliente);
        seleccion.add(filaArticulo);

        JButton btnQuitar = new JButton("Quitar");
        JButton btnVaciar = new JButton("Vaciar");
        JButton btnRegistrar = new JButton("Registrar pedido");
        btnQuitar.addActionListener(e -> quitarDelCarrito());
        btnVaciar.addActionListener(e -> vaciarCarrito());
        btnRegistrar.addActionListener(e -> registrarPedido());
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));

        JPanel acciones = new JPanel(new BorderLayout());
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        izquierda.add(btnQuitar);
        izquierda.add(btnVaciar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        derecha.add(lblTotal);
        derecha.add(btnRegistrar);
        acciones.add(izquierda, BorderLayout.WEST);
        acciones.add(derecha, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo pedido"));
        panel.add(seleccion, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelHistorial() {
        JButton btnConfirmar = new JButton("Confirmar pedido");
        JButton btnCancelar = new JButton("Cancelar pedido");
        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> cancelar());

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        superior.add(new JLabel("Buscar (nº, cliente, estado...):"));
        superior.add(txtBuscar);
        superior.add(btnConfirmar);
        superior.add(btnCancelar);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del pedido seleccionado"));
        scrollDetalle.setPreferredSize(new Dimension(380, 100));

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Historial de pedidos"));
        panel.add(superior, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        panel.add(scrollDetalle, BorderLayout.EAST);
        return panel;
    }

    @Override
    public void refrescar() {
        UI.ejecutar(this, () -> {
            cmbCliente.removeAllItems();
            for (Cliente c : clientes.listarActivos()) {
                cmbCliente.addItem(c);
            }
            cmbArticulo.removeAllItems();
            for (ArticuloMenu a : catalogo.listarArticulosVendibles()) {
                cmbArticulo.addItem(a);
            }
            cargarHistorial();
        });
        actualizarCarrito();
    }

    private void cargarHistorial() throws Exception {
        List<Pedido> lista = pedidos.listar();
        modeloPedidos.setDatos(lista);
        modeloDetalle.setDatos(List.of());
    }

    // ---------------- Carrito ----------------

    private void agregarAlCarrito() {
        ArticuloMenu articulo = (ArticuloMenu) cmbArticulo.getSelectedItem();
        if (articulo == null) {
            UI.error(this, "Elegí un artículo");
            return;
        }
        UI.ejecutar(this, () -> {
            carrito.agregar(articulo, (Integer) spnCantidad.getValue());
            spnCantidad.setValue(1);
            actualizarCarrito();
        });
    }

    private void quitarDelCarrito() {
        ItemCarrito item = UI.seleccionado(tablaCarrito, modeloCarrito);
        if (item == null) {
            UI.error(this, "Seleccioná una línea del pedido");
            return;
        }
        carrito.quitar(item.getArticulo());
        actualizarCarrito();
    }

    private void vaciarCarrito() {
        carrito.vaciar();
        actualizarCarrito();
    }

    private void actualizarCarrito() {
        modeloCarrito.setDatos(List.copyOf(carrito.getItems()));
        lblTotal.setText("Total: " + UI.moneda(carrito.getTotal()));
    }

    // ---------------- Operación transaccional ----------------

    private void registrarPedido() {
        Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
        if (cliente == null) {
            UI.error(this, "Elegí un cliente");
            return;
        }
        final Pedido[] registrado = new Pedido[1];
        boolean ok = UI.ejecutar(this, () -> registrado[0] = pedidos.registrarPedido(cliente, carrito));
        if (ok) {
            UI.info(this, "Pedido Nº " + registrado[0].getId() + " registrado por "
                    + UI.moneda(registrado[0].getTotal()) + ".\nQuedó PENDIENTE de confirmación.");
            carrito.vaciar();
            refrescar();
        }
    }

    private void mostrarDetalle() {
        Pedido p = UI.seleccionado(tablaPedidos, modeloPedidos);
        modeloDetalle.setDatos(p == null ? List.of() : p.getDetalles());
    }

    private void confirmar() {
        Pedido p = UI.seleccionado(tablaPedidos, modeloPedidos);
        if (p == null) {
            UI.error(this, "Seleccioná un pedido del historial");
            return;
        }
        UI.ejecutar(this, () -> pedidos.confirmar(p));
        refrescar();
    }

    private void cancelar() {
        Pedido p = UI.seleccionado(tablaPedidos, modeloPedidos);
        if (p == null) {
            UI.error(this, "Seleccioná un pedido del historial");
            return;
        }
        if (!UI.confirmar(this, "¿Cancelar el pedido Nº " + p.getId() + "?")) {
            return;
        }
        UI.ejecutar(this, () -> pedidos.cancelar(p));
        refrescar();
    }
}
