package br.com.folha.repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import br.com.folha.model.Funcionario;
import br.com.folha.model.FuncionarioComissionado;
import br.com.folha.model.FuncionarioPadrao;
import br.com.folha.model.FuncionarioProducao;

/**
 * Responsável por ler e escrever o database.tsv.
 *
 * Formato do arquivo:
 *   Linha 1 : #CONFIG\t<salarioBase>\t<tetoBonusPercentual>
 *   Linha 2 : MATRICULA\tNOME\t...   (cabeçalho)
 *   Linhas 3+: dados dos funcionários
 *
 * Arquivos antigos (sem linha #CONFIG) são aceitos com valores padrão.
 *
 * CORREÇÃO (regressão 3.3 / 3.4):
 *   toXLS() e toTSV() de FuncionarioProducao agora recebem o salário
 *   pré-calculado com teto, garantindo que o valor exportado seja
 *   idêntico ao exibido na folha da tela.
 */
public class FuncionarioRepository {

    private static final String DATABASE  = "database.tsv";
    private static final String CABECALHO =
            "MATRICULA\tNOME\tTIPO\tSALARIO_BASE\tVENDAS\tPERCENTUAL\tQTD_PECA\tVALOR_PECA\tSALARIO_TOTAL\tMES\tANO";

    // Valores padrão usados quando o arquivo não tem linha #CONFIG
    private static final double DEFAULT_SALARIO_BASE    = 2000.00;
    private static final double DEFAULT_TETO_PERCENTUAL = 200.0;

    // Formatadores de data para nomes de arquivo
    private static final DateTimeFormatter FMT_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter FMT_MES_ANO =
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String[] MESES_PT = {
        "", "janeiro", "fevereiro", "marco", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    };

    // ── Container para o resultado do carregamento ────────────────────────

    public static class DadosCarregados {
        public final List<Funcionario> funcionarios;
        public final double salarioBase;
        public final double tetoBonusPercentual;

        public DadosCarregados(List<Funcionario> funcionarios,
                               double salarioBase, double tetoBonusPercentual) {
            this.funcionarios        = funcionarios;
            this.salarioBase         = salarioBase;
            this.tetoBonusPercentual = tetoBonusPercentual;
        }
    }

    // ── Carregamento ──────────────────────────────────────────────────────

    public DadosCarregados carregar() throws Exception {
        File arquivo = new File(DATABASE);
        if (!arquivo.exists()) {
            return new DadosCarregados(new ArrayList<>(),
                    DEFAULT_SALARIO_BASE, DEFAULT_TETO_PERCENTUAL);
        }
        return parseTSV(DATABASE);
    }

    public List<Funcionario> importarDeArquivo(String caminho) throws Exception {
        return parseTSV(caminho).funcionarios;
    }

