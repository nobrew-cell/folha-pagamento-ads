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
import java.util.*;
import java.util.List;
import java.util.stream.*;
import javax.imageio.ImageIO;

/**
 * Dashboard analítico — Sistema de Folha de Pagamento v5.1
 *
 * Filosofia visual: ferramenta técnica de gestão, não painel SaaS.
 * Fundo escuro neutro, fonte monoespaçada, cores funcionais (não decorativas).
 * As cores dos tipos de funcionário são as mesmas do XLS gerado pelo sistema.
 *
 * 3 abas:
 *   1. Visão Geral   — cards + gráfico + pizza + top-5 + análise anual (scroll vertical)
 *   2. Funcionários  — tabela filtrável por mês/tipo/nome, exportação CSV
 *   3. Logs ADM      — auditoria com cores por operação, exportação CSV
 *
 * Exportação PNG na aba 1: captura o conteúdo, adiciona cabeçalho com logo + título,
 * rodapé e salva. Logo carregada de config/logo.png se existir.
 */
public class DashboardBI extends JFrame {

    // ── Paleta: terminal-profissional (monocromático + cores funcionais) ──
    static final Color F0 = new Color(18,  18,  18);   // fundo raiz
    static final Color F1 = new Color(26,  26,  26);   // painel interno
    static final Color F2 = new Color(34,  34,  34);   // card / barra de topo
    static final Color F3 = new Color(48,  48,  48);   // borda / separador / grid
    static final Color T0 = new Color(232, 232, 232);  // texto principal
    static final Color T1 = new Color(148, 148, 148);  // texto secundário
    static final Color T2 = new Color( 80,  80,  80);  // texto desabilitado / label

    // Cores funcionais — idênticas às do XLS do sistema principal
    static final Color C_PADRAO = new Color(130, 185, 130);  // verde suave
    static final Color C_COMISS = new Color(220, 155,  90);  // laranja suave
    static final Color C_PROD   = new Color(160, 130, 210);  // roxo suave
    static final Color C_ACCENT = new Color(210, 185, 120);  // dourado apagado (destaque único)

