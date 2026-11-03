package com.example.trabalho1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.trabalho1.entity.Jogo;

public interface JogoRepository extends JpaRepository<Jogo, Long>, JpaSpecificationExecutor<Jogo> {

    boolean existsByDesenvolvedoraId(Long desenvolvedoraId);

    boolean existsByCategoriasId(Long categoriaId);
}
