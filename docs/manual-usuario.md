# Manual do Usuário — PediuPatrão

Guia passo a passo das telas do sistema. Execute a aplicação com
`./mvnw spring-boot:run` (requer MongoDB na porta 27017) e acesse
[http://localhost:8080](http://localhost:8080).

> **Nota para a banca:** cada seção indica o ponto em que deve ser inserida a
> captura de tela correspondente (`[Captura de tela: ...]`).

---

## 1. Login

1. Acesse `http://localhost:8080` — você será redirecionado para `/login`.
2. Informe o usuário e a senha padrão criados pelo `DataInitializer`
   (`admin` / senha definida no primeiro boot — veja o console da aplicação).
3. Clique em **Entrar**.
4. Em caso de erro, a mensagem "Usuário ou senha inválidos" é exibida.

`[Captura de tela: tela de login]`

---

## 2. Dashboard (tela inicial `/`)

Após o login, a home exibe:

- **KPIs:** faturamento do dia e do mês, pedidos do dia/mês/total, ticket médio,
  total de clientes, total de produtos e pedidos em andamento.
- **Gráfico de linha:** evolução das vendas nos últimos 7 dias.
- **Gráfico de pizza:** sabores mais vendidos.
- **Gráfico de barras:** faturamento por método de pagamento (DINHEIRO, CARTAO, PIX).
- **Badges de status:** quantidade de pedidos por status.

Tudo alimentado por uma única requisição a `GET /api/dashboard/stats`.

`[Captura de tela: dashboard com KPIs e gráficos]`

---

## 3. Pedidos

### 3.1 Listar pedidos (`/pedidos`)

1. No menu lateral, clique em **Pedidos**.
2. A tabela mostra cliente, sabor, quantidade, valor, status e data.
3. Para avançar o status: selecione o próximo status permitido na linha do pedido
   (ex.: `PENDENTE → EM_PREPARO`) e clique no botão **Avançar status**.
   - Transições inválidas são bloqueadas pela máquina de estados e exibem erro.

`[Captura de tela: lista de pedidos com seletor de status]`

### 3.2 Novo pedido (`/pedidos/novo`)

1. Clique em **Novo Pedido**.
2. Selecione o **cliente** (cadastrado previamente — se não houver clientes,
   o sistema redireciona para o cadastro).
3. Informe **tipo de pizza**, **sabor** e **quantidade** (mínimo 1).
4. Escolha a **forma de pagamento**: DINHEIRO, CARTAO ou PIX.
5. Clique em **Salvar**. O valor unitário é resolvido automaticamente pelo
   preço do produto cadastrado e o total é calculado (`unitário × quantidade`).
6. Erros de validação aparecem em português abaixo dos campos.

`[Captura de tela: formulário de novo pedido]`

---

## 4. Clientes

### 4.1 Cadastrar cliente (`/clientes/novo`)

1. Menu lateral → **Clientes** → **Novo Cliente**.
2. Preencha **nome** (obrigatório), **telefone** e **CEP**.
3. Ao digitar o CEP válido, o endereço é preenchido automaticamente (ViaCEP).
4. Confirme o **consentimento LGPD** (obrigatório — Art. 18 da LGPD).
5. **Salvar**.

`[Captura de tela: cadastro de cliente com CEP auto-preenchido]`

### 4.2 Editar cliente (`/clientes/editar/{id}`)

1. Na lista de clientes, clique em **Editar** na linha desejada.
2. Altere os dados e clique em **Salvar**.

### 4.3 Excluir dados pessoais (LGPD)

1. Na lista de clientes, use a ação de exclusão.
2. Se o cliente possuir pedidos vinculados, o sistema **anonimiza** os dados
   (nome substituído por `[DADOS REMOVIDOS - LGPD]`) preservando o histórico;
   caso contrário, os dados são removidos por completo.

`[Captura de tela: cliente anonimizado após exclusão LGPD]`

---

## 5. Produtos (`/produtos`)

1. Menu lateral → **Configurações** → **Produtos**.
2. **Novo produto:** informe nome, tipo (Pizza, Esfiha, Bebida, Lanche) e preço
   (não pode ser negativo).
3. Edite ou remova produtos pela tabela.
4. O preço cadastrado alimenta o cálculo automático dos pedidos.

`[Captura de tela: catálogo de produtos]`

---

## 6. Usuários (`/usuarios`) — somente ADMIN

1. Menu lateral → **Configurações** → **Usuários** (visível apenas para perfil `ADMIN`).
2. Crie usuários informando **username**, **senha** (armazenada com BCrypt) e
   **papel** (`ADMIN` ou `USER`).
3. Edite ou exclua usuários conforme necessário.
4. Usuário sem papel `ADMIN` que acessar `/usuarios` verá a página
   **Acesso negado**.

`[Captura de tela: gestão de usuários]` e `[Captura de tela: página de acesso negado]`

---

## 7. Auditoria (`/admin/auditoria`) — somente ADMIN

1. Menu lateral → **Configurações** → **Auditoria**.
2. A tabela lista cada operação registrada: **usuário, ação, recurso, status,
   IP, detalhe e data/hora**.
3. Toda escrita (criar/editar/excluir) feita no sistema aparece aqui
   automaticamente.

`[Captura de tela: log de auditoria]`

---

## 8. Configurações (`/configuracoes`)

1. O item **Configurações** no menu lateral agrupa os submenus de manutenção:
   **Funcionários**, **Entregadores**, **Horário de Funcionamento**, **Produtos**,
   **Usuários** e **Auditoria**.
2. A página `/configuracoes` (acessível pela URL) reúne as informações gerais do
   sistema.
3. *Observação:* os itens Funcionários, Entregadores e Horário de Funcionamento
   são planejados e ainda não possuem telas — seus links retornam erro até a
   implementação.

`[Captura de tela: tela de configurações]`

---

## 9. Encerrar sessão

1. Clique em **Sair** no menu lateral.
2. A sessão é invalidada e você retorna para o login.

---

## Dúvidas

Consulte o `README.md` (execução local, testes e cobertura) e
`docs/requisitos.md` (RF01–RF15 e RNF01–RNF08).
