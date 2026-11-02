package hamburgueseria.gui;

import hamburgueseria.modelo.Cliente;
import hamburgueseria.servicio.ClienteService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** ABM de clientes, con selector de fecha (calendario numérico) y casilla "Activo". */
public class PanelClientes extends JPanel implements Refrescable {

    private final ClienteService servicio = new ClienteService();

    private final TablaModelo<Cliente> modelo = new TablaModelo<Cliente>(
            new String[]{"DNI", "Apellido", "Nombre", "Teléfono", "Email", "Nacimiento", "Activo"},
            c -> c.getDni(), c -> c.getApellido(), c -> c.getNombre(),
            c -> c.getTelefono() == null ? "" : c.getTelefono(),
            c -> c.getEmail() == null ? "" : c.getEmail(),
            c -> c.getFechaNacimiento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            c -> c.isActivo());
    private final JTable tabla = UI.crearTabla(modelo);

    private final JTextField txtDni = new JTextField(12);
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtApellido = new JTextField(20);
    private final JTextField txtTelefono = new JTextField(15);
    private final JTextField txtEmail = new JTextField(25);
    private final JSpinner spnNacimiento = crearSpinnerFecha();
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);
    private final JTextField txtBuscar = new JTextField(20);

    private Cliente actual;

    public PanelClientes() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        FormPanel form = new FormPanel();
        form.setBorder(BorderFactory.createTitledBorder("Cliente"));
        form.agregar("DNI *", txtDni);
        form.agregar("Nombre *", txtNombre);
        form.agregar("Apellido *", txtApellido);
        form.agregar("Fecha de nacimiento *", spnNacimiento);
        form.agregar("Teléfono", txtTelefono);
        form.agregar("Email", txtEmail);
        form.agregar("", chkActivo);

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
        buscador.add(new JLabel("Buscar (DNI, nombre, apellido...):"));
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

    private static JSpinner crearSpinnerFecha() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(
                fechaADate(LocalDate.of(2000, 1, 1)), null, null, java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    private static Date fechaADate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate dateAFecha(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public void refrescar() {
        UI.ejecutar(this, () -> modelo.setDatos(servicio.listar()));
        limpiar();
    }

    private void cargarSeleccion() {
        Cliente c = UI.seleccionado(tabla, modelo);
        if (c == null) {
            return;
        }
        actual = c;
        txtDni.setText(c.getDni());
        txtNombre.setText(c.getNombre());
        txtApellido.setText(c.getApellido());
        txtTelefono.setText(c.getTelefono() == null ? "" : c.getTelefono());
        txtEmail.setText(c.getEmail() == null ? "" : c.getEmail());
        spnNacimiento.setValue(fechaADate(c.getFechaNacimiento()));
        chkActivo.setSelected(c.isActivo());
    }

    private void limpiar() {
        actual = null;
        tabla.clearSelection();
        txtDni.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        spnNacimiento.setValue(fechaADate(LocalDate.of(2000, 1, 1)));
        chkActivo.setSelected(true);
        txtDni.requestFocusInWindow();
    }

    private void guardar() {
        boolean ok = UI.ejecutar(this, () -> {
            int id = actual == null ? 0 : actual.getId();
            Cliente c = new Cliente(id, txtDni.getText(), txtNombre.getText(),
                    txtApellido.getText(), txtTelefono.getText(), txtEmail.getText(),
                    dateAFecha((Date) spnNacimiento.getValue()), chkActivo.isSelected());
            servicio.guardar(c);
        });
        if (ok) {
            refrescar();
        }
    }

    private void eliminar() {
        if (actual == null) {
            UI.error(this, "Seleccioná un cliente de la lista");
            return;
        }
        if (!UI.confirmar(this, "¿Dar de baja a " + actual.getNombreCompleto() + "?")) {
            return;
        }
        boolean ok = UI.ejecutar(this, () -> servicio.eliminar(actual));
        if (ok) {
            refrescar();
        }
    }
}
