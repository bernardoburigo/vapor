package com.example.trabalho1.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.trabalho1.entity.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByUsuarioIdAndJogoId(Long usuarioId, Long jogoId);

    boolean existsByJogoId(Long jogoId);

    boolean existsByUsuarioId(Long usuarioId);

    List<Avaliacao> findAllByJogoId(Long jogoId);

    List<Avaliacao> findAllByUsuarioId(Long usuarioId);

    /**
     * Nota média e quantidade de avaliações de vários jogos em uma única consulta (evita N+1 nas listagens).
     * Jogos sem avaliações não aparecem no resultado.
     */
    @Query("""
            SELECT a.jogo.id AS jogoId, AVG(a.nota) AS media, COUNT(a) AS total
            FROM Avaliacao a
            WHERE a.jogo.id IN :jogoIds
            GROUP BY a.jogo.id
            """)
    List<ResumoNota> resumirNotasPorJogo(@Param("jogoIds") Collection<Long> jogoIds);

    interface ResumoNota {

        Long getJogoId();

        Double getMedia();

        Long getTotal();
    }
}
