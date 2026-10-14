package br.com.pix.repository;

import br.com.pix.api.CriarContaRequest;
import br.com.pix.domain.Conta;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ContaRepositoryTest {
    @Autowired
    EntityManager entityManager;

    @Autowired
    ContaRepository contaRepository;

    @Test
    @DisplayName("Caso de teste 1: Sucesso ao localizar chave pix")
    void findByChavePixSucesso() {
        String chavePix = "rafael@gmail.com";
        CriarContaRequest data = new CriarContaRequest("Rafael", chavePix, BigDecimal.valueOf(12000.00));
        this.createConta(data);

        Optional<Conta> result = this.contaRepository.findByChavePix(chavePix);

        System.out.println("Conta localizada pela chave pix: " + result.get().getChavePix());

        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Caso de teste 2: Chave pix não encontrada")
    void findByChavePixErro() {
        String chavePix = "rafael@gmail.com";

        Optional<Conta> result = this.contaRepository.findByChavePix(chavePix);

        assertThat(result.isEmpty()).isTrue();
    }

    private Conta createConta(CriarContaRequest data){
        Conta conta = new Conta(data.titular(), data.chavePix(), data.saldoInicial(), true);
        this.entityManager.persist(conta);
        return conta;
    }
}