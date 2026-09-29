package com.example.trabalho1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.trabalho1.dto.AvaliacaoRequestDTO;
import com.example.trabalho1.dto.AvaliacaoResponseDTO;
import com.example.trabalho1.dto.AvaliacaoUpdateRequestDTO;
import com.example.trabalho1.service.AvaliacaoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> criar(
            @Valid @RequestBody AvaliacaoRequestDTO dto) {
        AvaliacaoResponseDTO avaliacao = avaliacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
    }

    @GetMapping
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarTodas(
            @RequestParam(required = false) Long jogoId,
            @RequestParam(required = false) Long usuarioId) {

        if (jogoId != null) {
            return ResponseEntity.ok(avaliacaoService.listarPorJogo(jogoId));
        }
        if (usuarioId != null) {
            return ResponseEntity.ok(avaliacaoService.listarPorUsuario(usuarioId));
        }
        return ResponseEntity.ok(avaliacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AvaliacaoUpdateRequestDTO dto) {
        return ResponseEntity.ok(avaliacaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        avaliacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
