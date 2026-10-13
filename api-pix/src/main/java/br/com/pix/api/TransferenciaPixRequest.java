package br.com.pix.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferenciaPixRequest(
        @NotNull(message = "A conta de origem é obrigatória") Long contaOrigemId,
        @NotBlank(message = "A chave PIX de destino é obrigatória") String chavePixDestino,
        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero") BigDecimal valor) {
}
