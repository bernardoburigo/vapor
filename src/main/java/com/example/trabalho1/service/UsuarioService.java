package com.example.trabalho1.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trabalho1.dto.AuthResponseDTO;
import com.example.trabalho1.dto.LoginRequestDTO;
import com.example.trabalho1.dto.UsuarioRequestDTO;
import com.example.trabalho1.dto.UsuarioResponseDTO;
import com.example.trabalho1.dto.UsuarioUpdateRequestDTO;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.AvaliacaoRepository;
import com.example.trabalho1.repository.UsuarioRepository;
import com.example.trabalho1.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        String email = normalizarEmail(dto.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado", HttpStatus.CONFLICT);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário criado: id={}, email={}", salvo.getId(), salvo.getEmail());
        return UsuarioResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(UsuarioResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateRequestDTO dto) {
        Usuario usuario = buscarEntidadePorId(id);
        String email = normalizarEmail(dto.email());

        if (!usuario.getEmail().equals(email) && usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado", HttpStatus.CONFLICT);
        }

        usuario.setNome(dto.nome());
        usuario.setEmail(email);

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário atualizado: id={}", salvo.getId());
        return UsuarioResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = buscarEntidadePorId(id);

        if (avaliacaoRepository.existsByUsuarioId(id)) {
            throw new BusinessException(
                    "Não é possível remover: o usuário possui avaliações", HttpStatus.CONFLICT);
        }

        usuarioRepository.delete(usuario);
        log.info("Usuário removido: id={}", id);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = autenticar(dto);
        log.info("Login realizado: id={}", usuario.getId());
        return AuthResponseDTO.bearer(
                jwtService.gerarToken(usuario),
                jwtService.getValidadeEmSegundos(),
                UsuarioResponseDTO.fromEntity(usuario));
    }

    /** Valida email e senha. A mensagem é a mesma nos dois casos para não revelar quais emails existem. */
    private Usuario autenticar(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(dto.email()))
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            // O email vem do cliente sem formato validado: remove quebras de linha para evitar injeção no log.
            log.warn("Falha de login para o email {}", dto.email().replaceAll("[\\r\\n\\t]", "_"));
            throw new BusinessException("Email ou senha inválidos", HttpStatus.UNAUTHORIZED);
        }
        return usuario;
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id " + id));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
