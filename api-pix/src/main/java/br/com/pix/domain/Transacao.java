package br.com.pix.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private Conta contaOrigem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_destino_id", nullable = false)
    private Conta contaDestino;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_transacao", nullable = false, length = 20)
    private StatusTransacao status;

    protected Transacao() {
    }

    public Transacao(Conta contaOrigem, Conta contaDestino, BigDecimal valor, LocalDateTime dataHora,
                     StatusTransacao status) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        this.dataHora = dataHora;
        this.status = status;
    }

    public Long getId() { return id; }
    public Conta getContaOrigem() { return contaOrigem; }
    public Conta getContaDestino() { return contaDestino; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getDataHora() { return dataHora; }
    public StatusTransacao getStatus() { return status; }
}
