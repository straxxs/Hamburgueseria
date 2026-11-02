package hamburgueseria.modelo;

public class Categoria {

    private int id;
    private String nombre;
    private boolean activo = true;

    public Categoria(int id, String nombre, boolean activo) {
        this.id = id;
        setNombre(nombre);
        this.activo = activo;
    }

    public Categoria(String nombre) {
        this(0, nombre, true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (nombre.trim().length() > 60) {
            throw new IllegalArgumentException("El nombre admite hasta 60 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** Se usa para mostrar la categoría dentro de un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
