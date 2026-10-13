package br.com.pix.exception;

public class ContaInativaException extends RuntimeException {
    public ContaInativaException(String tipoConta) {
        super("Conta de " + tipoConta + " inativa");
    }
}
