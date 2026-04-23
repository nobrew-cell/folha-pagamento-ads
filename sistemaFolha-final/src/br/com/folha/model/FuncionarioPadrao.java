package br.com.folha.model;

public class FuncionarioPadrao extends Funcionario {

    public FuncionarioPadrao(String nome, int matricula) {
        super(nome, matricula);
    }

    @Override public String getTipo()              { return "Padrao"; }
    @Override public double calcularSalarioFinal() { return getSalarioBase(); }
    @Override public String getDetalheExtra()      { return "Extras       : " + moeda(0); }

    @Override
    public String toTSV() {
        return matricula + "\t" +
               nome + "\t" +
               "PADRAO\t" +
               getSalarioBase() + "\t" +
               "0\t0\t0\t0\t" +
               calcularSalarioFinal() + "\t" +
               getMesAnoAtual();
    }

    @Override
    public String toXLS() {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #E2EFDA;'>" +
               "<td>" + matricula + "</td><td>" + nome + "</td><td>PADRAO\n" +
               "<td>" + getSalarioBase() + "</td><td>0\n" +
               "<td>0\n" +
               "<td>0\n" +
               "<td>0\n" +
               "<td>" + calcularSalarioFinal() + "</td>" +
               "<td>" + agora.getMonthValue() + "</td>" +
               "<td>" + agora.getYear() + "</td>" +
               "</tr>";
    }
}