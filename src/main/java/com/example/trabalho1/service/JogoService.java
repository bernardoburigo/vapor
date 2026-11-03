package com.example.trabalho1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.dto.JogoRequestDTO;
import com.example.trabalho1.dto.JogoResponseDTO;
import com.example.trabalho1.entity.Categoria;
import com.example.trabalho1.entity.Desenvolvedora;
import com.example.trabalho1.entity.Jogo;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.AvaliacaoRepository;
import com.example.trabalho1.repository.AvaliacaoRepository.ResumoNota;
import com.example.trabalho1.repository.CategoriaRepository;
import com.example.trabalho1.repository.DesenvolvedoraRepository;
import com.example.trabalho1.repository.JogoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JogoService {

    private final JogoRepository jogoRepository;
    private final DesenvolvedoraRepository desenvolvedoraRepository;
    private final CategoriaRepository categoriaRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Transactional
    public JogoResponseDTO criar(JogoRequestDTO dto) {
        Jogo jogo = new Jogo();
        preencher(jogo, dto);

        Jogo salvo = jogoRepository.save(jogo);
        log.info("Jogo criado: id={}, titulo={}", salvo.getId(), salvo.getTitulo());
        return toResponse(salvo);
    }

    /** Lista jogos (RF08): filtros opcionais e combináveis por categoria, desenvolvedora e trecho do título. */
    @Transactional(readOnly = true)
    public Page<JogoResponseDTO> listar(Long categoriaId, Long desenvolvedoraId, String titulo, Pageable pageable) {
        Page<Jogo> pagina = jogoRepository.findAll(montarFiltro(categoriaId, desenvolvedoraId, titulo), pageable);

        Map<Long, ResumoNota> notas = buscarNotas(pagina.getContent());
        return pagina.map(jogo -> toResponse(jogo, notas.get(jogo.getId())));
    }

    @Transactional(readOnly = true)
    public JogoResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public JogoResponseDTO atualizar(Long id, JogoRequestDTO dto) {
        Jogo jogo = buscarEntidadePorId(id);
        preencher(jogo, dto);

        Jogo salvo = jogoRepository.save(jogo);
        log.info("Jogo atualizado: id={}", salvo.getId());
        return toResponse(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        Jogo jogo = buscarEntidadePorId(id);

        if (avaliacaoRepository.existsByJogoId(id)) {
            throw new BusinessException(
                    "Não é possível remover: o jogo possui avaliações", HttpStatus.CONFLICT);
        }

        jogoRepository.delete(jogo);
        log.info("Jogo removido: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private Jogo buscarEntidadePorId(Long id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado com id " + id));
    }

    private void preencher(Jogo jogo, JogoRequestDTO dto) {
        Desenvolvedora desenvolvedora = desenvolvedoraRepository.findById(dto.desenvolvedoraId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Desenvolvedora não encontrada com id " + dto.desenvolvedoraId()));

        jogo.setTitulo(dto.titulo().trim());
        jogo.setDescricao(dto.descricao());
        jogo.setPreco(dto.preco());
        jogo.setDataLancamento(dto.dataLancamento());
        jogo.setDesenvolvedora(desenvolvedora);
        jogo.setCategorias(buscarCategorias(dto.categoriaIds()));
    }

    private Set<Categoria> buscarCategorias(Set<Long> ids) {
        List<Categoria> encontradas = categoriaRepository.findAllById(ids);

        if (encontradas.size() != ids.size()) {
            Set<Long> ausentes = new HashSet<>(ids);
            encontradas.forEach(c -> ausentes.remove(c.getId()));
            throw new ResourceNotFoundException("Categoria(s) não encontrada(s) com id: " + ausentes);
        }
        return new HashSet<>(encontradas);
    }

    private Specification<Jogo> montarFiltro(Long categoriaId, Long desenvolvedoraId, String titulo) {
        Specification<Jogo> filtro = (root, query, cb) -> cb.conjunction();

        if (categoriaId != null) {
            // Chave composta em jogo_categoria: cada jogo casa no máximo uma linha do join, sem duplicatas.
            filtro = filtro.and((root, query, cb) -> cb.equal(root.join("categorias").get("id"), categoriaId));
        }
        if (desenvolvedoraId != null) {
            filtro = filtro.and((root, query, cb) ->
                    cb.equal(root.get("desenvolvedora").get("id"), desenvolvedoraId));
        }
        if (titulo != null && !titulo.isBlank()) {
            String padrao = "%" + escaparLike(titulo.trim().toLowerCase()) + "%";
            filtro = filtro.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("titulo")), padrao, '\\'));
        }
        return filtro;
    }

    private String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private Map<Long, ResumoNota> buscarNotas(List<Jogo> jogos) {
        if (jogos.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = jogos.stream().map(Jogo::getId).toList();
        return avaliacaoRepository.resumirNotasPorJogo(ids).stream()
                .collect(Collectors.toMap(ResumoNota::getJogoId, Function.identity()));
    }

    private JogoResponseDTO toResponse(Jogo jogo) {
        ResumoNota resumo = buscarNotas(List.of(jogo)).get(jogo.getId());
        return toResponse(jogo, resumo);
    }

    private JogoResponseDTO toResponse(Jogo jogo, ResumoNota resumo) {
        if (resumo == null) {
            return JogoResponseDTO.fromEntity(jogo, null, 0);
        }
        // RF09: média arredondada para uma casa decimal.
        Double media = BigDecimal.valueOf(resumo.getMedia()).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return JogoResponseDTO.fromEntity(jogo, media, resumo.getTotal());
    }
}
