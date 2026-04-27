package br.com.folha.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sistema de logs mensais do sistema de folha de pagamento.
 *
 * Cada mês gera um arquivo separado na pasta logs/:
 *   logs/2026-04_log.log
 *   logs/2026-05_log.log
 *   ...
 *
 * Formato de cada linha:
 *   2026-04-24 14:35:12 | OPERACAO | detalhe1 | detalhe2
 *
 * Falhas de escrita são silenciosas — o log é coadjuvante
 * e nunca deve interromper o fluxo principal do sistema.
 *
 * CORREÇÃO (charset explícito):
 *   FileWriter(arquivo, true) foi substituído por
 *   OutputStreamWriter(FileOutputStream, UTF-8) com append=true.
 *   Antes, o log era gravado com o defaultCharset da JVM, que em
 *   Windows pode ser windows-1252 — corrompendo nomes com acentos
 *   registrados nas entradas de log (ex: "Nome: João" → "Nome: Jo?o").
 *
 * ADIÇÕES nesta versão:
 *   - logInicializacao(): registra início de sessão com versão do sistema
 *   - logEncerramento(): registra fim de sessão com total da folha
 *   - logErro(): registra erros críticos capturados pelo sistema
 *   - logImportBackup(): registra separadamente o backup criado antes de
 *     uma importação (complementa logImport com o caminho do backup)
 */
public class LoggerUtil {

    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter FMT_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_MES =
            DateTimeFormatter.ofPattern("yyyy-MM");

    // ── API pública ───────────────────────────────────────────────────────

    /** Registra o início de uma sessão do sistema. */
    public static void logInicializacao(String versao, int totalFuncionarios) {
        log("INICIO_SESSAO",
            "Versao: " + versao + " | Funcionarios carregados: " + totalFuncionarios);
    }

    /** Registra o encerramento de uma sessão com o total da folha. */
    public static void logEncerramento(double totalFolha, boolean salvoComSucesso) {
        log("FIM_SESSAO",
            "Total da folha: R$ " + String.format("%.2f", totalFolha) +
            " | Salvo: " + (salvoComSucesso ? "Sim" : "NAO - VERIFICAR"));
    }

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

    /**
     * Registra o backup automático criado antes de uma importação.
     * Chamado separadamente de logImport para registrar o caminho do backup.
     */
    public static void logImportBackup(String caminhoBackup) {
        log("IMPORT_BACKUP", "Backup anterior: " + caminhoBackup);
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

    /**
     * Registra um erro não-fatal capturado pelo sistema.
     * Útil para operações que falham silenciosamente (ex: falha ao criar backup
     * durante importação quando o usuário opta por continuar mesmo assim).
     */
    public static void logErro(String operacao, String mensagem) {
        log("ERRO", "Operacao: " + operacao + " | " + mensagem);
    }

    // ── Núcleo ────────────────────────────────────────────────────────────

    private static void log(String operacao, String detalhes) {
        try {
            new File(LOG_DIR).mkdirs();
            String mesAno  = LocalDateTime.now().format(FMT_MES);
            String arquivo = LOG_DIR + File.separator + mesAno + "_log.log";
            String timestamp = LocalDateTime.now().format(FMT_TIMESTAMP);

            // CORREÇÃO: OutputStreamWriter com UTF-8 explícito + append=true
            // Antes: new FileWriter(arquivo, true) — usava defaultCharset (Windows: CP1252)
            // Agora: charset UTF-8 garantido em qualquer ambiente
            try (Writer fw = new OutputStreamWriter(
                    new FileOutputStream(arquivo, true), StandardCharsets.UTF_8)) {
                fw.write(timestamp + " | " + operacao + " | " + detalhes + "\n");
            }
        } catch (IOException e) {
            // Falha silenciosa — log nunca deve quebrar o sistema principal
            System.err.println("[LOG] Aviso: nao foi possivel registrar a operacao. " + e.getMessage());
        }
    }
}

