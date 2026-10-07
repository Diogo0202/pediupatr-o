package com.umc.pediupatrao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Registro de auditoria de ações sensíveis do sistema (LGPD / rastreabilidade).
 */
@Document(collection = "logs_auditoria")
public class LogAuditoria {

    @Id
    private String id;
    private Instant dataHora;
    private String usuario;
    private String acao;
    private String recurso;
    private String status;
    private String ip;
    private String detalhe;

    public LogAuditoria() {
    }

    public LogAuditoria(Instant dataHora, String usuario, String acao, String recurso,
                        String status, String ip, String detalhe) {
        this.dataHora = dataHora;
        this.usuario = usuario;
        this.acao = acao;
        this.recurso = recurso;
        this.status = status;
        this.ip = ip;
        this.detalhe = detalhe;
    }

    public String getDataHoraFormatada() {
        if (dataHora == null) {
            return "-";
        }
        return java.time.LocalDateTime.ofInstant(dataHora, java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getDataHora() { return dataHora; }
    public void setDataHora(Instant dataHora) { this.dataHora = dataHora; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getDetalhe() { return detalhe; }
    public void setDetalhe(String detalhe) { this.detalhe = detalhe; }
}
