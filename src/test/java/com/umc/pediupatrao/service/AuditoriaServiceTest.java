package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.LogAuditoria;
import com.umc.pediupatrao.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private LogAuditoriaRepository repository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    void registrar_gravaLogComUsuarioInformado() {
        when(repository.save(any(LogAuditoria.class))).thenAnswer(inv -> inv.getArgument(0));

        auditoriaService.registrar("joao", "CRIAR", "/pedidos", "201", "127.0.0.1", "Pedido criado");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());
        LogAuditoria log = captor.getValue();
        assertEquals("joao", log.getUsuario());
        assertEquals("CRIAR", log.getAcao());
        assertEquals("/pedidos", log.getRecurso());
        assertEquals("201", log.getStatus());
        assertEquals("127.0.0.1", log.getIp());
        assertEquals("Pedido criado", log.getDetalhe());
        assertNotNull(log.getDataHora());
    }

    @Test
    void registrar_usuarioNuloUsaTraco() {
        when(repository.save(any(LogAuditoria.class))).thenAnswer(inv -> inv.getArgument(0));

        auditoriaService.registrar(null, "LOGIN", "/login", "401", null, "Falha");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());
        assertEquals("-", captor.getValue().getUsuario());
    }

    @Test
    void registrar_falhaNoRepositorioNaoPropagaExcecao() {
        when(repository.save(any(LogAuditoria.class))).thenThrow(new RuntimeException("banco fora"));

        assertDoesNotThrow(() ->
                auditoriaService.registrar("joao", "CRIAR", "/pedidos", "201", null, "detalhe"));
    }

    @Test
    void listarRecentes_devolveResultadoDoRepositorio() {
        List<LogAuditoria> logs = List.of(new LogAuditoria());
        when(repository.findTop50ByOrderByDataHoraDesc()).thenReturn(logs);

        assertEquals(logs, auditoriaService.listarRecentes());
    }
}
