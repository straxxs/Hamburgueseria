package hamburgueseria.servicio;

import hamburgueseria.datos.ClienteDatos;
import hamburgueseria.datos.DatosException;
import hamburgueseria.modelo.Cliente;

import java.util.List;

public class ClienteService {

    private final ClienteDatos clienteDatos = new ClienteDatos();

    public List<Cliente> listar() throws DatosException {
        return clienteDatos.listar();
    }

    public List<Cliente> listarActivos() throws DatosException {
        return clienteDatos.listarActivos();
    }

    public void guardar(Cliente c) throws DatosException {
        if (c.getId() == 0) {
            clienteDatos.insertar(c);
        } else {
            clienteDatos.actualizar(c);
        }
    }

    public void eliminar(Cliente c) throws DatosException {
        clienteDatos.darDeBaja(c.getId());
    }
}
