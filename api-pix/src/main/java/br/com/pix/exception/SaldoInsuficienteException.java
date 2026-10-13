package br.com.pix.exception;

public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException() {
        super("Saldo insuficiente para realizar a transferência");
    }
}
