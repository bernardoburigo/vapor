package com.example.trabalho1.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.dto.AvaliacaoRequestDTO;
import com.example.trabalho1.dto.AvaliacaoResponseDTO;
import com.example.trabalho1.dto.AvaliacaoUpdateRequestDTO;
import com.example.trabalho1.entity.Avaliacao;
import com.example.trabalho1.entity.Jogo;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.AvaliacaoRepository;
import com.example.trabalho1.repository.JogoRepository;
import com.example.trabalho1.repository.UsuarioRepository;
import com.example.trabalho1.security.UsuarioPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JogoRepository jogoRepository;

    /** Cria a avaliação do usuário autenticado ({@code usuarioId} vem do token, nunca do corpo da requisição). */
    @Transactional
    public AvaliacaoResponseDTO criar(Long usuarioId, AvaliacaoRequestDTO dto) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        Jogo jogo = buscarJogoPorId(dto.jogoId());

        if (avaliacaoRepository.existsByUsuarioIdAndJogoId(usuarioId, dto.jogoId())) {
            throw new BusinessException(
                    "Usuário já avaliou este jogo", HttpStatus.CONFLICT);
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setJogo(jogo);
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        log.info("Avaliação criada: id={}, usuarioId={}, jogoId={}, nota={}",
                salva.getId(), usuario.getId(), jogo.getId(), salva.getNota());
        return AvaliacaoResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listarTodas() {
        return avaliacaoRepository.findAll().stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarPorId(Long id) {
        return AvaliacaoResponseDTO.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listarPorJogo(Long jogoId) {
        buscarJogoPorId(jogoId); // garante que o jogo existe
        return avaliacaoRepository.findAllByJogoId(jogoId).stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        buscarUsuarioPorId(usuarioId); // garante que o usuário existe
        return avaliacaoRepository.findAllByUsuarioId(usuarioId).stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public AvaliacaoResponseDTO atualizar(Long id, AvaliacaoUpdateRequestDTO dto, UsuarioPrincipal solicitante) {
        Avaliacao avaliacao = buscarEntidadePorId(id);
        garantirDonoOuAdmin(avaliacao, solicitante, "alterar");

        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        log.info("Avaliação atualizada: id={}, nota={}", salva.getId(), salva.getNota());
        return AvaliacaoResponseDTO.fromEntity(salva);
    }

    @Transactional
    public void deletar(Long id, UsuarioPrincipal solicitante) {
        Avaliacao avaliacao = buscarEntidadePorId(id);
        garantirDonoOuAdmin(avaliacao, solicitante, "remover");
        avaliacaoRepository.delete(avaliacao);
        log.info("Avaliação removida: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /** Só o autor da avaliação ou um ADMIN pode alterá-la ou removê-la. */
    private void garantirDonoOuAdmin(Avaliacao avaliacao, UsuarioPrincipal solicitante, String acao) {
        boolean dono = avaliacao.getUsuario().getId().equals(solicitante.getId());
        if (!dono && !solicitante.isAdmin()) {
            throw new BusinessException(
                    "Você só pode " + acao + " as suas próprias avaliações", HttpStatus.FORBIDDEN);
        }
    }

    private Avaliacao buscarEntidadePorId(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Avaliação não encontrada com id " + id));
    }

    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com id " + id));
    }

    private Jogo buscarJogoPorId(Long id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jogo não encontrado com id " + id));
    }
}
