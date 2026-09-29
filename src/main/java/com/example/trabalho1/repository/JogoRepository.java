package com.example.trabalho1.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trabalho1.entity.Jogo;

/**
 * Repositório de Jogo — responsabilidade de Bruno Baldessar.
 * Declarado aqui para satisfazer a injeção em AvaliacaoService.
 */
public interface JogoRepository extends JpaRepository<Jogo, Long> {
}
