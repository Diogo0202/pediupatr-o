# -*- coding: utf-8 -*-
"""Gera os slides da Banca Final do PediuPatrão (nativo, sem imagens de IA).

Uso:  python docs/gerar_slides.py
Saida: docs/slides-banca-final.pptx
"""

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.util import Inches, Pt

# ---------------------------------------------------------------- paleta
BG        = RGBColor(0x1C, 0x19, 0x17)   # dark quente
CARD      = RGBColor(0x29, 0x24, 0x21)   # card um pouco mais claro
ACCENT    = RGBColor(0xE2, 0x3E, 0x2C)   # vermelho tomate
ACCENT2   = RGBColor(0xF5, 0xB3, 0x01)   # amarelo queijo
TEXT      = RGBColor(0xFA, 0xFA, 0xF9)   # texto claro
MUTED     = RGBColor(0xA8, 0xA2, 0x9E)   # texto suave
FONT      = "Segoe UI"

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
BLANK = prs.slide_layouts[6]
PAGE = {"n": 0}


# ---------------------------------------------------------------- helpers
def new_slide(footer=True):
    s = prs.slides.add_slide(BLANK)
    s.background.fill.solid()
    s.background.fill.fore_color.rgb = BG
    PAGE["n"] += 1
    if footer:
        _footer(s)
    return s


def _footer(slide):
    tb = slide.shapes.add_textbox(Inches(0.55), Inches(7.05), Inches(9), Inches(0.35))
    p = tb.text_frame.paragraphs[0]
    run = p.add_run()
    run.text = "PediuPatrão  ·  Banca Final"
    run.font.size, run.font.color.rgb, run.font.name = Pt(10), MUTED, FONT
    tb2 = slide.shapes.add_textbox(Inches(12.4), Inches(7.05), Inches(0.6), Inches(0.35))
    p2 = tb2.text_frame.paragraphs[0]
    p2.alignment = PP_ALIGN.RIGHT
    r2 = p2.add_run()
    r2.text = str(PAGE["n"])
    r2.font.size, r2.font.color.rgb, r2.font.name = Pt(10), MUTED, FONT


def title(slide, text, subtitle=None):
    tb = slide.shapes.add_textbox(Inches(0.55), Inches(0.42), Inches(12.2), Inches(1.0))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    r = p.add_run()
    r.text = text
    r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(34), True, TEXT, FONT
    bar = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.58), Inches(1.22), Inches(1.6), Inches(0.07))
    bar.fill.solid()
    bar.fill.fore_color.rgb = ACCENT
    bar.line.fill.background()
    if subtitle:
        tb2 = slide.shapes.add_textbox(Inches(0.58), Inches(1.34), Inches(12), Inches(0.45))
        p2 = tb2.text_frame.paragraphs[0]
        r2 = p2.add_run()
        r2.text = subtitle
        r2.font.size, r2.font.color.rgb, r2.font.name = Pt(15), MUTED, FONT


def bullets(slide, items, top=2.0, left=0.75, width=11.9, size=17, gap=10, bullet="▪"):
    tb = slide.shapes.add_textbox(Inches(left), Inches(top), Inches(width), Inches(4.6))
    tf = tb.text_frame
    tf.word_wrap = True
    first = True
    for it in items:
        if isinstance(it, tuple):
            head, body = it
        else:
            head, body = None, it
        p = tf.paragraphs[0] if first else tf.add_paragraph()
        first = False
        p.space_after = Pt(gap)
        if head:
            r0 = p.add_run()
            r0.text = f"{bullet} {head} — "
            r0.font.size, r0.font.bold, r0.font.color.rgb, r0.font.name = Pt(size), True, ACCENT2, FONT
            r1 = p.add_run()
            r1.text = body
            r1.font.size, r1.font.color.rgb, r1.font.name = Pt(size), TEXT, FONT
        else:
            r = p.add_run()
            r.text = f"{bullet} {body}"
            r.font.size, r.font.color.rgb, r.font.name = Pt(size), TEXT, FONT
    return tb


