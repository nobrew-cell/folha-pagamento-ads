package br.com.folha.model;

public class FuncionarioPadrao extends Funcionario {

    public FuncionarioPadrao(String nome, int matricula) {
        super(nome, matricula);
    }

    @Override public String getTipo() { return "Padrao"; }

    @Override
    public double calcularSalarioFinal(double salarioBase) {
        return salarioBase;
    }

    @Override
    public String getDetalheExtra() {
        return "Extras       : " + moeda(0);
    }

    @Override
    public String toTSV(double salarioBase) {
        return matricula + "\t" +
               nome + "\t" +
               "PADRAO\t" +
               salarioBase + "\t" +
               "0\t0\t0\t0\t" +
               calcularSalarioFinal(salarioBase) + "\t" +
               getMesAnoAtual();
    }

    @Override
    public String toXLS(double salarioBase) {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #E2EFDA;'>" +
               "<td>" + matricula        + "</td>" +
               "<td>" + nome             + "</td>" +
               "<td>PADRAO</td>"                   +
               "<td>" + salarioBase      + "</td>" +
               "<td>0</td>"                        +
               "<td>0</td>"                        +
               "<td>0</td>"                        +
               "<td>0</td>"                        +
               "<td>" + calcularSalarioFinal(salarioBase) + "</td>" +
               "<td>" + agora.getMonthValue()      + "</td>" +
               "<td>" + agora.getYear()            + "</td>" +
               "</tr>";
    }
}