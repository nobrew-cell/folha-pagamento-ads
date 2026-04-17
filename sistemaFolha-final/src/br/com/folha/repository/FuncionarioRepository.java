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
 *   database.tsv   → estado atual (carregado ao abrir, salvo ao fechar/resetar)
 *   exportados/    → cópias com timestamp geradas pela opção 5
 *   backups/       → backup automático gerado ANTES de qualquer reset
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
     * Exporta um TSV imutável com timestamp em exportados/.
     * Retorna o caminho do arquivo ou null em caso de falha.
     */
    public String exportar(List<Funcionario> lista) {
        new File("exportados").mkdirs();
        String caminho = "exportados/folha_" + timestamp() + ".tsv";
        return escreverTSV(caminho, lista) ? caminho : null;
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

    private String timestamp() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }
}
