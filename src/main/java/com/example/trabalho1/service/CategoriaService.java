package com.example.trabalho1.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.dto.CategoriaRequestDTO;
import com.example.trabalho1.dto.CategoriaResponseDTO;
import com.example.trabalho1.entity.Categoria;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.CategoriaRepository;
import com.example.trabalho1.repository.JogoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final JogoRepository jogoRepository;

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        String nome = dto.nome().trim();

        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(
                    "Já existe uma categoria com o nome " + nome, HttpStatus.CONFLICT);
        }

        Categoria categoria = new Categoria();
        preencher(categoria, dto);

        Categoria salva = categoriaRepository.save(categoria);
        log.info("Categoria criada: id={}, nome={}", salva.getId(), salva.getNome());
        return CategoriaResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar(String nome) {
        List<Categoria> resultado = (nome != null && !nome.isBlank())
                ? categoriaRepository.findAllByNomeContainingIgnoreCase(nome.trim())
                : categoriaRepository.findAll();
        return resultado.stream()
                .map(CategoriaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        return CategoriaResponseDTO.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = buscarEntidadePorId(id);
        String nome = dto.nome().trim();

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new BusinessException(
                    "Já existe uma categoria com o nome " + nome, HttpStatus.CONFLICT);
        }

        preencher(categoria, dto);

        Categoria salva = categoriaRepository.save(categoria);
        log.info("Categoria atualizada: id={}", salva.getId());
        return CategoriaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public void deletar(Long id) {
        Categoria categoria = buscarEntidadePorId(id);

        if (jogoRepository.existsByCategoriasId(id)) {
            throw new BusinessException(
                    "Não é possível remover: a categoria está associada a jogos",
                    HttpStatus.CONFLICT);
        }

        categoriaRepository.delete(categoria);
        log.info("Categoria removida: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private Categoria buscarEntidadePorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria não encontrada com id " + id));
    }

    private void preencher(Categoria categoria, CategoriaRequestDTO dto) {
        categoria.setNome(dto.nome().trim());
        categoria.setDescricao(dto.descricao());
    }
}
