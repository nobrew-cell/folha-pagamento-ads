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
 * PALETA: preto/branco/cinza como base. Cores apenas nas 3 categorias de
 * funcionário (pastel suave) e azul para totais. Nada mais colorido.
 *
 * 4 abas:
 *   1. Visão Geral   — cards + gráfico evolução + pizza + top-5 + análise anual
 *   2. Funcionários  — tabela filtrável, exportação CSV
 *   3. Logs ADM      — auditoria filtrada, exportação CSV
 *   4. Relatórios    — resumo textual, exportar TSV, salvar PNG
 */
public class DashboardBI extends JFrame {

    // ── Paleta estrita: preto/cinza/branco ────────────────────────────────
    // Fundo
    static final Color BG_ROOT   = new Color(15,  15,  15);   // fundo raiz (quase preto)
    static final Color BG_PANEL  = new Color(22,  22,  22);   // painel / aba
    static final Color BG_CARD   = new Color(30,  30,  30);   // card e barra de topo
    static final Color BG_ROW_A  = new Color(22,  22,  22);   // linha par da tabela
    static final Color BG_ROW_B  = new Color(28,  28,  28);   // linha ímpar da tabela
    static final Color BG_HEADER = new Color(30,  30,  30);   // cabeçalho de tabela
    static final Color BG_SEL    = new Color(45,  55,  75);   // seleção (azul muito escuro)

    // Texto
    static final Color TX_PRIM   = new Color(220, 220, 220);  // texto principal
    static final Color TX_SEC    = new Color(130, 130, 130);  // texto secundário
    static final Color TX_DIS    = new Color(60,  60,  60);   // texto desabilitado
    static final Color TX_HEAD   = new Color(160, 160, 160);  // cabeçalho de tabela

    // Bordas / grid
    static final Color BD_LINE   = new Color(40,  40,  40);   // borda padrão
    static final Color BD_FOCUS  = new Color(65,  65,  65);   // borda com foco

    // Cores funcionais — APENAS pastel suave para os 3 tipos + azul para total
    static final Color C_PADRAO  = new Color(130, 185, 130);  // verde pastel
    static final Color C_COMISS  = new Color(210, 155,  90);  // laranja pastel
    static final Color C_PROD    = new Color(155, 130, 200);  // roxo pastel
    static final Color C_TOTAL   = new Color(100, 150, 210);  // azul (só para totais)
    static final Color C_REMOV   = new Color(180,  80,  80);  // vermelho (só para remoção no log)

