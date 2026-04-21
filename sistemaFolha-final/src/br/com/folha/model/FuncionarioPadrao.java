package br.com.folha.model;

/** Recebe somente o salário base mensal. */
public class FuncionarioPadrao extends Funcionario {

    public FuncionarioPadrao(String nome, int matricula) {
        super(nome, matricula);
    }

    @Override public String getTipo()              { return "Padrao"; }
    @Override public double calcularSalarioFinal() { return SALARIO_BASE; }
    @Override public String getDetalheExtra()      { return "Extras       : " + moeda(0); }

    // ── TSV com 11 colunas: matricula, nome, tipo, base, vendas, perc, qtd, valorPeca, total, mes, ano ──
    @Override
    public String toTSV() {
        return matricula + "\t" +
               nome + "\t" +
               "PADRAO\t" +
               SALARIO_BASE + "\t" +
               "0\t0\t0\t0\t" +
               calcularSalarioFinal() + "\t" +
               getMesAnoAtual();
    }

    // ── XLS com 11 colunas (mantém estilo visual atual) ────────────────────────────────────────────────
    @Override
    public String toXLS() {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #E2EFDA;'>" +
               "<td>" + matricula + "</td><td>" + nome + "</td><td>PADRAO\n" +
               "<td>" + SALARIO_BASE + "</td><td>0\n" +
               "<td>0\n" +
               "<td>0\n" +
               "<td>0\n" +
               "<td>" + calcularSalarioFinal() + "</td>" +
               "<td>" + agora.getMonthValue() + "</td>" +
               "<td>" + agora.getYear() + "</td>" +
               "</tr>";
    }
}