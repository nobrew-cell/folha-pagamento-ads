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

public class FuncionarioRepository {

    private static final String DATABASE  = "database.tsv";
    private static final String CABECALHO = "MATRICULA\tNOME\tTIPO\tSALARIO_BASE\tVENDAS\tPERCENTUAL\tQTD_PECA\tVALOR_PECA\tSALARIO_TOTAL\tMES\tANO";

    public void salvar(List<Funcionario> lista) {
        escreverTSV(DATABASE, lista);
    }

    public List<Funcionario> carregar() throws Exception {
        List<Funcionario> lista = new ArrayList<>();
        File arquivo = new File(DATABASE);
        if (!arquivo.exists()) return lista;

        int numeroLinha = 0;
        try (Scanner sc = new Scanner(arquivo)) {
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
                if (p.length < 11) {
                    throw new Exception("Linha " + numeroLinha + ": esperadas 11 colunas, encontradas " + p.length);
                }

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
                            vendas = Double.parseDouble(p[4]);
                            percentual = Double.parseDouble(p[5]);
                        } catch (NumberFormatException e) {
                            throw new Exception("Linha " + numeroLinha + ": vendas ou percentual inválido");
                        }
                        lista.add(new FuncionarioComissionado(nome, matricula, vendas, percentual));
                        break;
                    case "PRODUCAO":
                        int qtd;
                        double valorPeca;
                        try {
                            qtd = Integer.parseInt(p[6]);
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
            throw new Exception("ERRO AO CARREGAR " + DATABASE + "\n" + e.getMessage());
        }

        return lista;
    }

    public String resetar(List<Funcionario> lista) {
        String timestamp = timestamp();
        new File("backups").mkdirs();
        String caminhoBackup = "backups/backup_" + timestamp + ".tsv";
        escreverTSV(caminhoBackup, lista);

        try (FileWriter fw = new FileWriter(DATABASE)) {
            fw.write(CABECALHO + "\n");
        } catch (IOException e) {
            System.out.println("Erro ao resetar banco: " + e.getMessage());
        }
        return caminhoBackup;
    }

    public String[] exportar(List<Funcionario> lista) {
        new File("exportados/dados").mkdirs();
        new File("exportados/relatorios").mkdirs();
        String ts  = timestamp();
        String tsv = "exportados/dados/folha_" + ts + ".tsv";
        String xls = "exportados/relatorios/folha_" + ts + ".xls";
        boolean okTsv = escreverTSV(tsv, lista);
        boolean okXls = escreverXLS(xls, lista);
        return new String[] { okTsv ? tsv : null, okXls ? xls : null };
    }

    // ── NOVOS MÉTODOS PARA IMPORTAÇÃO, HISTÓRICO E LIMPEZA ──

    public List<Funcionario> importarDeArquivo(String caminho) throws Exception {
        List<Funcionario> lista = new ArrayList<>();
        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            throw new Exception("Arquivo nao encontrado: " + caminho);
        }

        int numeroLinha = 0;
        try (Scanner sc = new Scanner(arquivo)) {
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
                if (p.length < 11) {
                    throw new Exception("Linha " + numeroLinha + ": esperadas 11 colunas, encontradas " + p.length);
                }

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
                            vendas = Double.parseDouble(p[4]);
                            percentual = Double.parseDouble(p[5]);
                        } catch (NumberFormatException e) {
                            throw new Exception("Linha " + numeroLinha + ": vendas ou percentual inválido");
                        }
                        lista.add(new FuncionarioComissionado(nome, matricula, vendas, percentual));
                        break;
                    case "PRODUCAO":
                        int qtd;
                        double valorPeca;
                        try {
                            qtd = Integer.parseInt(p[6]);
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
            throw new Exception("ERRO AO IMPORTAR ARQUIVO\n" + e.getMessage());
        }
        return lista;
    }

    public String salvarHistorico(List<Funcionario> lista) throws IOException {
        new File("historico").mkdirs();
        String timestamp = timestamp();
        String caminho = "historico/folha_" + timestamp + ".tsv";
        boolean ok = escreverTSV(caminho, lista);
        if (!ok) throw new IOException("Falha ao salvar historico.");
        return caminho;
    }

    public void limparDados() throws IOException {
        try (FileWriter fw = new FileWriter(DATABASE)) {
            fw.write(CABECALHO + "\n");
        }
    }

    // ── MÉTODOS PRIVADOS EXISTENTES ──

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
            fw.write("<td>MATRICULA</th><th>NOME</th><th>TIPO</th><th>SALARIO_BASE</th>");
            fw.write("<th>VENDAS</th><th>PERCENTUAL</th><th>QTD_PECA</th><th>VALOR_PECA</th>");
            fw.write("<th>SALARIO_TOTAL</th><th>MES</th><th>ANO</th>");
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