    private static final DecimalFormat FMT_MOEDA;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("pt","BR"));
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        FMT_MOEDA = new DecimalFormat("R$ #,##0.00", sym);
    }

    private static final String DIR_HIST  = "historico";
    private static final String DIR_EXP   = "exportados/dados";
    private static final String DIR_LOGS  = "logs";
    private static final String LOGO_PATH = "config/logo.png";

    private final List<RegistroFolha> dados = new ArrayList<>();
    private String ultimoMes = "";
    private BufferedImage logoImg = null;

    // ── Ponto de entrada (chamado pelo ConsoleUI) ─────────────────────────
    public static void abrir() {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new DashboardBI().setVisible(true);
        });
    }

    public DashboardBI() {
        super("Folha de Pagamento — Dashboard Analítico v5.1");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(F0);
        carregarLogo();
        carregarDados();
        construirUI();
    }

    // ── Logo ──────────────────────────────────────────────────────────────
    private void carregarLogo() {
        File f = new File(LOGO_PATH);
        if (!f.exists()) return;
        try { logoImg = ImageIO.read(f); } catch (Exception ignored) {}
    }

    // ── Carregamento de dados ─────────────────────────────────────────────
    private void carregarDados() {
        dados.clear();
        lerPasta(DIR_HIST);
        lerPasta(DIR_EXP);
        // Remove duplicatas (mesmo mês+matrícula lido de duas pastas)
        Set<String> vistos = new HashSet<>();
        dados.removeIf(r -> !vistos.add(r.ano + "-" + r.mes + "-" + r.matricula));
        dados.sort(Comparator.comparingInt((RegistroFolha r) -> r.ano)
                             .thenComparingInt(r -> r.mes)
                             .thenComparingInt(r -> r.matricula));
        if (!dados.isEmpty()) {
            RegistroFolha ult = dados.get(dados.size()-1);
            ultimoMes = String.format("%04d-%02d", ult.ano, ult.mes);
        }
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

    // ── UI raiz ───────────────────────────────────────────────────────────
    private void construirUI() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(topo(),   BorderLayout.NORTH);
        add(abas(),   BorderLayout.CENTER);
        add(rodape(), BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    // ── Topo ──────────────────────────────────────────────────────────────
    private JPanel topo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(F2);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,F3),
            new EmptyBorder(10,18,10,18)));

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        esq.setBackground(F2);
        if (logoImg != null) {
            int h = 36;
            int w = (int)(logoImg.getWidth() * ((double)h / logoImg.getHeight()));
            esq.add(new JLabel(new ImageIcon(logoImg.getScaledInstance(w,h,Image.SCALE_SMOOTH))));
        }
        JLabel titulo = new JLabel("FOLHA DE PAGAMENTO  \u00b7  Dashboard Anal\u00edtico");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        titulo.setForeground(T0);
        esq.add(titulo);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        dir.setBackground(F2);
        JButton btnAt = btn("\u21bb  Atualizar");
        btnAt.addActionListener(e -> { carregarDados(); construirUI(); });
        JButton btnFc = btn("\u2715  Fechar");
        btnFc.setForeground(new Color(200,90,90));
        btnFc.addActionListener(e -> dispose());
        dir.add(btnAt); dir.add(btnFc);

        p.add(esq, BorderLayout.WEST);
        p.add(dir, BorderLayout.EAST);
        return p;
    }

    // ── Rodapé ────────────────────────────────────────────────────────────
    private JLabel rodape() {
        String txt = dados.isEmpty()
            ? "  Nenhum dado encontrado em historico/ ou exportados/dados/"
            : String.format("  %d registros  \u00b7  %d meses  \u00b7  \u00daltimo: %s",
                dados.size(),
                (int) dados.stream().map(r -> r.ano+"-"+r.mes).distinct().count(),
                labelMes(ultimoMes));
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Monospaced",Font.PLAIN,11));
        l.setForeground(T2); l.setBackground(F2); l.setOpaque(true);
        l.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,F3),new EmptyBorder(5,18,5,18)));
        return l;
    }

    // ── Abas ─────────────────────────────────────────────────────────────
    private JTabbedPane abas() {
        JTabbedPane t = new JTabbedPane();
        t.setBackground(F1); t.setForeground(T1);
        t.setFont(new Font("Monospaced",Font.PLAIN,12));
        t.setBorder(null);
        t.addTab("  Vis\u00e3o Geral  ",  abaVisaoGeral());
        t.addTab("  Funcion\u00e1rios  ", abaFuncionarios());
        t.addTab("  Logs ADM  ",          abaLogs());
        return t;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 1 — VISÃO GERAL (scroll vertical, narrativa única)
    // ═════════════════════════════════════════════════════════════════════
    private JScrollPane abaVisaoGeral() {
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(F0);
        corpo.setBorder(new EmptyBorder(18,18,18,18));

        List<RegistroFolha> mes = doMes(ultimoMes);

        // Seção: cards
        corpo.add(secLabel("ÚLTIMO MÊS  \u00b7  " + labelMes(ultimoMes).toUpperCase()));
        corpo.add(Box.createVerticalStrut(8));
        corpo.add(linhaCards(mes));
        corpo.add(Box.createVerticalStrut(22));

        // Seção: gráfico de evolução
        corpo.add(secLabel("EVOLUÇÃO TOTAL DA FOLHA"));
        corpo.add(Box.createVerticalStrut(8));
        Map<String,Double> totalPorMes = new LinkedHashMap<>();
        dados.stream()
             .collect(Collectors.groupingBy(
                 r -> String.format("%04d-%02d",r.ano,r.mes),
                 LinkedHashMap::new,
                 Collectors.summingDouble(r -> r.salTotal)))
             .entrySet().stream()
             .sorted(Map.Entry.comparingByKey())
             .forEach(e -> totalPorMes.put(labelMesCurto(e.getKey()), e.getValue()));
        BarChart bar = new BarChart(totalPorMes);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));
        bar.setPreferredSize(new Dimension(0, 185));
        corpo.add(bar);
        corpo.add(Box.createVerticalStrut(22));

        // Seção: pizza + top-5
        corpo.add(secLabel("DISTRIBUIÇÃO E RANKING  \u00b7  " + labelMes(ultimoMes).toUpperCase()));
        corpo.add(Box.createVerticalStrut(8));
        JPanel linha2 = new JPanel(new GridLayout(1,2,14,0));
        linha2.setBackground(F0); linha2.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha2.setMaximumSize(new Dimension(Integer.MAX_VALUE,200));
        Map<String,Long> porTipo = mes.stream()
            .collect(Collectors.groupingBy(r -> r.tipo, Collectors.counting()));
        linha2.add(envolve("Por Tipo de Funcion\u00e1rio", new PieChart(porTipo)));
        String[] colTop = {"Nome","Tipo","Total"};
        Object[][] dadosTop = mes.stream()
            .sorted(Comparator.comparingDouble((RegistroFolha r) -> r.salTotal).reversed())
            .limit(5)
            .map(r -> new Object[]{ r.nome, r.tipo, FMT_MOEDA.format(r.salTotal) })
            .toArray(Object[][]::new);
        linha2.add(envolve("Top 5 Sal\u00e1rios", tabelaLeve(dadosTop, colTop)));
        corpo.add(linha2);
        corpo.add(Box.createVerticalStrut(22));

        // Seção: análise anual
        int anoAtual = dados.isEmpty() ? LocalDateTime.now().getYear() : dados.get(dados.size()-1).ano;
        corpo.add(secLabel("ANÁLISE ANUAL  \u00b7  " + anoAtual));
        corpo.add(Box.createVerticalStrut(8));
        corpo.add(tabelaAnual(anoAtual));
        corpo.add(Box.createVerticalStrut(22));

        // Seção: exportar PNG
        corpo.add(secLabel("EXPORTAÇÃO"));
        corpo.add(Box.createVerticalStrut(10));
        JPanel expRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        expRow.setBackground(F0); expRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnPng = btn("\u2b07  Salvar Relat\u00f3rio como PNG");
        btnPng.setFont(new Font("Monospaced",Font.BOLD,12));
        btnPng.setForeground(C_ACCENT);
        btnPng.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_ACCENT,1), new EmptyBorder(7,18,7,18)));
        btnPng.addActionListener(e -> exportarPNG(corpo));
        expRow.add(btnPng);
        corpo.add(expRow);
        corpo.add(Box.createVerticalStrut(10));

        JScrollPane scroll = new JScrollPane(corpo);
        scroll.setBackground(F0); scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel linhaCards(List<RegistroFolha> mes) {
        JPanel row = new JPanel(new GridLayout(1,4,12,0));
        row.setBackground(F0); row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));
        double total = mes.stream().mapToDouble(r->r.salTotal).sum();
        long   qtd   = mes.size();
        double maior = mes.stream().mapToDouble(r->r.salTotal).max().orElse(0);
        double media = mes.stream().mapToDouble(r->r.salTotal).average().orElse(0);
        row.add(card("TOTAL DA FOLHA",  FMT_MOEDA.format(total), C_ACCENT));
        row.add(card("FUNCION\u00c1RIOS",     String.valueOf(qtd),    T0));
        row.add(card("MAIOR SAL\u00c1RIO",    FMT_MOEDA.format(maior),C_COMISS));
        row.add(card("M\u00c9DIA SALARIAL",   FMT_MOEDA.format(media),C_PADRAO));
        return row;
    }

    private JPanel card(String titulo, String valor, Color cor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(F2);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0,3,0,0,cor), new EmptyBorder(14,14,14,14)));
        JLabel lT = new JLabel(titulo); lT.setFont(new Font("Monospaced",Font.PLAIN,10)); lT.setForeground(T1); lT.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lV = new JLabel(valor);  lV.setFont(new Font("Monospaced",Font.BOLD,17));  lV.setForeground(cor); lV.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lT); p.add(Box.createVerticalStrut(6)); p.add(lV);
        return p;
    }

    private JPanel tabelaAnual(int ano) {
        String[] cols = {"M\u00eas","Funcion\u00e1rios","Total da Folha","Maior Sal\u00e1rio","M\u00e9dia","Varia\u00e7\u00e3o"};
        DefaultTableModel modelo = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        Map<Integer,List<RegistroFolha>> porMes = dados.stream()
            .filter(r -> r.ano==ano)
            .collect(Collectors.groupingBy(r->r.mes, TreeMap::new, Collectors.toList()));
        double anterior = -1;
        for (Map.Entry<Integer,List<RegistroFolha>> e : porMes.entrySet()) {
            List<RegistroFolha> regs = e.getValue();
            double tot = regs.stream().mapToDouble(r->r.salTotal).sum();
            double max = regs.stream().mapToDouble(r->r.salTotal).max().orElse(0);
            double med = regs.stream().mapToDouble(r->r.salTotal).average().orElse(0);
            String var = anterior<0 ? "\u2014" : String.format("%+.1f%%",(tot-anterior)/anterior*100);
            modelo.addRow(new Object[]{ nomeMesCompleto(e.getKey()), regs.size(),
                FMT_MOEDA.format(tot), FMT_MOEDA.format(max), FMT_MOEDA.format(med), var });
            anterior = tot;
        }
        if (modelo.getRowCount()>0) {
            double totalAno = porMes.values().stream().flatMap(Collection::stream).mapToDouble(r->r.salTotal).sum();
            modelo.addRow(new Object[]{"TOTAL "+ano,"\u2014",FMT_MOEDA.format(totalAno),"\u2014","\u2014","\u2014"});
        }
        int lastRow = modelo.getRowCount()-1;
        JTable t = tabelaLeve(modelo);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table,value,sel,focus,row,col);
                boolean isTotal = row==lastRow;
                setBackground(isTotal ? F3 : row%2==0 ? F1 : F2);
                setForeground(isTotal ? C_ACCENT : T0);
                setFont(new Font("Monospaced", isTotal ? Font.BOLD : Font.PLAIN, 12));
                setBorder(new EmptyBorder(0,8,0,8));
                return this;
            }
        });
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(F0); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,220));
        JScrollPane sc = new JScrollPane(t); sc.setBorder(null); sc.setBackground(F1);
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 2 — FUNCIONÁRIOS
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaFuncionarios() {
        JPanel painel = new JPanel(new BorderLayout(0,8));
        painel.setBackground(F0);
        painel.setBorder(new EmptyBorder(14,16,14,16));

        String[] mesesDisp = dados.stream()
            .map(r -> String.format("%04d-%02d",r.ano,r.mes))
            .distinct().sorted(Comparator.reverseOrder()).toArray(String[]::new);
        String[] mesesLabel = Arrays.stream(mesesDisp).map(this::labelMes).toArray(String[]::new);

        JComboBox<String> cbMes  = combo(mesesLabel.length>0 ? mesesLabel : new String[]{"(sem dados)"});
        JComboBox<String> cbTipo = combo("Todos","PADRAO","COMISSIONADO","PRODUCAO");
        JTextField tfBusca = campo();

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        filtros.setBackground(F0);
        filtros.add(lab("M\u00eas:")); filtros.add(cbMes);
        filtros.add(lab("Tipo:"));    filtros.add(cbTipo);
        filtros.add(lab("Nome:"));    filtros.add(tfBusca);

        String[] cols = {"Matr\u00edcula","Nome","Tipo","Sal\u00e1rio Base","Extra","Total"};
        DefaultTableModel modelo = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = tabelaLeve(modelo);
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table,value,sel,focus,row,col);
                Object tipo = table.getRowCount()>row ? table.getValueAt(row,2) : "";
                Color cor = "PADRAO".equals(tipo) ? C_PADRAO : "COMISSIONADO".equals(tipo) ? C_COMISS : "PRODUCAO".equals(tipo) ? C_PROD : T0;
                setForeground(sel ? Color.WHITE : col==2 ? cor : T0);
                setBackground(sel ? new Color(60,60,80) : row%2==0 ? F1 : F2);
                setFont(new Font("Monospaced",Font.PLAIN,12));
                setBorder(new EmptyBorder(0,8,0,8));
                return this;
            }
        });
        tabela.setRowSorter(new TableRowSorter<>(modelo));

        Runnable atualizar = () -> {
            modelo.setRowCount(0);
            String mesSel = mesesDisp.length>0 ? mesesDisp[Math.max(0,cbMes.getSelectedIndex())] : "";
            String tipoSel = (String) cbTipo.getSelectedItem();
            String busca = tfBusca.getText().toLowerCase().trim();
            doMes(mesSel).stream()
                .filter(r -> "Todos".equals(tipoSel) || r.tipo.equals(tipoSel))
                .filter(r -> busca.isEmpty() || r.nome.toLowerCase().contains(busca))
                .forEach(r -> {
                    double extra = r.tipo.equals("COMISSIONADO") ? r.vendas*r.percentual/100.0
                                 : r.tipo.equals("PRODUCAO") ? r.qtdPecas*r.valorPeca : 0;
                    modelo.addRow(new Object[]{ r.matricula, r.nome, r.tipo,
                        FMT_MOEDA.format(r.salarioBase), FMT_MOEDA.format(extra), FMT_MOEDA.format(r.salTotal) });
                });
        };
        cbMes.addActionListener(e -> atualizar.run());
        cbTipo.addActionListener(e -> atualizar.run());
        tfBusca.addKeyListener(new KeyAdapter() { @Override public void keyReleased(KeyEvent e) { atualizar.run(); } });
        atualizar.run();

        JButton btnCsv = btn("\u2b07  Exportar CSV");
        btnCsv.addActionListener(e -> exportarCSV(modelo, "funcionarios"));
        JPanel bBar = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); bBar.setBackground(F0); bBar.add(btnCsv);
        JPanel topBar = new JPanel(new BorderLayout()); topBar.setBackground(F0);
        topBar.add(filtros,BorderLayout.WEST); topBar.add(bBar,BorderLayout.EAST);

        JScrollPane sc = new JScrollPane(tabela); sc.setBorder(null); sc.setBackground(F1);
        painel.add(topBar, BorderLayout.NORTH);
        painel.add(sc,     BorderLayout.CENTER);
        return painel;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 3 — LOGS ADM
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaLogs() {
        JPanel painel = new JPanel(new BorderLayout(0,8));
        painel.setBackground(F0);
        painel.setBorder(new EmptyBorder(14,16,14,16));

        List<String[]> linhasLog = lerLogs();
        Set<String> ops = new TreeSet<>(), meses = new TreeSet<>();
        for (String[] l : linhasLog) {
            if (l.length>=3) { ops.add(l[1]); if (l[0].length()>=7) meses.add(l[0].substring(0,7)); }
        }
        String[] opsArr = Stream.concat(Stream.of("Todas"), ops.stream()).toArray(String[]::new);
        String[] mesArr = Stream.concat(Stream.of("Todos"), meses.stream().sorted(Comparator.reverseOrder())).toArray(String[]::new);

        JComboBox<String> cbOp  = combo(opsArr);
        JComboBox<String> cbMes = combo(mesArr);
        JTextField tfBusca = campo();
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); filtros.setBackground(F0);
        filtros.add(lab("Opera\u00e7\u00e3o:")); filtros.add(cbOp);
        filtros.add(lab("M\u00eas:"));           filtros.add(cbMes);
        filtros.add(lab("Busca:"));              filtros.add(tfBusca);

        String[] cols = {"Data/Hora","Opera\u00e7\u00e3o","Detalhes"};
        DefaultTableModel modelo = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabela = tabelaLeve(modelo);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(680);
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table,value,sel,focus,row,col);
                String op = table.getRowCount()>row ? String.valueOf(table.getValueAt(row,1)) : "";
                Color cor = switch(op) {
                    case "CADASTRO"   -> C_PADRAO;
                    case "REMOCAO"    -> new Color(200,80,80);
                    case "RESET"      -> new Color(220,120,60);
                    case "EDICAO","EDICAO_LOTE" -> C_COMISS;
                    case "CONFIG"     -> C_PROD;
                    default           -> T1;
                };
                setForeground(sel ? Color.WHITE : col==1 ? cor : T0);
                setBackground(sel ? new Color(60,60,80) : row%2==0 ? F1 : F2);
                setFont(new Font("Monospaced",Font.PLAIN,12)); setBorder(new EmptyBorder(0,8,0,8));
                return this;
            }
        });

        Runnable atualizar = () -> {
            modelo.setRowCount(0);
            String opSel  = (String) cbOp.getSelectedItem();
            String mesSel = (String) cbMes.getSelectedItem();
            String busca  = tfBusca.getText().toLowerCase().trim();
            linhasLog.stream()
                .filter(l -> l.length>=3)
                .filter(l -> "Todas".equals(opSel) || l[1].equals(opSel))
                .filter(l -> "Todos".equals(mesSel) || (l[0].length()>=7 && l[0].startsWith(mesSel)))
                .filter(l -> busca.isEmpty() || l[2].toLowerCase().contains(busca))
                .sorted((a,b) -> b[0].compareTo(a[0])).limit(500)
                .forEach(l -> modelo.addRow(new Object[]{ l[0], l[1], l[2] }));
        };
        cbOp.addActionListener(e -> atualizar.run()); cbMes.addActionListener(e -> atualizar.run());
        tfBusca.addKeyListener(new KeyAdapter() { @Override public void keyReleased(KeyEvent e) { atualizar.run(); } });
        atualizar.run();

        JButton btnCsv = btn("\u2b07  Exportar CSV");
        btnCsv.addActionListener(e -> exportarCSV(modelo, "logs"));
        JPanel bBar = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); bBar.setBackground(F0); bBar.add(btnCsv);
        JPanel topBar = new JPanel(new BorderLayout()); topBar.setBackground(F0);
        topBar.add(filtros,BorderLayout.WEST); topBar.add(bBar,BorderLayout.EAST);
        JScrollPane sc = new JScrollPane(tabela); sc.setBorder(null); sc.setBackground(F1);
        painel.add(topBar, BorderLayout.NORTH);
        painel.add(sc,     BorderLayout.CENTER);
        return painel;
    }

    private List<String[]> lerLogs() {
        List<String[]> linhas = new ArrayList<>();
        File pasta = new File(DIR_LOGS);
        if (!pasta.exists()) return linhas;
        File[] arqs = pasta.listFiles(f -> f.getName().endsWith("_log.txt"));
        if (arqs == null) return linhas;
        for (File f : arqs) {
            try (Scanner sc = new Scanner(f, "UTF-8")) {
                while (sc.hasNextLine()) {
                    String[] p = sc.nextLine().trim().split(" \\| ",3);
                    if (p.length>=3) linhas.add(p);
                }
            } catch (Exception ignored) {}
        }
        return linhas;
    }

    // ═════════════════════════════════════════════════════════════════════
    // EXPORTAÇÃO PNG — cabeçalho com logo + título + conteúdo + rodapé
    // ═════════════════════════════════════════════════════════════════════
    private void exportarPNG(JPanel conteudo) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Salvar Relat\u00f3rio como PNG");
        chooser.setFileFilter(new FileNameExtensionFilter("Imagem PNG","png"));
        String nome = "relatorio_folha_" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".png";
        chooser.setSelectedFile(new File(nome));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File dest = chooser.getSelectedFile();
        if (!dest.getName().endsWith(".png")) dest = new File(dest.getAbsolutePath()+".png");

        int largura  = 1200;
        int altTopo  = 70;
        int altRod   = 30;
        int marg     = 24;

        // Medir conteúdo em largura alvo
        conteudo.setSize(largura - marg*2, conteudo.getPreferredSize().height);
        conteudo.doLayout();
        int altConteudo = conteudo.getPreferredSize().height;
        int altTotal    = altTopo + marg + altConteudo + marg + altRod;

        BufferedImage img = new BufferedImage(largura, altTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        // Fundo
        g2.setColor(F0); g2.fillRect(0,0,largura,altTotal);

        // ── Cabeçalho ────────────────────────────────────────────────────
        g2.setColor(F2); g2.fillRect(0,0,largura,altTopo);
        g2.setColor(F3); g2.drawLine(0,altTopo-1,largura,altTopo-1);

        int xCursor = 20;
        if (logoImg != null) {
            int h = 40, w = (int)(logoImg.getWidth()*((double)h/logoImg.getHeight()));
            g2.drawImage(logoImg.getScaledInstance(w,h,Image.SCALE_SMOOTH), xCursor,(altTopo-h)/2, null);
            xCursor += w + 14;
        }
        g2.setColor(T0);
        g2.setFont(new Font("Monospaced",Font.BOLD,15));
        g2.drawString("FOLHA DE PAGAMENTO  \u00b7  Dashboard Anal\u00edtico  \u00b7  "
            + labelMes(ultimoMes).toUpperCase(), xCursor, altTopo/2+6);

        // Data (direita)
        g2.setColor(T2);
        g2.setFont(new Font("Monospaced",Font.PLAIN,11));
        String dataGen = "Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(dataGen, largura - fm.stringWidth(dataGen) - 20, altTopo/2+5);

        // ── Conteúdo ─────────────────────────────────────────────────────
        g2.translate(marg, altTopo+marg);
        conteudo.paint(g2);
        g2.translate(-marg, -(altTopo+marg));

        // ── Rodapé ───────────────────────────────────────────────────────
        int yRod = altTotal - altRod;
        g2.setColor(F2); g2.fillRect(0,yRod,largura,altRod);
        g2.setColor(F3); g2.drawLine(0,yRod,largura,yRod);
        g2.setColor(T2); g2.setFont(new Font("Monospaced",Font.PLAIN,10));
        g2.drawString("Sistema de Folha de Pagamento  \u00b7  v5.1", 20, yRod+18);

        g2.dispose();

        try {
            ImageIO.write(img, "png", dest);
            JOptionPane.showMessageDialog(this,
                "Relat\u00f3rio salvo em:\n" + dest.getAbsolutePath(),
                "PNG exportado", JOptionPane.INFORMATION_MESSAGE);
            try { Desktop.getDesktop().open(dest.getParentFile()); }
            catch (Exception ignored) {}
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Exportar CSV ──────────────────────────────────────────────────────
    private void exportarCSV(DefaultTableModel modelo, String prefixo) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Salvar CSV"); chooser.setFileFilter(new FileNameExtensionFilter("CSV","csv"));
        String nome = prefixo+"_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))+".csv";
        chooser.setSelectedFile(new File(nome));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = chooser.getSelectedFile();
        if (!dest.getName().endsWith(".csv")) dest = new File(dest.getAbsolutePath()+".csv");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest),"UTF-8"))) {
            pw.print('\uFEFF'); // BOM UTF-8 para Excel
            StringBuilder sb = new StringBuilder();
            for (int c=0; c<modelo.getColumnCount(); c++) { if(c>0) sb.append(';'); sb.append(modelo.getColumnName(c)); }
            pw.println(sb);
            for (int r=0; r<modelo.getRowCount(); r++) {
                sb.setLength(0);
                for (int c=0; c<modelo.getColumnCount(); c++) {
                    if(c>0) sb.append(';');
                    Object v = modelo.getValueAt(r,c); sb.append(v!=null?v.toString():"");
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this,"Arquivo salvo:\n"+dest.getAbsolutePath(),"CSV exportado",JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,"Erro: "+ex.getMessage(),"Erro",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // COMPONENTES GRÁFICOS (sem dependências externas)
    // ═════════════════════════════════════════════════════════════════════

    /** Gráfico de barras verticais — monocromático com gradiente sutil. */
    static class BarChart extends JPanel {
        private final Map<String,Double> dados;
        BarChart(Map<String,Double> dados) {
            this.dados = dados; setBackground(F1); setBorder(new EmptyBorder(12,12,12,12));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dados.isEmpty()) { g.setColor(T2); g.setFont(new Font("Monospaced",Font.PLAIN,12)); g.drawString("Sem dados",20,getHeight()/2); return; }
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int mE=65,mD=12,mT=28,mB=28, ld=getWidth()-mE-mD, ad=getHeight()-mT-mB;
            double maxV = dados.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
            int n=dados.size(), wb=Math.max(8,ld/n-4), esp=Math.max(2,(ld-n*wb)/(n+1));
            // Grid
            g2.setColor(new Color(42,42,42));
            for (int i=1;i<=4;i++) { int gy=mT+ad-(int)(ad*i/4.0); g2.drawLine(mE,gy,mE+ld,gy); }
            int i=0;
            for (Map.Entry<String,Double> entry : dados.entrySet()) {
                double v=entry.getValue(); int h=(int)(v/maxV*ad), x=mE+esp+i*(wb+esp), y=mT+ad-h;
                g2.setPaint(new GradientPaint(x,y,C_ACCENT.brighter(),x,y+h,C_ACCENT.darker()));
                g2.fillRect(x,y,wb,h);
                // Valor
                g2.setColor(T1); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
                String vStr = FMT_MOEDA.format(v).replace("R$ ","").trim();
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(vStr, x+(wb-fm.stringWidth(vStr))/2, Math.max(y-3,mT+10));
                // Label X
                g2.setColor(T2); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
                String lbl=entry.getKey(); fm=g2.getFontMetrics();
                g2.drawString(lbl, x+(wb-fm.stringWidth(lbl))/2, mT+ad+14);
                i++;
            }
            g2.setColor(T2); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
            g2.drawString(FMT_MOEDA.format(maxV).replace("R$ ","R$"),2,mT+8);
            g2.drawString("R$0",2,mT+ad+4);
        }
    }

    /** Gráfico de pizza/donut com legenda lateral. */
    static class PieChart extends JPanel {
        private final Map<String,Long> dados;
        private static final Color[] CORES = {C_PADRAO,C_COMISS,C_PROD,new Color(100,170,200)};
        PieChart(Map<String,Long> dados) { this.dados=dados; setBackground(F1); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dados.isEmpty()) return;
            Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            long total=dados.values().stream().mapToLong(Long::longValue).sum();
            int raio=Math.min(getWidth()/3,getHeight()/2)-16, cx=getWidth()/3, cy=getHeight()/2;
            double ang=0; int idx=0;
            for (Map.Entry<String,Long> e : dados.entrySet()) {
                double fatia=(double)e.getValue()/total*360.0;
                g2.setColor(CORES[idx%CORES.length]); g2.fillArc(cx-raio,cy-raio,raio*2,raio*2,(int)ang,(int)fatia);
                ang+=fatia; idx++;
            }
            // Donut hole
            g2.setColor(F1); int ri=raio/2; g2.fillOval(cx-ri,cy-ri,ri*2,ri*2);
            g2.setColor(T0); g2.setFont(new Font("Monospaced",Font.BOLD,11));
            String tot=String.valueOf(total); FontMetrics fm=g2.getFontMetrics();
            g2.drawString(tot, cx-fm.stringWidth(tot)/2, cy+4);
            // Legenda
            int lx=cx+raio+14, ly=cy-dados.size()*22/2; idx=0;
            for (Map.Entry<String,Long> e : dados.entrySet()) {
                g2.setColor(CORES[idx%CORES.length]); g2.fillRect(lx,ly+idx*22,12,12);
                g2.setColor(T0); g2.setFont(new Font("Monospaced",Font.PLAIN,10));
                double pct=(double)e.getValue()/total*100;
                g2.drawString(String.format("%s  %.0f%%",e.getKey(),pct),lx+16,ly+idx*22+11);
                idx++;
            }
        }
    }

    // ── Utilitários de UI ─────────────────────────────────────────────────
    private JLabel secLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Monospaced",Font.BOLD,10)); l.setForeground(T2);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,F3),new EmptyBorder(0,0,5,0)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE,22));
        return l;
    }
    private JPanel envolve(String titulo, JComponent filho) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(F1);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(F3),
            "  "+titulo, 0,0, new Font("Monospaced",Font.PLAIN,10), T2));
        p.add(filho, BorderLayout.CENTER); return p;
    }
    private JTable tabelaLeve(Object[][] dados, String[] cols) {
        DefaultTableModel m = new DefaultTableModel(dados,cols) { @Override public boolean isCellEditable(int r, int c){return false;} };
        return tabelaLeve(m);
    }
    private JTable tabelaLeve(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(F1); t.setForeground(T0); t.setGridColor(F3); t.setRowHeight(22);
        t.setFont(new Font("Monospaced",Font.PLAIN,12)); t.setSelectionBackground(new Color(60,60,80));
        t.setSelectionForeground(Color.WHITE); t.setShowVerticalLines(false);
        t.getTableHeader().setBackground(F2); t.getTableHeader().setForeground(T1);
        t.getTableHeader().setFont(new Font("Monospaced",Font.BOLD,11));
        t.getTableHeader().setBorder(new MatteBorder(0,0,1,0,F3));
        return t;
    }
    private JButton btn(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(F2); b.setForeground(T0); b.setFont(new Font("Monospaced",Font.PLAIN,12));
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(BorderFactory.createLineBorder(F3),new EmptyBorder(5,14,5,14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){b.setBackground(F3);}
            @Override public void mouseExited(MouseEvent e){b.setBackground(F2);}
        });
        return b;
    }
    @SafeVarargs
    private <T> JComboBox<T> combo(T... itens) {
        JComboBox<T> c = new JComboBox<>(itens);
        c.setBackground(F2); c.setForeground(T0); c.setFont(new Font("Monospaced",Font.PLAIN,12));
        c.setBorder(BorderFactory.createLineBorder(F3)); return c;
    }
    private JTextField campo() {
        JTextField t = new JTextField(14);
        t.setBackground(F2); t.setForeground(T0); t.setCaretColor(T0);
        t.setFont(new Font("Monospaced",Font.PLAIN,12));
        t.setBorder(new CompoundBorder(BorderFactory.createLineBorder(F3),new EmptyBorder(3,6,3,6)));
        return t;
    }
    private JLabel lab(String texto) {
        JLabel l = new JLabel(texto); l.setForeground(T1); l.setFont(new Font("Monospaced",Font.PLAIN,12)); return l;
    }

    // ── Utilitários de dados ──────────────────────────────────────────────
    private List<RegistroFolha> doMes(String mesAno) {
        if (mesAno.isEmpty()) return Collections.emptyList();
        String[] p = mesAno.split("-"); if(p.length<2) return Collections.emptyList();
        try { int ano=Integer.parseInt(p[0]),mes=Integer.parseInt(p[1]);
            return dados.stream().filter(r->r.ano==ano&&r.mes==mes).collect(Collectors.toList());
        } catch(Exception e){return Collections.emptyList();}
    }
    private String labelMes(String m) {
        if(m==null||m.isEmpty()) return "(sem dados)";
        try { String[] p=m.split("-"); return nomeMesCompleto(Integer.parseInt(p[1]))+" de "+p[0]; }
        catch(Exception e){return m;}
    }
    private String labelMesCurto(String m) {
        try { String[] p=m.split("-"); return nomeMesCompleto(Integer.parseInt(p[1])).substring(0,3)+"/"+p[0].substring(2); }
        catch(Exception e){return m;}
    }
    private String nomeMesCompleto(int m) {
        String[] n={"","Janeiro","Fevereiro","Mar\u00e7o","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        return m>=1&&m<=12?n[m]:"?";
    }

    // ── Modelo de dados ───────────────────────────────────────────────────
    static class RegistroFolha {
        int matricula,mes,ano,qtdPecas;
        String nome,tipo;
        double salarioBase,vendas,percentual,valorPeca,salTotal;
    }
}