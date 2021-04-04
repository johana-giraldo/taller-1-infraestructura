package edu.eam.ingesoft.ejemploback.model;

public class CuentaTransferencia  {

    private Cuenta cuentaOrigen;
    private Cuenta cuentaDestino;

    public Cuenta getCuentaOrigen() {
        return cuentaOrigen;
    }
    public CuentaTransferencia(Cuenta cuentaOrigen, Cuenta cuentaDestino) {
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
    }

    public void setCuentaOrigen(Cuenta cuentaOrigen) {
        this.cuentaOrigen = cuentaOrigen;
    }

    public Cuenta getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(Cuenta cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }


    }