    /**
     * Parser unificado — antes duplicado em carregar() e importarDeArquivo().
     * Lê a linha #CONFIG se presente; caso contrário usa valores padrão.
     */
    private DadosCarregados parseTSV(String caminho) throws Exception {
        List<Funcionario> lista = new ArrayList<>();
        double salarioBase    = DEFAULT_SALARIO_BASE;
        double tetoPercentual = DEFAULT_TETO_PERCENTUAL;

        int numeroLinha = 0;
        try (Scanner sc = new Scanner(new File(caminho))) {

            if (!sc.hasNextLine()) {
                throw new Exception("Arquivo vazio ou sem cabeçalho.");
            }

            // Primeira linha: pode ser #CONFIG ou cabeçalho (compatibilidade com versão antiga)
            String primeiraLinha = sc.nextLine().trim();
            numeroLinha++;

            if (primeiraLinha.startsWith("#CONFIG")) {
                String[] cfg = primeiraLinha.split("\t");
                try {
                    salarioBase    = Double.parseDouble(cfg[1]);
                    tetoPercentual = Double.parseDouble(cfg[2]);
                } catch (Exception e) {
                    // Se a linha #CONFIG estiver corrompida, usa padrão e continua
                    System.out.println("  [AVISO] Linha #CONFIG corrompida. Usando valores padrao.");
                }
                // Próxima linha deve ser o cabeçalho
                if (sc.hasNextLine()) {
                    sc.nextLine(); // pula o cabeçalho MATRICULA\tNOME\t...
                    numeroLinha++;
                } else {
                    // Arquivo só com #CONFIG (sem dados) — válido
                    return new DadosCarregados(lista, salarioBase, tetoPercentual);
                }
            }
            // else: primeiraLinha era o cabeçalho — já foi consumida, continua normalmente

            while (sc.hasNextLine()) {
                numeroLinha++;
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;

                String[] p = linha.split("\t");
                if (p.length < 11) {
                    throw new Exception("Linha " + numeroLinha +
                            ": esperadas 11 colunas, encontradas " + p.length);
                }

                int matricula;
                try {
                    matricula = Integer.parseInt(p[0]);
                    if (matricula <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new Exception("Linha " + numeroLinha +
                            ": matrícula inválida '" + p[0] + "'");
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
                            throw new Exception("Linha " + numeroLinha +
                                    ": vendas ou percentual inválido");
                        }
                        lista.add(new FuncionarioComissionado(nome, matricula, vendas, percentual));
                        break;
                    case "PRODUCAO":
                        int qtd;
                        double valorPeca;
                        try {
                            qtd       = Integer.parseInt(p[6]);
                            valorPeca = Double.parseDouble(p[7]);
                        } catch (NumberFormatException e) {
                            throw new Exception("Linha " + numeroLinha +
                                    ": quantidade ou valor por peça inválido");
                        }
                        lista.add(new FuncionarioProducao(nome, matricula, qtd, valorPeca));
                        break;
                    default:
                        throw new Exception("Linha " + numeroLinha +
                                ": tipo desconhecido '" + tipo + "'");
                }
            }
        } catch (Exception e) {
            throw new Exception("ERRO AO LER " + caminho + "\n" + e.getMessage());
        }

        return new DadosCarregados(lista, salarioBase, tetoPercentual);
    }

    // ── Persistência ──────────────────────────────────────────────────────

    /**
     * Salva a lista no database.tsv, precedida pela linha #CONFIG.
     *
     * @return true se a escrita foi bem-sucedida, false caso contrário.
     */
    public boolean salvar(List<Funcionario> lista,
                          double salarioBase, double tetoPercentual) {
        return escreverTSV(DATABASE, lista, salarioBase, tetoPercentual);
    }

    /**
     * Reseta o sistema: cria backup e limpa o database.
     * Lança IOException se não for possível limpar o arquivo (evita inconsistência).
     *
     * @return caminho do arquivo de backup gerado
     * @throws IOException se a limpeza do database falhar
     */
    public String resetar(List<Funcionario> lista,
                          double salarioBase, double tetoPercentual) throws IOException {
        String timestamp     = timestamp();
        new File("backups").mkdirs();
        String caminhoBackup = "backups/backup_" + timestamp + ".tsv";
        escreverTSV(caminhoBackup, lista, salarioBase, tetoPercentual);

        // Limpa o database — se falhar, lança exceção (não silencia o erro)
        try (FileWriter fw = new FileWriter(DATABASE)) {
            fw.write("#CONFIG\t" + salarioBase + "\t" + tetoPercentual + "\n");
            fw.write(CABECALHO + "\n");
        }
        // IOException propaga para FolhaService → ConsoleUI

        return caminhoBackup;
    }

    /**
     * Exporta TSV e XLS para a pasta exportados/.
     *
     * @return array [caminhoTSV, caminhoXLS]; null em cada posição se a escrita falhou
     */
    public String[] exportar(List<Funcionario> lista,
                             double salarioBase, double tetoPercentual) {
        new File("exportados/dados").mkdirs();
        new File("exportados/relatorios").mkdirs();
        String ts  = timestamp();
        String tsv = "exportados/dados/folha_"       + ts + ".tsv";
        String xls = "exportados/relatorios/folha_" + ts + ".xls";
        boolean okTsv = escreverTSV(tsv, lista, salarioBase, tetoPercentual);
        boolean okXls = escreverXLS(xls, lista, salarioBase, tetoPercentual);
        return new String[]{ okTsv ? tsv : null, okXls ? xls : null };
    }

