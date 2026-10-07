package com.umc.pediupatrao.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pedidos")
public class Pedido {
    @Id
    private String id;

    @NotBlank(message = "Informe o cliente do pedido.")
    private String clienteId;

    @NotBlank(message = "Informe o tipo de pizza.")
    private String tipoPizza;

    private String status;

    @NotNull(message = "Informe a quantidade.")
    @Min(value = 1, message = "A quantidade deve ser no mínimo 1.")
    private int quantidade;

    @NotBlank(message = "Informe o sabor.")
    private String sabor;

    /** Preço unitário resolvido na criação (do produto ou informado no formulário). */
    private Double valorUnitario;

    /** Total do pedido (unitário x quantidade), calculado pelo service. */
    private Double valorTotal;

    /** Data/hora de criação do pedido (usada no dashboard de vendas). */
    private java.time.Instant dataPedido;

    /** DINHEIRO, CARTAO ou PIX. */
    private String metodoPagamento;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getTipoPizza() {
        return tipoPizza;
    }

    public void setTipoPizza(String tipoPizza) {
        this.tipoPizza = tipoPizza;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getSabor() {
        return sabor;
    }

    public void setSabor(String sabor) {
        this.sabor = sabor;
    }

    public Double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(Double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public java.time.Instant getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(java.time.Instant dataPedido) {
        this.dataPedido = dataPedido;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }
}
