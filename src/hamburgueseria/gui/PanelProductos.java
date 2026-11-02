package hamburgueseria.gui;

import hamburgueseria.modelo.Categoria;
import hamburgueseria.modelo.Producto;
import hamburgueseria.servicio.CatalogoService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** ABM de productos. La categoría se elige de un ComboBox cargado desde la base (FK). */
public class PanelProductos extends JPanel implements Refrescable {

    private final CatalogoService servicio = new CatalogoService();

    private final TablaModelo<Producto> modelo = new TablaModelo<Producto>(
            new String[]{"Código", "Nombre", "Categoría", "Precio", "Disponible", "Activo"},
            p -> p.getCodigo(), p -> p.getNombre(), p -> p.getCategoria().getNombre(),
            p -> p.getPrecio(), p -> p.isDisponible(), p -> p.isActivo());
    private final JTable tabla = UI.crearTabla(modelo);

    private final JTextField txtCodigo = new JTextField(10);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtPrecio = new JTextField(10);
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private final JCheckBox chkDisponible = new JCheckBox("Disponible para la venta", true);
    private final JTextField txtBuscar = new JTextField(20);

    private Producto actual;

    public PanelProductos() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        FormPanel form = new FormPanel();
        form.setBorder(BorderFactory.createTitledBorder("Producto"));
        form.agregar("Código *", txtCodigo);
        form.agregar("Nombre *", txtNombre);
        form.agregar("Precio *", txtPrecio);
        form.agregar("Categoría *", cmbCategoria);
        form.agregar("", chkDisponible);

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
        norte.add(form, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buscador.add(new JLabel("Buscar (código, nombre, categoría...):"));
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

    @Override
    public void refrescar() {
        UI.ejecutar(this, () -> {
            cmbCategoria.removeAllItems();
            for (Categoria c : servicio.listarCategoriasActivas()) {
                cmbCategoria.addItem(c);
            }
            modelo.setDatos(servicio.listarProductos());
        });
        limpiar();
    }

    private void cargarSeleccion() {
        Producto p = UI.seleccionado(tabla, modelo);
        if (p == null) {
            return;
        }
        actual = p;
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(p.getPrecio().toPlainString());
        chkDisponible.setSelected(p.isDisponible());
        seleccionarCategoria(p.getCategoria());
    }

    private void seleccionarCategoria(Categoria categoria) {
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getId() == categoria.getId()) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
        // La categoría del producto fue dada de baja: se muestra igual (guardar la rechazará).
        cmbCategoria.addItem(categoria);
        cmbCategoria.setSelectedItem(categoria);
    }

    private void limpiar() {
        actual = null;
        tabla.clearSelection();
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        chkDisponible.setSelected(true);
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        txtCodigo.requestFocusInWindow();
    }

    private void guardar() {
        boolean ok = UI.ejecutar(this, () -> {
            Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
            if (categoria == null) {
                throw new IllegalArgumentException("Elegí una categoría");
            }
            int id = actual == null ? 0 : actual.getId();
            Producto p = new Producto(id, txtCodigo.getText(), txtNombre.getText(),
                    UI.leerPrecio(txtPrecio.getText()), chkDisponible.isSelected(), categoria);
            servicio.guardarProducto(p);
        });
        if (ok) {
            refrescar();
        }
    }

    private void eliminar() {
        if (actual == null) {
            UI.error(this, "Seleccioná un producto de la lista");
            return;
        }
        if (!UI.confirmar(this, "¿Dar de baja el producto \"" + actual.getNombre() + "\"?")) {
            return;
        }
        boolean ok = UI.ejecutar(this, () -> servicio.eliminarProducto(actual));
        if (ok) {
            refrescar();
        }
    }
}
