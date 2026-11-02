package hamburgueseria.gui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Modelo de tabla genérico: cada fila es un OBJETO DE DOMINIO (nunca un
 * ResultSet). Cada columna se define con una función que extrae el valor.
 */
public class TablaModelo<T> extends AbstractTableModel {

    private final String[] columnas;
    private final List<Function<T, Object>> extractores;
    private List<T> datos = new ArrayList<>();

    @SafeVarargs
    public TablaModelo(String[] columnas, Function<T, Object>... extractores) {
        if (columnas.length != extractores.length) {
            throw new IllegalArgumentException("Columnas y extractores no coinciden");
        }
        this.columnas = columnas;
        this.extractores = Arrays.asList(extractores);
    }

    public void setDatos(List<T> nuevos) {
        this.datos = new ArrayList<>(nuevos);
        fireTableDataChanged();
    }

    public T getFila(int filaModelo) {
        return datos.get(filaModelo);
    }

    @Override
    public int getRowCount() {
        return datos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return extractores.get(columna).apply(datos.get(fila));
    }

    /** Devuelve el tipo real del dato para que Swing ordene y dibuje bien (Boolean, BigDecimal...). */
    @Override
    public Class<?> getColumnClass(int columna) {
        if (!datos.isEmpty()) {
            Object valor = getValueAt(0, columna);
            if (valor != null) {
                return valor.getClass();
            }
        }
        return Object.class;
    }
}
