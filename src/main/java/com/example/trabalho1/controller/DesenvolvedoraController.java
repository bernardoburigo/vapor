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

import com.example.trabalho1.dto.DesenvolvedoraRequestDTO;
import com.example.trabalho1.dto.DesenvolvedoraResponseDTO;
import com.example.trabalho1.service.DesenvolvedoraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/desenvolvedoras")
@RequiredArgsConstructor
@Tag(name = "Desenvolvedoras", description = "Cadastro de estúdios e publicadoras de jogos")
public class DesenvolvedoraController {

    private final DesenvolvedoraService desenvolvedoraService;

    @Operation(summary = "Cadastrar desenvolvedora",
            description = "Cria uma desenvolvedora. O nome não pode se repetir (409 se já existir).")
    @PostMapping
    public ResponseEntity<DesenvolvedoraResponseDTO> criar(
            @Valid @RequestBody DesenvolvedoraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(desenvolvedoraService.criar(dto));
    }

    @Operation(summary = "Listar desenvolvedoras",
            description = "Lista todas, ou filtra por parte do nome (?nome=) ou por país (?paisOrigem=).")
    @GetMapping
    public ResponseEntity<List<DesenvolvedoraResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String paisOrigem) {
        return ResponseEntity.ok(desenvolvedoraService.listar(nome, paisOrigem));
    }

    @Operation(summary = "Buscar desenvolvedora por id")
    @GetMapping("/{id}")
    public ResponseEntity<DesenvolvedoraResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(desenvolvedoraService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar desenvolvedora")
    @PutMapping("/{id}")
    public ResponseEntity<DesenvolvedoraResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DesenvolvedoraRequestDTO dto) {
        return ResponseEntity.ok(desenvolvedoraService.atualizar(id, dto));
    }

    @Operation(summary = "Remover desenvolvedora",
            description = "Remove a desenvolvedora. Retorna 409 se ela ainda tiver jogos cadastrados.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        desenvolvedoraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
