package hamburgueseria.gui;

import hamburgueseria.modelo.Combo;
import hamburgueseria.modelo.ItemCombo;
import hamburgueseria.modelo.Producto;
import hamburgueseria.servicio.CatalogoService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** ABM de combos: un combo se compone de varios productos (relación N a M). */
public class PanelCombos extends JPanel implements Refrescable {

    private final CatalogoService servicio = new CatalogoService();

    // Grilla de combos
    private final TablaModelo<Combo> modelo = new TablaModelo<Combo>(
            new String[]{"Código", "Nombre", "Contenido", "Precio", "Disponible", "Activo"},
            c -> c.getCodigo(), c -> c.getNombre(), c -> c.getDescripcion(),
            c -> c.getPrecio(), c -> c.isDisponible(), c -> c.isActivo());
    private final JTable tabla = UI.crearTabla(modelo);

    // Formulario del combo
    private final JTextField txtCodigo = new JTextField(10);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtPrecio = new JTextField(10);
    private final JCheckBox chkDisponible = new JCheckBox("Disponible para la venta", true);
    private final JTextField txtBuscar = new JTextField(20);

    // Composición del combo
    private final JComboBox<Producto> cmbProducto = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final TablaModelo<ItemCombo> modeloItems = new TablaModelo<ItemCombo>(
            new String[]{"Producto", "Cantidad"},
            i -> i.getProducto().getNombre(), i -> i.getCantidad());
    private final JTable tablaItems = UI.crearTabla(modeloItems);

    /** Combo que se está editando (id 0 = nuevo). Guarda la composición en memoria. */
    private Combo borrador = nuevoBorrador();

    public PanelCombos() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        FormPanel form = new FormPanel();
        form.setBorder(BorderFactory.createTitledBorder("Combo"));
        form.agregar("Código *", txtCodigo);
        form.agregar("Nombre *", txtNombre);
        form.agregar("Precio *", txtPrecio);
        form.agregar("", chkDisponible);

        JButton btnAgregarItem = new JButton("Agregar");
        JButton btnQuitarItem = new JButton("Quitar seleccionado");
        btnAgregarItem.addActionListener(e -> agregarItem());
        btnQuitarItem.addActionListener(e -> quitarItem());

        // El desplegable ocupa el espacio que sobra; cantidad y botón quedan siempre visibles.
        JPanel cantidadYBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        cantidadYBoton.add(new JLabel("Cant.:"));
        cantidadYBoton.add(spnCantidad);
        cantidadYBoton.add(btnAgregarItem);

        JPanel selector = new JPanel(new BorderLayout(4, 0));
        selector.add(cmbProducto, BorderLayout.CENTER);
        selector.add(cantidadYBoton, BorderLayout.EAST);

        JScrollPane scrollItems = new JScrollPane(tablaItems);
        scrollItems.setPreferredSize(new Dimension(300, 90));

        JPanel composicion = new JPanel(new BorderLayout(0, 4));
        composicion.setBorder(BorderFactory.createTitledBorder("Productos del combo"));
        composicion.add(selector, BorderLayout.NORTH);
        composicion.add(scrollItems, BorderLayout.CENTER);
        composicion.add(btnQuitarItem, BorderLayout.SOUTH);

        JPanel datos = new JPanel(new GridLayout(1, 2, 8, 0));
        datos.add(form);
        datos.add(composicion);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Dar de baja");
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(datos, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buscador.add(new JLabel("Buscar (código, nombre, contenido...):"));
        buscador.add(txtBuscar);

        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.add(buscador, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        UI.conectarBuscador(txtBuscar, tabla);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    private static Combo nuevoBorrador() {
        // Valores provisorios: se validan y reemplazan al guardar.
        return new Combo(0, "TMP", "Nuevo combo", java.math.BigDecimal.ZERO, true);
    }

    @Override
    public void refrescar() {
        UI.ejecutar(this, () -> {
            cmbProducto.removeAllItems();
            for (Producto p : servicio.listarProductosDisponibles()) {
                cmbProducto.addItem(p);
            }
            modelo.setDatos(servicio.listarCombos());
        });
        limpiar();
    }

    private void cargarSeleccion() {
        Combo c = UI.seleccionado(tabla, modelo);
        if (c == null) {
            return;
        }
        // Se trabaja sobre una copia para no alterar la grilla hasta guardar.
        borrador = new Combo(c.getId(), c.getCodigo(), c.getNombre(), c.getPrecio(), c.isDisponible());
        for (ItemCombo item : c.getItems()) {
            borrador.agregarItem(item.getProducto(), item.getCantidad());
        }
        txtCodigo.setText(c.getCodigo());
        txtNombre.setText(c.getNombre());
        txtPrecio.setText(c.getPrecio().toPlainString());
        chkDisponible.setSelected(c.isDisponible());
        modeloItems.setDatos(borrador.getItems());
    }

    private void limpiar() {
        borrador = nuevoBorrador();
        borrador.vaciarItems();
        tabla.clearSelection();
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        chkDisponible.setSelected(true);
        spnCantidad.setValue(1);
        modeloItems.setDatos(borrador.getItems());
        txtCodigo.requestFocusInWindow();
    }

    private void agregarItem() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        if (p == null) {
            UI.error(this, "No hay productos disponibles para agregar");
            return;
        }
        UI.ejecutar(this, () -> {
            borrador.agregarItem(p, (Integer) spnCantidad.getValue());
            modeloItems.setDatos(borrador.getItems());
        });
    }

    private void quitarItem() {
        ItemCombo item = UI.seleccionado(tablaItems, modeloItems);
        if (item == null) {
            UI.error(this, "Seleccioná un producto de la lista del combo");
            return;
        }
        borrador.quitarItem(item);
        modeloItems.setDatos(borrador.getItems());
    }

    private void guardar() {
        boolean ok = UI.ejecutar(this, () -> {
            Combo c = new Combo(borrador.getId(), txtCodigo.getText(), txtNombre.getText(),
                    UI.leerPrecio(txtPrecio.getText()), chkDisponible.isSelected());
            for (ItemCombo item : borrador.getItems()) {
                c.agregarItem(item.getProducto(), item.getCantidad());
            }
            servicio.guardarCombo(c);
        });
        if (ok) {
            refrescar();
        }
    }

    private void eliminar() {
        if (borrador.getId() == 0) {
            UI.error(this, "Seleccioná un combo de la lista");
            return;
        }
        if (!UI.confirmar(this, "¿Dar de baja el combo \"" + borrador.getNombre() + "\"?")) {
            return;
        }
        boolean ok = UI.ejecutar(this, () -> servicio.eliminarCombo(borrador));
        if (ok) {
            refrescar();
        }
    }
}
