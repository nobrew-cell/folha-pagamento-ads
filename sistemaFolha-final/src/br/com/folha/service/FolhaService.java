package br.com.folha.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import br.com.folha.model.Funcionario;
import br.com.folha.model.FuncionarioComissionado;
import br.com.folha.model.FuncionarioPadrao;
import br.com.folha.model.FuncionarioProducao;
import br.com.folha.model.Configuracao;
import br.com.folha.repository.FuncionarioRepository;
import br.com.folha.repository.ConfiguracaoRepository;

public class FolhaService {

    private final FuncionarioRepository repository;
    private final List<Funcionario> lista;

    public FolhaService(FuncionarioRepository repository) throws Exception {
        ConfiguracaoRepository.carregar();
        Funcionario.setSalarioBase(Configuracao.getSalarioBase());

        this.repository = repository;
        this.lista = repository.carregar();
    }

    public List<Funcionario> listar() {
        lista.sort(Comparator.comparingInt(Funcionario::getMatricula));
        return Collections.unmodifiableList(lista);
    }

    public boolean matriculaExiste(int matricula) {
        return lista.stream().anyMatch(f -> f.getMatricula() == matricula);
    }

    public boolean bonusUltrapassaTeto(int quantidade, double valorPorPeca) {
        double bonus = quantidade * valorPorPeca;
        return bonus > Configuracao.getTetoBonusAbsoluto();
    }

    public double getTetoBonusProducao() {
        return Configuracao.getTetoBonusAbsoluto();
    }

    // ── Nome similar (ignora caixa e acentos, devolve lista de funcionários com nome parecido) ──
    public List<Funcionario> buscarPorNomeSimilar(String nome, int matriculaIgnorar) {
        String nomeNormalizado = Funcionario.normalizarComparacao(nome);
        return lista.stream()
                .filter(f -> f.getMatricula() != matriculaIgnorar)
                .filter(f -> Funcionario.normalizarComparacao(f.getNome()).equals(nomeNormalizado))
                .collect(Collectors.toList());
    }

    // ── Cadastros ──
    public void cadastrarPadrao(String nome, int matricula) {
        lista.add(new FuncionarioPadrao(Funcionario.normalizarNome(nome), matricula));
    }

    public void cadastrarComissionado(String nome, int matricula,
                                      double vendas, double percentual) {
        lista.add(new FuncionarioComissionado(
                Funcionario.normalizarNome(nome), matricula, vendas, percentual));
    }

    public void cadastrarProducao(String nome, int matricula,
                                  int quantidade, double valorPeca) {
        lista.add(new FuncionarioProducao(
                Funcionario.normalizarNome(nome), matricula, quantidade, valorPeca));
    }

    // ── Persistência ──
    public void salvar() {
        repository.salvar(lista);
    }

    public String[] exportar() {
        return repository.exportar(lista);
    }

    public String resetar() {
        String backup = repository.resetar(lista);
        lista.clear();
        return backup;
    }

    // ── Importação, novo mês, edição, remoção ──
    public List<Funcionario> validarArquivoImportacao(String caminho) throws Exception {
        return repository.importarDeArquivo(caminho);
    }

    public void importarArquivo(String caminho) throws Exception {
        List<Funcionario> novaLista = repository.importarDeArquivo(caminho);
        lista.clear();
        lista.addAll(novaLista);
        salvar();
    }

    public String iniciarNovoMes() throws Exception {
        return iniciarNovoMes(false);
    }

    public String iniciarNovoMes(boolean copiarFuncionarios) throws Exception {
        String arquivoHistorico = repository.salvarHistorico(lista);
        if (copiarFuncionarios) {
            List<Funcionario> novaLista = new ArrayList<>();
            for (Funcionario f : lista) {
                novaLista.add(clonarComZeros(f));
            }
            lista.clear();
            lista.addAll(novaLista);
            repository.limparDados();
            repository.salvar(lista);
        } else {
            repository.limparDados();
            lista.clear();
        }
        return arquivoHistorico;
    }

