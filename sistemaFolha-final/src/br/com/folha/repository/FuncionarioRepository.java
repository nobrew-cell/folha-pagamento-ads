package br.com.folha.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import br.com.folha.model.Funcionario;
import br.com.folha.model.FuncionarioComissionado;
import br.com.folha.model.FuncionarioPadrao;
import br.com.folha.model.FuncionarioProducao;

/**
 * Responsável por ler e escrever o database.tsv.
 *
 * Formato da linha #CONFIG (v8.1):
 *   #CONFIG\t<salarioBase>\t<tetoBonusPercentual>\t<limiteMatricula>\t<modoRigido>
 *
 * Compatibilidade retroativa: arquivos com menos campos na #CONFIG usam defaults.
 */
public class FuncionarioRepository {

    private static final String DATABASE  = "database.tsv";
    private static final String CABECALHO =
            "MATRICULA\tNOME\tTIPO\tSALARIO_BASE\tVENDAS\tPERCENTUAL\tQTD_PECA\tVALOR_PECA\tSALARIO_TOTAL\tMES\tANO";

    private static final double  DEFAULT_SALARIO_BASE    = 2000.00;
    private static final double  DEFAULT_TETO_PERCENTUAL = 200.0;
    private static final int     DEFAULT_LIMITE_MATRICULA = 0;    // 0 = sem limite
    private static final boolean DEFAULT_MODO_RIGIDO      = false; // flexível

    private static final DateTimeFormatter FMT_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter FMT_MES_ANO =
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String[] MESES_PT = {
        "", "janeiro", "fevereiro", "marco", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    };

    // ── Container de carregamento ─────────────────────────────────────────
    public static class DadosCarregados {
        public final List<Funcionario> funcionarios;
        public final double  salarioBase;
        public final double  tetoBonusPercentual;
        public final int     limiteMaximoMatricula;
        public final boolean modoSequenciaRigido;

        public DadosCarregados(List<Funcionario> funcionarios,
                               double salarioBase, double tetoBonusPercentual,
                               int limiteMaximoMatricula, boolean modoSequenciaRigido) {
            this.funcionarios           = funcionarios;
            this.salarioBase            = salarioBase;
            this.tetoBonusPercentual    = tetoBonusPercentual;
            this.limiteMaximoMatricula  = limiteMaximoMatricula;
            this.modoSequenciaRigido    = modoSequenciaRigido;
        }
    }

    // ── Carregamento ──────────────────────────────────────────────────────
    public DadosCarregados carregar() throws Exception {
        File arquivo = new File(DATABASE);
        if (!arquivo.exists()) {
            return new DadosCarregados(new ArrayList<>(),
                    DEFAULT_SALARIO_BASE, DEFAULT_TETO_PERCENTUAL,
                    DEFAULT_LIMITE_MATRICULA, DEFAULT_MODO_RIGIDO);
        }
        return parseTSV(DATABASE);
    }

    public List<Funcionario> importarDeArquivo(String caminho) throws Exception {
        return parseTSV(caminho).funcionarios;
    }

