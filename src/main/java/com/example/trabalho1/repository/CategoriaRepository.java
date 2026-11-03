package com.example.trabalho1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trabalho1.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Categoria> findAllByNomeContainingIgnoreCase(String nome);
}
