package br.com.pix.controller;

import br.com.pix.domain.Conta;
import br.com.pix.repository.ContaRepository;
import br.com.pix.repository.TransacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransferenciaControllerIT {

    private static final String URL = "/api/v1/pix/transferir";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ContaRepository contaRepository;
    @Autowired private TransacaoRepository transacaoRepository;

    private Conta origem;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();
        origem = contaRepository.save(new Conta("Alice", "alice@email.com", new BigDecimal("100.00"), true));
        contaRepository.save(new Conta("Bob", "bob@email.com", new BigDecimal("50.00"), true));
    }

    @Test
    void deveRealizarTransferenciaERetornar200() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(origem.getId(), "bob@email.com", "40.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaOrigemId").value(origem.getId()))
                .andExpect(jsonPath("$.valor").value(40.00))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        assertThat(contaRepository.findById(origem.getId()).orElseThrow().getSaldo()).isEqualByComparingTo("60.00");
        assertThat(contaRepository.findByChavePix("bob@email.com").orElseThrow().getSaldo()).isEqualByComparingTo("90.00");
        assertThat(transacaoRepository.count()).isEqualTo(1);
    }

    @Test
    void deveRetornar404QuandoChavePixNaoExiste() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body(origem.getId(), "inexistente@email.com", "10.00")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chave PIX não encontrada"));
    }

    @Test
    void deveRetornar400QuandoSaldoForInsuficiente() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body(origem.getId(), "bob@email.com", "101.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo insuficiente para realizar a transferência"));
    }

    @Test
    void deveRetornar400ParaAutotransferencia() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body(origem.getId(), "alice@email.com", "10.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Não é possível transferir para a mesma conta"));
    }

    @Test
    void deveRetornar422QuandoValorExcederLimite() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body(origem.getId(), "bob@email.com", "5000.01")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Valor excede o limite diário/transação"));
    }

    @Test
    void deveRetornar400ParaCorpoInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contaOrigemId\":null,\"chavePixDestino\":\"\",\"valor\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private String body(Long origemId, String chavePixDestino, String valor) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "contaOrigemId", origemId,
                "chavePixDestino", chavePixDestino,
                "valor", new BigDecimal(valor)));
    }
}
