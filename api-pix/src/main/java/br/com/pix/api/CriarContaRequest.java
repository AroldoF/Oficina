package br.com.pix.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados necessários para abrir uma conta PIX")
public record CriarContaRequest(
        @Schema(example = "Alice Silva")
        @NotBlank(message = "O titular é obrigatório")
        @Size(max = 100, message = "O titular deve ter no máximo 100 caracteres")
        String titular,

        @Schema(example = "alice@email.com")
        @NotBlank(message = "A chave PIX é obrigatória")
        @Size(max = 100, message = "A chave PIX deve ter no máximo 100 caracteres")
        String chavePix,

        @Schema(description = "Saldo disponível ao abrir a conta. Quando omitido, é zero.", example = "100.00")
        @DecimalMin(value = "0.00", message = "O saldo inicial não pode ser negativo")
        BigDecimal saldoInicial) {
}
