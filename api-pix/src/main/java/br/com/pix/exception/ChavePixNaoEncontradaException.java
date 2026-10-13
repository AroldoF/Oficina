package br.com.pix.exception;

public class ChavePixNaoEncontradaException extends RuntimeException {
    public ChavePixNaoEncontradaException() {
        super("Chave PIX não encontrada");
    }
}