    private Funcionario clonarComZeros(Funcionario original) {
        String nome = original.getNome();
        int matricula = original.getMatricula();
        String tipo = original.getTipo();

        if (tipo.equals("Padrao")) {
            return new FuncionarioPadrao(nome, matricula);
        } else if (tipo.equals("Comissionado")) {
            double percentual = ((FuncionarioComissionado) original).getPercentualComissao();
            return new FuncionarioComissionado(nome, matricula, 0.0, percentual);
        } else if (tipo.equals("Producao")) {
            double valorPeca = ((FuncionarioProducao) original).getValorPorPeca();
            return new FuncionarioProducao(nome, matricula, 0, valorPeca);
        }
        return original;
    }

    public Funcionario buscarPorMatricula(int matricula) {
        return lista.stream().filter(f -> f.getMatricula() == matricula).findFirst().orElse(null);
    }

    public double getVendasFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioComissionado) return ((FuncionarioComissionado) f).getVendas();
        return 0;
    }

    public double getPercentualFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioComissionado) return ((FuncionarioComissionado) f).getPercentualComissao();
        return 0;
    }

    public int getQuantidadePecasFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioProducao) return ((FuncionarioProducao) f).getQuantidadeProduzida();
        return 0;
    }

    public double getValorPecaFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioProducao) return ((FuncionarioProducao) f).getValorPorPeca();
        return 0;
    }

    public void editarFuncionario(int matricula, String novoNome,
                                  Double novasVendas, Double novoPercentual,
                                  Integer novaQtd, Double novoValorPeca) throws Exception {
        editarFuncionarioCompleto(matricula, novoNome, null, novasVendas, novoPercentual, novaQtd, novoValorPeca);
    }

    public void editarFuncionarioCompleto(int matricula, String novoNome, String novoTipo,
                                          Double novasVendas, Double novoPercentual,
                                          Integer novaQtd, Double novoValorPeca) throws Exception {
        Funcionario antigo = buscarPorMatricula(matricula);
        if (antigo == null) throw new Exception("Matricula nao encontrada.");
        String nomeFinal = (novoNome != null && !novoNome.trim().isEmpty())
                ? Funcionario.normalizarNome(novoNome)
                : antigo.getNome();
        String tipoFinal = (novoTipo != null) ? novoTipo : antigo.getTipo();
        int idx = lista.indexOf(antigo);
        Funcionario novo = null;

        switch (tipoFinal) {
            case "Padrao":
                novo = new FuncionarioPadrao(nomeFinal, matricula);
                break;
            case "Comissionado":
                double vendas = (novasVendas != null && novasVendas >= 0) ? novasVendas : getVendasFuncionario(matricula);
                double perc = (novoPercentual != null && novoPercentual >= 0) ? novoPercentual : getPercentualFuncionario(matricula);
                novo = new FuncionarioComissionado(nomeFinal, matricula, vendas, perc);
                break;
            case "Producao":
                int qtd = (novaQtd != null && novaQtd >= 0) ? novaQtd : getQuantidadePecasFuncionario(matricula);
                double valor = (novoValorPeca != null && novoValorPeca >= 0) ? novoValorPeca : getValorPecaFuncionario(matricula);
                if (bonusUltrapassaTeto(qtd, valor))
                    throw new Exception("Bonus ultrapassa o teto de " + Funcionario.moeda(getTetoBonusProducao()));
                novo = new FuncionarioProducao(nomeFinal, matricula, qtd, valor);
                break;
            default: throw new Exception("Tipo invalido: " + tipoFinal);
        }
        lista.set(idx, novo);
        salvar();
    }

    public void removerFuncionario(int matricula) {
        lista.removeIf(f -> f.getMatricula() == matricula);
        salvar();
    }

    // ── Configurações ──
    public double getSalarioBase() {
        return Configuracao.getSalarioBase();
    }

    public double getTetoPercentual() {
        return Configuracao.getTetoBonusPercentual();
    }

    public void setSalarioBase(double novoSalario) {
        Configuracao.setSalarioBase(novoSalario);
        Funcionario.setSalarioBase(novoSalario);
        ConfiguracaoRepository.salvar();
    }

    public void setTetoPercentual(double novoPercentual) {
        Configuracao.setTetoBonusPercentual(novoPercentual);
        ConfiguracaoRepository.salvar();
    }

    public double calcularTotalFolha() {
        return lista.stream().mapToDouble(Funcionario::calcularSalarioFinal).sum();
    }
}