package br.com.folha.model;

/**
 * Armazena as configurações globais do sistema (salário base, teto de bônus etc.)
 * Os valores são persistidos em config.properties.
 */
public class Configuracao {
    private static double salarioBase = 2000.00;
    private static double tetoBonusPercentual = 200.0; // 200% do salário base

    public static double getSalarioBase() {
        return salarioBase;
    }

    public static void setSalarioBase(double novoSalario) {
        salarioBase = novoSalario;
    }

    public static double getTetoBonusPercentual() {
        return tetoBonusPercentual;
    }

    public static void setTetoBonusPercentual(double novoPercentual) {
        tetoBonusPercentual = novoPercentual;
    }

    // Retorna o teto em valor absoluto (R$)
    public static double getTetoBonusAbsoluto() {
        return salarioBase * (tetoBonusPercentual / 100.0);
    }
}