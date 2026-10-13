package br.com.pix.repository;

import br.com.pix.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    Optional<Conta> findByChavePix(String chavePix);
}
