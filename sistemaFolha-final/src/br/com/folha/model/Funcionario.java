package br.com.folha.model;

import java.util.Locale;

public abstract class Funcionario {

    // Agora mutável, via configuração
    private static double salarioBase = 2000.00;

    public static double getSalarioBase() {
        return salarioBase;
    }

    public static void setSalarioBase(double novoSalario) {
        salarioBase = novoSalario;
    }

    protected final String nome;
    protected final int matricula;

    public Funcionario(String nome, int matricula) {
        this.nome = nome;
        this.matricula = matricula;
    }

    public String getNome()   { return nome; }
    public int getMatricula() { return matricula; }

    public abstract String getTipo();
    public abstract double calcularSalarioFinal();
    public abstract String getDetalheExtra();
    public abstract String toTSV();
    public abstract String toXLS();

    public static String moeda(double valor) {
        return String.format(Locale.US, "R$ %.2f", valor).replace(".", ",");
    }

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

    public static String getMesAnoAtual() {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return agora.getMonthValue() + "\t" + agora.getYear();
    }
}