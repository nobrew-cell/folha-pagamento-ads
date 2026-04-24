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
     * Sobrecarga sem teto — mantida apenas para compatibilidade com a interface
     * abstrata. NÃO deve ser usada para exibição em folha nem para exportação;
     * nesses contextos sempre use calcularSalarioFinal(base, teto) ou
     * FolhaService.calcularSalarioFinalCompleto().
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

    /**
     * Serialização TSV — recebe o salário já calculado com teto pelo FolhaService
     * para garantir consistência entre o valor exibido na tela e o gravado em arquivo.
     *
     * @param salarioBase       valor do salário base (gravado na coluna SALARIO_BASE)
     * @param salarioCalculado  valor final já com teto aplicado (gravado em SALARIO_TOTAL)
     */
    public String toTSV(double salarioBase, double salarioCalculado) {
        return matricula + "\t" +
               nome + "\t" +
               "PRODUCAO\t" +
               salarioBase + "\t" +
               "0\t0\t" +
               quantidadeProduzida + "\t" +
               valorPorPeca + "\t" +
               salarioCalculado + "\t" +
               getMesAnoAtual();
    }

    /**
     * Sobrecarga de compatibilidade — usa calcularSalarioFinal sem teto.
     * Usada apenas em contextos onde o teto não está disponível (ex: importação).
     * Para exportação real, prefira toTSV(salarioBase, salarioCalculado).
     */
    @Override
    public String toTSV(double salarioBase) {
        return toTSV(salarioBase, calcularSalarioFinal(salarioBase));
    }

    /**
     * Serialização XLS — recebe o salário já calculado com teto pelo FolhaService
     * para garantir consistência entre o valor exibido na tela e o exportado.
     *
     * @param salarioBase       valor do salário base (exibido na coluna SALARIO_BASE)
     * @param salarioCalculado  valor final já com teto aplicado (exibido em SALARIO_TOTAL)
     */
    public String toXLS(double salarioBase, double salarioCalculado) {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        return "<tr style='background-color: #EBE6F4;'>" +
               "<td>" + matricula                  + "</td>" +
               "<td>" + nome                       + "</td>" +
               "<td>PRODUCAO</td>"                          +
               "<td>" + salarioBase                + "</td>" +
               "<td>0</td>"                                 +
               "<td>0</td>"                                 +
               "<td>" + quantidadeProduzida        + "</td>" +
               "<td>" + valorPorPeca               + "</td>" +
               "<td>" + salarioCalculado           + "</td>" +
               "<td>" + agora.getMonthValue()      + "</td>" +
               "<td>" + agora.getYear()            + "</td>" +
               "</tr>";
    }

    /**
     * Sobrecarga de compatibilidade — usa calcularSalarioFinal sem teto.
     * Para exportação real, prefira toXLS(salarioBase, salarioCalculado).
     */
    @Override
    public String toXLS(double salarioBase) {
        return toXLS(salarioBase, calcularSalarioFinal(salarioBase));
    }

    public int    getQuantidadeProduzida() { return quantidadeProduzida; }
    public double getValorPorPeca()        { return valorPorPeca; }
}