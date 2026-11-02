package hamburgueseria.gui;

import hamburgueseria.modelo.Categoria;
import hamburgueseria.servicio.CatalogoService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** ABM de categorías (entidad maestra, lado "1" de Categoría-Producto). */
public class PanelCategorias extends JPanel implements Refrescable {

    private final CatalogoService servicio = new CatalogoService();

    private final TablaModelo<Categoria> modelo = new TablaModelo<Categoria>(
            new String[]{"ID", "Nombre", "Activa"},
            c -> c.getId(), c -> c.getNombre(), c -> c.isActivo());
    private final JTable tabla = UI.crearTabla(modelo);

    private final JTextField txtNombre = new JTextField(20);
    private final JCheckBox chkActiva = new JCheckBox("Activa", true);
    private final JTextField txtBuscar = new JTextField(20);

    /** Categoría que se está editando; null = alta nueva. */
    private Categoria actual;

    public PanelCategorias() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        FormPanel form = new FormPanel();
        form.setBorder(BorderFactory.createTitledBorder("Categoría"));
        form.agregar("Nombre *", txtNombre);
        form.agregar("", chkActiva);

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
        buscador.add(new JLabel("Buscar:"));
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
        UI.ejecutar(this, () -> modelo.setDatos(servicio.listarCategorias()));
        limpiar();
    }

    private void cargarSeleccion() {
        Categoria c = UI.seleccionado(tabla, modelo);
        if (c != null) {
            actual = c;
            txtNombre.setText(c.getNombre());
            chkActiva.setSelected(c.isActivo());
        }
    }

    private void limpiar() {
        actual = null;
        tabla.clearSelection();
        txtNombre.setText("");
        chkActiva.setSelected(true);
        txtNombre.requestFocusInWindow();
    }

    private void guardar() {
        boolean ok = UI.ejecutar(this, () -> {
            int id = actual == null ? 0 : actual.getId();
            Categoria c = new Categoria(id, txtNombre.getText(), chkActiva.isSelected());
            servicio.guardarCategoria(c);
        });
        if (ok) {
            refrescar();
        }
    }

    private void eliminar() {
        if (actual == null) {
            UI.error(this, "Seleccioná una categoría de la lista");
            return;
        }
        if (!UI.confirmar(this, "¿Dar de baja la categoría \"" + actual.getNombre()
                + "\"?\nSus productos dejarán de estar disponibles.")) {
            return;
        }
        boolean ok = UI.ejecutar(this, () -> servicio.eliminarCategoria(actual));
        if (ok) {
            refrescar();
        }
    }
}
