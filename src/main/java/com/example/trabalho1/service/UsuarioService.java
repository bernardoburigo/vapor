package com.example.trabalho1.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.trabalho1.dto.LoginRequestDTO;
import com.example.trabalho1.dto.UsuarioRequestDTO;
import com.example.trabalho1.dto.UsuarioResponseDTO;
import com.example.trabalho1.dto.UsuarioUpdateRequestDTO;
import com.example.trabalho1.entity.Usuario;
import com.example.trabalho1.exception.BusinessException;
import com.example.trabalho1.exception.ResourceNotFoundException;
import com.example.trabalho1.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado", HttpStatus.CONFLICT);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));

        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.fromEntity(buscarEntidadePorId(id));
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateRequestDTO dto) {
        Usuario usuario = buscarEntidadePorId(id);

        if (!usuario.getEmail().equalsIgnoreCase(dto.email())
                && usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado", HttpStatus.CONFLICT);
        }

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    public void deletar(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        usuarioRepository.delete(usuario);
    }

    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BusinessException("Email ou senha inválidos", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new BusinessException("Email ou senha inválidos", HttpStatus.UNAUTHORIZED);
        }

        return UsuarioResponseDTO.fromEntity(usuario);
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id " + id));
    }
}
