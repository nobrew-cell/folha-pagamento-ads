package br.com.folha.model;

public class FuncionarioComissionado extends Funcionario {

    private final double vendas;
    private final double percentualComissao;

    public FuncionarioComissionado(String nome, int matricula,
                                   double vendas, double percentualComissao) {
        super(nome, matricula);
        this.vendas = vendas;
        this.percentualComissao = percentualComissao;
    }

    public double calcularComissao() {
        return (vendas * percentualComissao) / 100.0;
    }

    @Override public String getTipo() { return "Comissionado"; }

    @Override
    public double calcularSalarioFinal(double salarioBase) {
        return salarioBase + calcularComissao();
    }

    @Override
    public String getDetalheExtra() {
        return String.format("Comissao     : %s  \n  (%.1f%% sobre vendas de %s)",
                moeda(calcularComissao()), percentualComissao, moeda(vendas));
    }

    @Override
    public String toTSV(double salarioBase) {
        return matricula + "\t" +
               nome + "\t" +
               "COMISSIONADO\t" +
               salarioBase + "\t" +
               vendas + "\t" +
               percentualComissao + "\t" +
               "0\t0\t" +
               calcularSalarioFinal(salarioBase) + "\t" +
               getMesAnoAtual();
    }

    @Override
    public String toXLS(double salarioBase) {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #FCE4D6;'>" +
               "<td>" + matricula                              + "</td>" +
               "<td>" + nome                                   + "</td>" +
               "<td>COMISSIONADO</td>"                                   +
               "<td>" + salarioBase                            + "</td>" +
               "<td>" + vendas                                 + "</td>" +
               "<td>" + percentualComissao                     + "</td>" +
               "<td>0</td>"                                              +
               "<td>0</td>"                                              +
               "<td>" + calcularSalarioFinal(salarioBase)      + "</td>" +
               "<td>" + agora.getMonthValue()                  + "</td>" +
               "<td>" + agora.getYear()                        + "</td>" +
               "</tr>";
    }

    public double getVendas()              { return vendas; }
    public double getPercentualComissao()  { return percentualComissao; }
}