def card(slide, x, y, w, h, heading, body, accent=ACCENT, head_size=13, body_size=11.5):
    sh = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(y), Inches(w), Inches(h))
    sh.fill.solid()
    sh.fill.fore_color.rgb = CARD
    sh.line.color.rgb = RGBColor(0x3F, 0x3A, 0x36)
    sh.line.width = Pt(1)
    sh.adjustments[0] = 0.08
    tf = sh.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = MSO_ANCHOR.TOP
    tf.margin_left = tf.margin_right = Inches(0.16)
    tf.margin_top = Inches(0.12)
    p = tf.paragraphs[0]
    r = p.add_run()
    r.text = heading
    r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(head_size), True, accent, FONT
    if body:
        p2 = tf.add_paragraph()
        p2.space_before = Pt(4)
        r2 = p2.add_run()
        r2.text = body
        r2.font.size, r2.font.color.rgb, r2.font.name = Pt(body_size), TEXT, FONT
    return sh


def chip(slide, x, y, w, text, color=ACCENT2):
    sh = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(y), Inches(w), Inches(0.52))
    sh.fill.solid()
    sh.fill.fore_color.rgb = CARD
    sh.line.color.rgb = color
    sh.line.width = Pt(1.25)
    sh.adjustments[0] = 0.5
    tf = sh.text_frame
    tf.word_wrap = False
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    p = tf.paragraphs[0]
    p.alignment = PP_ALIGN.CENTER
    r = p.add_run()
    r.text = text
    r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(13), True, TEXT, FONT
    return sh


def flow_box(slide, x, y, w, text, fill=ACCENT, font_size=13):
    sh = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(y), Inches(w), Inches(0.75))
    sh.fill.solid()
    sh.fill.fore_color.rgb = fill
    sh.line.fill.background()
    sh.adjustments[0] = 0.25
    tf = sh.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    p = tf.paragraphs[0]
    p.alignment = PP_ALIGN.CENTER
    r = p.add_run()
    r.text = text
    r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(font_size), True, RGBColor(0xFF, 0xFF, 0xFF), FONT
    return sh


def arrow(slide, x, y, w=0.55, label=None):
    sh = slide.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, Inches(x), Inches(y), Inches(w), Inches(0.35))
    sh.fill.solid()
    sh.fill.fore_color.rgb = MUTED
    sh.line.fill.background()
    if label:
        tb = slide.shapes.add_textbox(Inches(x - 0.15), Inches(y - 0.36), Inches(w + 0.3), Inches(0.3))
        p = tb.text_frame.paragraphs[0]
        p.alignment = PP_ALIGN.CENTER
        r = p.add_run()
        r.text = label
        r.font.size, r.font.color.rgb, r.font.name = Pt(10), ACCENT2, FONT
    return sh


def notes(slide, lines):
    tf = slide.notes_slide.notes_text_frame
    tf.text = "\n".join(lines)


# ================================================================ slide 1 — capa
s = new_slide(footer=False)
bar = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(0.35), Inches(7.5))
bar.fill.solid()
bar.fill.fore_color.rgb = ACCENT
bar.line.fill.background()

tb = s.shapes.add_textbox(Inches(1.1), Inches(2.15), Inches(11.5), Inches(1.6))
tf = tb.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
r = p.add_run()
r.text = "PediuPatrão"
r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(66), True, TEXT, FONT

tb2 = s.shapes.add_textbox(Inches(1.15), Inches(3.55), Inches(11), Inches(0.7))
p2 = tb2.text_frame.paragraphs[0]
r2 = p2.add_run()
r2.text = "Sistema de gestão de pizzaria — pedidos, clientes, dashboard e auditoria"
r2.font.size, r2.font.color.rgb, r2.font.name = Pt(21), ACCENT2, FONT

tb3 = s.shapes.add_textbox(Inches(1.15), Inches(4.5), Inches(11), Inches(1.2))
tf3 = tb3.text_frame
tf3.word_wrap = True
for i, line in enumerate([
    "Apresentação de Banca Final — [Disciplina] · [Turma] · [Data]",
    "Spring Boot 3.4 · Java 21 · MongoDB · Thymeleaf · Bootstrap",
]):
    p = tf3.paragraphs[0] if i == 0 else tf3.add_paragraph()
    p.space_after = Pt(6)
    r = p.add_run()
    r.text = line
    r.font.size, r.font.color.rgb, r.font.name = Pt(15), MUTED, FONT
