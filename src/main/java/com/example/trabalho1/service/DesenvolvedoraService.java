package com.example.trabalho1.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.dto.DesenvolvedoraRequestDTO;
import com.example.trabalho1.dto.DesenvolvedoraResponseDTO;
import com.example.trabalho1.entity.Desenvolvedora;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.DesenvolvedoraRepository;
import com.example.trabalho1.repository.JogoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesenvolvedoraService {

    private final DesenvolvedoraRepository desenvolvedoraRepository;
    private final JogoRepository jogoRepository;

    @Transactional
    public DesenvolvedoraResponseDTO criar(DesenvolvedoraRequestDTO dto) {
        String nome = dto.nome().trim();

        if (desenvolvedoraRepository.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(
                    "Já existe uma desenvolvedora com o nome " + nome, HttpStatus.CONFLICT);
        }

        Desenvolvedora desenvolvedora = new Desenvolvedora();
        preencher(desenvolvedora, dto);

        Desenvolvedora salva = desenvolvedoraRepository.save(desenvolvedora);
        log.info("Desenvolvedora criada: id={}, nome={}", salva.getId(), salva.getNome());
        return DesenvolvedoraResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<DesenvolvedoraResponseDTO> listar(String nome, String paisOrigem) {
        List<Desenvolvedora> resultado;
        if (nome != null && !nome.isBlank()) {
            resultado = desenvolvedoraRepository.findAllByNomeContainingIgnoreCase(nome.trim());
        } else if (paisOrigem != null && !paisOrigem.isBlank()) {
            resultado = desenvolvedoraRepository.findAllByPaisOrigemIgnoreCase(paisOrigem.trim());
        } else {
            resultado = desenvolvedoraRepository.findAll();
        }
        return resultado.stream()
                .map(DesenvolvedoraResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DesenvolvedoraResponseDTO buscarPorId(Long id) {
        return DesenvolvedoraResponseDTO.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public DesenvolvedoraResponseDTO atualizar(Long id, DesenvolvedoraRequestDTO dto) {
        Desenvolvedora desenvolvedora = buscarEntidadePorId(id);
        String nome = dto.nome().trim();

        if (desenvolvedoraRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new BusinessException(
                    "Já existe uma desenvolvedora com o nome " + nome, HttpStatus.CONFLICT);
        }

        preencher(desenvolvedora, dto);

        Desenvolvedora salva = desenvolvedoraRepository.save(desenvolvedora);
        log.info("Desenvolvedora atualizada: id={}", salva.getId());
        return DesenvolvedoraResponseDTO.fromEntity(salva);
    }

    @Transactional
    public void deletar(Long id) {
        Desenvolvedora desenvolvedora = buscarEntidadePorId(id);

        if (jogoRepository.existsByDesenvolvedoraId(id)) {
            throw new BusinessException(
                    "Não é possível remover: a desenvolvedora possui jogos cadastrados",
                    HttpStatus.CONFLICT);
        }

        desenvolvedoraRepository.delete(desenvolvedora);
        log.info("Desenvolvedora removida: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private Desenvolvedora buscarEntidadePorId(Long id) {
        return desenvolvedoraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Desenvolvedora não encontrada com id " + id));
    }

    private void preencher(Desenvolvedora desenvolvedora, DesenvolvedoraRequestDTO dto) {
        desenvolvedora.setNome(dto.nome().trim());
        desenvolvedora.setPaisOrigem(dto.paisOrigem());
        desenvolvedora.setSite(dto.site());
    }
}