    // ── Formatação monetária ──────────────────────────────────────────────
    private static final DecimalFormat FMT_M;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("pt","BR"));
        sym.setGroupingSeparator('.'); sym.setDecimalSeparator(',');
        FMT_M = new DecimalFormat("R$ #,##0.00", sym);
    }

    private static final String DIR_HIST = "historico";
    private static final String DIR_EXP  = "exportados/dados";
    private static final String DIR_LOGS = "logs";
    private static final String LOGO_PATH = "config/logo.png";

    private final List<RegistroFolha> dados = new ArrayList<>();
    private String ultimoMes = "";
    private BufferedImage logoImg = null;

    // ── Ponto de entrada ──────────────────────────────────────────────────
    public static void abrir() {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            // Força tabelas a não usarem fundo branco nativo
            UIManager.put("Table.background",        BG_PANEL);
            UIManager.put("Table.foreground",        TX_PRIM);
            UIManager.put("TableHeader.background",  BG_HEADER);
            UIManager.put("TableHeader.foreground",  TX_HEAD);
            UIManager.put("ScrollPane.background",   BG_PANEL);
            UIManager.put("Viewport.background",     BG_PANEL);
            new DashboardBI().setVisible(true);
        });
    }

    public DashboardBI() {
        super("Folha de Pagamento — Dashboard Analítico v5.1");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_ROOT);
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

    // ── Dados ─────────────────────────────────────────────────────────────
    private void carregarDados() {
        dados.clear();
        lerPasta(DIR_HIST);
        lerPasta(DIR_EXP);
        Set<String> vistos = new HashSet<>();
        dados.removeIf(r -> !vistos.add(r.ano + "-" + r.mes + "-" + r.matricula));
        dados.sort(Comparator.comparingInt((RegistroFolha r) -> r.ano)
                             .thenComparingInt(r -> r.mes)
                             .thenComparingInt(r -> r.matricula));
        if (!dados.isEmpty()) {
            RegistroFolha u = dados.get(dados.size()-1);
            ultimoMes = String.format("%04d-%02d", u.ano, u.mes);
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

    // ── Barra de topo ─────────────────────────────────────────────────────
    private JPanel topo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,BD_LINE),
            new EmptyBorder(10,18,10,18)));

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        esq.setBackground(BG_CARD);
        if (logoImg != null) {
            int h=34, w=(int)(logoImg.getWidth()*((double)h/logoImg.getHeight()));
            esq.add(new JLabel(new ImageIcon(logoImg.getScaledInstance(w,h,Image.SCALE_SMOOTH))));
        }
        JLabel titulo = new JLabel("FOLHA DE PAGAMENTO  \u00b7  Dashboard");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        titulo.setForeground(TX_PRIM);
        esq.add(titulo);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        dir.setBackground(BG_CARD);
        JButton btnAt = btnCinza("\u21bb  Atualizar");
        btnAt.addActionListener(e -> { carregarDados(); construirUI(); });
        JButton btnFc = btnCinza("\u2715  Fechar");
        btnFc.setForeground(C_REMOV);
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
                (int)dados.stream().map(r -> r.ano+"-"+r.mes).distinct().count(),
                labelMes(ultimoMes));
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Monospaced",Font.PLAIN,11));
        l.setForeground(TX_DIS); l.setBackground(BG_CARD); l.setOpaque(true);
        l.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,BD_LINE),new EmptyBorder(5,18,5,18)));
        return l;
    }

    // ── Abas ─────────────────────────────────────────────────────────────
    private JTabbedPane abas() {
        JTabbedPane t = new JTabbedPane();
        t.setBackground(BG_PANEL); t.setForeground(TX_SEC);
        t.setFont(new Font("Monospaced",Font.PLAIN,12));
        t.setBorder(null);
        t.addTab("  Vis\u00e3o Geral  ",  abaVisaoGeral());
        t.addTab("  Funcion\u00e1rios  ", abaFuncionarios());
        t.addTab("  Logs ADM  ",          abaLogs());
        t.addTab("  Relat\u00f3rios  ",   abaRelatorios());
        return t;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 1 — VISÃO GERAL
    // ═════════════════════════════════════════════════════════════════════
    private JScrollPane abaVisaoGeral() {
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(BG_ROOT);
        corpo.setBorder(new EmptyBorder(18,18,18,18));

        List<RegistroFolha> mesAtual = doMes(ultimoMes);
        String mesAnteriorKey = mesAnterior(ultimoMes);
        List<RegistroFolha> mesAnt = doMes(mesAnteriorKey);

        // ── Seção 1: cards do mês ────────────────────────────────────────
        corpo.add(secLabel("ÚLTIMO MÊS  \u00b7  " + labelMes(ultimoMes).toUpperCase()));
        corpo.add(vgap(8));
        corpo.add(cardsDoMes(mesAtual, mesAnt));
        corpo.add(vgap(6));

        // Cards por tipo (segunda linha)
        corpo.add(cardsPorTipo(mesAtual));
        corpo.add(vgap(22));

        // ── Seção 2: gráfico de evolução ─────────────────────────────────
        corpo.add(secLabel("EVOLUÇÃO TOTAL DA FOLHA  \u00b7  TODOS OS MESES"));
        corpo.add(vgap(8));
        Map<String,Double> evol = new LinkedHashMap<>();
        dados.stream()
             .collect(Collectors.groupingBy(
                 r -> String.format("%04d-%02d",r.ano,r.mes),
                 LinkedHashMap::new,
                 Collectors.summingDouble(r -> r.salTotal)))
             .entrySet().stream()
             .sorted(Map.Entry.comparingByKey())
             .forEach(e -> evol.put(labelMesCurto(e.getKey()), e.getValue()));
        BarChart bar = new BarChart(evol);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 195));
        bar.setPreferredSize(new Dimension(0, 195));
        corpo.add(bar);
        corpo.add(vgap(22));

        // ── Seção 3: pizza (maior) + top-5 lado a lado ───────────────────
        corpo.add(secLabel("DISTRIBUIÇÃO POR TIPO  \u00b7  TOP 5 SALÁRIOS  \u00b7  "
                + labelMes(ultimoMes).toUpperCase()));
        corpo.add(vgap(8));

        JPanel linha3 = new JPanel(new GridLayout(1,2,16,0));
        linha3.setBackground(BG_ROOT);
        linha3.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        Map<String,Long> porTipo = mesAtual.stream()
            .collect(Collectors.groupingBy(r -> r.tipo, Collectors.counting()));
        // Pizza ocupa o espaço todo do seu painel (sem miniaturizar)
        PieChart pizza = new PieChart(porTipo);
        linha3.add(envolve("Distribuição por Tipo", pizza));

        String[] colTop = {"#","Nome","Tipo","Salário Total"};
        List<RegistroFolha> top5 = mesAtual.stream()
            .sorted(Comparator.comparingDouble((RegistroFolha r) -> r.salTotal).reversed())
            .limit(5).collect(Collectors.toList());
        Object[][] dadosTop = new Object[top5.size()][4];
        for (int i=0; i<top5.size(); i++) {
            RegistroFolha r = top5.get(i);
            dadosTop[i] = new Object[]{ i+1, r.nome, r.tipo, FMT_M.format(r.salTotal) };
        }
        JTable tTop = tabelaEscura(dadosTop, colTop);
        // Renderer com cor de tipo na coluna 2
        tTop.setDefaultRenderer(Object.class, rendererTipo(tTop, 2));
        linha3.add(envolve("Top 5 Salários do Mês", new JScrollPane(tTop) {{
            setBorder(null); getViewport().setBackground(BG_PANEL);
        }}));
        corpo.add(linha3);
        corpo.add(vgap(22));

        // ── Seção 4: análise anual ────────────────────────────────────────
        int anoAtual = dados.isEmpty() ? LocalDateTime.now().getYear()
                                       : dados.get(dados.size()-1).ano;
        corpo.add(secLabel("ANÁLISE ANUAL  \u00b7  " + anoAtual));
        corpo.add(vgap(8));
        corpo.add(painelAnual(anoAtual));
        corpo.add(vgap(12));

        JScrollPane scroll = new JScrollPane(corpo);
        scroll.setBackground(BG_ROOT); scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_ROOT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // Cards linha 1: total, funcionários, maior, média + variação mês anterior
    private JPanel cardsDoMes(List<RegistroFolha> mes, List<RegistroFolha> ant) {
        JPanel row = new JPanel(new GridLayout(1,4,10,0));
        row.setBackground(BG_ROOT); row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        double total  = mes.stream().mapToDouble(r->r.salTotal).sum();
        double totAnt = ant.stream().mapToDouble(r->r.salTotal).sum();
        double maior  = mes.stream().mapToDouble(r->r.salTotal).max().orElse(0);
        double media  = mes.stream().mapToDouble(r->r.salTotal).average().orElse(0);
        long   qtd    = mes.size();

        String varStr = totAnt <= 0 ? "" : String.format("  %+.1f%% vs %s",
            (total-totAnt)/totAnt*100, labelMesCurto(mesAnterior(ultimoMes)));

        row.add(card2("TOTAL DA FOLHA",    FMT_M.format(total),       varStr,       C_TOTAL));
        row.add(card2("FUNCIONÁRIOS",      String.valueOf(qtd),        "no período", TX_PRIM));
        row.add(card2("MAIOR SALÁRIO",     FMT_M.format(maior),       "",            C_PADRAO));
        row.add(card2("MÉDIA SALARIAL",    FMT_M.format(media),       "",            TX_SEC));
        return row;
    }

    // Cards linha 2: total por tipo
    private JPanel cardsPorTipo(List<RegistroFolha> mes) {
        JPanel row = new JPanel(new GridLayout(1,3,10,0));
        row.setBackground(BG_ROOT); row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        double tPad = mes.stream().filter(r->"PADRAO".equals(r.tipo)).mapToDouble(r->r.salTotal).sum();
        double tCom = mes.stream().filter(r->"COMISSIONADO".equals(r.tipo)).mapToDouble(r->r.salTotal).sum();
        double tPro = mes.stream().filter(r->"PRODUCAO".equals(r.tipo)).mapToDouble(r->r.salTotal).sum();
        long   nPad = mes.stream().filter(r->"PADRAO".equals(r.tipo)).count();
        long   nCom = mes.stream().filter(r->"COMISSIONADO".equals(r.tipo)).count();
        long   nPro = mes.stream().filter(r->"PRODUCAO".equals(r.tipo)).count();

        row.add(card2("PADRÃO",       FMT_M.format(tPad), nPad+" func.", C_PADRAO));
        row.add(card2("COMISSIONADO", FMT_M.format(tCom), nCom+" func.", C_COMISS));
        row.add(card2("PRODUÇÃO",     FMT_M.format(tPro), nPro+" func.", C_PROD));
        return row;
    }

    // Card com título, valor grande e sub-texto
    private JPanel card2(String titulo, String valor, String sub, Color corValor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0,2,0,0,corValor),
            new EmptyBorder(10,12,10,12)));

        JLabel lT = new JLabel(titulo);
        lT.setFont(new Font("Monospaced",Font.PLAIN,9));
        lT.setForeground(TX_SEC); lT.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lV = new JLabel(valor);
        lV.setFont(new Font("Monospaced",Font.BOLD,15));
        lV.setForeground(corValor); lV.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lS = new JLabel(sub.isEmpty() ? " " : sub);
        lS.setFont(new Font("Monospaced",Font.PLAIN,9));
        lS.setForeground(TX_DIS); lS.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lT); p.add(Box.createVerticalStrut(4)); p.add(lV);
        p.add(Box.createVerticalStrut(2)); p.add(lS);
        return p;
    }

    // Tabela de análise anual
    private JPanel painelAnual(int ano) {
        String[] cols = {"Mês","Func.","Total da Folha","Maior Salário","Média","Variação"};
        DefaultTableModel m = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        Map<Integer,List<RegistroFolha>> porMes = dados.stream()
            .filter(r->r.ano==ano)
            .collect(Collectors.groupingBy(r->r.mes, TreeMap::new, Collectors.toList()));
        double anterior = -1;
        for (Map.Entry<Integer,List<RegistroFolha>> e : porMes.entrySet()) {
            List<RegistroFolha> regs = e.getValue();
            double tot = regs.stream().mapToDouble(r->r.salTotal).sum();
            double max = regs.stream().mapToDouble(r->r.salTotal).max().orElse(0);
            double med = regs.stream().mapToDouble(r->r.salTotal).average().orElse(0);
            String var = anterior<0 ? "\u2014" : String.format("%+.1f%%",(tot-anterior)/anterior*100);
            m.addRow(new Object[]{ nomeMes(e.getKey()), regs.size(),
                FMT_M.format(tot), FMT_M.format(max), FMT_M.format(med), var });
            anterior = tot;
        }
        if (m.getRowCount()>0) {
            double totAno = porMes.values().stream().flatMap(Collection::stream)
                .mapToDouble(r->r.salTotal).sum();
            m.addRow(new Object[]{"TOTAL "+ano,"\u2014",FMT_M.format(totAno),"\u2014","\u2014","\u2014"});
        }
        int lastRow = m.getRowCount()-1;
        JTable t = tabelaEscura(m);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tb,Object val,
                    boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(tb,val,sel,foc,row,col);
                boolean isTotal = (row==lastRow && lastRow>=0);
                setBackground(sel ? BG_SEL : isTotal ? new Color(32,32,38) : row%2==0 ? BG_ROW_A : BG_ROW_B);
                setForeground(sel ? Color.WHITE : isTotal ? C_TOTAL : TX_PRIM);
                setFont(new Font("Monospaced", isTotal?Font.BOLD:Font.PLAIN, 12));
                setBorder(new EmptyBorder(0,8,0,8));
                // Variação: verde se positivo, vermelho se negativo
                if (!isTotal && col==5 && val!=null) {
                    String s = val.toString();
                    if (s.startsWith("+")) setForeground(C_PADRAO);
                    else if (s.startsWith("-")) setForeground(C_REMOV);
                }
                return this;
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_ROOT); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        JScrollPane sc = new JScrollPane(t);
        sc.setBorder(null); sc.setBackground(BG_PANEL);
        sc.getViewport().setBackground(BG_PANEL);
        p.add(sc,BorderLayout.CENTER);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 2 — FUNCIONÁRIOS
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaFuncionarios() {
        JPanel painel = new JPanel(new BorderLayout(0,10));
        painel.setBackground(BG_ROOT);
        painel.setBorder(new EmptyBorder(14,16,14,16));

        String[] mDisp = dados.stream()
            .map(r->String.format("%04d-%02d",r.ano,r.mes))
            .distinct().sorted(Comparator.reverseOrder()).toArray(String[]::new);
        String[] mLabel = Arrays.stream(mDisp).map(this::labelMes).toArray(String[]::new);

        JComboBox<String> cbMes  = comboEscuro(mLabel.length>0 ? mLabel : new String[]{"(sem dados)"});
        JComboBox<String> cbTipo = comboEscuro("Todos","PADRAO","COMISSIONADO","PRODUCAO");
        JTextField tfBusca = campoEscuro(14);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        filtros.setBackground(BG_ROOT);
        filtros.add(lab("Mês:")); filtros.add(cbMes);
        filtros.add(lab("Tipo:")); filtros.add(cbTipo);
        filtros.add(lab("Nome:")); filtros.add(tfBusca);

        String[] cols = {"Matrícula","Nome","Tipo","Sal. Base","Extra","Total"};
        DefaultTableModel modelo = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tabela = tabelaEscura(modelo);
        tabela.setDefaultRenderer(Object.class, rendererTipo(tabela, 2));
        tabela.setRowSorter(new TableRowSorter<>(modelo));

        Runnable atualizar = () -> {
            modelo.setRowCount(0);
            String mesSel = mDisp.length>0 ? mDisp[Math.max(0,cbMes.getSelectedIndex())] : "";
            String tipoSel = (String)cbTipo.getSelectedItem();
            String busca = tfBusca.getText().toLowerCase().trim();
            doMes(mesSel).stream()
                .filter(r->"Todos".equals(tipoSel)||r.tipo.equals(tipoSel))
                .filter(r->busca.isEmpty()||r.nome.toLowerCase().contains(busca))
                .forEach(r->{
                    double extra = "COMISSIONADO".equals(r.tipo) ? r.vendas*r.percentual/100.0
                                 : "PRODUCAO".equals(r.tipo)     ? r.qtdPecas*r.valorPeca : 0;
                    modelo.addRow(new Object[]{r.matricula,r.nome,r.tipo,
                        FMT_M.format(r.salarioBase),FMT_M.format(extra),FMT_M.format(r.salTotal)});
                });
        };
        cbMes.addActionListener(e->atualizar.run());
        cbTipo.addActionListener(e->atualizar.run());
        tfBusca.addKeyListener(new KeyAdapter(){@Override public void keyReleased(KeyEvent e){atualizar.run();}});
        atualizar.run();

        JButton btnCsv = btnCinza("\u2b07  Exportar CSV");
        btnCsv.addActionListener(e->exportarCSV(modelo,"funcionarios"));
        JPanel topBar = new JPanel(new BorderLayout()); topBar.setBackground(BG_ROOT);
        JPanel bDir = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); bDir.setBackground(BG_ROOT); bDir.add(btnCsv);
        topBar.add(filtros,BorderLayout.WEST); topBar.add(bDir,BorderLayout.EAST);

        JScrollPane sc = scrollEscuro(tabela);
        painel.add(topBar,BorderLayout.NORTH);
        painel.add(sc,BorderLayout.CENTER);
        return painel;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 3 — LOGS ADM
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaLogs() {
        JPanel painel = new JPanel(new BorderLayout(0,10));
        painel.setBackground(BG_ROOT);
        painel.setBorder(new EmptyBorder(14,16,14,16));

        List<String[]> linhas = lerLogs();
        Set<String> ops=new TreeSet<>(), meses=new TreeSet<>();
        for (String[] l : linhas) {
            if (l.length>=3) {
                ops.add(l[1]);
                if (l[0].length()>=7) meses.add(l[0].substring(0,7));
            }
        }
        String[] opsArr = Stream.concat(Stream.of("Todas"),ops.stream()).toArray(String[]::new);
        String[] mesArr = Stream.concat(Stream.of("Todos"),
            meses.stream().sorted(Comparator.reverseOrder())).toArray(String[]::new);

        JComboBox<String> cbOp  = comboEscuro(opsArr);
        JComboBox<String> cbMes = comboEscuro(mesArr);
        JTextField tfBusca = campoEscuro(16);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        filtros.setBackground(BG_ROOT);
        filtros.add(lab("Operação:")); filtros.add(cbOp);
        filtros.add(lab("Mês:"));     filtros.add(cbMes);
        filtros.add(lab("Busca:"));   filtros.add(tfBusca);

        String[] cols = {"Data/Hora","Operação","Detalhes"};
        DefaultTableModel modelo = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tabela = tabelaEscura(modelo);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(700);

        // Renderer: cor na coluna operação
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tb,Object val,
                    boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(tb,val,sel,foc,row,col);
                String op = (tb.getRowCount()>row && tb.getValueAt(row,1)!=null)
                    ? tb.getValueAt(row,1).toString() : "";
                Color cor = switch(op) {
                    case "CADASTRO"             -> C_PADRAO;
                    case "REMOCAO"              -> C_REMOV;
                    case "RESET"                -> new Color(200,120,60);
                    case "EDICAO","EDICAO_LOTE" -> C_COMISS;
                    case "CONFIG"               -> C_PROD;
                    case "IMPORT"               -> C_TOTAL;
                    case "NOVO_MES"             -> new Color(120,180,180);
                    default                     -> TX_SEC;
                };
                setForeground(sel ? Color.WHITE : col==1 ? cor : TX_PRIM);
                setBackground(sel ? BG_SEL : row%2==0 ? BG_ROW_A : BG_ROW_B);
                setFont(new Font("Monospaced",Font.PLAIN,12));
                setBorder(new EmptyBorder(0,8,0,8));
                return this;
            }
        });

        Runnable atualizar = () -> {
            modelo.setRowCount(0);
            String opSel  = (String)cbOp.getSelectedItem();
            String mesSel = (String)cbMes.getSelectedItem();
            String busca  = tfBusca.getText().toLowerCase().trim();
            linhas.stream()
                .filter(l->l.length>=3)
                .filter(l->"Todas".equals(opSel)||l[1].equals(opSel))
                .filter(l->"Todos".equals(mesSel)||(l[0].length()>=7&&l[0].startsWith(mesSel)))
                .filter(l->busca.isEmpty()||l[2].toLowerCase().contains(busca))
                .sorted((a,b)->b[0].compareTo(a[0])).limit(500)
                .forEach(l->modelo.addRow(new Object[]{l[0],l[1],l[2]}));
        };
        cbOp.addActionListener(e->atualizar.run());
        cbMes.addActionListener(e->atualizar.run());
        tfBusca.addKeyListener(new KeyAdapter(){@Override public void keyReleased(KeyEvent e){atualizar.run();}});
        atualizar.run();

        JButton btnCsv = btnCinza("\u2b07  Exportar CSV");
        btnCsv.addActionListener(e->exportarCSV(modelo,"logs"));
        JPanel topBar = new JPanel(new BorderLayout()); topBar.setBackground(BG_ROOT);
        JPanel bDir = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); bDir.setBackground(BG_ROOT); bDir.add(btnCsv);
        topBar.add(filtros,BorderLayout.WEST); topBar.add(bDir,BorderLayout.EAST);

        painel.add(topBar,BorderLayout.NORTH);
        painel.add(scrollEscuro(tabela),BorderLayout.CENTER);
        return painel;
    }

    private List<String[]> lerLogs() {
        List<String[]> linhas = new ArrayList<>();
        File pasta = new File(DIR_LOGS);
        if (!pasta.exists()) return linhas;
        File[] arqs = pasta.listFiles(f->f.getName().endsWith("_log.txt"));
        if (arqs==null) return linhas;
        for (File f : arqs) {
            try (Scanner sc=new Scanner(f,"UTF-8")) {
                while (sc.hasNextLine()) {
                    String[] p = sc.nextLine().trim().split(" \\| ",3);
                    if (p.length>=3) linhas.add(p);
                }
            } catch (Exception ignored) {}
        }
        return linhas;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ABA 4 — RELATÓRIOS
    // ═════════════════════════════════════════════════════════════════════
    private JPanel abaRelatorios() {
        JPanel painel = new JPanel(new BorderLayout(0,0));
        painel.setBackground(BG_ROOT);
        painel.setBorder(new EmptyBorder(18,18,18,18));

        // Painel esquerdo: controles
        JPanel esq = new JPanel();
        esq.setLayout(new BoxLayout(esq,BoxLayout.Y_AXIS));
        esq.setBackground(BG_ROOT);
        esq.setPreferredSize(new Dimension(240,0));
        esq.setBorder(new CompoundBorder(
            new MatteBorder(0,0,0,1,BD_LINE),
            new EmptyBorder(0,0,0,18)));

        // Seletor de mês e ano
        String[] mDisp = dados.stream()
            .map(r->String.format("%04d-%02d",r.ano,r.mes))
            .distinct().sorted(Comparator.reverseOrder()).toArray(String[]::new);
        String[] mLabel = Arrays.stream(mDisp).map(this::labelMes).toArray(String[]::new);
        Integer[] anosDisp = dados.stream().map(r->r.ano).distinct()
            .sorted(Comparator.reverseOrder()).toArray(Integer[]::new);

        JComboBox<String> cbMes = comboEscuro(mLabel.length>0 ? mLabel : new String[]{"(sem dados)"});
        JComboBox<Integer> cbAno = comboEscuro(anosDisp.length>0 ? anosDisp : new Integer[]{LocalDateTime.now().getYear()});

        esq.add(secLabel("PERÍODO"));
        esq.add(vgap(6));
        esq.add(lab("Mês para resumo:")); esq.add(vgap(4)); esq.add(cbMes);
        esq.add(vgap(10));
        esq.add(lab("Ano para TSV:")); esq.add(vgap(4)); esq.add(cbAno);
        esq.add(vgap(24));
        esq.add(secLabel("AÇÕES"));
        esq.add(vgap(10));

        // Botão resumo textual
        JButton btnResumo = btnAcao("📄  Gerar Resumo do Mês");
        esq.add(btnResumo); esq.add(vgap(8));

        // Botão exportar TSV do ano
        JButton btnTsv = btnAcao("⬇  Exportar TSV do Ano");
        esq.add(btnTsv); esq.add(vgap(8));

        // Botão salvar PNG
        JButton btnPng = btnAcao("🖼  Salvar Relatório PNG");
        btnPng.setForeground(C_TOTAL);
        btnPng.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_TOTAL,1), new EmptyBorder(7,14,7,14)));
        esq.add(btnPng);

        // Painel direito: área de texto com o resumo
        JTextArea txtArea = new JTextArea("  Selecione um mês e clique em \"Gerar Resumo\".\n\n"
            + "  O resumo textual ficará aqui.\n\n"
            + "  Ele pode ser copiado, colado em e-mail\n"
            + "  ou enviado por mensagem.");
        txtArea.setFont(new Font("Monospaced",Font.PLAIN,12));
        txtArea.setForeground(TX_PRIM); txtArea.setBackground(BG_PANEL);
        txtArea.setCaretColor(TX_PRIM); txtArea.setEditable(false);
        txtArea.setBorder(new EmptyBorder(14,14,14,14));
        txtArea.setLineWrap(true); txtArea.setWrapStyleWord(true);

        JScrollPane scTxt = new JScrollPane(txtArea);
        scTxt.setBorder(BorderFactory.createLineBorder(BD_LINE));
        scTxt.setBackground(BG_PANEL);
        scTxt.getViewport().setBackground(BG_PANEL);

        // ── Ações dos botões ─────────────────────────────────────────────

        btnResumo.addActionListener(e -> {
            String mesSel = mDisp.length>0 ? mDisp[Math.max(0,cbMes.getSelectedIndex())] : "";
            txtArea.setText(gerarResumoTextual(mesSel));
            txtArea.setCaretPosition(0);
        });

        btnTsv.addActionListener(e -> {
            int anoSel = (Integer)cbAno.getSelectedItem();
            exportarTSVAno(anoSel);
        });

        btnPng.addActionListener(e -> {
            // Cria um painel temporário com a visão geral para capturar
            JPanel captura = construirPainelParaPNG();
            exportarPNG(captura);
        });

        painel.add(esq,  BorderLayout.WEST);
        painel.add(scTxt, BorderLayout.CENTER);
        return painel;
    }

    // ── Geração de resumo textual ─────────────────────────────────────────
    private String gerarResumoTextual(String mesAno) {
        List<RegistroFolha> mes = doMes(mesAno);
        if (mes.isEmpty()) return "  Nenhum dado encontrado para " + labelMes(mesAno) + ".";

        StringBuilder sb = new StringBuilder();
        String sep = "  " + "─".repeat(50);
        sb.append("  RESUMO DA FOLHA  ·  ").append(labelMes(mesAno).toUpperCase()).append("\n");
        sb.append(sep).append("\n\n");

        double total = mes.stream().mapToDouble(r->r.salTotal).sum();
        double maior = mes.stream().mapToDouble(r->r.salTotal).max().orElse(0);
        double media = mes.stream().mapToDouble(r->r.salTotal).average().orElse(0);

        sb.append(String.format("  Total da folha    : %s%n", FMT_M.format(total)));
        sb.append(String.format("  Funcionários      : %d%n", mes.size()));
        sb.append(String.format("  Maior salário     : %s%n", FMT_M.format(maior)));
        sb.append(String.format("  Média salarial    : %s%n", FMT_M.format(media)));
        sb.append("\n").append(sep).append("\n\n");
        sb.append("  POR TIPO\n\n");

        for (String tipo : List.of("PADRAO","COMISSIONADO","PRODUCAO")) {
            List<RegistroFolha> sub = mes.stream().filter(r->r.tipo.equals(tipo)).collect(Collectors.toList());
            if (sub.isEmpty()) continue;
            double tTipo = sub.stream().mapToDouble(r->r.salTotal).sum();
            sb.append(String.format("  %-14s: %d func.  |  Total: %s%n",
                tipo, sub.size(), FMT_M.format(tTipo)));
        }

        sb.append("\n").append(sep).append("\n\n");
        sb.append("  RANKING COMPLETO\n\n");
        mes.stream()
           .sorted(Comparator.comparingDouble((RegistroFolha r)->r.salTotal).reversed())
           .forEach(r -> sb.append(String.format("  [%4d]  %-28s  %-14s  %s%n",
               r.matricula, r.nome, r.tipo, FMT_M.format(r.salTotal))));

        sb.append("\n").append(sep).append("\n");
        sb.append("  Gerado em: ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");

        return sb.toString();
    }

    // ── Exportar TSV do ano ───────────────────────────────────────────────
    private void exportarTSVAno(int ano) {
        List<RegistroFolha> doAno = dados.stream().filter(r->r.ano==ano).collect(Collectors.toList());
        if (doAno.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Nenhum dado para "+ano,"Aviso",JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Exportar TSV do ano "+ano);
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivo TSV","tsv"));
        chooser.setSelectedFile(new File("folha_"+ano+".tsv"));
        if (chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File dest = chooser.getSelectedFile();
        if (!dest.getName().endsWith(".tsv")) dest=new File(dest.getAbsolutePath()+".tsv");
        try (PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest),"UTF-8"))) {
            pw.println("MATRICULA\tNOME\tTIPO\tSALARIO_BASE\tVENDAS\tPERCENTUAL\tQTD_PECA\tVALOR_PECA\tSALARIO_TOTAL\tMES\tANO");
            for (RegistroFolha r : doAno) {
                pw.printf("%d\t%s\t%s\t%.2f\t%.2f\t%.2f\t%d\t%.2f\t%.2f\t%d\t%d%n",
                    r.matricula,r.nome,r.tipo,r.salarioBase,r.vendas,r.percentual,
                    r.qtdPecas,r.valorPeca,r.salTotal,r.mes,r.ano);
            }
            JOptionPane.showMessageDialog(this,"TSV salvo:\n"+dest.getAbsolutePath(),
                "Exportado",JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,"Erro: "+ex.getMessage(),"Erro",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Painel para captura PNG (constrói inline sem scroll) ──────────────
    private JPanel construirPainelParaPNG() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(BG_ROOT);
        p.setBorder(new EmptyBorder(18,18,18,18));

        List<RegistroFolha> mes = doMes(ultimoMes);
        String mesAnt = mesAnterior(ultimoMes);
        List<RegistroFolha> ant = doMes(mesAnt);

        p.add(secLabel("ÚLTIMO MÊS  ·  "+labelMes(ultimoMes).toUpperCase())); p.add(vgap(8));
        p.add(cardsDoMes(mes,ant)); p.add(vgap(6));
        p.add(cardsPorTipo(mes)); p.add(vgap(22));

        p.add(secLabel("EVOLUÇÃO")); p.add(vgap(8));
        Map<String,Double> evol = new LinkedHashMap<>();
        dados.stream()
            .collect(Collectors.groupingBy(r->String.format("%04d-%02d",r.ano,r.mes),
                LinkedHashMap::new, Collectors.summingDouble(r->r.salTotal)))
            .entrySet().stream().sorted(Map.Entry.comparingByKey())
            .forEach(e->evol.put(labelMesCurto(e.getKey()),e.getValue()));
        BarChart bar = new BarChart(evol);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE,195));
        bar.setPreferredSize(new Dimension(0,195));
        p.add(bar); p.add(vgap(22));

        int anoAtual = dados.isEmpty() ? LocalDateTime.now().getYear() : dados.get(dados.size()-1).ano;
        p.add(secLabel("ANÁLISE ANUAL  ·  "+anoAtual)); p.add(vgap(8));
        p.add(painelAnual(anoAtual));
        return p;
    }

    // ── Exportação PNG ────────────────────────────────────────────────────
    private void exportarPNG(JPanel conteudo) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Salvar Relatório como PNG");
        chooser.setFileFilter(new FileNameExtensionFilter("Imagem PNG","png"));
        String nome = "relatorio_folha_"+
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))+".png";
        chooser.setSelectedFile(new File(nome));
        if (chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File dest = chooser.getSelectedFile();
        if (!dest.getName().endsWith(".png")) dest=new File(dest.getAbsolutePath()+".png");

        int larg=1200, altTopo=68, altRod=28, marg=22;
        conteudo.setSize(larg-marg*2, conteudo.getPreferredSize().height);
        conteudo.doLayout();
        int altC = conteudo.getPreferredSize().height;
        int altT = altTopo+marg+altC+marg+altRod;

        BufferedImage img = new BufferedImage(larg,altT,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(BG_ROOT); g2.fillRect(0,0,larg,altT);

        // Cabeçalho
        g2.setColor(BG_CARD); g2.fillRect(0,0,larg,altTopo);
        g2.setColor(BD_LINE); g2.drawLine(0,altTopo-1,larg,altTopo-1);
        int xC=20;
        if (logoImg!=null) {
            int h=40,w=(int)(logoImg.getWidth()*((double)h/logoImg.getHeight()));
            g2.drawImage(logoImg.getScaledInstance(w,h,Image.SCALE_SMOOTH),xC,(altTopo-h)/2,null);
            xC+=w+14;
        }
        g2.setColor(TX_PRIM); g2.setFont(new Font("Monospaced",Font.BOLD,15));
        g2.drawString("FOLHA DE PAGAMENTO  ·  Dashboard  ·  "+labelMes(ultimoMes).toUpperCase(),
            xC,altTopo/2+6);
        g2.setColor(TX_DIS); g2.setFont(new Font("Monospaced",Font.PLAIN,11));
        String dg="Gerado em "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(dg,larg-fm.stringWidth(dg)-20,altTopo/2+5);

        // Conteúdo
        g2.translate(marg,altTopo+marg);
        conteudo.paint(g2);
        g2.translate(-marg,-(altTopo+marg));

        // Rodapé
        int yR=altT-altRod;
        g2.setColor(BG_CARD); g2.fillRect(0,yR,larg,altRod);
        g2.setColor(BD_LINE); g2.drawLine(0,yR,larg,yR);
        g2.setColor(TX_DIS); g2.setFont(new Font("Monospaced",Font.PLAIN,10));
        g2.drawString("Sistema de Folha de Pagamento  ·  v5.1",20,yR+18);
        g2.dispose();

        try {
            ImageIO.write(img,"png",dest);
            JOptionPane.showMessageDialog(this,"Salvo em:\n"+dest.getAbsolutePath(),
                "PNG exportado",JOptionPane.INFORMATION_MESSAGE);
            try { Desktop.getDesktop().open(dest.getParentFile()); } catch (Exception ignored) {}
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,"Erro: "+ex.getMessage(),"Erro",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Exportar CSV ──────────────────────────────────────────────────────
    private void exportarCSV(DefaultTableModel modelo, String pref) {
        JFileChooser chooser=new JFileChooser(".");
        chooser.setDialogTitle("Salvar CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV","csv"));
        chooser.setSelectedFile(new File(pref+"_"+
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))+".csv"));
        if (chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File dest=chooser.getSelectedFile();
        if (!dest.getName().endsWith(".csv")) dest=new File(dest.getAbsolutePath()+".csv");
        try (PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest),"UTF-8"))) {
            pw.print('\uFEFF');
            StringBuilder sb=new StringBuilder();
            for (int c=0;c<modelo.getColumnCount();c++){if(c>0)sb.append(';');sb.append(modelo.getColumnName(c));}
            pw.println(sb);
            for (int r=0;r<modelo.getRowCount();r++){
                sb.setLength(0);
                for (int c=0;c<modelo.getColumnCount();c++){
                    if(c>0)sb.append(';');
                    Object v=modelo.getValueAt(r,c);sb.append(v!=null?v.toString():"");
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this,"Salvo:\n"+dest.getAbsolutePath(),"CSV",JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,"Erro: "+ex.getMessage(),"Erro",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // COMPONENTES GRÁFICOS
    // ═════════════════════════════════════════════════════════════════════

    static class BarChart extends JPanel {
        private final Map<String,Double> dados;
        BarChart(Map<String,Double> dados) {
            this.dados=dados; setBackground(BG_PANEL);
            setBorder(new EmptyBorder(12,12,12,12));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dados.isEmpty()) {
                g.setColor(TX_DIS); g.setFont(new Font("Monospaced",Font.PLAIN,12));
                g.drawString("Sem dados",20,getHeight()/2); return;
            }
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int mE=62,mD=10,mT=26,mB=26;
            int ld=getWidth()-mE-mD, ad=getHeight()-mT-mB;
            double maxV=dados.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
            int n=dados.size(), wb=Math.max(8,ld/n-4), esp=Math.max(2,(ld-n*wb)/(n+1));
            // Grid
            g2.setColor(new Color(35,35,35));
            for (int i=1;i<=4;i++){int gy=mT+ad-(int)(ad*i/4.0);g2.drawLine(mE,gy,mE+ld,gy);}
            int i=0;
            for (Map.Entry<String,Double> entry:dados.entrySet()){
                double v=entry.getValue();
                int h=(int)(v/maxV*ad), x=mE+esp+i*(wb+esp), y=mT+ad-h;
                // Barra: azul (C_TOTAL) com gradiente sutil
                g2.setPaint(new GradientPaint(x,y,new Color(110,160,220),x,y+h,new Color(70,110,170)));
                g2.fillRect(x,y,wb,h);
                // Valor
                g2.setColor(TX_SEC); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
                String vs=FMT_M.format(v).replace("R$ ","").trim();
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(vs,x+(wb-fm.stringWidth(vs))/2,Math.max(y-3,mT+10));
                // Label X
                g2.setColor(TX_DIS); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
                String lbl=entry.getKey(); fm=g2.getFontMetrics();
                g2.drawString(lbl,x+(wb-fm.stringWidth(lbl))/2,mT+ad+14);
                i++;
            }
            g2.setColor(TX_DIS); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
            g2.drawString(FMT_M.format(maxV).replace("R$ ","R$"),2,mT+8);
            g2.drawString("R$0",2,mT+ad+4);
        }
    }

    static class PieChart extends JPanel {
        private final Map<String,Long> dados;
        private static final Color[] CORES = {C_PADRAO,C_COMISS,C_PROD,C_TOTAL};
        PieChart(Map<String,Long> dados) { this.dados=dados; setBackground(BG_PANEL); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dados.isEmpty()) {
                g.setColor(TX_DIS); g.setFont(new Font("Monospaced",Font.PLAIN,11));
                g.drawString("Sem dados",20,getHeight()/2); return;
            }
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            long total=dados.values().stream().mapToLong(Long::longValue).sum();
            // Pizza ocupa ~55% da largura, legenda no resto
            int raio = Math.min(getWidth()*55/200, getHeight()/2) - 12;
            int cx = getWidth()*55/200, cy=getHeight()/2;
            // Sombra sutil
            g2.setColor(new Color(10,10,10));
            g2.fillOval(cx-raio+3,cy-raio+3,raio*2,raio*2);
            // Fatias
            double ang=0; int idx=0;
            for (Map.Entry<String,Long> e:dados.entrySet()){
                double fatia=(double)e.getValue()/total*360.0;
                g2.setColor(CORES[idx%CORES.length]);
                g2.fillArc(cx-raio,cy-raio,raio*2,raio*2,(int)ang,(int)Math.ceil(fatia));
                ang+=fatia; idx++;
            }
            // Donut hole
            g2.setColor(BG_PANEL);
            int ri=(int)(raio*0.45);
            g2.fillOval(cx-ri,cy-ri,ri*2,ri*2);
            // Total no centro
            g2.setColor(TX_PRIM); g2.setFont(new Font("Monospaced",Font.BOLD,13));
            String tot=String.valueOf(total); FontMetrics fm=g2.getFontMetrics();
            g2.drawString(tot,cx-fm.stringWidth(tot)/2,cy+5);
            g2.setColor(TX_DIS); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
            g2.drawString("func.",cx-fm.stringWidth("func.")/2+2,cy+16);
            // Legenda
            int lx=cx+raio+18, ly=cy-dados.size()*26/2; idx=0;
            for (Map.Entry<String,Long> e:dados.entrySet()){
                Color cor=CORES[idx%CORES.length];
                // Quadrado colorido
                g2.setColor(cor); g2.fillRoundRect(lx,ly+idx*26,14,14,4,4);
                // Texto
                g2.setColor(TX_PRIM); g2.setFont(new Font("Monospaced",Font.BOLD,11));
                double pct=(double)e.getValue()/total*100;
                g2.drawString(String.format("%.0f%%",pct),lx+20,ly+idx*26+12);
                g2.setColor(TX_SEC); g2.setFont(new Font("Monospaced",Font.PLAIN,10));
                String nome=e.getKey()+" ("+e.getValue()+")";
                g2.drawString(nome,lx+20,ly+idx*26+23);
                idx++;
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // COMPONENTES UI REUTILIZÁVEIS
    // ═════════════════════════════════════════════════════════════════════

    private JLabel secLabel(String texto) {
        JLabel l=new JLabel(texto);
        l.setFont(new Font("Monospaced",Font.BOLD,10)); l.setForeground(TX_DIS);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BD_LINE),new EmptyBorder(0,0,5,0)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
        return l;
    }

    private JPanel envolve(String titulo, JComponent filho) {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BD_FOCUS),
            "  "+titulo,0,0,new Font("Monospaced",Font.PLAIN,10),TX_DIS));
        p.add(filho,BorderLayout.CENTER); return p;
    }

    private JTable tabelaEscura(Object[][] dados, String[] cols) {
        DefaultTableModel m=new DefaultTableModel(dados,cols){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        return tabelaEscura(m);
    }

    private JTable tabelaEscura(DefaultTableModel m) {
        JTable t=new JTable(m);
        t.setBackground(BG_PANEL); t.setForeground(TX_PRIM);
        t.setGridColor(BD_LINE); t.setRowHeight(23);
        t.setFont(new Font("Monospaced",Font.PLAIN,12));
        t.setSelectionBackground(BG_SEL); t.setSelectionForeground(Color.WHITE);
        t.setShowVerticalLines(false); t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(BG_HEADER); t.getTableHeader().setForeground(TX_HEAD);
        t.getTableHeader().setFont(new Font("Monospaced",Font.BOLD,11));
        t.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BD_FOCUS));
        // Renderer padrão para linhas alternadas escuras (sem branco)
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tb,Object val,
                    boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(tb,val,sel,foc,row,col);
                setBackground(sel ? BG_SEL : row%2==0 ? BG_ROW_A : BG_ROW_B);
                setForeground(sel ? Color.WHITE : TX_PRIM);
                setFont(new Font("Monospaced",Font.PLAIN,12));
                setBorder(new EmptyBorder(0,8,0,8));
                return this;
            }
        });
        return t;
    }

    // Renderer que colore a coluna de tipo com as cores dos funcionários
    private DefaultTableCellRenderer rendererTipo(JTable tabela, int colTipo) {
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tb,Object val,
                    boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(tb,val,sel,foc,row,col);
                Object tipo=(tb.getRowCount()>row&&tb.getValueAt(row,colTipo)!=null)
                    ? tb.getValueAt(row,colTipo).toString() : "";
                Color cor="PADRAO".equals(tipo)?C_PADRAO:"COMISSIONADO".equals(tipo)?C_COMISS:
                    "PRODUCAO".equals(tipo)?C_PROD:TX_PRIM;
                setForeground(sel?Color.WHITE:col==colTipo?cor:TX_PRIM);
                setBackground(sel?BG_SEL:row%2==0?BG_ROW_A:BG_ROW_B);
                setFont(new Font("Monospaced",Font.PLAIN,12));
                setBorder(new EmptyBorder(0,8,0,8));
                return this;
            }
        };
    }

    private JScrollPane scrollEscuro(JTable tabela) {
        JScrollPane sc=new JScrollPane(tabela);
        sc.setBorder(BorderFactory.createLineBorder(BD_LINE));
        sc.setBackground(BG_PANEL);
        sc.getViewport().setBackground(BG_PANEL);
        return sc;
    }

    private JButton btnCinza(String texto) {
        JButton b=new JButton(texto);
        b.setBackground(BG_CARD); b.setForeground(TX_PRIM);
        b.setFont(new Font("Monospaced",Font.PLAIN,12)); b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BD_FOCUS),new EmptyBorder(5,14,5,14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){b.setBackground(BD_FOCUS);}
            @Override public void mouseExited(MouseEvent e){b.setBackground(BG_CARD);}
        });
        return b;
    }

    private JButton btnAcao(String texto) {
        JButton b=btnCinza(texto);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        return b;
    }

    @SafeVarargs
    private <T> JComboBox<T> comboEscuro(T... itens) {
        JComboBox<T> c=new JComboBox<>(itens);
        c.setBackground(BG_CARD); c.setForeground(TX_PRIM);
        c.setFont(new Font("Monospaced",Font.PLAIN,12));
        c.setBorder(BorderFactory.createLineBorder(BD_FOCUS));
        return c;
    }

    private JTextField campoEscuro(int cols) {
        JTextField t=new JTextField(cols);
        t.setBackground(BG_CARD); t.setForeground(TX_PRIM); t.setCaretColor(TX_PRIM);
        t.setFont(new Font("Monospaced",Font.PLAIN,12));
        t.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BD_FOCUS),new EmptyBorder(3,6,3,6)));
        return t;
    }

    private JLabel lab(String texto) {
        JLabel l=new JLabel(texto);
        l.setForeground(TX_SEC); l.setFont(new Font("Monospaced",Font.PLAIN,12));
        return l;
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    // ── Utilitários de dados ──────────────────────────────────────────────
    private List<RegistroFolha> doMes(String mesAno) {
        if (mesAno==null||mesAno.isEmpty()) return Collections.emptyList();
        String[] p=mesAno.split("-"); if(p.length<2) return Collections.emptyList();
        try { int a=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]);
            return dados.stream().filter(r->r.ano==a&&r.mes==m).collect(Collectors.toList());
        } catch(Exception e){return Collections.emptyList();}
    }

    private String mesAnterior(String mesAno) {
        if (mesAno==null||mesAno.isEmpty()) return "";
        try {
            String[] p=mesAno.split("-");
            int ano=Integer.parseInt(p[0]), mes=Integer.parseInt(p[1]);
            if (mes==1) return String.format("%04d-12",ano-1);
            return String.format("%04d-%02d",ano,mes-1);
        } catch(Exception e){return "";}
    }

    private String labelMes(String m) {
        if(m==null||m.isEmpty()) return "(sem dados)";
        try { String[] p=m.split("-"); return nomeMes(Integer.parseInt(p[1]))+" de "+p[0]; }
        catch(Exception e){return m;}
    }
    private String labelMesCurto(String m) {
        try { String[] p=m.split("-"); return nomeMes(Integer.parseInt(p[1])).substring(0,3)+"/"+p[0].substring(2); }
        catch(Exception e){return m;}
    }
    private String nomeMes(int m) {
        String[] n={"","Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                    "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        return m>=1&&m<=12?n[m]:"?";
    }

    // ── Modelo de dados ───────────────────────────────────────────────────
    static class RegistroFolha {
        int matricula,mes,ano,qtdPecas;
        String nome,tipo;
        double salarioBase,vendas,percentual,valorPeca,salTotal;
    }
}