notes(s, ["Abrir com o problema: pizzarias precisam controlar pedidos, entregas e faturamento.",
          "PediuPatrão é o sistema completo desenvolvido na disciplina."])

# ================================================================ slide 2 — agenda
s = new_slide()
title(s, "Agenda")
items = [
    ("1. Contexto e objetivos", "o problema que o sistema resolve"),
    ("2. Arquitetura técnica", "Spring Boot, MongoDB e camadas da aplicação"),
    ("3. Funcionalidades", "RF01–RF15 implementados"),
    ("4. Fluxo do pedido", "máquina de estados até a entrega"),
    ("5. Segurança, auditoria e LGPD", "Spring Security, logs e direitos do titular"),
    ("6. Qualidade de software", "110 testes automatizados e cobertura JaCoCo"),
    ("7. Entregáveis e próximos passos", "documentação, manual, slides e vídeo"),
]
bullets(s, items, top=1.9, size=17, gap=13)
notes(s, ["Passar rapidamente; detalhar cada item nos próximos slides."])

# ================================================================ slide 3 — contexto
s = new_slide()
title(s, "Contexto e objetivos", "Por que o PediuPatrão existe")
bullets(s, [
    ("Problema:", "pizzarias controlam pedidos em papel/planilhas, sem visão de faturamento, "
                  "sem histórico por cliente e sem rastreio de quem alterou o quê."),
    ("Objetivo geral:", "centralizar o ciclo completo do pedido — do cadastro do cliente "
                        "até a entrega — com indicadores gerenciais em tempo real."),
], top=1.95, size=16, gap=12)
sub = [
    ("Cadastro", "clientes com ViaCEP e consentimento LGPD"),
    ("Operação", "pedidos com máquina de estados e cálculo automático de totais"),
    ("Gestão", "dashboard com KPIs, gráficos e faturamento"),
    ("Conformidade", "auditoria de ações e anonimização de dados (LGPD)"),
]
for i, (h, b) in enumerate(sub):
    card(s, 0.7 + i * 3.15, 4.4, 3.0, 1.7, h, b, accent=ACCENT2)
notes(s, ["Destacar que tudo é integrado: o mesmo pedido alimenta o dashboard e a auditoria."])

# ================================================================ slide 4 — arquitetura
s = new_slide()
title(s, "Arquitetura técnica", "Aplicação web em camadas sobre Spring Boot 3.4 + MongoDB")
for i, t in enumerate(["Java 21", "Spring Boot 3.4", "Spring Security", "MongoDB",
                       "Thymeleaf + Bootstrap", "Maven Wrapper"]):
    chip(s, 0.7 + i * 2.1, 1.9, 1.95, t)
rows = [
    ("config/", "SecurityConfig (RBAC, CSRF), AuditoriaInterceptor, DataInitializer, RestExceptionHandler"),
    ("controller/", "Telas MVC (HomeController) e APIs REST (/api/pedidos, /api/clientes, /api/produtos, /api/dashboard)"),
    ("service/", "Regras de negócio: PedidoService (máquina de estados), ClienteService (LGPD), UsuarioService"),
    ("repository/", "Spring Data MongoDB — repositórios de Cliente, Pedido, Produto, Usuario, LogAuditoria"),
    ("entity/", "Documentos MongoDB com Bean Validation (@NotBlank, @Min, @Pattern)"),
]
for i, (h, b) in enumerate(rows):
    card(s, 0.7, 2.75 + i * 0.82, 12.0, 0.74, h, b, accent=ACCENT, head_size=13, body_size=12)
notes(s, ["Explicar que a API REST e as telas Thymeleaf compartilham os mesmos services.",
          "MongoDB escolhido pela flexibilidade de esquema durante o desenvolvimento."])

