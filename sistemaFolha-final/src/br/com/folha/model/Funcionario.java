package br.com.folha.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.text.Normalizer;

/**
 * Classe base abstrata de todos os funcionários.
 *
 * Salário base e teto de bônus FORAM REMOVIDOS daqui — eles vivem
 * em FolhaService e são persistidos no database.tsv via linha #CONFIG.
 * Use FolhaService.getSalarioBase() / getTetoBonusAbsoluto() para obter
 * os valores em tempo de execução; as subclasses recebem o salárioBase
 * como parâmetro em calcularSalarioFinal().
 */
public abstract class Funcionario {

    // ── Formatação monetária (pt-BR com separador de milhar) ──────────────
    private static final DecimalFormat DF;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("pt", "BR"));
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        DF = new DecimalFormat("R$ #,##0.00", sym);
    }

    public static String moeda(double valor) {
        return DF.format(valor);
    }

    // ── Campos imutáveis ──────────────────────────────────────────────────
    protected final String nome;
    protected final int matricula;

    public Funcionario(String nome, int matricula) {
        this.nome = nome;
        this.matricula = matricula;
    }

    public String getNome()       { return nome; }
    public int    getMatricula()  { return matricula; }

    /** Retorna o nome sem acentos — para exibição no terminal. */
    public String getNomeExibicao() {
        return removerAcentos(nome);
    }

    // ── Contrato das subclasses ───────────────────────────────────────────
    public abstract String getTipo();

    /**
     * Calcula o salário final usando o salárioBase informado.
     * Receber o valor como parâmetro elimina a dependência de estado
     * estático e permite recalcular a qualquer momento com o valor atual.
     */
    public abstract double calcularSalarioFinal(double salarioBase);

    public abstract String getDetalheExtra();
    public abstract String toTSV(double salarioBase);
    public abstract String toXLS(double salarioBase);

    // ── Utilitários de nome ───────────────────────────────────────────────

    /** Capitaliza cada palavra e mantém acentos (usado para armazenamento). */
    public static String normalizarNome(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        String[] partes = raw.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0)));
            sb.append(p.substring(1));
        }
        return sb.toString();
    }

    /** Remove acentos de um texto (ex: "João" → "Joao"). */
    public static String removerAcentos(String texto) {
        if (texto == null) return null;
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /** Normaliza para comparação: remove acentos e converte para minúsculas. */
    public static String normalizarComparacao(String texto) {
        if (texto == null) return null;
        return removerAcentos(texto).toLowerCase();
    }

    /** Retorna mês e ano atuais separados por tab (para o TSV). */
    public static String getMesAnoAtual() {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return agora.getMonthValue() + "\t" + agora.getYear();
    }
}