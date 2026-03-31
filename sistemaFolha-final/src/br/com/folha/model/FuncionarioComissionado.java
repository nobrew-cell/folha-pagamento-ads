package br.com.folha.model;

/** Comissão calculada sobre o total de vendas mensais. */
public class FuncionarioComissionado extends Funcionario {

    private final double vendas;
    private final double percentualComissao; // %

    public FuncionarioComissionado(String nome, int matricula,
                                   double vendas, double percentualComissao) {
        super(nome, matricula);
        this.vendas             = vendas;
        this.percentualComissao = percentualComissao;
    }

    /** Comissão = vendas × percentual / 100 */
    public double calcularComissao() {
        return (vendas * percentualComissao) / 100.0;
    }

    @Override public String getTipo()              { return "Comissionado"; }
    @Override public double calcularSalarioFinal() { return SALARIO_BASE + calcularComissao(); }

    @Override
    public String getDetalheExtra() {
        return String.format("Comissao     : %s  (%.1f%% sobre vendas de %s)",
                moeda(calcularComissao()), percentualComissao, moeda(vendas));
    }

    @Override
    public String toCSV() {
        return "COMISSIONADO;" + nome + ";" + matricula + ";" + vendas + ";" + percentualComissao;
    }
}