# ================================================================ slide 5 — funcionalidades
s = new_slide()
title(s, "Funcionalidades principais", "RF01–RF15 — ver docs/requisitos.md")
feat = [
    ("RF01–RF02 · Acesso", "Login com BCrypt, sessão e gestão de usuários (ADMIN)"),
    ("RF03–RF04 · Clientes", "CRUD completo + endereço automático via ViaCEP"),
    ("RF05 · Produtos", "Catálogo (pizza, esfiha, bebida, lanche) com preços"),
    ("RF06–RF07 · Pedidos", "Validações obrigatórias e cálculo unitário × quantidade"),
    ("RF08 · Estados", "PENDENTE → EM_PREPARO → EM_ENTREGA → ENTREGUE (+ cancelamento)"),
    ("RF09 · Listagem", "Pedidos com nome do cliente resolvido"),
    ("RF10–RF11 · Dashboard", "KPIs, gráfico de 7 dias, sabores e métodos de pagamento"),
    ("RF12 · Auditoria", "Log de toda escrita: usuário, ação, IP, status, data/hora"),
    ("RF13 · RBAC", "Áreas ADMIN protegidas + página de acesso negado"),
    ("RF14 · LGPD", "Consentimento e anonimização (Art. 18) preservando histórico"),
    ("RF15 · API REST", "CRUDs JSON com códigos 200/201/400/404 e CSRF"),
]
for i, (h, b) in enumerate(feat):
    col, row = divmod(i, 6)
    card(s, 0.7 + col * 6.3, 1.95 + row * 0.84, 6.0, 0.76, h, b, accent=ACCENT2, head_size=12, body_size=11)
notes(s, ["Não ler tudo: escolher 3–4 destaques conforme o interesse da banca."])

# ================================================================ slide 6 — fluxo
s = new_slide()
title(s, "Fluxo do pedido", "Máquina de estados — transições inválidas são rejeitadas (400)")
y = 2.5
flow_box(s, 0.7, y, 2.1, "PENDENTE")
arrow(s, 2.9, y + 0.2, 0.6, "aprovar")
flow_box(s, 3.6, y, 2.3, "EM_PREPARO", fill=RGBColor(0xD9, 0x77, 0x06))
arrow(s, 6.0, y + 0.2, 0.6, "saiu")
flow_box(s, 6.7, y, 2.3, "EM_ENTREGA", fill=RGBColor(0x25, 0x63, 0xEB))
arrow(s, 9.1, y + 0.2, 0.6, "entregue")
flow_box(s, 9.8, y, 2.5, "ENTREGUE", fill=RGBColor(0x15, 0x80, 0x3D))

flow_box(s, 0.7, y + 2.0, 2.1, "CANCELADO", fill=RGBColor(0x7F, 0x1D, 0x1D))
arrow(s, 2.9, y + 2.2, 0.6, "cancelar")
tb = s.shapes.add_textbox(Inches(3.6), Inches(y + 1.95), Inches(8.8), Inches(1.4))
tf = tb.text_frame
tf.word_wrap = True
for i, line in enumerate([
    "· Cancelamento permitido apenas em PENDENTE e EM_PREPARO.",
    "· ENTREGUE e CANCELADO são estados finais (sem saída).",
    "· Fonte de verdade: PedidoService.TRANSICOES — testada por 20+ testes unitários.",
    "· A tela /pedidos oferece seletor de próximo status (auditado a cada mudança).",
]):
    p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
    p.space_after = Pt(6)
    r = p.add_run()
    r.text = line
    r.font.size, r.font.color.rgb, r.font.name = Pt(14), TEXT, FONT
notes(s, ["Demonstrar na tela de pedidos se houver tempo: avançar status ao vivo."])

# ================================================================ slide 7 — dashboard
s = new_slide()
title(s, "Dashboard e indicadores", "Uma única requisição: GET /api/dashboard/stats (Chart.js 2.9.4)")
kpis = [
    ("Faturamento", "dia · mês · ticket médio"),
    ("Pedidos", "dia · mês · total · em andamento"),
    ("Clientes", "total cadastrado"),
    ("Produtos", "catálogo ativo"),
]
for i, (h, b) in enumerate(kpis):
    card(s, 0.7 + i * 3.15, 1.95, 3.0, 1.5, h, b, accent=ACCENT2, head_size=20, body_size=12)
