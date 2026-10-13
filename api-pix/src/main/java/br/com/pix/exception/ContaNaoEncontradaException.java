package br.com.pix.exception;

public class ContaNaoEncontradaException extends RuntimeException {
    public ContaNaoEncontradaException(Long id) {
        super("Conta de origem não encontrada: " + id);
    }
}
