package br.com.pix.exception;

public class LimiteTransferenciaExcedidoException extends RuntimeException {
    public LimiteTransferenciaExcedidoException() {
        super("Valor excede o limite diário/transação");
    }
}