    private DadosCarregados parseTSV(String caminho) throws Exception {
        List<Funcionario> lista = new ArrayList<>();
        double  salarioBase    = DEFAULT_SALARIO_BASE;
        double  tetoPercentual = DEFAULT_TETO_PERCENTUAL;
        int     limiteMatric   = DEFAULT_LIMITE_MATRICULA;
        boolean modoRigido     = DEFAULT_MODO_RIGIDO;

        int numeroLinha = 0;
        try (Scanner sc = new Scanner(new File(caminho), StandardCharsets.UTF_8)) {

            if (!sc.hasNextLine()) throw new Exception("Arquivo vazio ou sem cabeçalho.");

            String primeiraLinha = sc.nextLine().trim();
            numeroLinha++;

            if (primeiraLinha.startsWith("#CONFIG")) {
                String[] cfg = primeiraLinha.split("\t");
                try {
                    // cfg[0]="#CONFIG" cfg[1]=salBase cfg[2]=teto cfg[3]=limite cfg[4]=rigido
                    if (cfg.length > 1) salarioBase    = Double.parseDouble(cfg[1]);
                    if (cfg.length > 2) tetoPercentual = Double.parseDouble(cfg[2]);
                    if (cfg.length > 3) limiteMatric   = Integer.parseInt(cfg[3]);
                    if (cfg.length > 4) modoRigido     = Boolean.parseBoolean(cfg[4]);
                } catch (Exception e) {
                    System.out.println("  [AVISO] Linha #CONFIG corrompida. Usando valores padrao.");
                }
                if (sc.hasNextLine()) {
                    sc.nextLine(); // pula cabeçalho
                    numeroLinha++;
                } else {
                    return new DadosCarregados(lista, salarioBase, tetoPercentual, limiteMatric, modoRigido);
                }
            }

            while (sc.hasNextLine()) {
                numeroLinha++;
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;

                String[] p = linha.split("\t");
                if (p.length < 11) throw new Exception("Linha " + numeroLinha +
                        ": esperadas 11 colunas, encontradas " + p.length);

                int matricula;
                try {
                    matricula = Integer.parseInt(p[0]);
                    if (matricula <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new Exception("Linha " + numeroLinha + ": matrícula inválida '" + p[0] + "'");
                }

                String nome = p[1];
                String tipo = p[2];

                switch (tipo) {
                    case "PADRAO":
                        lista.add(new FuncionarioPadrao(nome, matricula));
                        break;
                    case "COMISSIONADO":
                        double vendas, percentual;
                        try {
                            vendas     = Double.parseDouble(p[4]);
                            percentual = Double.parseDouble(p[5]);
                        } catch (NumberFormatException e) {
                            throw new Exception("Linha " + numeroLinha + ": vendas ou percentual inválido");
                        }
                        lista.add(new FuncionarioComissionado(nome, matricula, vendas, percentual));
                        break;
                    case "PRODUCAO":
                        int qtd; double valorPeca;
                        try {
                            qtd       = Integer.parseInt(p[6]);
                            valorPeca = Double.parseDouble(p[7]);
                        } catch (NumberFormatException e) {
                            throw new Exception("Linha " + numeroLinha + ": quantidade ou valor por peça inválido");
                        }
                        lista.add(new FuncionarioProducao(nome, matricula, qtd, valorPeca));
                        break;
                    default:
                        throw new Exception("Linha " + numeroLinha + ": tipo desconhecido '" + tipo + "'");
                }
            }
        } catch (Exception e) {
            throw new Exception("ERRO AO LER " + caminho + "\n" + e.getMessage());
        }
        return new DadosCarregados(lista, salarioBase, tetoPercentual, limiteMatric, modoRigido);
    }

    // ── Persistência ──────────────────────────────────────────────────────
    public boolean salvar(List<Funcionario> lista, double salarioBase, double tetoPercentual,
                          int limiteMatric, boolean modoRigido) {
        return escreverTSV(DATABASE, lista, salarioBase, tetoPercentual, limiteMatric, modoRigido);
    }

    public String criarBackup(String prefixo, List<Funcionario> lista, double salarioBase,
                              double tetoPercentual, int limiteMatric, boolean modoRigido) throws IOException {
        new File("backups").mkdirs();
        String caminho = "backups/backup_" + prefixo + "_" + timestamp() + ".tsv";
        if (!escreverTSV(caminho, lista, salarioBase, tetoPercentual, limiteMatric, modoRigido))
            throw new IOException("Falha ao criar backup: " + caminho);
        return caminho;
    }

    public String resetar(List<Funcionario> lista, double salarioBase, double tetoPercentual,
                          int limiteMatric, boolean modoRigido) throws IOException {
        new File("backups").mkdirs();
        String caminhoBackup = "backups/backup_" + timestamp() + ".tsv";
        escreverTSV(caminhoBackup, lista, salarioBase, tetoPercentual, limiteMatric, modoRigido);
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(DATABASE), StandardCharsets.UTF_8)) {
            fw.write(configLine(salarioBase, tetoPercentual, limiteMatric, modoRigido));
            fw.write(CABECALHO + "\n");
        }
        return caminhoBackup;
    }

    public String[] exportar(List<Funcionario> lista, double salarioBase, double tetoPercentual,
                             int limiteMatric, boolean modoRigido) {
        new File("exportados/dados").mkdirs();
        new File("exportados/relatorios").mkdirs();
        String ts  = timestamp();
        String tsv = "exportados/dados/folha_"       + ts + ".tsv";
        String xls = "exportados/relatorios/folha_" + ts + ".xls";
        boolean okTsv = escreverTSV(tsv, lista, salarioBase, tetoPercentual, limiteMatric, modoRigido);
        boolean okXls = escreverXLS(xls, lista, salarioBase, tetoPercentual);
        return new String[]{ okTsv ? tsv : null, okXls ? xls : null };
    }

    public String salvarHistorico(List<Funcionario> lista, double salarioBase,
                                  double tetoPercentual, int limiteMatric, boolean modoRigido) throws IOException {
        new File("historico").mkdirs();
        LocalDateTime agora = LocalDateTime.now();
        String mesAno  = agora.format(FMT_MES_ANO);
        String nomeMes = MESES_PT[agora.getMonthValue()];
        String caminho = "historico/" + mesAno + "_" + nomeMes + "_" + timestamp() + ".tsv";
        if (!escreverTSV(caminho, lista, salarioBase, tetoPercentual, limiteMatric, modoRigido))
            throw new IOException("Falha ao salvar historico.");
        return caminho;
    }

    public void limparDados(double salarioBase, double tetoPercentual,
                            int limiteMatric, boolean modoRigido) throws IOException {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(DATABASE), StandardCharsets.UTF_8)) {
            fw.write(configLine(salarioBase, tetoPercentual, limiteMatric, modoRigido));
            fw.write(CABECALHO + "\n");
        }
    }

    // ── Escrita privada ───────────────────────────────────────────────────
    private boolean escreverTSV(String caminho, List<Funcionario> lista, double salarioBase,
                                double tetoPercentual, int limiteMatric, boolean modoRigido) {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(caminho), StandardCharsets.UTF_8)) {
            fw.write(configLine(salarioBase, tetoPercentual, limiteMatric, modoRigido));
            fw.write(CABECALHO + "\n");
            double teto = salarioBase * (tetoPercentual / 100.0);
            for (Funcionario f : lista) {
                if (f instanceof FuncionarioProducao fp) {
                    double salComTeto = fp.calcularSalarioFinal(salarioBase, teto);
                    fw.write(fp.toTSV(salarioBase, salComTeto) + "\n");
                } else {
                    fw.write(f.toTSV(salarioBase) + "\n");
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao escrever arquivo: " + e.getMessage());
            return false;
        }
    }

    private boolean escreverXLS(String caminho, List<Funcionario> lista,
                                double salarioBase, double tetoPercentual) {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(caminho), StandardCharsets.UTF_8)) {
            fw.write("<html><meta charset='utf-8'><body>\n");
            fw.write("<table border='1' style='font-family: Arial; border-collapse: collapse;'>\n");
            fw.write("<tr style='background-color: #1F3864; color: white; font-weight: bold;'>");
            fw.write("<th>MATRICULA</th><th>NOME</th><th>TIPO</th><th>SALARIO_BASE</th>");
            fw.write("<th>VENDAS</th><th>PERCENTUAL</th><th>QTD_PECA</th><th>VALOR_PECA</th>");
            fw.write("<th>SALARIO_TOTAL</th><th>MES</th><th>ANO</th>");
            fw.write("</tr>\n");
            double total = 0, teto = salarioBase * (tetoPercentual / 100.0);
            for (Funcionario f : lista) {
                if (f instanceof FuncionarioProducao fp) {
                    double sal = fp.calcularSalarioFinal(salarioBase, teto);
                    total += sal;
                    fw.write(fp.toXLS(salarioBase, sal) + "\n");
                } else {
                    double sal = f.calcularSalarioFinal(salarioBase);
                    total += sal;
                    fw.write(f.toXLS(salarioBase) + "\n");
                }
            }
            fw.write("<tr style='background-color: #1F3864; color: white; font-weight: bold;'>");
            fw.write("<td colspan='8' style='text-align:right;'>TOTAL DA FOLHA</td>");
            fw.write("<td>" + Funcionario.moeda(total) + "</td><td colspan='2'></td>");
            fw.write("</tr>\n</table></body></html>\n");
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao escrever relatorio: " + e.getMessage());
            return false;
        }
    }

    private String configLine(double salBase, double teto, int limite, boolean rigido) {
        return "#CONFIG\t" + salBase + "\t" + teto + "\t" + limite + "\t" + rigido + "\n";
    }

    private String timestamp() { return LocalDateTime.now().format(FMT_TIMESTAMP); }
}

