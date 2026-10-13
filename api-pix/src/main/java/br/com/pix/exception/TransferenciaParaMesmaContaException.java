package br.com.pix.exception;

public class TransferenciaParaMesmaContaException extends RuntimeException {
    public TransferenciaParaMesmaContaException() {
        super("Não é possível transferir para a mesma conta");
    }
}
