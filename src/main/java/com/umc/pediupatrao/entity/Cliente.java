package com.umc.pediupatrao.entity;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clientes")
public class Cliente {

    @Id
    private String id;

    @NotBlank(message = "Informe o nome do cliente.")
    private String nome;

    @Pattern(regexp = "[0-9()+\\-\\s]{8,20}|^$",
            message = "Telefone inválido (use números, espaços, + e -).")
    private String telefone;

    // Endereço completo
    @Pattern(regexp = "[0-9]{5}-?[0-9]{3}|^$",
            message = "CEP inválido (formato 00000-000).")
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    // Mantido para compatibilidade com dados antigos
    private String endereco;

    // LGPD: consentimento para tratamento de dados pessoais
    private Boolean consentimentoLgpd;
    private String dataConsentimento;

    @AssertTrue(message = "É necessário aceitar o tratamento dos dados pessoais (LGPD).")
    public boolean isConsentimentoAceito() {
        return Boolean.TRUE.equals(consentimentoLgpd);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public Boolean getConsentimentoLgpd() { return consentimentoLgpd; }
    public void setConsentimentoLgpd(Boolean consentimentoLgpd) { this.consentimentoLgpd = consentimentoLgpd; }

    public String getDataConsentimento() { return dataConsentimento; }
    public void setDataConsentimento(String dataConsentimento) { this.dataConsentimento = dataConsentimento; }
}