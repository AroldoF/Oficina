package br.com.pix.service;

import br.com.pix.api.TransferenciaPixRequest;
import br.com.pix.api.TransferenciaPixResponse;
import br.com.pix.domain.Conta;
import br.com.pix.domain.StatusTransacao;
import br.com.pix.domain.Transacao;
import br.com.pix.exception.ChavePixNaoEncontradaException;
import br.com.pix.exception.ContaInativaException;
import br.com.pix.exception.LimiteTransferenciaExcedidoException;
import br.com.pix.exception.SaldoInsuficienteException;
import br.com.pix.exception.TransferenciaParaMesmaContaException;
import br.com.pix.repository.ContaRepository;
import br.com.pix.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock private ContaRepository contaRepository;
    @Mock private TransacaoRepository transacaoRepository;

    private TransferenciaService service;

    @BeforeEach
    void setUp() {
        service = new TransferenciaService(contaRepository, transacaoRepository);
    }

    @Test
    void deveTransferirQuandoContasAtivasESaldoSuficiente() {
        Conta origem = conta(1L, "alice@email.com", "100.00", true);
        Conta destino = conta(2L, "bob@email.com", "50.00", true);
        TransferenciaPixRequest request = request("bob@email.com", "40.00");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByChavePix("bob@email.com")).thenReturn(Optional.of(destino));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferenciaPixResponse response = service.transferir(request);

        assertThat(origem.getSaldo()).isEqualByComparingTo("60.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("90.00");
        assertThat(response.status()).isEqualTo(StatusTransacao.CONCLUIDO);
        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getContaOrigem()).isSameAs(origem);
        assertThat(captor.getValue().getContaDestino()).isSameAs(destino);
        verify(contaRepository).save(origem);
        verify(contaRepository).save(destino);
    }

    @Test
    void naoDeveTransferirComSaldoInsuficiente() {
        Conta origem = conta(1L, "alice@email.com", "30.00", true);
        Conta destino = conta(2L, "bob@email.com", "50.00", true);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByChavePix("bob@email.com")).thenReturn(Optional.of(destino));

        assertThatThrownBy(() -> service.transferir(request("bob@email.com", "50.00")))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo insuficiente para realizar a transferência");

        assertThat(origem.getSaldo()).isEqualByComparingTo("30.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("50.00");
        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveRecusarChavePixInexistente() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta(1L, "alice@email.com", "100.00", true)));
        when(contaRepository.findByChavePix("nao-existe@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transferir(request("nao-existe@email.com", "10.00")))
                .isInstanceOf(ChavePixNaoEncontradaException.class);
    }

    @Test
    void deveRecusarContaDeDestinoInativa() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta(1L, "alice@email.com", "100.00", true)));
        when(contaRepository.findByChavePix("bob@email.com"))
                .thenReturn(Optional.of(conta(2L, "bob@email.com", "50.00", false)));

        assertThatThrownBy(() -> service.transferir(request("bob@email.com", "10.00")))
                .isInstanceOf(ContaInativaException.class)
                .hasMessage("Conta de destino inativa");
    }

    @Test
    void deveRecusarAutotransferencia() {
        Conta conta = conta(1L, "alice@email.com", "100.00", true);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.findByChavePix("alice@email.com")).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.transferir(request("alice@email.com", "10.00")))
                .isInstanceOf(TransferenciaParaMesmaContaException.class);
    }

    @Test
    void deveRecusarValorAcimaDoLimiteSemConsultarBanco() {
        assertThatThrownBy(() -> service.transferir(request("bob@email.com", "5000.01")))
                .isInstanceOf(LimiteTransferenciaExcedidoException.class)
                .hasMessage("Valor excede o limite diário/transação");

        verify(contaRepository, never()).findById(any());
    }

    private TransferenciaPixRequest request(String chave, String valor) {
        return new TransferenciaPixRequest(1L, chave, new BigDecimal(valor));
    }

    private Conta conta(Long id, String chave, String saldo, boolean ativa) {
        Conta conta = new Conta("Titular", chave, new BigDecimal(saldo), ativa);
        conta.setId(id);
        return conta;
    }
}
