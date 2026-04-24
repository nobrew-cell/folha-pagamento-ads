package br.com.folha.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sistema de logs mensais do sistema de folha de pagamento.
 *
 * Cada mês gera um arquivo separado na pasta logs/:
 *   logs/2026-04_log.txt
 *   logs/2026-05_log.txt
 *   ...
 *
 * Formato de cada linha:
 *   2026-04-24 14:35:12 | OPERACAO | detalhe1 | detalhe2
 *
 * Falhas de escrita são silenciosas — o log é coadjuvante
 * e nunca deve interromper o fluxo principal do sistema.
 */
public class LoggerUtil {

    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter FMT_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_MES =
            DateTimeFormatter.ofPattern("yyyy-MM");

    // ── API pública ───────────────────────────────────────────────────────

    /** Registra uma ação de cadastro. */
    public static void logCadastro(String tipo, String nome, int matricula) {
        log("CADASTRO", tipo + " | Nome: " + nome + " | Matricula: " + matricula);
    }

    /** Registra uma ação de edição. */
    public static void logEdicao(int matricula, String detalhes) {
        log("EDICAO", "Matricula: " + matricula + " | " + detalhes);
    }

    /** Registra uma ação de remoção. */
    public static void logRemocao(String nome, int matricula) {
        log("REMOCAO", "Nome: " + nome + " | Matricula: " + matricula);
    }

    /** Registra exportação. */
    public static void logExport(String caminhoTsv, String caminhoXls) {
        log("EXPORT", "TSV: " + caminhoTsv + " | XLS: " + caminhoXls);
    }

    /** Registra importação. */
    public static void logImport(String caminho, int qtdFuncionarios) {
        log("IMPORT", "Arquivo: " + caminho + " | Funcionarios: " + qtdFuncionarios);
    }

    /** Registra reset. */
    public static void logReset(String caminhoBackup) {
        log("RESET", "Backup: " + caminhoBackup);
    }

    /** Registra novo mês. */
    public static void logNovoMes(boolean copiouFuncionarios, String caminhoHistorico) {
        log("NOVO_MES",
            "Copiou funcionarios: " + (copiouFuncionarios ? "Sim" : "Nao") +
            " | Historico: " + caminhoHistorico);
    }

    /** Registra alteração de configuração. */
    public static void logConfig(String campo, String valorAntigo, String valorNovo) {
        log("CONFIG", campo + " | Anterior: " + valorAntigo + " | Novo: " + valorNovo);
    }

    /** Registra edição em lote. */
    public static void logEdicaoLote(String tipo, int qtdEditados, int qtdPulados) {
        log("EDICAO_LOTE",
            "Tipo: " + tipo + " | Editados: " + qtdEditados + " | Pulados: " + qtdPulados);
    }

    // ── Núcleo ────────────────────────────────────────────────────────────

    private static void log(String operacao, String detalhes) {
        try {
            new File(LOG_DIR).mkdirs();
            String mesAno   = LocalDateTime.now().format(FMT_MES);
            String arquivo  = LOG_DIR + File.separator + mesAno + "_log.txt";
            String timestamp = LocalDateTime.now().format(FMT_TIMESTAMP);
            try (FileWriter fw = new FileWriter(arquivo, true)) {
                fw.write(timestamp + " | " + operacao + " | " + detalhes + "\n");
            }
        } catch (IOException e) {
            // Falha silenciosa — log nunca deve quebrar o sistema principal
            System.err.println("[LOG] Aviso: nao foi possivel registrar a operacao. " + e.getMessage());
        }
    }
}