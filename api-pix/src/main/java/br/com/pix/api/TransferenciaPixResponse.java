package br.com.pix.api;

import br.com.pix.domain.StatusTransacao;
import br.com.pix.domain.Transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaPixResponse(
        Long transacaoId,
        Long contaOrigemId,
        Long contaDestinoId,
        BigDecimal valor,
        LocalDateTime dataHora,
        StatusTransacao status) {

    public static TransferenciaPixResponse from(Transacao transacao) {
        return new TransferenciaPixResponse(
                transacao.getId(),
                transacao.getContaOrigem().getId(),
                transacao.getContaDestino().getId(),
                transacao.getValor(),
                transacao.getDataHora(),
                transacao.getStatus());
    }
}
