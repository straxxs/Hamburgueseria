package hamburgueseria.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pantalla principal: menú lateral + área central donde se muestra cada módulo. */
public class MenuPrincipal extends JFrame {

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Map<String, JPanel> modulos = new LinkedHashMap<>();

    public MenuPrincipal() {
        super("Hamburguesería - Sistema de gestión");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);

        modulos.put("Pedidos", new PanelPedidos());
        modulos.put("Clientes", new PanelClientes());
        modulos.put("Productos", new PanelProductos());
        modulos.put("Combos", new PanelCombos());
        modulos.put("Categorías", new PanelCategorias());

        setJMenuBar(crearBarraMenu());
        add(crearBarraLateral(), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        modulos.forEach(contenido::add);
        mostrar("Pedidos");
    }

    private JPanel crearBarraLateral() {
        JPanel barra = new JPanel();
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel titulo = new JLabel("🍔 Hamburguesería");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        barra.add(titulo);
        barra.add(Box.createVerticalStrut(14));

        ButtonGroup grupo = new ButtonGroup();
        for (String nombre : modulos.keySet()) {
            JToggleButton boton = new JToggleButton(nombre);
            boton.setAlignmentX(LEFT_ALIGNMENT);
            boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            boton.setPreferredSize(new Dimension(160, 40));
            boton.setFocusPainted(false);
            boton.addActionListener(e -> mostrar(nombre));
            boton.setSelected(nombre.equals("Pedidos"));
            grupo.add(boton);
            barra.add(boton);
            barra.add(Box.createVerticalStrut(6));
        }

        barra.add(Box.createVerticalGlue());
        JButton salir = new JButton("Salir");
        salir.setAlignmentX(LEFT_ALIGNMENT);
        salir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        salir.addActionListener(e -> salir());
        barra.add(salir);
        return barra;
    }

    private JMenuBar crearBarraMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu modulosMenu = new JMenu("Módulos");
        for (String nombre : modulos.keySet()) {
            JMenuItem item = new JMenuItem(nombre);
            item.addActionListener(e -> mostrar(nombre));
            modulosMenu.add(item);
        }
        modulosMenu.addSeparator();
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> salir());
        modulosMenu.add(salir);

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de");
        acerca.addActionListener(e -> UI.info(this,
                "Hamburguesería - Trabajo Práctico Final\nJava Swing + JDBC + MariaDB (XAMPP)"));
        ayuda.add(acerca);

        barra.add(modulosMenu);
        barra.add(ayuda);
        return barra;
    }

    /** Muestra un módulo y vuelve a cargar sus datos desde la base. */
    private void mostrar(String nombre) {
        cartas.show(contenido, nombre);
        JPanel panel = modulos.get(nombre);
        if (panel instanceof Refrescable r) {
            r.refrescar();
        }
    }

    private void salir() {
        if (UI.confirmar(this, "¿Salir de la aplicación?")) {
            dispose();
            System.exit(0);
        }
    }
}
