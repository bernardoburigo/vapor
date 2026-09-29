package com.example.trabalho1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.trabalho1.entity.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByUsuarioIdAndJogoId(Long usuarioId, Long jogoId);

    List<Avaliacao> findAllByJogoId(Long jogoId);

    List<Avaliacao> findAllByUsuarioId(Long usuarioId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.jogo.id = :jogoId")
    Double calcularMediaPorJogoId(@Param("jogoId") Long jogoId);
}
