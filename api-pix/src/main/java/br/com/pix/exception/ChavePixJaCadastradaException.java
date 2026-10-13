package br.com.pix.exception;

public class ChavePixJaCadastradaException extends RuntimeException {
    public ChavePixJaCadastradaException() {
        super("Já existe uma conta cadastrada com esta chave PIX");
    }
}
