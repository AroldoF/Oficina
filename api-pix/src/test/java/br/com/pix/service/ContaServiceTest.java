package br.com.pix.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.pix.api.ContaResponse;
import br.com.pix.api.CriarContaRequest;
import br.com.pix.domain.Conta;
import br.com.pix.repository.ContaRepository;


@ExtendWith(MockitoExtension.class)
class ContaServiceTest {
    @Mock private ContaRepository contaRepository;

    @InjectMocks
    private ContaService contaService;

    @Test
    void deveCriarContaQuandoChavePixAindaNaoExiste() {
        // Arrange: prepara os dados e define o comportamento do mock.
        CriarContaRequest request = new CriarContaRequest(
            "Maria Silva",
            "maria@example.com",
            new BigDecimal("100.00")
        );

        when(contaRepository.existsByChavePix(request.chavePix()))
            .thenReturn(false);

        // Um mock retorna null por padrão. Simulamos o save devolvendo a entidade recebida.
        when(contaRepository.save(any(Conta.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: executa o método real do serviço.
        ContaResponse response = contaService.criar(request);

        // Assert: verifica o resultado e como o serviço interagiu com o repositório.
        assertNotNull(response);
        verify(contaRepository).existsByChavePix(request.chavePix());

        ArgumentCaptor<Conta> captor = ArgumentCaptor.forClass(Conta.class);
        verify(contaRepository).save(captor.capture());

        Conta contaSalva = captor.getValue();
        assertEquals("Maria Silva", contaSalva.getTitular());
        assertEquals("maria@example.com", contaSalva.getChavePix());
        assertEquals(0, contaSalva.getSaldo().compareTo(new BigDecimal("100.00")));
    }
}