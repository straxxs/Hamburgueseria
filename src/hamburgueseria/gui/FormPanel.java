package hamburgueseria.gui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Panel de formulario: etiqueta a la izquierda, campo a la derecha. */
class FormPanel extends JPanel {

    private int fila = 0;

    FormPanel() {
        super(new GridBagLayout());
    }

    void agregar(String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila++;
        c.insets = new Insets(3, 4, 3, 4);

        c.gridx = 0;
        c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel(etiqueta), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        add(campo, c);
    }
}