bullets(s, [
    ("Gráfico de linha:", "evolução das vendas nos últimos 7 dias"),
    ("Gráfico de pizza:", "sabores mais vendidos"),
    ("Gráfico de barras:", "faturamento por método de pagamento (DINHEIRO, CARTAO, PIX)"),
    ("Badges:", "quantidade de pedidos por status"),
    ("Nota:", "pedidos CANCELADOS contam apenas no total geral"),
], top=3.9, size=15, gap=9)
notes(s, ["Mencionar que KPIs e bases dos gráficos vêm juntos — performance (RNF05)."])

# ================================================================ slide 8 — segurança
s = new_slide()
title(s, "Segurança", "Spring Security + CSRF + controle de acesso por papéis")
cols = [
    ("Autenticação", [
        "Senha com BCrypt ($2a$…)",
        "Campo password nunca retorna no JSON",
        "Usuário admin criado no 1º boot",
        "Página /acesso-negado para erros 403",
    ]),
    ("Autorização (RBAC)", [
        "/usuarios, /admin, /api/usuarios → ROLE ADMIN",
        "Demais áreas exigem login",
        "Padrão: negar, escalar por papel",
    ]),
    ("Proteções web", [
        "CSRF habilitado (CookieCsrfToken)",
        "Front envia X-XSRF-TOKEN",
        "Cabeçalhos padrão Spring Security",
        "Tratamento centralizado de erros (RestExceptionHandler)",
    ]),
]
for i, (h, lines) in enumerate(cols):
    sh = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.7 + i * 4.2), Inches(1.95),
                            Inches(4.0), Inches(4.4))
    sh.fill.solid()
    sh.fill.fore_color.rgb = CARD
    sh.line.color.rgb = ACCENT
    sh.line.width = Pt(1.25)
    sh.adjustments[0] = 0.05
    tf = sh.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_right = Inches(0.2)
    tf.margin_top = Inches(0.18)
    p = tf.paragraphs[0]
    r = p.add_run()
    r.text = h
    r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(17), True, ACCENT2, FONT
    for line in lines:
        p = tf.add_paragraph()
        p.space_before = Pt(9)
        r = p.add_run()
        r.text = "• " + line
        r.font.size, r.font.color.rgb, r.font.name = Pt(13), TEXT, FONT
notes(s, ["Segurança confirmada também pela análise final com a skill security-review."])

# ================================================================ slide 9 — auditoria + LGPD
s = new_slide()
title(s, "Auditoria e LGPD", "Rastreabilidade e privacidade por design")
card(s, 0.7, 1.95, 5.9, 4.5, "Auditoria (RF12 · RNF03)",
     "AuditoriaInterceptor registra toda escrita (POST/PUT/DELETE) em logs_auditoria:\n\n"
     "· usuário autenticado\n· ação e recurso\n· status HTTP\n· IP de origem\n· detalhe\n· data/hora (Instant)\n\n"
     "Consulta em /admin/auditoria (somente ADMIN).", accent=ACCENT, body_size=13)
card(s, 6.8, 1.95, 5.9, 4.5, "LGPD (RF14 · RNF04)",
     "· Consentimento obrigatório no cadastro (getConsentimentoAceito)\n"
     "· Exclusão de cliente SEM histórico → remoção total\n"
     "· Exclusão COM histórico → anonimização\n"
     "  (nome = '[DADOS REMOVIDOS - LGPD]', histórico preservado)\n"
     "· Dados em MongoDB local — sem transferência internacional\n"
     "· Telas e textos em conformidade com o Art. 18", accent=ACCENT2, body_size=13)
notes(s, ["LGPD é exigência da banca — enfatizar o direito ao esquecimento sem quebrar auditoria contábil."])

