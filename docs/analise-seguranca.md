# Análise de Segurança — PediuPatrão

> Executada ao final das 6 fases do plano, com base no checklist da skill `security-review`.
> Método: revisão estática de `src/main/java`, `src/main/resources/templates` e `application.properties`,
> verificação dos controles do Spring Security e execução da suíte de testes.

## 1. Resumo executivo

| Item | Resultado |
|---|---|
| Achado crítico/alto | **1 (corrigido)** — XSS armazenado na listagem de clientes |
| Pontos fortes verificados | 10 |
| Recomendações residuais (não críticas) | 6 |
| Suíte de testes após correções | **110 testes, 0 falhas** (`mvnw test`, BUILD SUCCESS) |

## 2. Achado corrigido

### XSS armazenado em `templates/clientes/lista.html` — severidade: MÉDIA/ALTA ✅ corrigido
- **Problema:** campos `nome`, `telefone` e endereço de clientes eram interpolados
  diretamente em `innerHTML` sem escape. Nome como `<img src=x onerror=...>` executaria
  script no painel de quem abrisse `/clientes`.
- **Impacto:** com o cookie `XSRF-TOKEN` legível por JS (necessário aos `fetch()`),
  o XSS permitia executar ações em nome do usuário autenticado (ex.: excluir clientes).
- **Correção:** função `escapar()` (escape de `& < > " '`) aplicada a todos os valores
  interpolados antes da inserção em `innerHTML` (clientes/lista.html).
- **Demais templates:** verificados — nenhum outro usa `innerHTML` com dados,
  `th:utext` ou `insertAdjacentHTML`/`document.write`.

## 3. Pontos fortes verificados (controles presentes)

1. **Autenticação:** form login do Spring Security, senhas com `BCryptPasswordEncoder`.
2. **Autorização (RBAC):** `/admin/**`, `/usuarios/**`, `/api/usuarios/**` exigem `ROLE_ADMIN`;
   demais rotas autenticadas; `/acesso-negado` para acesso negado (`SecurityConfig.java:58-81`).
3. **CSRF:** ativo com `CookieCsrfTokenRepository.withHttpOnlyFalse()` (token nos `fetch()`).
4. **Senhas nunca serializadas:** `@JsonProperty(Access.WRITE_ONLY)` em `Usuario.password`
   (`Usuario.java:17`) + `HomeController` zera a senha antes de devolver ao template.
5. **Sem vazamento de stack trace:** `RestExceptionHandler` devolve JSON genérico
   ("Erro interno ao processar a solicitação") sem detalhes internos.
6. **Sem XSS por templates:** ausência de `th:utext` em todos os templates (Thymeleaf escapa por padrão).
7. **Auditoria sem corpo de requisição:** `AuditoriaInterceptor` grava usuário/ação/URI/status/IP —
   nunca bodies (senhas e dados de clientes não vão para o log de auditoria).
8. **Sem CORS permissivo:** nenhuma configuração `allowedOrigins`/`addCorsMappings` no projeto.
9. **Sem injeção NoSQL:** sem `@Query` dinâmico, `BasicDBObject` ou concatenação de critérios —
   apenas métodos derivados do Spring Data (parametrizados).
10. **Anonimização LGPD:** `ClienteService.anonimizarDados/excluir` + consentimento no cadastro
    + tela `/admin/auditoria` restrita a ADMIN.

## 4. Recomendações residuais (fora do escopo do Trabalho, não bloqueantes)

| # | Risco | Severidade | Sugestão |
|---|---|---|---|
| 1 | Sem rate limiting/lockout no `/login` (força bruta) | Média | Bucket4j, `LoginAttemptService` ou WAF/reverse proxy |
| 2 | `DataInitializer` loga a senha inicial no console no 1º boot | Baixa | Aceitável para bootstrap; trocar a senha após o primeiro acesso |
| 3 | IP de auditoria aceita `X-Forwarded-For` vindo do cliente (spoofável) | Baixa | Confiar no XFF apenas atrás de proxy confiável (ForwardedHeaderFilter) |
| 4 | Sem cabeçalho CSP / `Referrer-Policy` customizados | Baixa | Adicionar headers via `SecurityFilterChain` |
| 5 | MongoDB sem autenticação (`mongodb://localhost:27017`) | Baixa (dev) | Habilitar auth + não expor 27017 em produção |
| 6 | Campos CPF/telefone ainda expostos em logs gerais (item pendente do README) | Baixa | Máscara de PII nos logs (já anotado na Fase 1) |

## 5. Conclusão

O sistema atende os controles essenciais esperados para o escopo acadêmico:
autenticação forte (BCrypt), RBAC, CSRF, auditoria completa de escritas, LGPD
(consentimento/anonimização) e tratamento de erros sem vazamento. O único achado
explorável (XSS armazenado) foi corrigido e a suíte completa permanece verde
(110 testes, 0 falhas). As 6 recomendações da seção 4 são melhorias indicadas
para um ciclo de produção futura.
