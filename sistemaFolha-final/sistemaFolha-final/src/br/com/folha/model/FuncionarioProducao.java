package br.com.folha.model;

/** Bônus por produtividade mensal: quantidade de peças × valor unitário. */
public class FuncionarioProducao extends Funcionario {

    private final int    quantidadeProduzida;
    private final double valorPorPeca;

    public FuncionarioProducao(String nome, int matricula,
                               int quantidadeProduzida, double valorPorPeca) {
        super(nome, matricula);
        this.quantidadeProduzida = quantidadeProduzida;
        this.valorPorPeca        = valorPorPeca;
    }

    /** Bonus = quantidade × valor por peca */
    public double calcularBonus() {
        return quantidadeProduzida * valorPorPeca;
    }

    @Override public String getTipo()              { return "Producao"; }
    @Override public double calcularSalarioFinal() { return SALARIO_BASE + calcularBonus(); }

    @Override
    public String getDetalheExtra() {
        return String.format("Bonus prod.  : %s  (%d pecas x %s)",
                moeda(calcularBonus()), quantidadeProduzida, moeda(valorPorPeca));
    }

    @Override
    public String toCSV() {
        return "PRODUCAO;" + nome + ";" + matricula + ";" + quantidadeProduzida + ";" + valorPorPeca;
    }
}
