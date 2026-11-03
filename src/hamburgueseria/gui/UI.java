package hamburgueseria.gui;

import hamburgueseria.datos.DatosException;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

/** Utilidades compartidas por todas las pantallas. */
final class UI {

    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private UI() {
    }

    /** Acción de pantalla que puede fallar. */
    @FunctionalInterface
    interface Accion {
        void ejecutar() throws Exception;
    }

    /**
     * Ejecuta la acción y muestra cualquier error de validación o de base
     * de datos en un cuadro de diálogo, sin que se cierre la aplicación.
     */
    static boolean ejecutar(Component padre, Accion accion) {
        try {
            accion.ejecutar();
            return true;
        } catch (DatosException | IllegalArgumentException | IllegalStateException
                 | NullPointerException | ArithmeticException e) {
            error(padre, e.getMessage() != null ? e.getMessage() : "Datos incompletos");
        } catch (Exception e) {
            error(padre, "Error inesperado: " + e);
        }
        return false;
    }

    static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmar(Component padre, String pregunta) {
        return JOptionPane.showConfirmDialog(padre, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    static String moneda(BigDecimal valor) {
        return MONEDA.format(valor);
    }

    /** Convierte el texto de un precio ("1500", "1500,50" o "1500.50") a BigDecimal. */
    static BigDecimal leerPrecio(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }
        try {
            return new BigDecimal(texto.trim().replace(',', '.')).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un número (ej.: 5000 o 5000,50)");
        }
    }

    /** Crea una JTable ordenable por columnas, con formato de moneda y fecha. */
    static JTable crearTabla(TablaModelo<?> modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowSorter(new TableRowSorter<>(modelo));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);

        tabla.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(valor == null ? "" : moneda((BigDecimal) valor));
            }
        });
        tabla.setDefaultRenderer(LocalDateTime.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setText(valor == null ? "" : FECHA_HORA.format((LocalDateTime) valor));
            }
        });
        return tabla;
    }

    /** Filtra la grilla en tiempo real mientras se escribe en el buscador. */
    static void conectarBuscador(JTextField buscador, JTable tabla) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicar();
            }

            private void aplicar() {
                TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
                String texto = buscador.getText().trim();
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter(
                            "(?iu)" + Pattern.quote(texto)));
                }
            }
        };
        buscador.getDocument().addDocumentListener(listener);
    }

    /** Devuelve el objeto de la fila seleccionada (o null si no hay selección). */
    static <T> T seleccionado(JTable tabla, TablaModelo<T> modelo) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return modelo.getFila(tabla.convertRowIndexToModel(fila));
    }
}
