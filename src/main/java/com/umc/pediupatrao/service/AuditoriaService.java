package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.LogAuditoria;
import com.umc.pediupatrao.repository.LogAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final LogAuditoriaRepository repository;

    public AuditoriaService(LogAuditoriaRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra uma ação auditável. Nunca lança exceção: auditoria não pode derrubar o fluxo de negócio.
     */
    public void registrar(String usuario, String acao, String recurso, String status,
                          String ip, String detalhe) {
        try {
            repository.save(new LogAuditoria(Instant.now(),
                    usuario == null ? "-" : usuario,
                    acao, recurso, status, ip, detalhe));
        } catch (Exception e) {
            log.warn("Falha ao gravar log de auditoria ({}): {}", acao, e.getMessage());
        }
    }

    public List<LogAuditoria> listarRecentes() {
        return repository.findTop50ByOrderByDataHoraDesc();
    }
}
