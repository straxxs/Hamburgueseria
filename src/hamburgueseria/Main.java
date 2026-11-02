package hamburgueseria;

import hamburgueseria.datos.Conexion;
import hamburgueseria.datos.DatosException;
import hamburgueseria.gui.MenuPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            aplicarEstilo();

            // Se prueba la conexión antes de abrir la ventana principal.
            try (Connection cn = Conexion.obtener()) {
                cn.isValid(2);
            } catch (DatosException | SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "No se pudo conectar a la base de datos", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            new MenuPrincipal().setVisible(true);
        });
    }

    private static void aplicarEstilo() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla el estilo se usa el que trae Swing por defecto.
        }
    }
}
