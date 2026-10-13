package br.com.pix.service;

import br.com.pix.api.TransferenciaPixRequest;
import br.com.pix.api.TransferenciaPixResponse;
import br.com.pix.domain.Conta;
import br.com.pix.domain.StatusTransacao;
import br.com.pix.domain.Transacao;
import br.com.pix.exception.ChavePixNaoEncontradaException;
import br.com.pix.exception.ContaInativaException;
import br.com.pix.exception.ContaNaoEncontradaException;
import br.com.pix.exception.LimiteTransferenciaExcedidoException;
import br.com.pix.exception.SaldoInsuficienteException;
import br.com.pix.exception.TransferenciaParaMesmaContaException;
import br.com.pix.repository.ContaRepository;
import br.com.pix.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferenciaService {

    public static final BigDecimal LIMITE_POR_TRANSACAO = new BigDecimal("5000.00");

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public TransferenciaService(ContaRepository contaRepository, TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public TransferenciaPixResponse transferir(TransferenciaPixRequest request) {
        if (request.valor().compareTo(LIMITE_POR_TRANSACAO) > 0) {
            throw new LimiteTransferenciaExcedidoException();
        }

        Conta origem = contaRepository.findById(request.contaOrigemId())
                .orElseThrow(() -> new ContaNaoEncontradaException(request.contaOrigemId()));
        Conta destino = contaRepository.findByChavePix(request.chavePixDestino())
                .orElseThrow(ChavePixNaoEncontradaException::new);

        validarContas(origem, destino);

        if (origem.getSaldo().compareTo(request.valor()) < 0) {
            throw new SaldoInsuficienteException();
        }

        origem.debitar(request.valor());
        destino.creditar(request.valor());
        contaRepository.save(origem);
        contaRepository.save(destino);

        Transacao transacao = transacaoRepository.save(new Transacao(
                origem, destino, request.valor(), LocalDateTime.now(), StatusTransacao.CONCLUIDO));
        return TransferenciaPixResponse.from(transacao);
    }

    private void validarContas(Conta origem, Conta destino) {
        if (!origem.isAtiva()) {
            throw new ContaInativaException("origem");
        }
        if (!destino.isAtiva()) {
            throw new ContaInativaException("destino");
        }
        if (origem.getId().equals(destino.getId())) {
            throw new TransferenciaParaMesmaContaException();
        }
    }
}
