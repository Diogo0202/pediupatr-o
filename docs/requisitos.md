# Especificação de Requisitos — PediuPatrão

Sistema de gestão de pizzaria: cadastro de clientes, catálogo de produtos, fluxo de
pedidos com máquina de estados, dashboard gerencial, auditoria e conformidade com a LGPD.

---

## 1. Requisitos Funcionais (RF01 – RF15)

| ID | Requisito | Descrição | Prioridade | Status / Implementação |
|----|-----------|-----------|------------|------------------------|
| RF01 | Autenticação de usuários | Tela de login (`/login`), encerramento de sessão (logout) e autenticação com senha criptografada (BCrypt). Usuário padrão `admin` criado no primeiro boot pelo `DataInitializer`. | Alta | ✅ `SecurityConfig`, `login.html`, `UsuarioDetailsService` |
| RF02 | Gestão de usuários | CRUD de usuários (listar, criar, editar, excluir) com papéis (`role`), restrito a `ADMIN`. | Alta | ✅ `/usuarios` (telas) e `/api/usuarios` (REST) |
| RF03 | Cadastro de clientes | CRUD completo com validações: nome obrigatório, telefone e CEP com máscara, endereço completo e **consentimento LGPD obrigatório**. | Alta | ✅ `/clientes`, `ClienteController`, `ClienteService` |
| RF04 | Consulta automática de CEP | Preenchimento automático de logradouro/bairro/cidade/estado ao digitar o CEP, via ViaCEP. | Média | ✅ `clientes/form.html` (fetch `https://viacep.com.br/ws/{cep}/json/`) |
| RF05 | Catálogo de produtos | CRUD de produtos (pizza, esfiha, bebida, lanche) com nome, tipo, preço e flag `ativo`. Preços negativos rejeitados. | Alta | ✅ `/produtos`, `ProdutoController`, `ProdutoService` |
| RF06 | Registro de pedidos | Formulário com validações obrigatórias: cliente, tipo de pizza, sabor, quantidade ≥ 1 e método de pagamento (DINHEIRO, CARTAO, PIX). | Alta | ✅ `/pedidos/novo`, `POST /pedidos/salvar`, `POST /api/pedidos` |
| RF07 | Cálculo automático de totais | Valor unitário resolvido pelo cadastro do produto (quando não informado) e `valorTotal = unitário × quantidade` arredondado em 2 casas. | Alta | ✅ `PedidoService.criarPedido` |
| RF08 | Máquina de estados do pedido | Transições permitidas: `PENDENTE → EM_PREPARO → EM_ENTREGA → ENTREGUE`; cancelamento a partir de `PENDENTE`/`EM_PREPARO`; estados finais sem saída. Transição inválida retorna erro. | Alta | ✅ `PedidoService.TRANSICOES`, `PUT /api/pedidos/{id}/status`, `GET /api/pedidos/status/permitidos` |
| RF09 | Listagem de pedidos | Tabela com todos os pedidos, exibindo o **nome do cliente** resolvido a partir do `clienteId`. | Média | ✅ `/pedidos` (`HomeController.pedidos`) |
| RF10 | Dashboard (KPIs) | Big numbers: faturamento do dia/mês, pedidos do dia/mês/total, ticket médio, total de clientes, total de produtos e pedidos em andamento. Pedidos cancelados entram apenas no total geral. | Alta | ✅ `GET /api/dashboard/stats` |
| RF11 | Gráficos dinâmicos | Gráfico de linha (vendas 7 dias), pizza (sabores mais vendidos), barras (métodos de pagamento) e badges por status, renderizados com Chart.js. | Alta | ✅ `home.html` + `DashboardController` |
| RF12 | Auditoria de operações | Registro de toda escrita (POST/PUT/DELETE) com usuário, ação, recurso, status HTTP, IP, detalhe e data/hora; tela de consulta para administradores. | Alta | ✅ `AuditoriaInterceptor`, `LogAuditoria`, `/admin/auditoria` |
| RF13 | Controle de acesso por perfis (RBAC) | Recursos administrativos (`/usuarios`, `/admin`, `/api/usuarios`) exigem papel `ADMIN`; demais áreas exigem autenticação; acesso negado redireciona para `/acesso-negado`. | Alta | ✅ `SecurityConfig` |
| RF14 | Direitos do titular (LGPD) | Consentimento explícito no cadastro e **exclusão/anonimização** dos dados pessoais preservando o histórico de pedidos (Art. 18). | Alta | ✅ `POST /clientes/lgpd/excluir/{id}`, `ClienteService.anonimizarDados` |
| RF15 | API REST JSON | Endpoints `/api/pedidos`, `/api/clientes`, `/api/produtos`, `/api/usuarios` com códigos HTTP corretos (200/201/400/404) e mensagens de erro estruturadas; protegidos por CSRF. | Média | ✅ Controllers REST + `RestExceptionHandler` |

---

## 2. Requisitos Não-Funcionais (RNF01 – RNF08)

| ID | Categoria | Requisito | Critério de aceite | Status |
|----|-----------|-----------|--------------------|--------|
| RNF01 | Segurança | Senhas nunca em texto claro | Senhas armazenadas com BCrypt (`$2a$…`) e o campo `password` marcado como somente escrita na API (nunca retorna no JSON). | ✅ |
| RNF02 | Segurança | Proteção contra CSRF e cabeçalhos seguros | CSRF habilitado com `CookieCsrfTokenRepository` (sem HttpOnly) e cabeçalhos de segurança padrão do Spring Security ativos; front envia token no header `X-XSRF-TOKEN`. | ✅ |
| RNF03 | Segurança | Rastreabilidade de operações | 100% das escritas autenticadas geram `LogAuditoria` com usuário, IP e timestamp; tela `/admin/auditoria` disponível apenas para `ADMIN`. | ✅ |
| RNF04 | LGPD | Privacidade por design | Consentimento explícito obrigatório no cadastro; anonimização em vez de exclusão quando há histórico; dados residentes em MongoDB local (sem transferência internacional). | ✅ |
| RNF05 | Desempenho | Resposta rápida do dashboard | Todos os KPIs e bases dos gráficos entregues em **uma única requisição** (`GET /api/dashboard/stats`), resposta JSON enxuta e renderização em < 2 s em máquina local. | ✅ |
| RNF06 | Usabilidade | Interface simples em português | Layout responsivo (sidebar + Bootstrap), mensagens de validação em PT-BR vindas do Bean Validation, novo pedido registrado em uma única tela. | ✅ |
| RNF07 | Confiabilidade | Qualidade verificável por testes | Suíte automatizada JUnit 5 + Mockito com cobertura ≥ 50% (atual: **75% instruções / 58% branches**, 110 testes). | ✅ |
| RNF08 | Manutenibilidade | Portabilidade e documentação | Java 21 + Spring Boot 3.4 + MongoDB, execução via Maven Wrapper em Windows/Linux, código documentado e guias em `README.md` e `docs/`. | ✅ |
