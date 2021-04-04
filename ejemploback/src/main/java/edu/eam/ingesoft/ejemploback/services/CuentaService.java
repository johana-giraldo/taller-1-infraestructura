package edu.eam.ingesoft.ejemploback.services;

import edu.eam.ingesoft.ejemploback.model.Cliente;
import edu.eam.ingesoft.ejemploback.model.Cuenta;
import edu.eam.ingesoft.ejemploback.model.CuentaTransferencia;
import edu.eam.ingesoft.ejemploback.model.Transaccion;
import edu.eam.ingesoft.ejemploback.repositories.ClienteRepository;
import edu.eam.ingesoft.ejemploback.repositories.CuentaRepository;
import edu.eam.ingesoft.ejemploback.repositories.TransaccionRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class CuentaService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    public Cuenta crearCuenta(Cuenta cuenta) {
        Cliente cliente = clienteRepository.findById(cuenta.getCedulaCliente()).orElse(null);

        if (cliente == null) {
            throw new RuntimeException("no existe el cliente");
        }

        List<Cuenta> cuentasCliente = cuentaRepository.buscarCuentasCliente(cuenta.getCedulaCliente());

        if (cuentasCliente.size() == 3) {
            throw new RuntimeException("supero el limite de cuentas");
        }

        cuentaRepository.save(cuenta);

        return cuenta;
    }

    public List<Cuenta> listarCuentasCliente(String cedula) {
        return cuentaRepository.buscarCuentasCliente(cedula);
    }

    public String consignarDinero(Cuenta consignacion) {
        //1.Traer la cuenta
        Cuenta cuentaBD = obtenerCuenta(consignacion);

        //2.Sumar el dinero entrante con el que hay
        double dinerocuentaBD = cuentaBD.getAmount();
        double dineroConsignacion = consignacion.getAmount();
        double resultadoConsignacion = dinerocuentaBD + dineroConsignacion;

        //3.Guardar el dinero entrante
        cuentaBD.setAmount(resultadoConsignacion);

        Cuenta consignacionGuardada = cuentaRepository.save(cuentaBD);

        //4.Guardar transaccion
        Transaccion transaccion = new Transaccion(cuentaBD.getId(), "consignacion", dineroConsignacion);
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        return "Transactionid: " + transaccionGuardada.getNumero().toString();
    }

    private Cuenta obtenerCuenta(Cuenta cuentaCliente) {
        Cuenta cuentaEncontrada = null;
        List<Cuenta> cuentas = listarCuentasCliente(cuentaCliente.getCedulaCliente());

        //buscar en la lista de cuentas donde el id de la cuenta se igual al id que esta llegando en
        //cuenta cliente y guardar en variable si existe
        //de lo contrario retornar un null
        for (int i = 0; i < cuentas.size(); i++) {
            if (cuentas.get(i).getId().equals(cuentaCliente.getId())) {
                cuentaEncontrada = cuentas.get(i);
            }
        }
        if (cuentaEncontrada == null) {
            throw new RuntimeException("No existe la cuenta");
        }
        return cuentaEncontrada;
    }

    public String retirarDinero(Cuenta retirar) {

        //1.Traer la cuenta
        Cuenta cuentaBD = obtenerCuenta(retirar);
        //2.Restar lo que retire de lo que habia
        double dinerocuentaBD = cuentaBD.getAmount();
        double dineroRetiro = retirar.getAmount();
        double resultadoRetiro = dinerocuentaBD - dineroRetiro;

        //3.guardar el retiro
        cuentaBD.setAmount(resultadoRetiro);

        Cuenta retiroGuardado = cuentaRepository.save(cuentaBD);

        //4.Guardar transaccion
        Transaccion transaccion = new Transaccion(cuentaBD.getId(), "retiro", dineroRetiro);
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        return "Transactionid: " + transaccionGuardada.getNumero().toString();

    }

    public boolean esCuentaConSaldo(Cuenta cuentaOrigen, double monto) {

        if (cuentaOrigen.getAmount() - monto < 0) {
            return false;
        } else {
            return true;
        }
    }

    public String transaccionEntreCuentas(Cuenta cuentaOrigen, Cuenta cuentaDestino) {

        cuentaDestino.setAmount(cuentaOrigen.getAmount());

        //1.Traer las cuentas
        Cuenta cuenta1 = obtenerCuenta(cuentaOrigen);
        Cuenta cuenta2 = obtenerCuenta(cuentaDestino);
        boolean tieneSaldo = esCuentaConSaldo(cuenta1, cuentaOrigen.getAmount());
        //2.Realizar el retiro de la cuenta origen, validar si tiene saldo

        double dinerocuenta1 = cuenta1.getAmount();
        double dineroRetiro = cuentaOrigen.getAmount();
        double resultadoRetiro = dinerocuenta1 - dineroRetiro;

        if (tieneSaldo == false) {
            throw new RuntimeException("La cuenta no tiene saldo");
        }

        //3.Guardar el retiro de la cuenta origen
        cuenta1.setAmount(resultadoRetiro);

        Cuenta retiroGuardado = cuentaRepository.save(cuenta1);

        //4.Guardar la transaccion de retiro
        Transaccion transferenciaRetiro = new Transaccion(cuentaOrigen.getId(), "retiro", dineroRetiro);
        Transaccion transaccionRetiro = transaccionRepository.save(transferenciaRetiro);

        //4 Realizar la consignacion a la cuenta Destino
        double dinerocuenta2 = cuenta2.getAmount();
        double dineroConsignacion = cuentaDestino.getAmount();
        double resultadoConsignacion = dinerocuenta2 + dineroConsignacion;

        //5.Guardar el dinero consignado
        cuenta2.setAmount(resultadoConsignacion);

        Cuenta consignacionGuardada = cuentaRepository.save(cuenta2);

        //6 Guardar la transaccion
        Transaccion transferenciaConsignacion = new Transaccion(cuentaDestino.getId(), "consignacion", dineroConsignacion);
        Transaccion transaccionConsignacion = transaccionRepository.save(transferenciaConsignacion);

        return "Transactionid: " + transaccionConsignacion.getNumero().toString() + "\n" + "Transactionid: " + transaccionRetiro.getNumero().toString();

    }

    public void cancelarCuenta(String id) {

        try {
            //1. traer la cuenta
            Cuenta cuentaBD = cuentaRepository.getOne(id);
            //2. comprobar si la cuenta tiene saldo
            if (cuentaBD.getAmount() > 0) {
                throw new RuntimeException("La cuenta tiene saldo");
            } else {
                //3.Borrar la cuenta
                cuentaRepository.deleteById(id);
            }
            //excepciones
        }catch (EntityNotFoundException err) {
            throw new EntityNotFoundException("No existe la cuenta");
        }catch (RuntimeException err){
            throw err;

           }
    }
}

