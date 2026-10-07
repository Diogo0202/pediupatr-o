# Modelagem — Diagramas UML e BPMN

Os diagramas abaixo usam [Mermaid](https://mermaid.js.org/) e são renderizados
automaticamente pelo GitLab, GitHub e pela maioria dos editores Markdown.

---

## 1. Diagrama de Classes UML (entidades do sistema)

```mermaid
classDiagram
    class Cliente {
        -String id
        -String nome
        -String telefone
        -String cep
        -String logradouro
        -String numero
        -String complemento
        -String bairro
        -String cidade
        -String estado
        -String endereco
        -Boolean consentimentoLgpd
        -String dataConsentimento
        +novoCliente(Cliente) Cliente
        +anonimizarDados(id) void
    }

    class Pedido {
        -String id
        -String clienteId
        -String tipoPizza
        -String status
        -int quantidade
        -String sabor
        -Double valorUnitario
        -Double valorTotal
        -Instant dataPedido
        -String metodoPagamento
        +criarPedido(Pedido) Pedido
        +atualizarStatus(id, novoStatus) Pedido
        +proximosStatus(statusAtual) List~String~
    }

    class Produto {
        -String id
        -String nome
        -String tipo
        -Double preco
        -Boolean ativo
        +salvar(Produto) Produto
    }

    class Usuario {
        -String id
        -String username
        -String password
        -String role
        +criarUsuario(Usuario) Usuario
    }

    class LogAuditoria {
        -String id
        -Instant dataHora
        -String usuario
        -String acao
        -String recurso
        -String status
        -String ip
        -String detalhe
    }

    Cliente "1" --> "0..*" Pedido : realiza
    Produto "1" --> "0..*" Pedido : referenciado por (sabor)
    Usuario "1" --> "0..*" LogAuditoria : gera
```

---

## 2. Diagrama BPMN — Fluxo de negócio do pedido (do pedido até a entrega)

> Pools e lanes: **Cliente**, **Atendente/Sistema**, **Cozinha**, **Entregador** e
> **Gestor**. Losangos são gateways; retângulos arredondados são atividades.

```mermaid
flowchart TD
    subgraph POOL_PIZZARIA[PediuPatrão — Pizzaria]
        subgraph LANE_CLIENTE[Lane: Cliente]
            A([Início: cliente deseja uma pizza]) --> B[Monta o pedido: sabor, tipo, quantidade]
            B --> C[Informe seus dados e CEP]
            C --> D{Consente com o tratamento dos dados?}
            D -- Não --> E[Cancela o cadastro]
            E --> Z1([Fim — sem pedido])
            D -- Sim --> F[Confirma o pedido e forma de pagamento]
        end

        subgraph LANE_SISTEMA[Lane: Atendente / Sistema]
            F --> G[[Valida dados: Bean Validation + ViaCEP]]
            G -- Dados inválidos --> H[Mensagens de erro em PT-BR]
            H --> B
            G -- OK --> I[Cálculo do valor unitário e total]
            I --> J((Pedido criado — status PENDENTE))
            J --> K{Aprova o pedido?}
            K -- Não --> L((Pedido CANCELADO))
            K -- Sim --> M((Status: EM_PREPARO))
        end

        subgraph LANE_COZINHA[Lane: Cozinha]
            M --> N[Prepara a pizza]
            N --> O{Pronta?}
            O -- Não --> N
            O -- Sim --> P((Status: EM_ENTREGA))
        end

        subgraph LANE_ENTREGADOR[Lane: Entregador]
            P --> Q[Leva o pedido ao cliente]
            Q --> R((Status: ENTREGUE))
            R --> S([Fim: pedido entregue e pago])
        end

        subgraph LANE_GESTOR[Lane: Gestor / Admin]
            T[Auditoria registra cada ação: usuário, IP, status] -.-> J
            T -.-> K
            T -.-> M
            L -.-> T
            R -.-> T
            U[Dashboard consolida KPIs e gráficos] -.-> T
        end
    end
```

### 2.1 Diagrama de estados do pedido (máquina de estados)

```mermaid
stateDiagram-v2
    [*] --> PENDENTE: POST /api/pedidos
    PENDENTE --> EM_PREPARO: aprovar
    PENDENTE --> CANCELADO: cancelar
    EM_PREPARO --> EM_ENTREGA: saiu para entrega
    EM_PREPARO --> CANCELADO: cancelar
    EM_ENTREGA --> ENTREGUE: entregue ao cliente
    ENTREGUE --> [*]
    CANCELADO --> [*]
```

Transições fora da tabela acima são rejeitadas com erro 400 por
`PedidoService.atualizarStatus` (fonte de verdade: `TRANSICOES`).

---

## 3. Diagrama de sequência — Registro de um pedido

```mermaid
sequenceDiagram
    participant C as Cliente
    participant W as Navegador (Thymeleaf)
    participant H as HomeController
    participant S as PedidoService
    participant R as PedidoRepository
    participant A as AuditoriaInterceptor

    C->>W: Preenche /pedidos/novo e envia
    W->>H: POST /pedidos/salvar
    A->>A: Intercepta a escrita (log pendente)
    H->>S: criarPedido(pedido)
    S->>S: valida status, método de pagamento e quantidade
    S->>R: resolve preço do produto (findByNomeIgnoreCase)
    S->>R: save(pedido) — valorTotal = unitário x quantidade
    R-->>S: pedido persistido
    S-->>H: pedido
    H-->>W: redirect:/pedidos
    A->>A: grava LogAuditoria (usuario, IP, status, dataHora)
    W->>C: Lista de pedidos atualizada
```
