package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    private Usuario usuario(String role) {
        Usuario u = new Usuario();
        u.setUsername("joao");
        u.setPassword("HASH");
        u.setRole(role);
        return u;
    }

    @Test
    void loadUserByUsername_comRoleAdmin() {
        when(usuarioRepository.findByUsername("joao")).thenReturn(Optional.of(usuario("ADMIN")));

        UserDetails user = usuarioDetailsService.loadUserByUsername("joao");

        assertEquals("joao", user.getUsername());
        assertEquals("HASH", user.getPassword());
        assertTrue(user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet())
                .contains("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsername_semRoleAssumeUser() {
        when(usuarioRepository.findByUsername("joao")).thenReturn(Optional.of(usuario(null)));

        UserDetails user = usuarioDetailsService.loadUserByUsername("joao");

        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_roleComPrefixoRoleRemovido() {
        when(usuarioRepository.findByUsername("joao")).thenReturn(Optional.of(usuario("ROLE_ADMIN")));

        UserDetails user = usuarioDetailsService.loadUserByUsername("joao");

        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_roleEmBrancoAssumeUser() {
        when(usuarioRepository.findByUsername("joao")).thenReturn(Optional.of(usuario("   ")));

        UserDetails user = usuarioDetailsService.loadUserByUsername("joao");

        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_inexistenteLancaExcecao() {
        when(usuarioRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername("ghost"));
    }
}
