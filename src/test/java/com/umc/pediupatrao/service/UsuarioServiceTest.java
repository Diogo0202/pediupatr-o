package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setId("1");
        u.setUsername("joao");
        u.setPassword("senha123");
        u.setRole("USER");
        return u;
    }

    @Test
    void salvarUsuario_criptografaSenhaAntesDePersistir() {
        when(passwordEncoder.encode("senha123")).thenReturn("HASH");

        usuarioService.salvarUsuario(usuario());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("HASH", captor.getValue().getPassword());
    }

    @Test
    void atualizarUsuario_atualizaUsernameRoleENovaSenha() {
        Usuario existente = usuario();
        existente.setPassword("HASH_ANTIGO");
        when(usuarioRepository.findById("1")).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nova")).thenReturn("HASH_NOVO");

        Usuario alterado = new Usuario();
        alterado.setUsername("joao.silva");
        alterado.setRole("ADMIN");
        alterado.setPassword("nova");

        usuarioService.atualizarUsuario("1", alterado);

        assertEquals("joao.silva", existente.getUsername());
        assertEquals("ADMIN", existente.getRole());
        assertEquals("HASH_NOVO", existente.getPassword());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void atualizarUsuario_senhaEmBrancoMantemSenhaAtual() {
        Usuario existente = usuario();
        existente.setPassword("HASH_ANTIGO");
        when(usuarioRepository.findById("1")).thenReturn(Optional.of(existente));

        Usuario alterado = new Usuario();
        alterado.setUsername("joao");
        alterado.setRole("USER");
        alterado.setPassword("   ");

        usuarioService.atualizarUsuario("1", alterado);

        assertEquals("HASH_ANTIGO", existente.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void atualizarUsuario_senhaNulaMantemSenhaAtual() {
        Usuario existente = usuario();
        existente.setPassword("HASH_ANTIGO");
        when(usuarioRepository.findById("1")).thenReturn(Optional.of(existente));

        Usuario alterado = new Usuario();
        alterado.setUsername("joao");
        alterado.setRole("USER");

        usuarioService.atualizarUsuario("1", alterado);

        assertEquals("HASH_ANTIGO", existente.getPassword());
    }

    @Test
    void atualizarUsuario_idInexistenteNaoFazNada() {
        when(usuarioRepository.findById("404")).thenReturn(Optional.empty());

        usuarioService.atualizarUsuario("404", usuario());

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void buscarPorId_devolveOptional() {
        when(usuarioRepository.findById("1")).thenReturn(Optional.of(usuario()));
        when(usuarioRepository.findById("2")).thenReturn(Optional.empty());

        assertTrue(usuarioService.buscarPorId("1").isPresent());
        assertTrue(usuarioService.buscarPorId("2").isEmpty());
    }

    @Test
    void buscarPorUsername_devolveOptional() {
        when(usuarioRepository.findByUsername("joao")).thenReturn(Optional.of(usuario()));
        when(usuarioRepository.findByUsername("outro")).thenReturn(Optional.empty());

        assertTrue(usuarioService.buscarPorUsername("joao").isPresent());
        assertTrue(usuarioService.buscarPorUsername("outro").isEmpty());
    }

    @Test
    void buscarPorEmail_devolveOptional() {
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario()));
        when(usuarioRepository.findByEmail("outro@email.com")).thenReturn(Optional.empty());

        assertTrue(usuarioService.buscarPorEmail("joao@email.com").isPresent());
        assertTrue(usuarioService.buscarPorEmail("outro@email.com").isEmpty());
    }

    @Test
    void existeUsernameOuEmail_detectaDuplicidade() {
        when(usuarioRepository.findByUsername("joao")).thenReturn(Optional.of(usuario()));
        assertTrue(usuarioService.existeUsernameOuEmail("joao", ""));

        when(usuarioRepository.findByUsername("novo")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("novo@email.com")).thenReturn(Optional.of(usuario()));
        assertTrue(usuarioService.existeUsernameOuEmail("novo", "novo@email.com"));

        when(usuarioRepository.findByEmail("livre@email.com")).thenReturn(Optional.empty());
        assertFalse(usuarioService.existeUsernameOuEmail("novo", "livre@email.com"));
    }

    @Test
    void deletarUsuario_delegaParaRepositorio() {
        usuarioService.deletarUsuario("1");
        verify(usuarioRepository).deleteById("1");
    }

    @Test
    void listarTodos_devolveResultadoDoRepositorio() {
        List<Usuario> usuarios = List.of(usuario());
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        assertEquals(usuarios, usuarioService.listarTodos());
    }
}
