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

    // ── TSV com 11 colunas ──
    @Override
    public String toTSV() {
        return matricula + "\t" +
               nome + "\t" +
               "PRODUCAO\t" +
               SALARIO_BASE + "\t" +
               "0\t0\t" +
               quantidadeProduzida + "\t" +
               valorPorPeca + "\t" +
               calcularSalarioFinal() + "\t" +
               getMesAnoAtual();
    }

    // ── XLS com 11 colunas ──
    @Override
    public String toXLS() {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #EBE6F4;'>" +
               "<td>" + matricula + "</td><td>" + nome + "</td><td>PRODUCAO\n" +
               "<td>" + SALARIO_BASE + "</td><td>0\n" +
               "<td>0\n" +
               "<td>" + quantidadeProduzida + "</td>" +
               "<td>" + valorPorPeca + "</td>" +
               "<td>" + calcularSalarioFinal() + "</td>" +
               "<td>" + agora.getMonthValue() + "</td>" +
               "<td>" + agora.getYear() + "</td>" +
               "</tr>";
    }

    // ── Getters para edição ──
    public int getQuantidadeProduzida() {
        return quantidadeProduzida;
    }

    public double getValorPorPeca() {
        return valorPorPeca;
    }
}