# ================================================================ slide 10 — qualidade
s = new_slide()
title(s, "Qualidade de software", "Testes automatizados e cobertura contínua")
stats = [
    ("110", "testes automatizados\nJUnit 5 + Mockito"),
    ("75%", "cobertura de\ninstruções (JaCoCo)"),
    ("58%", "cobertura de\nbranches"),
    ("0", "falhas na suíte\n./mvnw test"),
]
for i, (big, small) in enumerate(stats):
    x = 0.7 + i * 3.15
    sh = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(1.95), Inches(3.0), Inches(2.2))
    sh.fill.solid()
    sh.fill.fore_color.rgb = CARD
    sh.line.color.rgb = ACCENT2
    sh.line.width = Pt(1.25)
    sh.adjustments[0] = 0.07
    tf = sh.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    p = tf.paragraphs[0]
    p.alignment = PP_ALIGN.CENTER
    r = p.add_run()
    r.text = big
    r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(44), True, ACCENT, FONT
    p2 = tf.add_paragraph()
    p2.alignment = PP_ALIGN.CENTER
    r2 = p2.add_run()
    r2.text = small
    r2.font.size, r2.font.color.rgb, r2.font.name = Pt(12), TEXT, FONT
bullets(s, [
    "Meta da disciplina: ≥ 50% de cobertura — atingida com folga (linhas, branches e instruções).",
    "Slices @WebMvcTest para controllers (MockMvc) + testes unitários de todos os services.",
    "Validações de Bean Validation, máquina de estados e auditoria cobertas por testes.",
    "Execução: ./mvnw test  ·  Relatório: target/site/jacoco/index.html",
], top=4.6, size=15, gap=9)
notes(s, ["Se a banca pedir, mostrar o relatório JaCoCo aberto no navegador."])

# ================================================================ slide 11 — entregáveis
s = new_slide()
title(s, "Entregáveis e documentação")
items = [
    ("README.md", "plano de execução com checklist das 6 fases + como rodar o projeto"),
    ("docs/requisitos.md", "RF01–RF15 e RNF01–RNF08 com status de implementação"),
    ("docs/diagramas.md", "UML de classes, BPMN do fluxo pedido→entrega e sequência"),
    ("docs/manual-usuario.md", "passo a passo de todas as telas com pontos de captura"),
    ("Slides (este arquivo)", "docs/slides-banca-final.pptx"),
    ("Vídeo demonstrativo", "gravação navegando pelo sistema (a produzir — requer app rodando)"),
]
for i, (h, b) in enumerate(items):
    card(s, 0.7, 1.95 + i * 0.83, 12.0, 0.75, h, b, accent=ACCENT2, head_size=13, body_size=12)
notes(s, ["Capturas de tela e vídeo dependem da aplicação rodando com MongoDB — feitos no ambiente local."])

# ================================================================ slide 12 — conclusão
s = new_slide(footer=False)
bar = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(0.35), Inches(7.5))
bar.fill.solid()
bar.fill.fore_color.rgb = ACCENT
bar.line.fill.background()
tb = s.shapes.add_textbox(Inches(1.1), Inches(2.3), Inches(11.5), Inches(1.2))
p = tb.text_frame.paragraphs[0]
r = p.add_run()
r.text = "Obrigado!"
r.font.size, r.font.bold, r.font.color.rgb, r.font.name = Pt(60), True, TEXT, FONT
tb2 = s.shapes.add_textbox(Inches(1.15), Inches(3.6), Inches(11), Inches(2.4))
tf = tb2.text_frame
tf.word_wrap = True
for i, line in enumerate([
    "PediuPatrão: sistema completo — segurança, LGPD, dashboard e 110 testes.",
    "Próximos passos: capturas de tela, vídeo demonstrativo e",
    "módulos planejados (funcionários, entregadores, horários).",
    "",
    "Perguntas?",
]):
    p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
    p.space_after = Pt(8)
    r = p.add_run()
    r.text = line
    r.font.size, r.font.color.rgb, r.font.name = Pt(19), (ACCENT2 if line == "Perguntas?" else MUTED), FONT
notes(s, ["Abrir para perguntas da banca."])

# ---------------------------------------------------------------- salvar
out = r"docs\slides-banca-final.pptx"
prs.save(out)
print(f"OK: {len(prs.slides.__iter__.__self__._sldIdLst)} slides -> {out}")
