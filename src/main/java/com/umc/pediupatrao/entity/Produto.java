package com.umc.pediupatrao.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "produtos")
public class Produto {
    
    @Id
    private String id;

    @NotBlank(message = "Informe o nome do produto.")
    private String nome;

    @NotBlank(message = "Informe o tipo do produto.")
    private String tipo; // Pizza, Esfiha, Bebida, Lanche

    @NotNull(message = "Informe o preço do produto.")
    @Min(value = 0, message = "O preço não pode ser negativo.")
    private Double preco;

    private Boolean ativo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    
}
