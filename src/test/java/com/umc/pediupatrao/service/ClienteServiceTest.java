package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteComId() {
        Cliente c = new Cliente();
        c.setId("1");
        c.setNome("João");
        c.setTelefone("11999999999");
        c.setCep("01001-000");
        c.setLogradouro("Praça da Sé");
        c.setNumero("10");
        c.setBairro("Sé");
        c.setCidade("São Paulo");
        c.setEstado("SP");
        c.setConsentimentoLgpd(true);
        return c;
    }

    @Test
    void temPedidosVinculados_quandoHaPedidosRetornaTrue() {
        when(pedidoRepository.countByClienteId("1")).thenReturn(3L);
        assertTrue(clienteService.temPedidosVinculados("1"));
    }

    @Test
    void temPedidosVinculados_quandoNaoHaRetornaFalse() {
        when(pedidoRepository.countByClienteId("1")).thenReturn(0L);
        assertFalse(clienteService.temPedidosVinculados("1"));
    }

    @Test
    void anonimizarDados_substituiCamposPessoais() {
        Cliente cliente = clienteComId();
        when(clienteRepository.findById("1")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteService.anonimizarDados("1");

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        Cliente salvo = captor.getValue();
        assertEquals("[DADOS REMOVIDOS - LGPD]", salvo.getNome());
        assertEquals("[REMOVIDO]", salvo.getTelefone());
        assertEquals("--", salvo.getEstado());
        assertFalse(salvo.getConsentimentoLgpd());
        assertNull(salvo.getDataConsentimento());
    }

    @Test
    void anonimizarDados_clienteInexistenteNaoFazNada() {
        when(clienteRepository.findById("404")).thenReturn(Optional.empty());

        clienteService.anonimizarDados("404");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void salvar_clienteNovoCriaRegistro() {
        Cliente c = new Cliente();
        c.setNome("Maria");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente salvo = clienteService.salvar(c);

        assertNotNull(salvo);
        verify(clienteRepository).save(c);
    }

    @Test
    void salvar_clienteExistenteAtualiza() {
        Cliente c = clienteComId();
        when(clienteRepository.existsById("1")).thenReturn(true);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> clienteService.salvar(c));
        verify(clienteRepository).save(c);
    }

    @Test
    void salvar_clienteInexistenteLancaExcecao() {
        Cliente c = clienteComId();
        when(clienteRepository.existsById("1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(c));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void excluir_delegaParaRepositorio() {
        clienteService.excluir("1");
        verify(clienteRepository).deleteById("1");
    }

    @Test
    void listarClientes_devolveResultadoDoRepositorio() {
        List<Cliente> clientes = List.of(clienteComId());
        when(clienteRepository.findAll()).thenReturn(clientes);

        assertEquals(clientes, clienteService.listarClientes());
    }

    @Test
    void buscarPorId_devolveOptional() {
        Cliente c = clienteComId();
        when(clienteRepository.findById("1")).thenReturn(Optional.of(c));
        when(clienteRepository.findById("2")).thenReturn(Optional.empty());

        assertTrue(clienteService.buscarPorId("1").isPresent());
        assertTrue(clienteService.buscarPorId("2").isEmpty());
    }

    @Test
    void novoCliente_salvaDireto() {
        Cliente c = new Cliente();
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        assertNotNull(clienteService.novoCliente(c));
        verify(clienteRepository).save(c);
    }
}
