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

    // ── TSV com 11 colunas ──
    @Override
    public String toTSV() {
        return matricula + "\t" +
               nome + "\t" +
               "COMISSIONADO\t" +
               SALARIO_BASE + "\t" +
               vendas + "\t" +
               percentualComissao + "\t" +
               "0\t0\t" +
               calcularSalarioFinal() + "\t" +
               getMesAnoAtual();
    }

    // ── XLS com 11 colunas ──
    @Override
    public String toXLS() {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #FCE4D6;'>" +
               "<td>" + matricula + "</td><td>" + nome + "</td><td>COMISSIONADO\n" +
               "<td>" + SALARIO_BASE + "</td><td>" + vendas + "</td>" +
               "<td>" + percentualComissao + "</td><td>0\n" +
               "<td>0\n" +
               "<td>" + calcularSalarioFinal() + "</td>" +
               "<td>" + agora.getMonthValue() + "</td>" +
               "<td>" + agora.getYear() + "</td>" +
               "</tr>";
    }
}