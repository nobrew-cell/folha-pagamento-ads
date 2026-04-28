package br.com.folha.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/**
 * Dashboard Analítico — Folha de Pagamento v5.1
 *
 * ESTÉTICA: monocromático puritano. Modo escuro = preto/branco. Modo claro = branco/preto.
 * Cores funcionais restritas: verde para PADRÃO, laranja para COMISSIONADO,
 * roxo para PRODUÇÃO, azul apenas para totais monetários.
 */
public class DashboardBI extends JFrame {

    private static final String VERSAO_DASH = "v5.1";

    private static final double  DEFAULT_SALARIO_BASE     = 2000.00;
    private static final double  DEFAULT_TETO_PERCENTUAL  = 200.0;
    private static final int     DEFAULT_LIMITE_MATRICULA = 0;
    private static final boolean DEFAULT_MODO_RIGIDO      = false;

    private static final String DIR_HIST      = "historico";
    private static final String DIR_EXP       = "exportados/dados";
    private static final String DIR_LOGS      = "logs";
    private static final String LOGO_PATH     = "config/logo.png";
    private static final String CONFIG_PATH   = "config/dashboard.properties";
    private static final String DATABASE_PATH = "database.tsv";

    // ── Paleta ────────────────────────────────────────────────────────────
    private Color BG_ROOT, BG_PANEL, BG_CARD, BG_ROW_A, BG_ROW_B, BG_HEADER, BG_SEL;
    private Color TX_PRIM, TX_SEC, TX_DIS, TX_HEAD;
    private Color BD_LINE, BD_FOCUS;
    private Color C_PADRAO, C_COMISS, C_PROD, C_TOTAL;

    // ── Estado ────────────────────────────────────────────────────────────
    private boolean modoEscuro = true;
    private int     fontSize   = 12;
    private int     abaAtiva   = 0;

    // ── Animação "cursor piscante" terminal ───────────────────────────────
    private javax.swing.Timer timerCursor;
    private boolean cursorVisivel = true;

    private final String[] NOMES_ABAS = {
        "VISÃO GERAL", "FUNCIONÁRIOS", "RELATÓRIOS", "AUDITORIA", "CONFIGURAÇÕES"
    };

    // ── Dados ─────────────────────────────────────────────────────────────
    private final List<RegistroFolha> dados = new ArrayList<>();
    private String ultimoMes = "";

    private double  cfgSalarioBase     = DEFAULT_SALARIO_BASE;
    private double  cfgTetoPercentual  = DEFAULT_TETO_PERCENTUAL;
    private int     cfgLimiteMatricula = DEFAULT_LIMITE_MATRICULA;
    private boolean cfgModoRigido      = DEFAULT_MODO_RIGIDO;

    private String cfgNomeEmpresa = "EMPRESA EXEMPLO LTDA";
    private String cfgNomeUsuario = "administrador";
    private String cfgNomePerfil  = "Administrador";

    private BufferedImage logoImg = null;

