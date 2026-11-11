# PediuPatrão

Sistema web para gestão de pizzarias e operações de delivery. O projeto foi desenvolvido como Projeto Final de Curso de Engenharia de Software na UMC, com foco no controle diário de clientes, produtos, pedidos, usuários e indicadores do negócio.

## Demonstração em vídeo

[Assistir à demonstração do produto, 4 min 3 s, MP4 em 1080p](docs/demonstracoes/pediupatrao-demo-2026-11-10.mp4)

## Funcionalidades

- Cadastro e manutenção de clientes, produtos e usuários.
- Criação de pedidos, cálculo de total e fluxo de status entre pendente, preparo, entrega, concluído e cancelado.
- Painel administrativo com indicadores de vendas e consulta de auditoria.
- Controle de acesso por perfil, criptografia de senhas com BCrypt e proteção CSRF.
- Cadastro com consentimento LGPD, termos de uso e política de privacidade.
- Consulta de CEP pelo ViaCEP no formulário de clientes.

## Tecnologias

| Camada | Implementação |
| --- | --- |
| Aplicação | Java 21 e Spring Boot 3.4.4 |
| Persistência | MongoDB com Spring Data MongoDB |
| Interface | Thymeleaf, HTML, CSS e JavaScript |
| Segurança | Spring Security, BCrypt, RBAC e CSRF |
| Qualidade | JUnit 5, Mockito, MockMvc e JaCoCo |

## Requisitos locais

- JDK 21 instalado e disponível no `PATH`.
- MongoDB em execução, com escuta na porta `27017` ou URI configurada em `src/main/resources/application.properties`.
- Git, caso queira versionar as alterações.
- PowerShell no Windows ou terminal compatível com o Maven Wrapper.

## Executar

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Em Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Acesse `http://localhost:8080`. A rota inicial redireciona para `/login` quando não existe sessão autenticada.

Defina `APP_ADMIN_PASSWORD` antes do primeiro início para controlar a credencial inicial do administrador. Os parâmetros de conexão do MongoDB e os dados do administrador ficam em `application.properties`.

## Testes

```powershell
.\mvnw.cmd test
```

A suíte cobre serviços, controladores, regras de pedidos e segurança. A execução local de 7 de outubro de 2026 terminou com 122 testes aprovados, sem falhas ou erros. O relatório JaCoCo é gerado em `target/site/jacoco/index.html`.

## Rotas principais

| Área | Caminhos |
| --- | --- |
| Autenticação e cadastro | `/login`, `/cadastro` |
| Gestão de clientes | `/clientes`, `/api/clientes` |
| Gestão de produtos | `/produtos`, `/api/produtos` |
| Pedidos | `/pedidos`, `/api/pedidos` |
| Administração | `/admin`, `/admin/auditoria`, `/api/dashboard/stats` |
| Páginas públicas | `/termos-de-uso`, `/politica-privacidade` |

## Estrutura

```text
src/main/java/com/umc/pediupatrao/
  config/       Configuração de segurança, auditoria e inicialização
  controller/   Rotas MVC e API REST
  entity/       Documentos persistidos no MongoDB
  repository/   Acesso aos dados
  service/      Regras de negócio
src/main/resources/
  templates/    Páginas Thymeleaf
  static/       Estilos, scripts e imagens
src/test/       Testes de unidade e de controladores
```

## Documentação complementar

- [Requisitos](docs/requisitos.md)
- [Diagramas](docs/diagramas.md)
- [Manual do usuário](docs/manual-usuario.md)
- [Análise de segurança](docs/analise-seguranca.md)

## Estado validado

O Spring Boot iniciou localmente em aproximadamente três segundos. `GET /` respondeu com redirecionamento para `/login`, e `GET /login` respondeu com HTTP 200. Para testar operações que gravam ou consultam dados, inicie o MongoDB antes da aplicação.
