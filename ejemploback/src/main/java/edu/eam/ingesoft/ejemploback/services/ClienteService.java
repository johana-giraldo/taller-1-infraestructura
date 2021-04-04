package edu.eam.ingesoft.ejemploback.services;

import ch.qos.logback.core.net.server.Client;
import edu.eam.ingesoft.ejemploback.model.Cliente;
import edu.eam.ingesoft.ejemploback.model.Cuenta;
import edu.eam.ingesoft.ejemploback.repositories.ClienteRepository;
import edu.eam.ingesoft.ejemploback.repositories.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private ClienteRepository clienteRepository;


    public void crearCliente(Cliente cliente) {
        //buscando el cliente en la BD
        Cliente clienteBD = clienteRepository.findById(cliente.getCedula()).orElse(null);

        //si es diferente de null es porque ya existe....
        if (clienteBD != null) {
            throw new RuntimeException("ya existe este cliente");
        }

        clienteRepository.save(cliente);
    }

    public Cliente buscarCliente(String cedula) {
        //orElse retorna el resultado o null si no hay resultado...
        return clienteRepository.findById(cedula).orElse(null);
    }

    public List<Cliente> listarClientes(){

        return clienteRepository.findAll();
    }

    public Cliente editarCliente(Cliente cliente) {
        Cliente clieteBD = clienteRepository.getOne(cliente.getCedula());

        if (clieteBD == null) {
            throw new RuntimeException("No existe el cliente");
        }

        clienteRepository.save(cliente);

        return cliente;
    }

    public void borrarCliente(String cedula) {

        try {
            //1. traer el cliente
            Cliente clieteBD = clienteRepository.getOne(cedula);
            //2.Traer las cuentas
            List<Cuenta> cuentasCliente = cuentaRepository.buscarCuentasCliente(clieteBD.getCedula());
           //3.comprobar si el cliente tiene cuentas
            if (cuentasCliente.size()  > 0) {
                throw new RuntimeException("El cliente tiene cuentas");
            }else {
                //4 Borrar cliente
                clienteRepository.deleteById(cedula);
            }
            //excepciones
        }catch (EmptyResultDataAccessException err) {
            throw new EntityNotFoundException("No existe el cliente");
        }catch (RuntimeException err){
            throw err;

        }
    }
}


