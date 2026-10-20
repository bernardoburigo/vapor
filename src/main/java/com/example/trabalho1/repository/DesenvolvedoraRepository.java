package com.example.trabalho1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trabalho1.entity.Desenvolvedora;

public interface DesenvolvedoraRepository extends JpaRepository<Desenvolvedora, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Desenvolvedora> findAllByNomeContainingIgnoreCase(String nome);

    List<Desenvolvedora> findAllByPaisOrigemIgnoreCase(String paisOrigem);
}
