package br.com.folha.model;

/** Sem adicionais — recebe somente o salário base mensal. */
public class FuncionarioPadrao extends Funcionario {

    public FuncionarioPadrao(String nome, int matricula) {
        super(nome, matricula);
    }

    @Override public String getTipo()              { return "Padrao"; }
    @Override public double calcularSalarioFinal() { return SALARIO_BASE; }
    @Override public String getDetalheExtra()      { return "Extras       : " + moeda(0); }
    @Override public String toTSV()                { return "PADRAO\t" + nome + "\t" + matricula + "\t\t"; }
}
