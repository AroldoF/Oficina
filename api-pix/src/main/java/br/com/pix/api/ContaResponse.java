package br.com.pix.api;

import br.com.pix.domain.Conta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dados públicos de uma conta PIX")
public record ContaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Alice Silva") String titular,
        @Schema(example = "alice@email.com") String chavePix,
        @Schema(example = "100.00") BigDecimal saldo,
        @Schema(example = "true") boolean ativa) {

    public static ContaResponse from(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getTitular(),
                conta.getChavePix(),
                conta.getSaldo(),
                conta.isAtiva());
    }
}
