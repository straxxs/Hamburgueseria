package hamburgueseria.modelo;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

public class Cliente {

    private static final Pattern DNI = Pattern.compile("\\d{7,10}");
    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private int id;
    private String dni;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private LocalDate fechaNacimiento;
    private boolean activo = true;

    public Cliente(int id, String dni, String nombre, String apellido,
                   String telefono, String email, LocalDate fechaNacimiento,
                   boolean activo) {
        this.id = id;
        setDni(dni);
        setNombre(nombre);
        setApellido(apellido);
        setTelefono(telefono);
        setEmail(email);
        setFechaNacimiento(fechaNacimiento);
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public final void setDni(String dni) {
        if (dni == null || !DNI.matcher(dni.trim()).matches()) {
            throw new IllegalArgumentException("El DNI debe tener entre 7 y 10 dígitos");
        }
        this.dni = dni.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getApellido() {
        return apellido;
    }

    public final void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        this.apellido = apellido.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public final void setTelefono(String telefono) {
        this.telefono = (telefono == null || telefono.isBlank()) ? null : telefono.trim();
    }

    public String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        if (email == null || email.isBlank()) {
            this.email = null;
            return;
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
        this.email = email.trim();
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public final void setFechaNacimiento(LocalDate fechaNacimiento) {
        Objects.requireNonNull(fechaNacimiento, "La fecha de nacimiento es obligatoria");
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + dni + ")";
    }
}
