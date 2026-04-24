package br.com.folha.model;

public class FuncionarioProducao extends Funcionario {

    private final int    quantidadeProduzida;
    private final double valorPorPeca;

    public FuncionarioProducao(String nome, int matricula,
                               int quantidadeProduzida, double valorPorPeca) {
        super(nome, matricula);
        this.quantidadeProduzida = quantidadeProduzida;
        this.valorPorPeca        = valorPorPeca;
    }

    /** Bônus bruto sem aplicar teto. Útil para exibir o valor original ao usuário. */
    public double calcularBonus() {
        return quantidadeProduzida * valorPorPeca;
    }

    @Override public String getTipo() { return "Producao"; }

    /**
     * Salário final com teto aplicado.
     * O teto é passado pelo FolhaService, que é a única fonte de verdade
     * para as configurações — sem depender de estado estático em Configuracao.
     *
     * @param salarioBase   valor atual do salário base
     * @param tetoBonusAbs  valor absoluto do teto de bônus
     */
    public double calcularSalarioFinal(double salarioBase, double tetoBonusAbs) {
        double bonus = Math.min(calcularBonus(), tetoBonusAbs);
        return salarioBase + bonus;
    }

    /**
     * Sobrecarga sem teto — aplicada quando o contexto não tem teto disponível
     * (ex: importação antes de carregar as configurações).
     * NÃO deve ser usada para exibição em folha nem para exportação;
     * nesses contextos sempre passe o teto explicitamente.
     */
    @Override
    public double calcularSalarioFinal(double salarioBase) {
        return salarioBase + calcularBonus(); // sem teto — apenas para compatibilidade
    }

    @Override
    public String getDetalheExtra() {
        return String.format("Bonus prod.  : %s  (%d pecas x %s)",
                moeda(calcularBonus()), quantidadeProduzida, moeda(valorPorPeca));
    }

    @Override
    public String toTSV(double salarioBase) {
        return matricula + "\t" +
               nome + "\t" +
               "PRODUCAO\t" +
               salarioBase + "\t" +
               "0\t0\t" +
               quantidadeProduzida + "\t" +
               valorPorPeca + "\t" +
               calcularSalarioFinal(salarioBase) + "\t" +
               getMesAnoAtual();
    }

    @Override
    public String toXLS(double salarioBase) {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #EBE6F4;'>" +
               "<td>" + matricula                         + "</td>" +
               "<td>" + nome                              + "</td>" +
               "<td>PRODUCAO</td>"                                  +
               "<td>" + salarioBase                        + "</td>" +
               "<td>0</td>"                                         +
               "<td>0</td>"                                         +
               "<td>" + quantidadeProduzida                + "</td>" +
               "<td>" + valorPorPeca                       + "</td>" +
               "<td>" + calcularSalarioFinal(salarioBase)  + "</td>" +
               "<td>" + agora.getMonthValue()              + "</td>" +
               "<td>" + agora.getYear()                    + "</td>" +
               "</tr>";
    }

    public int    getQuantidadeProduzida() { return quantidadeProduzida; }
    public double getValorPorPeca()        { return valorPorPeca; }
}