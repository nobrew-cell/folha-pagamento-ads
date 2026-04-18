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

    public List<Funcionario> carregar() {
        List<Funcionario> lista = new ArrayList<>();
        File arquivo = new File(DATABASE);
        if (!arquivo.exists()) return lista;

        try (Scanner sc = new Scanner(arquivo)) {
            if (sc.hasNextLine()) sc.nextLine(); // pula cabeçalho

            while (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;
                String[] p = linha.split("\t");

                switch (p[0]) {
                    case "PADRAO" -> {
                        if (p.length >= 3)
                            lista.add(new FuncionarioPadrao(p[1], Integer.parseInt(p[2])));
                    }
                    case "COMISSIONADO" -> {
                        if (p.length >= 5)
                            lista.add(new FuncionarioComissionado(
                                    p[1], Integer.parseInt(p[2]),
                                    Double.parseDouble(p[3]), Double.parseDouble(p[4])));
                    }
                    case "PRODUCAO" -> {
                        if (p.length >= 5)
                            lista.add(new FuncionarioProducao(
                                    p[1], Integer.parseInt(p[2]),
                                    Integer.parseInt(p[3]), Double.parseDouble(p[4])));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Aviso: erro ao carregar dados. " + e.getMessage());
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