    /**
     * Salva histórico do mês com nome no formato YYYY-MM_nome-do-mes.tsv
     * para facilitar ordenação e leitura humana.
     */
    public String salvarHistorico(List<Funcionario> lista,
                                  double salarioBase, double tetoPercentual) throws IOException {
        new File("historico").mkdirs();
        LocalDateTime agora = LocalDateTime.now();
        String mesAno  = agora.format(FMT_MES_ANO);
        String nomeMes = MESES_PT[agora.getMonthValue()];
        String caminho = "historico/" + mesAno + "_" + nomeMes + "_" + timestamp() + ".tsv";
        if (!escreverTSV(caminho, lista, salarioBase, tetoPercentual)) {
            throw new IOException("Falha ao salvar historico.");
        }
        return caminho;
    }

    public void limparDados(double salarioBase, double tetoPercentual) throws IOException {
        try (FileWriter fw = new FileWriter(DATABASE)) {
            fw.write("#CONFIG\t" + salarioBase + "\t" + tetoPercentual + "\n");
            fw.write(CABECALHO + "\n");
        }
    }

    // ── Métodos privados de escrita ───────────────────────────────────────

    /**
     * Escreve TSV com salário correto (com teto) para FuncionarioProducao.
     * Resolve a regressão 3.3 / 3.4 do segundo relatório.
     */
    private boolean escreverTSV(String caminho, List<Funcionario> lista,
                                double salarioBase, double tetoPercentual) {
        try (FileWriter fw = new FileWriter(caminho)) {
            fw.write("#CONFIG\t" + salarioBase + "\t" + tetoPercentual + "\n");
            fw.write(CABECALHO + "\n");
            for (Funcionario f : lista) {
                if (f instanceof FuncionarioProducao fp) {
                    double teto = salarioBase * (tetoPercentual / 100.0);
                    double salarioComTeto = fp.calcularSalarioFinal(salarioBase, teto);
                    fw.write(fp.toTSV(salarioBase, salarioComTeto) + "\n");
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

    /**
     * Escreve XLS com salário correto (com teto) para FuncionarioProducao.
     * Resolve a regressão 3.3 do segundo relatório.
     */
    private boolean escreverXLS(String caminho, List<Funcionario> lista,
                                double salarioBase, double tetoPercentual) {
        try (FileWriter fw = new FileWriter(caminho)) {
            fw.write("<html><meta charset='utf-8'><body>\n");
            fw.write("<table border='1' style='font-family: Arial; border-collapse: collapse;'>\n");

            // Cabeçalho
            fw.write("<tr style='background-color: #1F3864; color: white; font-weight: bold;'>");
            fw.write("<th>MATRICULA</th><th>NOME</th><th>TIPO</th><th>SALARIO_BASE</th>");
            fw.write("<th>VENDAS</th><th>PERCENTUAL</th><th>QTD_PECA</th><th>VALOR_PECA</th>");
            fw.write("<th>SALARIO_TOTAL</th><th>MES</th><th>ANO</th>");
            fw.write("</tr>\n");

            // Linhas de funcionários — CORREÇÃO: produção usa salário com teto
            double total = 0;
            double teto  = salarioBase * (tetoPercentual / 100.0);
            for (Funcionario f : lista) {
                if (f instanceof FuncionarioProducao fp) {
                    double salarioComTeto = fp.calcularSalarioFinal(salarioBase, teto);
                    total += salarioComTeto;
                    fw.write(fp.toXLS(salarioBase, salarioComTeto) + "\n");
                } else {
                    double salario = f.calcularSalarioFinal(salarioBase);
                    total += salario;
                    fw.write(f.toXLS(salarioBase) + "\n");
                }
            }

            // Linha de total
            fw.write("<tr style='background-color: #1F3864; color: white; font-weight: bold;'>");
            fw.write("<td colspan='8' style='text-align:right;'>TOTAL DA FOLHA</td>");
            fw.write("<td>" + Funcionario.moeda(total) + "</td>");
            fw.write("<td colspan='2'></td>");
            fw.write("</tr>\n");

            fw.write("</table></body></html>\n");
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao escrever relatorio: " + e.getMessage());
            return false;
        }
    }

    private String timestamp() {
        return LocalDateTime.now().format(FMT_TIMESTAMP);
    }
}