package br.com.folha.repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import br.com.folha.model.Funcionario;
import br.com.folha.model.FuncionarioComissionado;
import br.com.folha.model.FuncionarioPadrao;
import br.com.folha.model.FuncionarioProducao;

/**
 * Cuida de toda a persistência.
 *
 *   database.tsv          → estado atual (carregado ao abrir, salvo ao fechar/resetar)
 *   exportados/dados/     → TSV com timestamp gerado pela opcao 5
 *   exportados/relatorios/→ XLS com timestamp gerado pela opcao 5
 *   backups/              → backup automatico gerado ANTES de qualquer reset
 */

public class FuncionarioRepository {

    private static final String DATABASE  = "database.tsv";
    private static final String CABECALHO = "tipo\tnome\tmatricula\tcampo1\tcampo2";

    // ── Banco fixo ───────────────────────────────────────────────────────────

    public void salvar(List<Funcionario> lista) {
        escreverTSV(DATABASE, lista);
    }

    /**
     * Carrega a lista do arquivo database.tsv.
     * Se o arquivo não existir, retorna lista vazia (primeira execução).
     * Se existir e estiver corrompido, lança Exception com detalhes da linha.
     */
    public List<Funcionario> carregar() throws Exception {
        List<Funcionario> lista = new ArrayList<>();
        File arquivo = new File(DATABASE);
        if (!arquivo.exists()) return lista;

        int numeroLinha = 0;
        try (Scanner sc = new Scanner(arquivo)) {
            // cabeçalho
            if (sc.hasNextLine()) {
                sc.nextLine();
                numeroLinha++;
            } else {
                throw new Exception("Arquivo vazio ou sem cabeçalho.");
            }

            while (sc.hasNextLine()) {
                numeroLinha++;
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;

                String[] p = linha.split("\t");

                // valida colunas mínimas (tipo, nome, matricula)
                if (p.length < 3) {
                    throw new Exception("Linha " + numeroLinha + ": poucas colunas (mínimo 3)");
                }

                String tipo = p[0];
                String nome = p[1];
                String matriculaStr = p[2];

                // valida matrícula
                int matricula;
                try {
                    matricula = Integer.parseInt(matriculaStr);
                    if (matricula <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new Exception("Linha " + numeroLinha + ": matrícula inválida ('" + matriculaStr + "')");
                }

                switch (tipo) {
                    case "PADRAO":
                        // Padrão precisa de pelo menos 3 colunas (já validado)
                        lista.add(new FuncionarioPadrao(nome, matricula));
                        break;
                    case "COMISSIONADO":
                        if (p.length < 5) {
                            throw new Exception("Linha " + numeroLinha + ": COMISSIONADO precisa de 5 colunas");
                        }
                        double vendas, percentual;
                        try {
                            vendas = Double.parseDouble(p[3]);
                            percentual = Double.parseDouble(p[4]);
                        } catch (NumberFormatException e) {
                            throw new Exception("Linha " + numeroLinha + ": vendas ou percentual inválido");
                        }
                        lista.add(new FuncionarioComissionado(nome, matricula, vendas, percentual));
                        break;
                    case "PRODUCAO":
                        if (p.length < 5) {
                            throw new Exception("Linha " + numeroLinha + ": PRODUCAO precisa de 5 colunas");
                        }
                        int qtd;
                        double valorPeca;
                        try {
                            qtd = Integer.parseInt(p[3]);
                            valorPeca = Double.parseDouble(p[4]);
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
            // relança com contexto do arquivo
            throw new Exception("ERRO AO CARREGAR " + DATABASE + "\n" + e.getMessage());
        }

        return lista;
    }

    /**
     * Antes de resetar, salva um backup em backups/backup_<timestamp>.tsv.
     * Depois limpa o banco principal.
     * Retorna o caminho do backup gerado.
     */
    public String resetar(List<Funcionario> lista) {
        String timestamp = timestamp();
        new File("backups").mkdirs();
        String caminhoBackup = "backups/backup_" + timestamp + ".tsv";
        escreverTSV(caminhoBackup, lista);

        // limpa banco principal
        try (FileWriter fw = new FileWriter(DATABASE)) {
            fw.write(CABECALHO + "\n");
        } catch (IOException e) {
            System.out.println("Erro ao resetar banco: " + e.getMessage());
        }

        return caminhoBackup;
    }

    // ── Exportação ───────────────────────────────────────────────────────────

    /**
     * Exporta dois arquivos com timestamp:
     *   exportados/dados/       → TSV (leitura por maquina e Power BI)
     *   exportados/relatorios/  → XLS (visualizacao humana no Excel)
     * Retorna array com os dois caminhos, ou null em cada posicao em caso de falha.
     */
    public String[] exportar(List<Funcionario> lista) {
        new File("exportados/dados").mkdirs();
        new File("exportados/relatorios").mkdirs();
        String ts  = timestamp();
        String tsv = "exportados/dados/folha_"       + ts + ".tsv";
        String xls = "exportados/relatorios/folha_"  + ts + ".xls";
        boolean okTsv = escreverTSV(tsv, lista);
        boolean okXls = escreverXLS(xls, lista);
        return new String[] {
            okTsv ? tsv : null,
            okXls ? xls : null
        };
    }

    // ── Utilitários internos ─────────────────────────────────────────────────

    private boolean escreverTSV(String caminho, List<Funcionario> lista) {
        try (FileWriter fw = new FileWriter(caminho)) {
            fw.write(CABECALHO + "\n");
            for (Funcionario f : lista) {
                fw.write(f.toTSV() + "\n");
            }
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao escrever arquivo: " + e.getMessage());
            return false;
        }
    }
    private boolean escreverXLS(String caminho, List<Funcionario> lista) {
        try (FileWriter fw = new FileWriter(caminho)) {
            fw.write("<html><meta charset='utf-8'><body>");
            fw.write("<table border='1' style='font-family: Arial; border-collapse: collapse;'>");
            fw.write("<tr style='background-color: #1F3864; color: white; font-weight: bold;'>");
            fw.write("<td>TIPO</td><td>NOME</td><td>MATRICULA</td><td>CAMPO1</td><td>CAMPO2</td>");
            fw.write("</tr>");
            for (Funcionario f : lista) {
                fw.write(f.toXLS() + "\n");
            }
            fw.write("</table></body></html>");
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao escrever relatorio: " + e.getMessage());
            return false;
        }
    }

    private String timestamp() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }
}