    // ── Formatação ────────────────────────────────────────────────────────
    private static final DecimalFormat FMT_M;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("pt", "BR"));
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        FMT_M = new DecimalFormat("R$ #,##0.00", sym);
    }

    // ── Ponto de entrada ──────────────────────────────────────────────────
    public static void abrir() {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new DashboardBI().setVisible(true);
        });
    }

    public DashboardBI() {
        super("Dashboard Analítico — Folha de Pagamento " + VERSAO_DASH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 640));
        setLocationRelativeTo(null);
        aplicarPaleta(true);
        carregarConfigDashboard();
        carregarLogo();
        carregarDados();
        iniciarAnimacaoCursor();
        construirUI();
    }

    // ── Animação cursor terminal ──────────────────────────────────────────
    private void iniciarAnimacaoCursor() {
        timerCursor = new javax.swing.Timer(530, e -> {
        cursorVisivel = !cursorVisivel;
        // Repaint apenas o rodapé — leve
        repaint();
    });
        timerCursor.start();
    }

    // ═════════════════════════════════════════════════════════════════════
    // INICIALIZAÇÃO
    // ═════════════════════════════════════════════════════════════════════

    private void aplicarPaleta(boolean escuro) {
        this.modoEscuro = escuro;
        if (escuro) {
            BG_ROOT   = new Color(0,   0,   0);
            BG_PANEL  = new Color(8,   8,   8);
            BG_CARD   = new Color(14,  14,  14);
            BG_ROW_A  = new Color(8,   8,   8);
            BG_ROW_B  = new Color(16,  16,  16);
            BG_HEADER = new Color(5,   5,   5);
            BG_SEL    = new Color(35,  35,  35);
            TX_PRIM   = new Color(230, 230, 230);
            TX_SEC    = new Color(160, 160, 160);
            TX_DIS    = new Color(80,  80,  80);
            TX_HEAD   = new Color(200, 200, 200);
            BD_LINE   = new Color(35,  35,  35);
            BD_FOCUS  = new Color(70,  70,  70);
        } else {
            BG_ROOT   = new Color(255, 255, 255);
            BG_PANEL  = new Color(248, 248, 248);
            BG_CARD   = new Color(240, 240, 240);
            BG_ROW_A  = new Color(248, 248, 248);
            BG_ROW_B  = new Color(238, 238, 238);
            BG_HEADER = new Color(228, 228, 228);
            BG_SEL    = new Color(210, 210, 210);
            TX_PRIM   = new Color(20,  20,  20);
            TX_SEC    = new Color(80,  80,  80);
            TX_DIS    = new Color(150, 150, 150);
            TX_HEAD   = new Color(40,  40,  40);
            BD_LINE   = new Color(200, 200, 200);
            BD_FOCUS  = new Color(140, 140, 140);
        }
        C_PADRAO = new Color(60,  160, 60);
        C_COMISS = new Color(190, 120, 20);
        C_PROD   = new Color(110, 60,  200);
        C_TOTAL  = new Color(30,  110, 200);
    }

    private void carregarConfigDashboard() {
        new File("config").mkdirs();
        File f = new File(CONFIG_PATH);
        if (!f.exists()) return;
        Properties props = new Properties();
        try (Reader r = new InputStreamReader(new FileInputStream(f),
                java.nio.charset.StandardCharsets.UTF_8)) {
            props.load(r);
            cfgNomeEmpresa = props.getProperty("nomeEmpresa", cfgNomeEmpresa).trim();
            cfgNomeUsuario = props.getProperty("nomeUsuario", cfgNomeUsuario).trim();
            cfgNomePerfil  = props.getProperty("nomePerfil",  cfgNomePerfil).trim();
        } catch (Exception ignored) {}
    }

    private void salvarConfigDashboard() {
        new File("config").mkdirs();
        Properties props = new Properties();
        props.setProperty("nomeEmpresa", cfgNomeEmpresa);
        props.setProperty("nomeUsuario", cfgNomeUsuario);
        props.setProperty("nomePerfil",  cfgNomePerfil);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(CONFIG_PATH),
                java.nio.charset.StandardCharsets.UTF_8)) {
            props.store(w, "Dashboard Analítico — configurações");
        } catch (Exception ignored) {}
    }

    private void carregarLogo() {
        File f = new File(LOGO_PATH);
        if (!f.exists()) return;
        try {
            logoImg = ImageIO.read(f);
            if (logoImg != null) {
                List<Image> icones = new ArrayList<>();
                for (int sz : new int[]{16, 24, 32, 48, 64, 128}) {
                    icones.add(logoImg.getScaledInstance(sz, sz, Image.SCALE_SMOOTH));
                }
                setIconImages(icones);
            }
        } catch (Exception ignored) {}
    }

    private void carregarDados() {
        dados.clear();
        lerConfigDatabase();
        lerPasta(DIR_HIST);
        lerPasta(DIR_EXP);
        Set<String> vistos = new HashSet<>();
        dados.removeIf(r -> !vistos.add(r.ano + "-" + r.mes + "-" + r.matricula));
        dados.sort(Comparator.comparingInt((RegistroFolha r) -> r.ano)
                             .thenComparingInt(r -> r.mes)
                             .thenComparingInt(r -> r.matricula));
        if (!dados.isEmpty()) {
            RegistroFolha u = dados.get(dados.size() - 1);
            ultimoMes = String.format("%04d-%02d", u.ano, u.mes);
        }
    }

    private void lerConfigDatabase() {
        File db = new File(DATABASE_PATH);
        if (!db.exists()) return;
        try (Scanner sc = new Scanner(db, "UTF-8")) {
            if (!sc.hasNextLine()) return;
            String linha = sc.nextLine().trim();
            if (!linha.startsWith("#CONFIG")) return;
            String[] cfg = linha.split("\t");
            if (cfg.length > 1) cfgSalarioBase     = Double.parseDouble(cfg[1].trim());
            if (cfg.length > 2) cfgTetoPercentual   = Double.parseDouble(cfg[2].trim());
            if (cfg.length > 3) cfgLimiteMatricula  = Integer.parseInt(cfg[3].trim());
            if (cfg.length > 4) cfgModoRigido       = Boolean.parseBoolean(cfg[4].trim());
        } catch (Exception ignored) {}
    }

    private void lerPasta(String dir) {
        File pasta = new File(dir);
        if (!pasta.exists()) return;
        File[] tsv = pasta.listFiles(f -> f.getName().endsWith(".tsv"));
        if (tsv == null) return;
        for (File f : tsv) {
            try (Scanner sc = new Scanner(f, "UTF-8")) {
                if (!sc.hasNextLine()) continue;
                String primeira = sc.nextLine().trim();
                if (primeira.startsWith("#CONFIG") && sc.hasNextLine()) sc.nextLine();
                while (sc.hasNextLine()) {
                    String linha = sc.nextLine().trim();
                    if (linha.isEmpty() || linha.startsWith("MATRICULA")) continue;
                    String[] p = linha.split("\t");
                    if (p.length < 11) continue;
                    try {
                        RegistroFolha r = new RegistroFolha();
                        r.matricula   = Integer.parseInt(p[0].trim());
                        r.nome        = p[1].trim();
                        r.tipo        = p[2].trim();
                        r.salarioBase = Double.parseDouble(p[3].trim());
                        r.vendas      = Double.parseDouble(p[4].trim());
                        r.percentual  = Double.parseDouble(p[5].trim());
                        r.qtdPecas    = Integer.parseInt(p[6].trim());
                        r.valorPeca   = Double.parseDouble(p[7].trim());
                        r.salTotal    = Double.parseDouble(p[8].trim());
                        r.mes         = Integer.parseInt(p[9].trim());
                        r.ano         = Integer.parseInt(p[10].trim());
                        dados.add(r);
                    } catch (NumberFormatException ignored) {}
                }
            } catch (Exception ignored) {}
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // UI RAIZ
    // ═════════════════════════════════════════════════════════════════════

    private void construirUI() {
        getContentPane().removeAll();
        getContentPane().setBackground(BG_ROOT);
        setLayout(new BorderLayout());
        add(construirTopo(),   BorderLayout.NORTH);
        add(construirAbas(),   BorderLayout.CENTER);
        add(construirRodape(), BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    private JPanel construirTopo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_ROOT);
        p.setBorder(new MatteBorder(0, 0, 1, 0, BD_LINE));

        JPanel linha = new JPanel(new BorderLayout());
        linha.setBackground(BG_ROOT);
        linha.setBorder(new EmptyBorder(7, 14, 6, 14));

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esq.setBackground(BG_ROOT);
        if (logoImg != null) {
            int h = 30, w = (int)(logoImg.getWidth() * ((double)h / logoImg.getHeight()));
            esq.add(new JLabel(new ImageIcon(logoImg.getScaledInstance(w, h, Image.SCALE_SMOOTH))));
        }
        JLabel titulo = new JLabel("Dashboard Analítico  |  " + cfgNomeEmpresa);
        titulo.setFont(fonte(Font.BOLD, 0));
        titulo.setForeground(TX_PRIM);
        esq.add(titulo);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        dir.setBackground(BG_ROOT);

        JButton btnMenos = botao("-");
        btnMenos.setToolTipText("Diminuir fonte");
        btnMenos.addActionListener(e -> { if (fontSize > 9)  { fontSize--; construirUI(); }});

        JButton btnMais = botao("+");
        btnMais.setToolTipText("Aumentar fonte");
        btnMais.addActionListener(e -> { if (fontSize < 18) { fontSize++; construirUI(); }});

        JButton btnModo = botao(modoEscuro ? "☀ CLARO" : "☾ ESCURO");
        btnModo.addActionListener(e -> { aplicarPaleta(!modoEscuro); construirUI(); });

        JButton btnF5 = botao("↺ F5 ATUALIZAR");
        btnF5.addActionListener(e -> { carregarDados(); construirUI(); });

        dir.add(btnMenos); dir.add(btnMais);
        dir.add(sep_v());  dir.add(btnModo);
        dir.add(sep_v());  dir.add(btnF5);

        linha.add(esq, BorderLayout.WEST);
        linha.add(dir, BorderLayout.EAST);
        p.add(linha, BorderLayout.CENTER);
        return p;
    }

    private JPanel construirBarraAbas() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(BG_ROOT);
        bar.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < NOMES_ABAS.length; i++) {
            final int idx = i;
            boolean ativa = (i == abaAtiva);
            String label = ativa ? "[" + NOMES_ABAS[i] + "]" : " " + NOMES_ABAS[i] + " ";
            JButton btn = new JButton(label);
            btn.setFont(fonte(ativa ? Font.BOLD : Font.PLAIN, -1));
            btn.setForeground(ativa ? BG_ROOT : TX_DIS);
            btn.setBackground(ativa ? TX_PRIM  : BG_ROOT);
            btn.setBorder(new EmptyBorder(5, 10, 5, 10));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setOpaque(true);
            btn.addActionListener(e -> { abaAtiva = idx; construirUI(); });
            if (!ativa) {
                btn.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { btn.setForeground(TX_PRIM); btn.setBackground(BG_CARD); }
                    @Override public void mouseExited(MouseEvent e)  { btn.setForeground(TX_DIS);  btn.setBackground(BG_ROOT); }
                });
            }
            bar.add(btn);
        }
        return bar;
    }

    private JPanel construirAbas() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BG_ROOT);
        JPanel barraWrapper = new JPanel(new BorderLayout());
        barraWrapper.setBackground(BG_ROOT);
        barraWrapper.setBorder(new MatteBorder(0, 0, 1, 0, BD_LINE));
        barraWrapper.add(construirBarraAbas(), BorderLayout.WEST);
        container.add(barraWrapper, BorderLayout.NORTH);
        JPanel conteudo = switch (abaAtiva) {
            case 0 -> abaVisaoGeral();
            case 1 -> abaFuncionarios();
            case 2 -> abaRelatorios();
            case 3 -> abaAuditoria();
            case 4 -> abaConfiguracoes();
            default -> abaVisaoGeral();
        };
        container.add(conteudo, BorderLayout.CENTER);
        return container;
    }

    // ── Rodapé com cursor piscante estilo terminal ────────────────────────
    private JPanel construirRodape() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        p.setBackground(BG_ROOT);
        p.setBorder(new MatteBorder(1, 0, 0, 0, BD_LINE));

        // Esquerda: texto com cursor piscante
        JLabel lEsq = new JLabel() {
            @Override public String getText() {
                String base = "  Dashboard Analítico " + VERSAO_DASH +
                    "  ·  Lê dados de historico/ e exportados/dados/" +
                    "  ·  Configure em config/dashboard.properties";
                return base + (cursorVisivel ? " _" : "  ");
            }
        };
        lEsq.setFont(fonte(Font.PLAIN, -3));
        lEsq.setForeground(TX_DIS);
        lEsq.setBorder(new EmptyBorder(4, 0, 4, 0));

        // Direita: usuário — com clip via setMaximumSize não funciona em label,
        // então usamos um painel com overflow oculto
        JLabel lDir = new JLabel("USUÁRIO: " + cfgNomeUsuario + "  |  PERFIL: " + cfgNomePerfil + "  ") {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(Math.min(d.width, 280), d.height);
            }
            @Override public Dimension getMaximumSize() {
                return new Dimension(280, super.getMaximumSize().height);
            }
        };
        lDir.setFont(fonte(Font.PLAIN, -2));
        lDir.setForeground(TX_DIS);
        lDir.setBorder(new EmptyBorder(4, 0, 4, 0));

        // Wrapper para lDir que respeita largura máxima e corta com "..."
        JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(Math.min(super.getPreferredSize().width, 300), super.getPreferredSize().height);
            }
            @Override public Dimension getMaximumSize() {
                return new Dimension(300, Integer.MAX_VALUE);
            }
        };
        dirPanel.setBackground(BG_ROOT);
        dirPanel.setOpaque(false);

        // Clip label: usa clipagem manual
        String userStr = "USUÁRIO: " + cfgNomeUsuario + "  |  PERFIL: " + cfgNomePerfil + "  ";
        JLabel lDirClip = new JLabel(userStr) {
            @Override public void paintComponent(Graphics g) {
                // Clip ao tamanho disponível
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setClip(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        lDirClip.setFont(fonte(Font.PLAIN, -2));
        lDirClip.setForeground(TX_DIS);
        lDirClip.setBorder(new EmptyBorder(4, 4, 4, 4));

        // Adicionar timer para atualizar o cursor piscante

        p.add(lEsq, BorderLayout.CENTER);
        p.add(lDirClip, BorderLayout.EAST);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 0 — VISÃO GERAL
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaVisaoGeral() {
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(BG_ROOT);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        toolbar.setBackground(BG_ROOT);
        toolbar.setBorder(new EmptyBorder(0, 0, 4, 0));
        JButton btnExportPNG = botao("📷 EXPORTAR PNG");
        btnExportPNG.setToolTipText("Escolher blocos e exportar como PNG");
        btnExportPNG.addActionListener(e -> mostrarDialogoExportVG());
        toolbar.add(btnExportPNG);

        JPanel conteudo = criarPainelVisaoGeral();
        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(null);
        scroll.setBackground(BG_ROOT);
        scroll.getViewport().setBackground(BG_ROOT);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        painelPrincipal.add(toolbar, BorderLayout.NORTH);
        painelPrincipal.add(scroll, BorderLayout.CENTER);
        return painelPrincipal;
    }

    // ── Diálogo de seleção de blocos para exportar ────────────────────────
    private void mostrarDialogoExportVG() {
        String[] blocos = {
            "KPIs (Totais do mês)",
            "Gráfico Evolução 6 meses",
            "Pizza Distribuição por Tipo",
            "Comparação com Mês Anterior",
            "Top 5 Maiores Salários",
            "Consistência dos Dados",
            "Logs Recentes",
            "Análise Anual"
        };
        JCheckBox[] checks = new JCheckBox[blocos.length];
        JPanel pSel = new JPanel(new GridLayout(0, 1, 0, 4));
        pSel.setBackground(BG_CARD);
        pSel.setBorder(new EmptyBorder(12, 16, 12, 16));
        for (int i = 0; i < blocos.length; i++) {
            checks[i] = new JCheckBox(blocos[i], true);
            checks[i].setBackground(BG_CARD);
            checks[i].setForeground(TX_PRIM);
            checks[i].setFont(fonte(Font.PLAIN, -1));
            pSel.add(checks[i]);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CARD);
        JLabel lTitulo = new JLabel("  Selecione os blocos a incluir no PNG:");
        lTitulo.setFont(fonte(Font.BOLD, 0));
        lTitulo.setForeground(TX_HEAD);
        lTitulo.setBorder(new EmptyBorder(10, 10, 8, 10));
        wrapper.add(lTitulo, BorderLayout.NORTH);
        wrapper.add(pSel, BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, wrapper,
            "Exportar Visão Geral — PNG", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        boolean[] sel = new boolean[blocos.length];
        for (int i = 0; i < checks.length; i++) sel[i] = checks[i].isSelected();
        exportarVisaoGeralPNG(sel);
    }

    /**
     * Cria o painel completo da Visão Geral (todos os blocos) sem scroll.
     */
    private JPanel criarPainelVisaoGeral() {
        return criarPainelVisaoGeralSel(new boolean[]{true,true,true,true,true,true,true,true});
    }

    private JPanel criarPainelVisaoGeralSel(boolean[] sel) {
        List<RegistroFolha> mesAtual = doMes(ultimoMes);
        List<RegistroFolha> mesAnt   = doMes(mesAnterior(ultimoMes));

        double totalBruto = mesAtual.stream().mapToDouble(r -> r.salTotal).sum();
        double totalAnt   = mesAnt.stream().mapToDouble(r -> r.salTotal).sum();
        double maior      = mesAtual.stream().mapToDouble(r -> r.salTotal).max().orElse(0);
        double media      = mesAtual.stream().mapToDouble(r -> r.salTotal).average().orElse(0);
        int    qtdFunc    = mesAtual.size();

        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(BG_ROOT);

        // ── Linha 1: KPI cards ─────────────────────────────────────────
        if (sel[0]) {
            JPanel linhaCards = new JPanel(new GridLayout(1, 4, 1, 0));
            linhaCards.setBackground(BD_LINE);
            String varStr = totalAnt > 0
                ? String.format("%+.1f%% vs %s", (totalBruto - totalAnt) / totalAnt * 100,
                    labelMesCurto(mesAnterior(ultimoMes)))
                : "primeiro mês";
            linhaCards.add(cardKPI("TOTAL DA FOLHA",  FMT_M.format(totalBruto), varStr,       C_TOTAL));
            linhaCards.add(cardKPI("FUNCIONÁRIOS",    String.valueOf(qtdFunc),   "no período", TX_PRIM));
            linhaCards.add(cardKPI("MAIOR SALÁRIO",   FMT_M.format(maior),      "",           C_PADRAO));
            linhaCards.add(cardKPI("MÉDIA SALARIAL",  FMT_M.format(media),      "",           TX_SEC));
            corpo.add(linhaCards);
            corpo.add(Box.createVerticalStrut(8));
            JPanel sep = new JPanel(); sep.setBackground(BD_LINE); sep.setPreferredSize(new Dimension(0,1)); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
            corpo.add(sep);
            corpo.add(Box.createVerticalStrut(8));
        }

        // ── Linha 2: gráfico evolução + pizza ─────────────────────────
        boolean temGraf = sel[1], temPizza = sel[2];
        if (temGraf || temPizza) {
            if (temGraf && temPizza) {
                JPanel linha2 = new JPanel(new GridLayout(1, 2, 1, 0));
                linha2.setBackground(BD_LINE);
                linha2.add(blocoGraficoEvolucao());
                linha2.add(blocoPizzaDistribuicao(mesAtual));
                corpo.add(linha2);
            } else if (temGraf) {
                corpo.add(blocoGraficoEvolucao());
            } else {
                corpo.add(blocoPizzaDistribuicao(mesAtual));
            }
        }

        // ── Linha 3: comparação | top5 | consistência ─────────────────
        boolean temComp = sel[3], temTop5 = sel[4], temCons = sel[5];
        if (temComp || temTop5 || temCons) {
            List<JPanel> blocos3 = new ArrayList<>();
            if (temComp) blocos3.add(blocoComparacao(totalBruto, totalAnt, qtdFunc, mesAnt.size()));
            if (temTop5) blocos3.add(blocoTop5(mesAtual));
            if (temCons) blocos3.add(blocoConsistencia(mesAtual));
            JPanel linha3 = new JPanel(new GridLayout(1, blocos3.size(), 1, 0));
            linha3.setBackground(BD_LINE);
            linha3.setBorder(new MatteBorder(1, 0, 0, 0, BD_LINE));
            for (JPanel b : blocos3) linha3.add(b);
            corpo.add(linha3);
        }

        // ── Linha 4: logs recentes + análise anual ────────────────────
        boolean temLogs = sel[6], temAnual = sel[7];
        if (temLogs || temAnual) {
            if (temLogs && temAnual) {
                JPanel linha4 = new JPanel(new GridLayout(1, 2, 1, 0));
                linha4.setBackground(BD_LINE);
                linha4.setBorder(new MatteBorder(1, 0, 0, 0, BD_LINE));
                linha4.add(blocoLogsRecentes());
                linha4.add(blocoAnaliseAnual());
                corpo.add(linha4);
            } else if (temLogs) {
                corpo.add(blocoLogsRecentes());
            } else {
                corpo.add(blocoAnaliseAnual());
            }
        }

        return corpo;
    }

    // ── Card KPI ──────────────────────────────────────────────────────────
    private JPanel cardKPI(String titulo, String valor, String sub, Color corValor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 2, 0, 0, corValor),
            new EmptyBorder(10, 12, 10, 12)));
        JLabel lT = new JLabel(titulo);
        lT.setFont(fonte(Font.PLAIN, -3)); lT.setForeground(TX_DIS);
        lT.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lV = new JLabel(valor);
        lV.setFont(fonte(Font.BOLD, 2)); lV.setForeground(corValor);
        lV.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lS = new JLabel(sub.isEmpty() ? " " : sub);
        lS.setFont(fonte(Font.PLAIN, -3)); lS.setForeground(TX_DIS);
        lS.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lT); p.add(Box.createVerticalStrut(4));
        p.add(lV); p.add(Box.createVerticalStrut(2)); p.add(lS);
        return p;
    }

    // ── Gráfico de evolução (barras) ──────────────────────────────────────
    private JPanel blocoGraficoEvolucao() {
        JPanel p = blocoTerminal("EVOLUÇÃO DA FOLHA  ·  ÚLTIMOS 6 MESES");
        p.setLayout(new BorderLayout());

        Map<String, Double> evolOrdenado = dados.stream()
            .collect(Collectors.groupingBy(
                r -> String.format("%04d-%02d", r.ano, r.mes),
                LinkedHashMap::new,
                Collectors.summingDouble(r -> r.salTotal)))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (e1, e2) -> e1, LinkedHashMap::new));

        List<Map.Entry<String, Double>> ultimos6 = new ArrayList<>(evolOrdenado.entrySet());
        if (ultimos6.size() > 6) ultimos6 = ultimos6.subList(ultimos6.size() - 6, ultimos6.size());
        final List<Map.Entry<String, Double>> dadosGraf = ultimos6;

        JPanel graf = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(BG_CARD);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (dadosGraf.isEmpty()) {
                    g2.setColor(TX_DIS); g2.setFont(fonte(Font.PLAIN, 0));
                    g2.drawString("Nenhum dado em historico/ ou exportados/dados/", 20, getHeight() / 2);
                    return;
                }

                int mE = 65, mD = 10, mT = 22, mB = 38;
                int ld = getWidth() - mE - mD;
                int ad = getHeight() - mT - mB;
                double maxV = dadosGraf.stream().mapToDouble(Map.Entry::getValue).max().orElse(1);
                int n    = dadosGraf.size();
                int grpW = n > 0 ? ld / n : ld;
                int barW = Math.max(6, grpW - 10);

                float[] dash = {3f, 4f};
                g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
                for (int i = 1; i <= 4; i++) {
                    int gy = mT + ad - (int)(ad * i / 4.0);
                    g2.setColor(BD_LINE);
                    g2.drawLine(mE, gy, mE + ld, gy);
                    g2.setColor(TX_DIS); g2.setFont(fonte(Font.PLAIN, -3));
                    g2.drawString(formatarMilhar(maxV * i / 4.0), 2, gy + 4);
                }
                g2.setStroke(new BasicStroke(1f));

                for (int i = 0; i < dadosGraf.size(); i++) {
                    Map.Entry<String, Double> e = dadosGraf.get(i);
                    double v = e.getValue();
                    int h = (int)(v / maxV * ad);
                    int x = mE + i * grpW + (grpW - barW) / 2;
                    int y = mT + ad - h;

                    g2.setColor(modoEscuro ? new Color(70, 70, 70) : new Color(150, 150, 150));
                    g2.fillRect(x, y, barW, h);
                    g2.setColor(TX_PRIM);
                    g2.fillRect(x, y, barW, 2);

                    g2.setColor(TX_SEC); g2.setFont(fonte(Font.PLAIN, -3));
                    String vs = formatarMilhar(v);
                    FontMetrics fm = g2.getFontMetrics();
                    int vx = x + (barW - fm.stringWidth(vs)) / 2;
                    if (vx >= mE) g2.drawString(vs, vx, Math.max(y - 3, mT + 10));

                    String lbl = labelMesCurto(e.getKey());
                    g2.setColor(TX_DIS); g2.setFont(fonte(Font.PLAIN, -3));
                    fm = g2.getFontMetrics();
                    g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, mT + ad + 14);
                }
                g2.setColor(BD_FOCUS);
                g2.drawLine(mE, mT + ad, mE + ld, mT + ad);
            }
        };
        graf.setBackground(BG_CARD);
        p.add(graf, BorderLayout.CENTER);
        return p;
    }

    // ── Pizza terminal — legenda lateral, sem linhas de raio ─────────────
    private JPanel blocoPizzaDistribuicao(List<RegistroFolha> mes) {
        JPanel p = blocoTerminal("DISTRIBUIÇÃO POR TIPO  ·  " + labelMesCurto(ultimoMes).toUpperCase());
        p.setLayout(new BorderLayout(8, 0));

        double tPad  = mes.stream().filter(r -> "PADRAO".equals(r.tipo)).mapToDouble(r -> r.salTotal).sum();
        double tCom  = mes.stream().filter(r -> "COMISSIONADO".equals(r.tipo)).mapToDouble(r -> r.salTotal).sum();
        double tPro  = mes.stream().filter(r -> "PRODUCAO".equals(r.tipo)).mapToDouble(r -> r.salTotal).sum();
        double total = tPad + tCom + tPro;
        if (total <= 0) total = 1;

        final double tot   = total;
        final double[] vals = {tPad, tCom, tPro};
        final Color[]  cors = {C_PADRAO, C_COMISS, C_PROD};
        final String[] noms = {"PADRÃO", "COMISSIONADO", "PRODUÇÃO"};

        // Pizza em paintComponent — SEM linhas de raio, com legenda lateral
        JPanel pizza = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(BG_CARD);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int cx = getWidth() / 2, cy = getHeight() / 2;
                int r  = Math.min(cx, cy) - 8;
                if (r < 10) return;

                // 1ª passagem: fatias preenchidas (sem separadores)
                double inicio = -90.0;
                for (int i = 0; i < vals.length; i++) {
                    double arco = vals[i] / tot * 360.0;
                    if (arco < 0.5) { inicio += arco; continue; }
                    Color base = cors[i];
                    g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(),
                                          modoEscuro ? 110 : 160));
                    g2.fillArc(cx - r, cy - r, r * 2, r * 2, (int)inicio, (int)arco);
                    inicio += arco;
                }

                // 2ª passagem: bordas coloridas por cima (sem linha de raio)
                inicio = -90.0;
                for (int i = 0; i < vals.length; i++) {
                    double arco = vals[i] / tot * 360.0;
                    if (arco < 0.5) { inicio += arco; continue; }
                    g2.setColor(cors[i]);
                    g2.setStroke(new BasicStroke(modoEscuro ? 1.5f : 1.2f));
                    g2.drawArc(cx - r, cy - r, r * 2, r * 2, (int)inicio, (int)arco);
                    inicio += arco;
                }

                // Anel interno (donut)
                int ri = (int)(r * 0.38);
                g2.setColor(BG_CARD);
                g2.fillOval(cx - ri, cy - ri, ri * 2, ri * 2);
                g2.setColor(BD_LINE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(cx - ri, cy - ri, ri * 2, ri * 2);

                // Total no centro
                g2.setColor(C_TOTAL);
                g2.setFont(fonte(Font.BOLD, -2));
                FontMetrics fm = g2.getFontMetrics();
                String totStr = formatarMilhar(tot);
                g2.drawString(totStr, cx - fm.stringWidth(totStr) / 2, cy + fm.getAscent() / 2 - 2);
                g2.setColor(TX_DIS);
                g2.setFont(fonte(Font.PLAIN, -4));
                fm = g2.getFontMetrics();
                String subStr = "total";
                g2.drawString(subStr, cx - fm.stringWidth(subStr) / 2, cy + fm.getAscent() / 2 + 8);
            }
        };
        pizza.setPreferredSize(new Dimension(140, 0));
        pizza.setBackground(BG_CARD);

        // Legenda lateral com %  e valor — estilo ASCII terminal
        JPanel legenda = new JPanel();
        legenda.setLayout(new BoxLayout(legenda, BoxLayout.Y_AXIS));
        legenda.setBackground(BG_CARD);
        legenda.setBorder(new EmptyBorder(8, 4, 8, 8));

        int[] qtds = {
            (int) mes.stream().filter(r -> "PADRAO".equals(r.tipo)).count(),
            (int) mes.stream().filter(r -> "COMISSIONADO".equals(r.tipo)).count(),
            (int) mes.stream().filter(r -> "PRODUCAO".equals(r.tipo)).count()
        };

        for (int i = 0; i < noms.length; i++) {
            double pct = vals[i] / tot * 100;

            // Linha 1: percentual em destaque + cor
            JLabel lPct = new JLabel(String.format("%.0f%%", pct));
            lPct.setFont(fonte(Font.BOLD, 2));
            lPct.setForeground(cors[i]);
            lPct.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Linha 2: nome do tipo
            JLabel lNome = new JLabel(noms[i] + " (" + qtds[i] + ")");
            lNome.setFont(fonte(Font.PLAIN, -2));
            lNome.setForeground(TX_SEC);
            lNome.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Linha 3: valor monetário
            JLabel lVal = new JLabel(FMT_M.format(vals[i]));
            lVal.setFont(fonte(Font.PLAIN, -2));
            lVal.setForeground(TX_DIS);
            lVal.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Barra ASCII proporcional
            int barLen = (int)(pct / 100.0 * 14);
            JLabel lBar = new JLabel("[" + "█".repeat(Math.max(0, barLen))
                + "░".repeat(Math.max(0, 14 - barLen)) + "]");
            lBar.setFont(fonte(Font.PLAIN, -3));
            lBar.setForeground(new Color(cors[i].getRed(), cors[i].getGreen(), cors[i].getBlue(), 180));
            lBar.setAlignmentX(Component.LEFT_ALIGNMENT);

            legenda.add(lPct);
            legenda.add(lNome);
            legenda.add(lVal);
            legenda.add(lBar);
            if (i < noms.length - 1) {
                legenda.add(Box.createVerticalStrut(8));
                legenda.add(hSep());
                legenda.add(Box.createVerticalStrut(8));
            }
        }

        // Linha TOTAL
        legenda.add(Box.createVerticalStrut(8));
        legenda.add(hSep());
        JLabel lTLabel = new JLabel("TOTAL");
        lTLabel.setFont(fonte(Font.BOLD, -1)); lTLabel.setForeground(TX_DIS);
        lTLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lTVal = new JLabel(FMT_M.format(total));
        lTVal.setFont(fonte(Font.BOLD, 0)); lTVal.setForeground(C_TOTAL);
        lTVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        legenda.add(Box.createVerticalStrut(4));
        legenda.add(lTLabel);
        legenda.add(lTVal);

        p.add(pizza,   BorderLayout.WEST);
        p.add(legenda, BorderLayout.CENTER);
        return p;
    }

    // ── Comparação com mês anterior ───────────────────────────────────────
    private JPanel blocoComparacao(double totalAtual, double totalAnt, int qtdAtual, int qtdAnt) {
        JPanel p = blocoTerminal("COMPARAÇÃO  ·  MÊS ANTERIOR");

        String mesAntLabel = labelMesCurto(mesAnterior(ultimoMes)).toUpperCase();
        String mesAtLabel  = labelMesCurto(ultimoMes).toUpperCase();

        double difTotal = totalAtual - totalAnt;
        double pctTotal = totalAnt > 0 ? difTotal / totalAnt * 100 : 0;
        int    difQtd   = qtdAtual - qtdAnt;

        p.add(linhaKVComp("",          mesAntLabel,                  mesAtLabel,                  TX_DIS));
        p.add(hSep());
        p.add(linhaKVComp("TOTAL",     FMT_M.format(totalAnt),       FMT_M.format(totalAtual),    TX_PRIM));
        p.add(linhaKVComp("VARIAÇÃO",  totalAnt > 0 ? "—" : "—",
            totalAnt > 0 ? String.format("%+.1f%%", pctTotal) : "primeiro mês",
            pctTotal >= 0 ? C_PADRAO : new Color(180, 50, 50)));
        p.add(linhaKVComp("DIF. ABS.", totalAnt > 0 ? "—" : "—",
            totalAnt > 0 ? FMT_M.format(difTotal) : "—",
            difTotal >= 0 ? C_PADRAO : new Color(180, 50, 50)));
        p.add(hSep());
        p.add(linhaKVComp("FUNC.",
            qtdAnt > 0 ? String.valueOf(qtdAnt) : "—",
            String.valueOf(qtdAtual),
            TX_PRIM));
        p.add(linhaKVComp("DIF.",
            "—",
            qtdAnt > 0 ? String.format("%+d", difQtd) : "primeiro mês",
            difQtd >= 0 ? C_PADRAO : new Color(180, 50, 50)));

        if (totalAnt <= 0) {
            p.add(Box.createVerticalStrut(8));
            JLabel nota = new JLabel("  Sem dados do mês anterior.");
            nota.setFont(fonte(Font.ITALIC, -2));
            nota.setForeground(TX_DIS);
            nota.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(nota);
        }
        return p;
    }

    private JPanel linhaKVComp(String label, String valEsq, String valDir, Color cor) {
        JPanel row = new JPanel(new GridLayout(1, 3, 4, 0));
        row.setBackground(BG_CARD);
        row.setBorder(new EmptyBorder(2, 0, 2, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 8));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lL = new JLabel(label);
        lL.setFont(fonte(Font.PLAIN, -2)); lL.setForeground(TX_DIS);

        JLabel lE = new JLabel(valEsq);
        lE.setFont(fonte(Font.PLAIN, -1)); lE.setForeground(TX_SEC);

        JLabel lD = new JLabel(valDir);
        lD.setFont(fonte(Font.BOLD, -1)); lD.setForeground(cor);

        row.add(lL); row.add(lE); row.add(lD);
        return row;
    }

    // ── Top 5 maiores salários ─────────────────────────────────────────────
    private JPanel blocoTop5(List<RegistroFolha> mes) {
        JPanel p = blocoTerminal("TOP 5  ·  MAIORES SALÁRIOS  ·  " + labelMesCurto(ultimoMes).toUpperCase());

        List<RegistroFolha> top5 = mes.stream()
            .sorted(Comparator.comparingDouble((RegistroFolha r) -> r.salTotal).reversed())
            .limit(5)
            .collect(Collectors.toList());

        if (top5.isEmpty()) {
            JLabel vazio = new JLabel("  Sem dados para o período.");
            vazio.setFont(fonte(Font.PLAIN, -1)); vazio.setForeground(TX_DIS);
            vazio.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(vazio);
            return p;
        }

        double maxSal = top5.get(0).salTotal;
        int pos = 1;
        for (RegistroFolha r : top5) {
            Color cor = switch (r.tipo) {
                case "PADRAO"       -> C_PADRAO;
                case "COMISSIONADO" -> C_COMISS;
                case "PRODUCAO"     -> C_PROD;
                default             -> TX_PRIM;
            };
            int barLen = maxSal > 0 ? (int)(r.salTotal / maxSal * 18) : 0;

            JPanel rowA = new JPanel(new BorderLayout(4, 0));
            rowA.setBackground(BG_CARD);
            rowA.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 6));
            rowA.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lPos = new JLabel(String.format("%d.", pos));
            lPos.setFont(fonte(Font.BOLD, -2)); lPos.setForeground(TX_DIS);
            lPos.setPreferredSize(new Dimension(18, 16));
            JLabel lNome = new JLabel(r.nome.length() > 22 ? r.nome.substring(0, 20) + "…" : r.nome);
            lNome.setFont(fonte(Font.PLAIN, -1)); lNome.setForeground(cor);
            JLabel lVal = new JLabel(FMT_M.format(r.salTotal));
            lVal.setFont(fonte(Font.BOLD, -1)); lVal.setForeground(cor);
            rowA.add(lPos,  BorderLayout.WEST);
            rowA.add(lNome, BorderLayout.CENTER);
            rowA.add(lVal,  BorderLayout.EAST);
            p.add(rowA);

            JPanel rowB = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            rowB.setBackground(BG_CARD);
            rowB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
            rowB.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lBar = new JLabel("   " + "#".repeat(Math.max(0, barLen)));
            lBar.setFont(fonte(Font.PLAIN, -3)); lBar.setForeground(cor);
            rowB.add(lBar);
            p.add(rowB);
            p.add(Box.createVerticalStrut(3));
            pos++;
        }
        return p;
    }

    // ── Consistência dos dados ────────────────────────────────────────────
    private JPanel blocoConsistencia(List<RegistroFolha> mes) {
        JPanel p = blocoTerminal("CONSISTÊNCIA  ·  " + labelMesCurto(ultimoMes).toUpperCase());

        boolean dbExiste = new File(DATABASE_PATH).exists();
        long semSalario  = mes.stream().filter(r -> r.salTotal <= 0).count();
        long semNome     = mes.stream().filter(r -> r.nome == null || r.nome.isBlank()).count();
        long mesesNoHist = dados.stream()
            .map(r -> String.format("%04d-%02d", r.ano, r.mes))
            .distinct().count();
        boolean histVazio = dados.isEmpty();

        Object[][] itens = {
            {"[OK]", "database.tsv encontrado", dbExiste    ? "SIM" : "NÃO", dbExiste    ? C_PADRAO : new Color(180, 50, 50)},
            {"[OK]", "Histórico carregado",      !histVazio  ? "SIM" : "NÃO", !histVazio  ? C_PADRAO : new Color(180, 50, 50)},
            {"[  ]", "Meses no histórico",        String.valueOf(mesesNoHist),  C_TOTAL},
            {"[  ]", "Funcionários neste mês",    String.valueOf(mes.size()),   TX_PRIM},
            {"[!!]", "Sal. zero neste mês",        String.valueOf(semSalario),   semSalario == 0 ? TX_DIS : new Color(180, 50, 50)},
            {"[!!]", "Nome vazio neste mês",       String.valueOf(semNome),      semNome    == 0 ? TX_DIS : new Color(180, 50, 50)},
        };

        for (Object[] item : itens) {
            String icone = (String) item[0];
            String desc  = (String) item[1];
            String valor = (String) item[2];
            Color  cor   = (Color)  item[3];

            JPanel row = new JPanel(new BorderLayout(4, 0));
            row.setBackground(BG_CARD);
            row.setBorder(new EmptyBorder(2, 0, 2, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 8));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lIcon = new JLabel(icone);
            lIcon.setFont(fonte(Font.BOLD, -2)); lIcon.setForeground(cor);
            lIcon.setPreferredSize(new Dimension(32, 16));

            JLabel lDesc = new JLabel(desc);
            lDesc.setFont(fonte(Font.PLAIN, -2)); lDesc.setForeground(TX_SEC);

            JLabel lVal = new JLabel(valor);
            lVal.setFont(fonte(Font.BOLD, -1)); lVal.setForeground(cor);

            row.add(lIcon, BorderLayout.WEST);
            row.add(lDesc, BorderLayout.CENTER);
            row.add(lVal,  BorderLayout.EAST);
            p.add(row);
        }

        p.add(hSep());
        boolean tudo = dbExiste && !histVazio && semSalario == 0 && semNome == 0;
        JLabel lStatus = new JLabel(tudo ? "[OK] DADOS CONSISTENTES" : "[!!] VERIFIQUE OS ITENS ACIMA");
        lStatus.setFont(fonte(Font.BOLD, -1));
        lStatus.setForeground(tudo ? C_PADRAO : new Color(180, 50, 50));
        lStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(Box.createVerticalStrut(4));
        p.add(lStatus);

        return p;
    }

    // ── Logs recentes ─────────────────────────────────────────────────────
    private JPanel blocoLogsRecentes() {
        JPanel p = blocoTerminal("AUDITORIA  ·  ÚLTIMAS OPERAÇÕES");
        p.setLayout(new BorderLayout());

        List<String[]> logs = lerLogs();
        logs.sort((a, b) -> b[0].compareTo(a[0]));
        List<String[]> recentes = logs.stream().limit(8).collect(Collectors.toList());

        String[] cols = {"DATA/HORA", "OPERAÇÃO", "DETALHES"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] l : recentes)
            modelo.addRow(new Object[]{l[0], l[1], l[2]});
        if (recentes.isEmpty())
            modelo.addRow(new Object[]{"—", "—", "Nenhum log encontrado em logs/"});

        JTable tabela = tabelaTerminal(modelo);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(500);
        tabela.setDefaultRenderer(Object.class, rendererOperacao(1));

        p.add(scrollTerminal(tabela), BorderLayout.CENTER);
        return p;
    }

    // ── Análise anual ─────────────────────────────────────────────────────
    private JPanel blocoAnaliseAnual() {
        int anoAtual = dados.isEmpty() ? LocalDateTime.now().getYear() : dados.get(dados.size()-1).ano;
        JPanel p = blocoTerminal("ANÁLISE ANUAL  ·  " + anoAtual);
        p.setLayout(new BorderLayout());

        String[] cols = {"MÊS", "FUNC.", "TOTAL DA FOLHA", "MAIOR SALÁRIO", "MÉDIA", "VARIAÇÃO"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        Map<Integer, List<RegistroFolha>> porMes = dados.stream()
            .filter(r -> r.ano == anoAtual)
            .collect(Collectors.groupingBy(r -> r.mes, TreeMap::new, Collectors.toList()));

        double anterior = -1;
        for (Map.Entry<Integer, List<RegistroFolha>> e : porMes.entrySet()) {
            List<RegistroFolha> regs = e.getValue();
            double tot = regs.stream().mapToDouble(r -> r.salTotal).sum();
            double max = regs.stream().mapToDouble(r -> r.salTotal).max().orElse(0);
            double med = regs.stream().mapToDouble(r -> r.salTotal).average().orElse(0);
            String var = anterior < 0 ? "—"
                : String.format("%+.1f%%", (tot - anterior) / anterior * 100);
            modelo.addRow(new Object[]{nomeMes(e.getKey()), regs.size(),
                FMT_M.format(tot), FMT_M.format(max), FMT_M.format(med), var});
            anterior = tot;
        }
        if (modelo.getRowCount() > 0) {
            double totAno = porMes.values().stream().flatMap(Collection::stream)
                .mapToDouble(r -> r.salTotal).sum();
            modelo.addRow(new Object[]{"TOTAL " + anoAtual, "—", FMT_M.format(totAno), "—", "—", "—"});
        }

        final int lastRow = modelo.getRowCount() - 1;
        JTable tabela = tabelaTerminal(modelo);
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, val, sel, foc, row, col);
                boolean isTotal = (row == lastRow && lastRow >= 0);
                setBackground(sel ? BG_SEL : isTotal ? BG_HEADER : row % 2 == 0 ? BG_ROW_A : BG_ROW_B);
                setForeground(sel ? BG_ROOT : isTotal ? C_TOTAL : TX_PRIM);
                setFont(fonte(isTotal ? Font.BOLD : Font.PLAIN, -1));
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        p.add(scrollTerminal(tabela), BorderLayout.CENTER);
        return p;
    }

    // ── Exportar visão geral como PNG (com seleção de blocos) ─────────────
    private void exportarVisaoGeralPNG(boolean[] sel) {
        JPanel painelVGA = criarPainelVisaoGeralSel(sel);

        int larg = 1280, hTopo = 72, hRod = 30, marg = 20;
        painelVGA.setSize(larg - marg * 2, 900);
        painelVGA.doLayout();
        // Forçar layout recursivo
        forcarLayout(painelVGA);
        int altC = Math.max(painelVGA.getPreferredSize().height, 400);
        int altT = hTopo + marg + altC + marg + hRod;

        BufferedImage img = new BufferedImage(larg, altT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        g2.setColor(BG_ROOT); g2.fillRect(0, 0, larg, altT);
        g2.setColor(BG_CARD); g2.fillRect(0, 0, larg, hTopo);
        g2.setColor(BD_LINE); g2.drawLine(0, hTopo - 1, larg, hTopo - 1);

        int xC = 16;
        if (logoImg != null) {
            int h = 36, w = (int)(logoImg.getWidth() * ((double)h / logoImg.getHeight()));
            g2.drawImage(logoImg.getScaledInstance(w, h, Image.SCALE_SMOOTH), xC, (hTopo - h) / 2, null);
            xC += w + 12;
        }
        g2.setColor(TX_PRIM);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.drawString("Dashboard Analítico " + VERSAO_DASH + "  ·  " + cfgNomeEmpresa, xC, hTopo / 2 + 6);
        g2.setColor(TX_DIS);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String dg = "Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(dg, larg - fm.stringWidth(dg) - 16, hTopo / 2 + 5);

        // Segunda linha no cabeçalho: blocos selecionados
        String[] nomBlocos = {"KPIs","Evolução","Pizza","Comparação","Top5","Consistência","Logs","Anual"};
        StringBuilder bSel = new StringBuilder("Blocos: ");
        boolean primeiro = true;
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) { if (!primeiro) bSel.append(", "); bSel.append(nomBlocos[i]); primeiro = false; }
        }
        g2.setColor(TX_DIS);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.drawString(bSel.toString(), xC, hTopo / 2 + 20);

        g2.translate(marg, hTopo + marg);
        painelVGA.setSize(larg - marg * 2, altC);
        forcarLayout(painelVGA);
        painelVGA.paint(g2);
        g2.translate(-marg, -(hTopo + marg));

        int yR = altT - hRod;
        g2.setColor(BG_CARD); g2.fillRect(0, yR, larg, hRod);
        g2.setColor(BD_LINE); g2.drawLine(0, yR, larg, yR);
        g2.setColor(TX_DIS);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString("Dashboard Analítico " + VERSAO_DASH + "  ·  Usuário: " + cfgNomeUsuario
            + "  ·  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            16, yR + 18);
        g2.dispose();

        salvarPNGComDialog(img, "visao_geral");
    }

    /** Força layout recursivo em todos os filhos do painel */
    private void forcarLayout(Container c) {
        c.doLayout();
        for (Component ch : c.getComponents()) {
            if (ch instanceof Container) forcarLayout((Container) ch);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 1 — FUNCIONÁRIOS
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaFuncionarios() {
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBackground(BG_ROOT);
        painel.setBorder(new EmptyBorder(10, 14, 10, 14));

        String[] mDisp = dados.stream()
            .map(r -> String.format("%04d-%02d", r.ano, r.mes))
            .distinct().sorted(Comparator.reverseOrder()).toArray(String[]::new);
        String[] mLabel = Arrays.stream(mDisp).map(this::labelMes).toArray(String[]::new);

        JComboBox<String> cbMes  = comboT(mLabel.length > 0 ? mLabel : new String[]{"(sem dados)"});
        JComboBox<String> cbTipo = comboT("Todos", "PADRAO", "COMISSIONADO", "PRODUCAO");
        JComboBox<String> cbOrd  = comboT("Matrícula ↑", "Nome A→Z", "Total ↓", "Total ↑");
        JTextField tfBusca = campoT(16);

        JPanel filtros = painelFiltros();
        filtros.add(lbl("Mês:"));  filtros.add(cbMes);
        filtros.add(lbl("Tipo:")); filtros.add(cbTipo);
        filtros.add(lbl("Ord.:")); filtros.add(cbOrd);
        filtros.add(lbl("Nome:")); filtros.add(tfBusca);

        String[] cols = {"Matrícula", "Nome", "Tipo", "Sal. Base", "Extra", "Total"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = tabelaTerminal(modelo);
        tabela.setDefaultRenderer(Object.class, rendererTipo(2));

        Runnable atualizar = () -> {
            modelo.setRowCount(0);
            String mesSel  = mDisp.length > 0 ? mDisp[Math.max(0, cbMes.getSelectedIndex())] : "";
            String tipoSel = (String) cbTipo.getSelectedItem();
            String busca   = tfBusca.getText().toLowerCase().trim();
            int    ordSel  = cbOrd.getSelectedIndex();

            Stream<RegistroFolha> stream = doMes(mesSel).stream()
                .filter(r -> "Todos".equals(tipoSel) || r.tipo.equals(tipoSel))
                .filter(r -> busca.isEmpty() || r.nome.toLowerCase().contains(busca));

            stream = switch (ordSel) {
                case 1 -> stream.sorted(Comparator.comparing(r -> r.nome));
                case 2 -> stream.sorted(Comparator.comparingDouble((RegistroFolha r) -> r.salTotal).reversed());
                case 3 -> stream.sorted(Comparator.comparingDouble(r -> r.salTotal));
                default -> stream.sorted(Comparator.comparingInt(r -> r.matricula));
            };

            stream.forEach(r -> {
                double extra = "COMISSIONADO".equals(r.tipo) ? r.vendas * r.percentual / 100.0
                             : "PRODUCAO".equals(r.tipo)     ? r.qtdPecas * r.valorPeca : 0;
                modelo.addRow(new Object[]{r.matricula, r.nome, r.tipo,
                    FMT_M.format(r.salarioBase), FMT_M.format(extra), FMT_M.format(r.salTotal)});
            });
        };

        cbMes.addActionListener(e -> atualizar.run());
        cbTipo.addActionListener(e -> atualizar.run());
        cbOrd.addActionListener(e -> atualizar.run());
        tfBusca.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { atualizar.run(); }
        });
        atualizar.run();

        JButton btnTsv = botao("⬇ EXPORTAR TSV");
        btnTsv.addActionListener(e -> exportarModeloTSV(modelo, "data.funcionarios"));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_ROOT);
        JPanel bDir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bDir.setBackground(BG_ROOT); bDir.add(btnTsv);
        topBar.add(filtros, BorderLayout.WEST);
        topBar.add(bDir,    BorderLayout.EAST);

        painel.add(topBar,               BorderLayout.NORTH);
        painel.add(scrollTerminal(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 2 — RELATÓRIOS
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaRelatorios() {
        JPanel painel = new JPanel(new BorderLayout(0, 0));
        painel.setBackground(BG_ROOT);
        painel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JTextArea txtArea = new JTextArea(
            "  Selecione um mês à direita e clique em [GERAR RESUMO].\n\n"
            + "  O relatório textual aparecerá aqui.\n\n"
            + "  Pode ser copiado ou exportado como TXT ou PNG.");
        txtArea.setFont(fonte(Font.PLAIN, -1));
        txtArea.setForeground(TX_PRIM);
        txtArea.setBackground(BG_PANEL);
        txtArea.setCaretColor(TX_PRIM);
        txtArea.setEditable(false);
        txtArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);

        JScrollPane scTxt = new JScrollPane(txtArea);
        scTxt.setBorder(new MatteBorder(0, 0, 0, 1, BD_LINE));
        scTxt.setBackground(BG_PANEL);
        scTxt.getViewport().setBackground(BG_PANEL);

        JPanel dir = new JPanel();
        dir.setLayout(new BoxLayout(dir, BoxLayout.Y_AXIS));
        dir.setBackground(BG_ROOT);
        dir.setPreferredSize(new Dimension(220, 0));
        dir.setBorder(new EmptyBorder(0, 12, 0, 0));

        String[] mDisp = dados.stream()
            .map(r -> String.format("%04d-%02d", r.ano, r.mes))
            .distinct().sorted(Comparator.reverseOrder()).toArray(String[]::new);
        String[] mLabel = Arrays.stream(mDisp).map(this::labelMes).toArray(String[]::new);
        Integer[] anosDisp = dados.stream().map(r -> r.ano).distinct()
            .sorted(Comparator.reverseOrder()).toArray(Integer[]::new);

        JComboBox<String>  cbMes = comboT(mLabel.length > 0 ? mLabel : new String[]{"(sem dados)"});
        JComboBox<Integer> cbAno = comboTG(anosDisp.length > 0 ? anosDisp
            : new Integer[]{LocalDateTime.now().getYear()});

        // Limitar largura dos combos para não estourar
        cbMes.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 16));
        cbAno.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 16));

        dir.add(secLabel(">> PERÍODO"));
        dir.add(Box.createVerticalStrut(6));
        dir.add(lbl("Mês para resumo:"));
        dir.add(Box.createVerticalStrut(3)); dir.add(cbMes);
        dir.add(Box.createVerticalStrut(10));
        dir.add(lbl("Ano para TSV completo:"));
        dir.add(Box.createVerticalStrut(3)); dir.add(cbAno);
        dir.add(Box.createVerticalStrut(16));
        dir.add(secLabel(">> AÇÕES"));
        dir.add(Box.createVerticalStrut(10));

        JButton btnResumo = botaoAcao("[📄] GERAR RESUMO");
        JButton btnTxt    = botaoAcao("[⬇] SALVAR TXT");
        JButton btnPng    = botaoAcao("[🖼] SALVAR PNG");
        JButton btnTsv    = botaoAcao("[💾] EXPORTAR TSV ANO");

        // Garantir que os botões não ultrapassem a largura do painel
        for (JButton b : new JButton[]{btnResumo, btnTxt, btnPng, btnTsv}) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 20));
        }

        // Referência ao texto atual para PNG
        final String[] textoAtual = {""};

        btnResumo.addActionListener(e -> {
            String ms = mDisp.length > 0 ? mDisp[Math.max(0, cbMes.getSelectedIndex())] : "";
            textoAtual[0] = gerarResumoTextual(ms);
            txtArea.setText(textoAtual[0]);
            txtArea.setCaretPosition(0);
        });
        btnTxt.addActionListener(e -> {
            String ms = mDisp.length > 0 ? mDisp[Math.max(0, cbMes.getSelectedIndex())] : "";
            String txt = textoAtual[0].isEmpty() ? gerarResumoTextual(ms) : textoAtual[0];
            salvarTXT(txt, ms);
        });
        btnPng.addActionListener(e -> {
            String ms = mDisp.length > 0 ? mDisp[Math.max(0, cbMes.getSelectedIndex())] : "";
            String txt = textoAtual[0].isEmpty() ? gerarResumoTextual(ms) : textoAtual[0];
            if (textoAtual[0].isEmpty()) {
                txtArea.setText(txt);
                txtArea.setCaretPosition(0);
                textoAtual[0] = txt;
            }
            exportarRelatorioTextoPNG(txt, ms);
        });
        btnTsv.addActionListener(e -> exportarTSVAno((Integer) cbAno.getSelectedItem()));

        dir.add(btnResumo); dir.add(Box.createVerticalStrut(5));
        dir.add(btnTxt);    dir.add(Box.createVerticalStrut(5));
        dir.add(btnPng);    dir.add(Box.createVerticalStrut(5));
        dir.add(btnTsv);

        painel.add(scTxt, BorderLayout.CENTER);
        painel.add(dir,   BorderLayout.EAST);
        return painel;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 3 — AUDITORIA
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaAuditoria() {
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBackground(BG_ROOT);
        painel.setBorder(new EmptyBorder(10, 14, 10, 14));

        List<String[]> linhas = lerLogs();
        Set<String> ops = new TreeSet<>(), meses = new TreeSet<>();
        for (String[] l : linhas) {
            if (l.length >= 3) {
                ops.add(l[1]);
                if (l[0].length() >= 7) meses.add(l[0].substring(0, 7));
            }
        }
        String[] opsArr = Stream.concat(Stream.of("Todas"), ops.stream()).toArray(String[]::new);
        String[] mesArr = Stream.concat(Stream.of("Todos"),
            meses.stream().sorted(Comparator.reverseOrder())).toArray(String[]::new);

        JComboBox<String> cbOp  = comboT(opsArr);
        JComboBox<String> cbMes = comboT(mesArr);
        JTextField tfBusca = campoT(18);

        JPanel filtros = painelFiltros();
        filtros.add(lbl("Operação:")); filtros.add(cbOp);
        filtros.add(lbl("Mês:"));     filtros.add(cbMes);
        filtros.add(lbl("Busca:"));   filtros.add(tfBusca);

        String[] cols = {"DATA/HORA", "OPERAÇÃO", "DETALHES"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = tabelaTerminal(modelo);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(700);
        tabela.setDefaultRenderer(Object.class, rendererOperacao(1));

        Runnable atualizar = () -> {
            modelo.setRowCount(0);
            String opSel  = (String) cbOp.getSelectedItem();
            String mesSel = (String) cbMes.getSelectedItem();
            String busca  = tfBusca.getText().toLowerCase().trim();
            linhas.stream()
                .filter(l -> l.length >= 3)
                .filter(l -> "Todas".equals(opSel) || l[1].equals(opSel))
                .filter(l -> "Todos".equals(mesSel) || (l[0].length() >= 7 && l[0].startsWith(mesSel)))
                .filter(l -> busca.isEmpty() || l[2].toLowerCase().contains(busca))
                .sorted((a, b) -> b[0].compareTo(a[0])).limit(500)
                .forEach(l -> modelo.addRow(new Object[]{l[0], l[1], l[2]}));
        };
        cbOp.addActionListener(e -> atualizar.run());
        cbMes.addActionListener(e -> atualizar.run());
        tfBusca.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { atualizar.run(); }
        });
        atualizar.run();

        JButton btnTsv = botao("⬇ EXPORTAR TSV");
        btnTsv.addActionListener(e -> exportarModeloTSV(modelo, "data.auditoria"));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_ROOT);
        JPanel bDir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bDir.setBackground(BG_ROOT); bDir.add(btnTsv);
        topBar.add(filtros, BorderLayout.WEST);
        topBar.add(bDir,    BorderLayout.EAST);

        painel.add(topBar,               BorderLayout.NORTH);
        painel.add(scrollTerminal(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 4 — CONFIGURAÇÕES
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaConfiguracoes() {
        JPanel painel = new JPanel(new BorderLayout(16, 0));
        painel.setBackground(BG_ROOT);
        painel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel esq = new JPanel(new BorderLayout(0, 8));
        esq.setBackground(BG_ROOT);
        esq.add(secLabel(">> PARÂMETROS DO SISTEMA  (database.tsv)"), BorderLayout.NORTH);

        JTextArea nota = new JTextArea(
            "ATENÇÃO — RESET (opção 6 do terminal):\n" +
            "Remove apenas os funcionários. As configurações\n" +
            "(salário base, teto, etc.) são PRESERVADAS.\n" +
            "Para redefinir configurações, use a opção 7 do terminal.");
        nota.setFont(fonte(Font.PLAIN, -2));
        nota.setForeground(TX_DIS);
        nota.setBackground(BG_CARD);
        nota.setEditable(false);
        nota.setBorder(new EmptyBorder(8, 10, 8, 10));

        String limStr    = cfgLimiteMatricula == 0 ? "sem limite" : String.valueOf(cfgLimiteMatricula);
        String defLimStr = DEFAULT_LIMITE_MATRICULA == 0 ? "sem limite" : String.valueOf(DEFAULT_LIMITE_MATRICULA);

        String[] cols = {"PARÂMETRO", "PADRÃO DE FÁBRICA", "VALOR ATUAL"};
        Object[][] rows = {
            {"Salário Base",          FMT_M.format(DEFAULT_SALARIO_BASE),
                                      FMT_M.format(cfgSalarioBase)},
            {"Teto de Bônus (%)",     String.format("%.1f%%", DEFAULT_TETO_PERCENTUAL),
                                      String.format("%.1f%%", cfgTetoPercentual)},
            {"Limite de Matrícula",   defLimStr,  limStr},
            {"Modo Sequência Rígida", String.valueOf(DEFAULT_MODO_RIGIDO),
                                      String.valueOf(cfgModoRigido)},
        };
        DefaultTableModel modelo = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = tabelaTerminal(modelo);
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, val, sel, foc, row, col);
                setBackground(sel ? BG_SEL : row % 2 == 0 ? BG_ROW_A : BG_ROW_B);
                boolean diferente = col == 2
                    && !String.valueOf(tb.getValueAt(row, 1)).equals(String.valueOf(tb.getValueAt(row, 2)));
                setForeground(sel ? BG_ROOT : diferente ? C_TOTAL : TX_PRIM);
                setFont(fonte(diferente ? Font.BOLD : Font.PLAIN, -1));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JPanel tabelaPanel = new JPanel(new BorderLayout(0, 8));
        tabelaPanel.setBackground(BG_ROOT);
        tabelaPanel.add(nota,                BorderLayout.NORTH);
        tabelaPanel.add(scrollTerminal(tabela), BorderLayout.CENTER);
        esq.add(tabelaPanel, BorderLayout.CENTER);

        // Direita: config do dashboard — com setMaximumSize para evitar overflow
        JPanel dir = new JPanel();
        dir.setLayout(new BoxLayout(dir, BoxLayout.Y_AXIS));
        dir.setBackground(BG_ROOT);
        dir.setPreferredSize(new Dimension(260, 0));
        dir.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));
        dir.setBorder(new CompoundBorder(
            new MatteBorder(0, 1, 0, 0, BD_LINE),
            new EmptyBorder(0, 12, 0, 0)));

        dir.add(secLabel(">> DASHBOARD  (config/dashboard.properties)"));
        dir.add(Box.createVerticalStrut(10));

        dir.add(lbl("Nome da empresa:"));
        dir.add(Box.createVerticalStrut(3));
        JTextField tfEmpresa = campoT(18);
        tfEmpresa.setText(cfgNomeEmpresa);
        tfEmpresa.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 16));
        dir.add(tfEmpresa);
        dir.add(Box.createVerticalStrut(8));

        dir.add(lbl("Nome do usuário:"));
        dir.add(Box.createVerticalStrut(3));
        JTextField tfUsuario = campoT(18);
        tfUsuario.setText(cfgNomeUsuario);
        tfUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 16));
        dir.add(tfUsuario);
        dir.add(Box.createVerticalStrut(8));

        dir.add(lbl("Perfil:"));
        dir.add(Box.createVerticalStrut(3));
        JTextField tfPerfil = campoT(18);
        tfPerfil.setText(cfgNomePerfil);
        tfPerfil.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 16));
        dir.add(tfPerfil);
        dir.add(Box.createVerticalStrut(14));

        JButton btnSalvar = botaoAcao("[💾] SALVAR E APLICAR");
        btnSalvar.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 20));
        btnSalvar.addActionListener(e -> {
            cfgNomeEmpresa = tfEmpresa.getText().trim();
            cfgNomeUsuario = tfUsuario.getText().trim();
            cfgNomePerfil  = tfPerfil.getText().trim();
            salvarConfigDashboard();
            construirUI();
        });
        dir.add(btnSalvar);

        dir.add(Box.createVerticalStrut(20));
        dir.add(secLabel(">> PASTAS MONITORADAS"));
        dir.add(Box.createVerticalStrut(8));

        for (String[] pasta : new String[][]{
            {"Histórico de folhas", DIR_HIST},
            {"Exportações (dados)", DIR_EXP},
            {"Logs do sistema",     DIR_LOGS},
            {"Config / Logo",       "config/"},
            {"Database",            DATABASE_PATH},
        }) {
            JPanel rowP = new JPanel(new BorderLayout(6, 0));
            rowP.setBackground(BG_ROOT);
            rowP.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 10));
            JLabel lNome = new JLabel(pasta[0]);
            lNome.setFont(fonte(Font.PLAIN, -2)); lNome.setForeground(TX_DIS);
            lNome.setPreferredSize(new Dimension(140, 18));
            JLabel lPath = new JLabel(pasta[1]);
            lPath.setFont(fonte(Font.BOLD, -2)); lPath.setForeground(TX_SEC);
            rowP.add(lNome, BorderLayout.WEST);
            rowP.add(lPath, BorderLayout.CENTER);
            dir.add(rowP);
            dir.add(Box.createVerticalStrut(4));
        }

        painel.add(esq, BorderLayout.CENTER);
        painel.add(dir, BorderLayout.EAST);
        return painel;
    }

    // ═════════════════════════════════════════════════════════════════════
    // GERAÇÃO / EXPORTAÇÃO
    // ═════════════════════════════════════════════════════════════════════

    private String gerarResumoTextual(String mesAno) {
        List<RegistroFolha> mes = doMes(mesAno);
        if (mes.isEmpty()) return "  Nenhum dado encontrado para " + labelMes(mesAno) + ".";

        String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String sep  = "  " + "═".repeat(58);
        String sep2 = "  " + "─".repeat(58);
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("  Dashboard Analítico ").append(VERSAO_DASH)
          .append("  ·  ").append(cfgNomeEmpresa).append("\n");
        sb.append(sep).append("\n");
        sb.append("  RESUMO DA FOLHA  ·  ").append(labelMes(mesAno).toUpperCase()).append("\n");
        sb.append(sep).append("\n\n");

        double bruto = mes.stream().mapToDouble(r -> r.salTotal).sum();
        double maior = mes.stream().mapToDouble(r -> r.salTotal).max().orElse(0);
        double media = mes.stream().mapToDouble(r -> r.salTotal).average().orElse(0);

        sb.append(String.format("  %-22s : %s%n", "TOTAL DA FOLHA", FMT_M.format(bruto)));
        sb.append(String.format("  %-22s : %d%n",  "FUNCIONÁRIOS",  mes.size()));
        sb.append(String.format("  %-22s : %s%n", "MAIOR SALÁRIO",  FMT_M.format(maior)));
        sb.append(String.format("  %-22s : %s%n", "MÉDIA SALARIAL", FMT_M.format(media)));
        sb.append("\n").append(sep2).append("\n\n");

        sb.append("  POR TIPO\n\n");
        for (String tipo : List.of("PADRAO", "COMISSIONADO", "PRODUCAO")) {
            List<RegistroFolha> sub = mes.stream()
                .filter(r -> r.tipo.equals(tipo)).collect(Collectors.toList());
            if (sub.isEmpty()) continue;
            double tTipo = sub.stream().mapToDouble(r -> r.salTotal).sum();
            sb.append(String.format("  %-14s : %d func.  |  Total: %s%n",
                tipo, sub.size(), FMT_M.format(tTipo)));
        }

        sb.append("\n").append(sep2).append("\n\n");
        sb.append("  RANKING\n\n");
        mes.stream()
           .sorted(Comparator.comparingDouble((RegistroFolha r) -> r.salTotal).reversed())
           .forEach(r -> sb.append(String.format("  [%4d]  %-28s  %-14s  %s%n",
               r.matricula, r.nome, r.tipo, FMT_M.format(r.salTotal))));

        sb.append("\n").append(sep).append("\n");
        sb.append("  Gerado em: ").append(ts)
          .append("  ·  Usuário: ").append(cfgNomeUsuario).append("\n\n");
        return sb.toString();
    }

    /**
     * Exporta o resumo textual como PNG com o mesmo padrão de cabeçalho/rodapé,
     * renderizando o texto monoespaçado linha a linha.
     */
    private void exportarRelatorioTextoPNG(String texto, String mesAno) {
        if (texto == null || texto.isBlank()) {
            JOptionPane.showMessageDialog(this,
                "Gere o resumo primeiro clicando em [GERAR RESUMO].",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] linhas = texto.split("\n", -1);
        int fontSzPx = Math.max(10, fontSize - 1);
        Font fMono = new Font("Monospaced", Font.PLAIN, fontSzPx);

        // Medir largura máxima
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2tmp = tmp.createGraphics();
        g2tmp.setFont(fMono);
        FontMetrics fmTmp = g2tmp.getFontMetrics();
        int maxW = 0;
        for (String l : linhas) maxW = Math.max(maxW, fmTmp.stringWidth(l));
        g2tmp.dispose();

        int larg   = Math.max(900, maxW + 80);
        int hTopo  = 60;
        int hRod   = 28;
        int marg   = 20;
        int lineH  = fmTmp.getHeight() + 2;
        int altC   = linhas.length * lineH + marg * 2;
        int altT   = hTopo + altC + hRod;

        BufferedImage img = new BufferedImage(larg, altT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fundo
        g2.setColor(BG_ROOT); g2.fillRect(0, 0, larg, altT);

        // Cabeçalho
        g2.setColor(BG_CARD); g2.fillRect(0, 0, larg, hTopo);
        g2.setColor(BD_LINE); g2.drawLine(0, hTopo - 1, larg, hTopo - 1);
        int xC = 16;
        if (logoImg != null) {
            int h = 32, w = (int)(logoImg.getWidth() * ((double)h / logoImg.getHeight()));
            g2.drawImage(logoImg.getScaledInstance(w, h, Image.SCALE_SMOOTH), xC, (hTopo - h) / 2, null);
            xC += w + 12;
        }
        g2.setColor(TX_PRIM);
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.drawString("Dashboard Analítico " + VERSAO_DASH + "  ·  " + cfgNomeEmpresa, xC, hTopo / 2 + 5);
        g2.setColor(TX_DIS);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String subH = "RESUMO DA FOLHA  ·  " + labelMes(mesAno).toUpperCase();
        g2.drawString(subH, xC, hTopo / 2 + 18);
        g2.setColor(TX_DIS);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String dg = "Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        FontMetrics fmH = g2.getFontMetrics();
        g2.drawString(dg, larg - fmH.stringWidth(dg) - 16, hTopo / 2 + 5);

        // Área do texto
        g2.setFont(fMono);
        FontMetrics fmText = g2.getFontMetrics();
        int y = hTopo + marg + fmText.getAscent();
        for (String linha : linhas) {
            // Colorir linhas especiais
            Color cor = TX_PRIM;
            if (linha.trim().startsWith("═") || linha.trim().startsWith("─")) cor = TX_DIS;
            else if (linha.contains("TOTAL DA FOLHA") || linha.contains("TOTAL")) cor = C_TOTAL;
            else if (linha.contains("PADRAO"))       cor = C_PADRAO;
            else if (linha.contains("COMISSIONADO")) cor = C_COMISS;
            else if (linha.contains("PRODUCAO"))     cor = C_PROD;
            else if (linha.contains("Gerado em"))    cor = TX_DIS;
            g2.setColor(cor);
            g2.drawString(linha, marg, y);
            y += lineH;
        }

        // Rodapé
        int yR = altT - hRod;
        g2.setColor(BG_CARD); g2.fillRect(0, yR, larg, hRod);
        g2.setColor(BD_LINE); g2.drawLine(0, yR, larg, yR);
        g2.setColor(TX_DIS);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString("Dashboard Analítico " + VERSAO_DASH
            + "  ·  Usuário: " + cfgNomeUsuario
            + "  ·  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            16, yR + 18);
        g2.dispose();

        salvarPNGComDialog(img, "relatorio_" + mesAno.replace("-", "_"));
    }

    private void salvarPNGComDialog(BufferedImage img, String prefixo) {
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Salvar PNG");
        jfc.setFileFilter(new FileNameExtensionFilter("Imagem PNG (.png)", "png"));
        String nome = prefixo + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".png";
        jfc.setSelectedFile(new File(nome));
        if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = jfc.getSelectedFile();
        if (!dest.getName().endsWith(".png")) dest = new File(dest.getAbsolutePath() + ".png");
        try {
            ImageIO.write(img, "png", dest);
            JOptionPane.showMessageDialog(this, "PNG salvo em:\n" + dest.getAbsolutePath(),
                "PNG exportado", JOptionPane.INFORMATION_MESSAGE);
            try { Desktop.getDesktop().open(dest.getParentFile()); } catch (Exception ignored) {}
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarTXT(String texto, String mesAno) {
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Salvar como TXT");
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivo de texto (.txt)", "txt"));
        String nome = "resumo_folha_" + mesAno.replace("-", "_") + "_" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".txt";
        jfc.setSelectedFile(new File(nome));
        if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = jfc.getSelectedFile();
        if (!dest.getName().endsWith(".txt")) dest = new File(dest.getAbsolutePath() + ".txt");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(dest), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.print(texto);
            JOptionPane.showMessageDialog(this, "Salvo em:\n" + dest.getAbsolutePath(),
                "TXT exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarTSVAno(int ano) {
        List<RegistroFolha> doAno = dados.stream().filter(r -> r.ano == ano).collect(Collectors.toList());
        if (doAno.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum dado para " + ano, "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Exportar TSV — " + ano);
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivo TSV (.tsv)", "tsv"));
        jfc.setSelectedFile(new File("data.folha." + ano + ".tsv"));
        if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = jfc.getSelectedFile();
        if (!dest.getName().endsWith(".tsv")) dest = new File(dest.getAbsolutePath() + ".tsv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(dest), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.println("MATRICULA\tNOME\tTIPO\tSALARIO_BASE\tVENDAS\tPERCENTUAL\t" +
                       "QTD_PECA\tVALOR_PECA\tSALARIO_TOTAL\tMES\tANO");
            for (RegistroFolha r : doAno)
                pw.printf("%d\t%s\t%s\t%.2f\t%.2f\t%.2f\t%d\t%.2f\t%.2f\t%d\t%d%n",
                    r.matricula, r.nome, r.tipo, r.salarioBase, r.vendas, r.percentual,
                    r.qtdPecas, r.valorPeca, r.salTotal, r.mes, r.ano);
            JOptionPane.showMessageDialog(this, "TSV salvo:\n" + dest.getAbsolutePath(),
                "Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarModeloTSV(DefaultTableModel modelo, String prefixo) {
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Exportar como TSV");
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivo TSV (.tsv)", "tsv"));
        jfc.setSelectedFile(new File(prefixo + "." +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".tsv"));
        if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = jfc.getSelectedFile();
        if (!dest.getName().endsWith(".tsv")) dest = new File(dest.getAbsolutePath() + ".tsv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(dest), java.nio.charset.StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < modelo.getColumnCount(); c++) {
                if (c > 0) sb.append('\t');
                sb.append(modelo.getColumnName(c));
            }
            pw.println(sb);
            for (int r = 0; r < modelo.getRowCount(); r++) {
                sb.setLength(0);
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    if (c > 0) sb.append('\t');
                    Object v = modelo.getValueAt(r, c);
                    sb.append(v != null ? v.toString() : "");
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this, "TSV salvo:\n" + dest.getAbsolutePath(),
                "Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Leitura de logs ───────────────────────────────────────────────────
    private List<String[]> lerLogs() {
        List<String[]> linhas = new ArrayList<>();
        File pasta = new File(DIR_LOGS);
        if (!pasta.exists()) return linhas;
        File[] arqs = pasta.listFiles(f ->
            f.getName().endsWith("_log.txt") || f.getName().endsWith("_log.log"));
        if (arqs == null) return linhas;
        for (File f : arqs) {
            try (Scanner sc = new Scanner(f, "UTF-8")) {
                while (sc.hasNextLine()) {
                    String[] p = sc.nextLine().trim().split(" \\| ", 3);
                    if (p.length >= 3) linhas.add(p);
                }
            } catch (Exception ignored) {}
        }
        return linhas;
    }

    // ═════════════════════════════════════════════════════════════════════
    // COMPONENTES REUTILIZÁVEIS
    // ═════════════════════════════════════════════════════════════════════

    private JPanel blocoTerminal(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 1, BD_LINE),
            new EmptyBorder(8, 12, 8, 12)));
        JLabel lT = new JLabel(titulo);
        lT.setFont(fonte(Font.BOLD, -1)); lT.setForeground(TX_HEAD);
        lT.setAlignmentX(Component.LEFT_ALIGNMENT);
        lT.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        JPanel sep = new JPanel();
        sep.setBackground(BD_LINE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        p.add(lT); p.add(Box.createVerticalStrut(2)); p.add(sep);
        p.add(Box.createVerticalStrut(6));
        return p;
    }

    private JPanel hSep() {
        JPanel sep = new JPanel();
        sep.setBackground(BD_LINE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private JLabel secLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(Font.BOLD, -1)); l.setForeground(TX_DIS);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        l.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BD_LINE),
            new EmptyBorder(0, 0, 4, 0)));
        return l;
    }

    private JTable tabelaTerminal(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(BG_PANEL); t.setForeground(TX_PRIM);
        t.setGridColor(BD_LINE);
        t.setRowHeight(fontSize + 10);
        t.setFont(fonte(Font.PLAIN, -1));
        t.setSelectionBackground(BG_SEL); t.setSelectionForeground(BG_ROOT);
        t.setShowVerticalLines(false); t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(BG_HEADER);
        t.getTableHeader().setForeground(TX_HEAD);
        t.getTableHeader().setFont(fonte(Font.BOLD, -1));
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BD_FOCUS));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, val, sel, foc, row, col);
                setBackground(sel ? BG_SEL : row % 2 == 0 ? BG_ROW_A : BG_ROW_B);
                setForeground(sel ? BG_ROOT : TX_PRIM);
                setFont(fonte(Font.PLAIN, -1));
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
        return t;
    }

    private JScrollPane scrollTerminal(JTable t) {
        JScrollPane sc = new JScrollPane(t);
        sc.setBorder(new MatteBorder(1, 0, 0, 0, BD_LINE));
        sc.setBackground(BG_PANEL);
        sc.getViewport().setBackground(BG_PANEL);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        return sc;
    }

    private JButton botao(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(BG_CARD); b.setForeground(TX_PRIM);
        b.setFont(fonte(Font.BOLD, -1)); b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BD_FOCUS),
            new EmptyBorder(4, 10, 4, 10)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(BG_SEL); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(BG_CARD); }
        });
        return b;
    }

    private JButton botaoAcao(String texto) {
        JButton b = botao(texto);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontSize + 20));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        // Clip do texto do botão se necessário
        b.setToolTipText(texto);
        return b;
    }

    private JComboBox<String> comboT(String... itens) {
        JComboBox<String> c = new JComboBox<>(itens);
        c.setBackground(BG_CARD); c.setForeground(TX_PRIM);
        c.setFont(fonte(Font.PLAIN, -1));
        c.setBorder(BorderFactory.createLineBorder(BD_FOCUS));
        return c;
    }

    private <T> JComboBox<T> comboTG(T[] itens) {
        JComboBox<T> c = new JComboBox<>(itens);
        c.setBackground(BG_CARD); c.setForeground(TX_PRIM);
        c.setFont(fonte(Font.PLAIN, -1));
        c.setBorder(BorderFactory.createLineBorder(BD_FOCUS));
        return c;
    }

    private JTextField campoT(int cols) {
        JTextField t = new JTextField(cols);
        t.setBackground(BG_CARD); t.setForeground(TX_PRIM);
        t.setCaretColor(TX_PRIM);
        t.setFont(fonte(Font.PLAIN, -1));
        t.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BD_FOCUS),
            new EmptyBorder(2, 5, 2, 5)));
        return t;
    }

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(TX_SEC); l.setFont(fonte(Font.PLAIN, -1));
        return l;
    }

    private JPanel painelFiltros() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(BG_ROOT);
        return p;
    }

    private JLabel sep_v() {
        JLabel l = new JLabel(" | ");
        l.setForeground(TX_DIS); l.setFont(fonte(Font.PLAIN, -1));
        return l;
    }

    private Font fonte(int style, int delta) {
        return new Font("Monospaced", style, Math.max(8, fontSize + delta));
    }

    // ── Renderers ─────────────────────────────────────────────────────────

    private DefaultTableCellRenderer rendererTipo(int colTipo) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, val, sel, foc, row, col);
                String tipo = (tb.getRowCount() > row && tb.getValueAt(row, colTipo) != null)
                    ? tb.getValueAt(row, colTipo).toString() : "";
                Color cor = switch (tipo) {
                    case "PADRAO"       -> C_PADRAO;
                    case "COMISSIONADO" -> C_COMISS;
                    case "PRODUCAO"     -> C_PROD;
                    default             -> TX_PRIM;
                };
                setForeground(sel ? BG_ROOT : col == colTipo ? cor : TX_PRIM);
                setBackground(sel ? BG_SEL : row % 2 == 0 ? BG_ROW_A : BG_ROW_B);
                setFont(fonte(Font.PLAIN, -1));
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        };
    }

    private DefaultTableCellRenderer rendererOperacao(int colOp) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, val, sel, foc, row, col);
                String op = (tb.getRowCount() > row && tb.getValueAt(row, colOp) != null)
                    ? tb.getValueAt(row, colOp).toString() : "";
                Color cor = switch (op) {
                    case "CADASTRO"               -> C_PADRAO;
                    case "REMOCAO"                -> new Color(180, 50, 50);
                    case "RESET"                  -> C_COMISS;
                    case "EDICAO", "EDICAO_LOTE"  -> C_COMISS;
                    case "CONFIG"                 -> C_PROD;
                    case "IMPORT", "IMPORT_BACKUP"-> C_TOTAL;
                    case "NOVO_MES"               -> C_TOTAL;
                    default                       -> TX_SEC;
                };
                setForeground(sel ? BG_ROOT : col == colOp ? cor : TX_PRIM);
                setBackground(sel ? BG_SEL : row % 2 == 0 ? BG_ROW_A : BG_ROW_B);
                setFont(fonte(Font.PLAIN, -1));
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        };
    }

    // ═════════════════════════════════════════════════════════════════════
    // UTILITÁRIOS DE DADOS
    // ═════════════════════════════════════════════════════════════════════

    private List<RegistroFolha> doMes(String mesAno) {
        if (mesAno == null || mesAno.isEmpty()) return Collections.emptyList();
        String[] p = mesAno.split("-");
        if (p.length < 2) return Collections.emptyList();
        try {
            int a = Integer.parseInt(p[0]), m = Integer.parseInt(p[1]);
            return dados.stream().filter(r -> r.ano == a && r.mes == m).collect(Collectors.toList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private String mesAnterior(String mesAno) {
        if (mesAno == null || mesAno.isEmpty()) return "";
        try {
            String[] p = mesAno.split("-");
            int ano = Integer.parseInt(p[0]), mes = Integer.parseInt(p[1]);
            return mes == 1 ? String.format("%04d-12", ano - 1)
                            : String.format("%04d-%02d", ano, mes - 1);
        } catch (Exception e) { return ""; }
    }

    private String labelMes(String m) {
        if (m == null || m.isEmpty()) return "(sem dados)";
        try {
            String[] p = m.split("-");
            return nomeMes(Integer.parseInt(p[1])) + " de " + p[0];
        } catch (Exception e) { return m; }
    }

    private String labelMesCurto(String m) {
        if (m == null || m.isEmpty()) return "—";
        try {
            String[] p = m.split("-");
            return nomeMesCurto(Integer.parseInt(p[1])) + "/" + p[0].substring(2);
        } catch (Exception e) { return m; }
    }

    private String nomeMes(int m) {
        String[] n = {"","Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                      "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        return (m >= 1 && m <= 12) ? n[m] : "?";
    }

    private String nomeMesCurto(int m) {
        String[] n = {"","JAN","FEV","MAR","ABR","MAI","JUN",
                      "JUL","AGO","SET","OUT","NOV","DEZ"};
        return (m >= 1 && m <= 12) ? n[m] : "?";
    }

    private String formatarMilhar(double v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
        if (v >= 1_000)     return String.format("%.0fk", v / 1_000);
        return String.format("%.0f", v);
    }

    // ═════════════════════════════════════════════════════════════════════
    // MODELO DE DADOS
    // ═════════════════════════════════════════════════════════════════════

    static class RegistroFolha {
        int    matricula, mes, ano, qtdPecas;
        String nome, tipo;
        double salarioBase, vendas, percentual, valorPeca, salTotal;
    }
}