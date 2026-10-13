package br.com.pix.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titular;

    @Column(name = "chave_pix", nullable = false, unique = true, length = 100)
    private String chavePix;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    @Column(name = "status_ativa", nullable = false)
    private boolean ativa;

    protected Conta() {
    }

    public Conta(String titular, String chavePix, BigDecimal saldo, boolean ativa) {
        this.titular = titular;
        this.chavePix = chavePix;
        this.saldo = saldo;
        this.ativa = ativa;
    }

    public Long getId() { return id; }
    public String getTitular() { return titular; }
    public String getChavePix() { return chavePix; }
    public BigDecimal getSaldo() { return saldo; }
    public boolean isAtiva() { return ativa; }

    public void debitar(BigDecimal valor) { this.saldo = this.saldo.subtract(valor); }
    public void creditar(BigDecimal valor) { this.saldo = this.saldo.add(valor); }

    // Útil em testes sem persistência.
    public void setId(Long id) { this.id = id; }
}
