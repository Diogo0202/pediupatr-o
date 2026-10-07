package com.umc.pediupatrao.service;

import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOuEmail) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(usernameOuEmail)
                .or(() -> usuarioRepository.findByEmail(usernameOuEmail))
                .map(usuario -> User.builder()
                        .username(usuario.getUsername() != null && !usuario.getUsername().isBlank()
                                ? usuario.getUsername() : usuario.getEmail())
                        .password(usuario.getPassword())
                        // ✅ Papel vindo do cadastro (ADMIN, ATENDENTE, GERENTE, FORNECEDOR); sem role cadastrada assume USER
                        .roles(papel(usuario.getRole()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + usernameOuEmail));
    }

    private String papel(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        // Remove o prefixo "ROLE_" caso já venha gravado assim
        return role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
    }
}
