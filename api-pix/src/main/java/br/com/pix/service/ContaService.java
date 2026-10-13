package br.com.pix.service;

import br.com.pix.api.ContaResponse;
import br.com.pix.api.CriarContaRequest;
import br.com.pix.domain.Conta;
import br.com.pix.exception.ChavePixJaCadastradaException;
import br.com.pix.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional
    public ContaResponse criar(CriarContaRequest request) {
        if (contaRepository.existsByChavePix(request.chavePix())) {
            throw new ChavePixJaCadastradaException();
        }

        BigDecimal saldoInicial = request.saldoInicial() == null ? BigDecimal.ZERO : request.saldoInicial();
        Conta conta = contaRepository.save(new Conta(request.titular(), request.chavePix(), saldoInicial, true));
        return ContaResponse.from(conta);
    }
}
