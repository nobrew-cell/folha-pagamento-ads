package br.com.folha.model;

import java.util.Locale;

/**
 * Classe base abstrata.
 * Para adicionar um novo tipo: estenda esta classe, implemente os quatro
 * métodos abstratos e adicione o case correspondente em FuncionarioRepository.
 */
public abstract class Funcionario {

    /** Salário fixo mensal comum a todos os tipos. Altere somente aqui. */
    public static final double SALARIO_BASE = 2000.00;

    protected final String nome;
    protected final int    matricula;

    public Funcionario(String nome, int matricula) {
        this.nome      = nome;
        this.matricula = matricula;
    }

    public String getNome()   { return nome; }
    public int getMatricula() { return matricula; }

    public abstract String getTipo();
    public abstract double calcularSalarioFinal();
    public abstract String getDetalheExtra();
    public abstract String toTSV();

    // ── formatação compartilhada ────────────────────────────────────────────
    public static String moeda(double valor) {
        return String.format(Locale.US, "R$ %.2f", valor).replace(".", ",");
    }

    /**
     * Normaliza um nome: capitaliza a primeira letra de cada palavra,
     * o restante em minúsculo. Ex: "JOSE DA SILVA" → "Jose Da Silva".
     */
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
}
