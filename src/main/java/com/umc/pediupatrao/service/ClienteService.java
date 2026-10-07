package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    /** LGPD: decide entre exclusão total e anonimização. */
    public boolean temPedidosVinculados(String clienteId) {
        return pedidoRepository.countByClienteId(clienteId) > 0;
    }

    public Cliente novoCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // Método para excluir cliente
    public void excluir(String id) {
        clienteRepository.deleteById(id);
    }

    /**
     * LGPD - Art. 18: anonimiza os dados pessoais do cliente, mantendo apenas o
     * identificador para preservar o histórico de pedidos já entregues.
     */
    public void anonimizarDados(String id) {
        clienteRepository.findById(id).ifPresent(cliente -> {
            cliente.setNome("[DADOS REMOVIDOS - LGPD]");
            cliente.setTelefone("[REMOVIDO]");
            cliente.setCep("[REMOVIDO]");
            cliente.setLogradouro("[REMOVIDO]");
            cliente.setNumero("[REMOVIDO]");
            cliente.setComplemento("[REMOVIDO]");
            cliente.setBairro("[REMOVIDO]");
            cliente.setCidade("[REMOVIDO]");
            cliente.setEstado("--");
            cliente.setEndereco("[REMOVIDO]");
            cliente.setConsentimentoLgpd(false);
            cliente.setDataConsentimento(null);
            clienteRepository.save(cliente);
        });
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    // Método para salvar um novo cliente ou atualizar um cliente existente
    public Cliente salvar(Cliente cliente) {
        // Se o cliente não tem ID (novo cliente), salva como novo
        if (cliente.getId() == null) {
            return clienteRepository.save(cliente);  // Cria um novo cliente
        } // Se já tem ID (cliente existente), atualiza
        else {
            // Verifica se o cliente existe antes de atualizar
            if (clienteRepository.existsById(cliente.getId())) {
                return clienteRepository.save(cliente);  // Atualiza o cliente existente
            } else {
                throw new IllegalArgumentException("Cliente não encontrado para atualização.");
            }
        }
    }

}
