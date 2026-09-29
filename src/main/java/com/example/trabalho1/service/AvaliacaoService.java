package com.example.trabalho1.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JogoRepository jogoRepository;

    public AvaliacaoResponseDTO criar(AvaliacaoRequestDTO dto) {
        Usuario usuario = buscarUsuarioPorId(dto.usuarioId());
        Jogo jogo = buscarJogoPorId(dto.jogoId());

        if (avaliacaoRepository.existsByUsuarioIdAndJogoId(dto.usuarioId(), dto.jogoId())) {
            throw new BusinessException(
                    "Usuário já avaliou este jogo", HttpStatus.CONFLICT);
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setJogo(jogo);
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        return AvaliacaoResponseDTO.fromEntity(avaliacaoRepository.save(avaliacao));
    }

    public List<AvaliacaoResponseDTO> listarTodas() {
        return avaliacaoRepository.findAll().stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    public AvaliacaoResponseDTO buscarPorId(Long id) {
        return AvaliacaoResponseDTO.fromEntity(buscarEntidadePorId(id));
    }

    public List<AvaliacaoResponseDTO> listarPorJogo(Long jogoId) {
        buscarJogoPorId(jogoId); // garante que o jogo existe
        return avaliacaoRepository.findAllByJogoId(jogoId).stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    public List<AvaliacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        buscarUsuarioPorId(usuarioId); // garante que o usuário existe
        return avaliacaoRepository.findAllByUsuarioId(usuarioId).stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    public AvaliacaoResponseDTO atualizar(Long id, AvaliacaoUpdateRequestDTO dto) {
        Avaliacao avaliacao = buscarEntidadePorId(id);

        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        return AvaliacaoResponseDTO.fromEntity(avaliacaoRepository.save(avaliacao));
    }

    public void deletar(Long id) {
        Avaliacao avaliacao = buscarEntidadePorId(id);
        avaliacaoRepository.delete(avaliacao);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

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
