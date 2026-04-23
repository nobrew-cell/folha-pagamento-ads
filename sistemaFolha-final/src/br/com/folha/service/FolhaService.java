package br.com.folha.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.folha.model.Funcionario;
import br.com.folha.model.FuncionarioComissionado;
import br.com.folha.model.FuncionarioPadrao;
import br.com.folha.model.FuncionarioProducao;
import br.com.folha.repository.FuncionarioRepository;

public class FolhaService {

    private final FuncionarioRepository repository;
    private final List<Funcionario> lista;

    private static final double TETO_BONUS = Funcionario.SALARIO_BASE * 2;

    public FolhaService(FuncionarioRepository repository) throws Exception {
        this.repository = repository;
        this.lista = repository.carregar();
    }

    // ── Consultas existentes ──
    public List<Funcionario> listar() {
        lista.sort(Comparator.comparingInt(Funcionario::getMatricula));
        return Collections.unmodifiableList(lista);
    }

    public boolean matriculaExiste(int matricula) {
        return lista.stream().anyMatch(f -> f.getMatricula() == matricula);
    }

    public boolean bonusUltrapassaTeto(int quantidade, double valorPorPeca) {
        return (quantidade * valorPorPeca) > TETO_BONUS;
    }

    public double getTetoBonusProducao() {
        return TETO_BONUS;
    }

    // ── Cadastros existentes ──
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

    // ── Persistência existente ──
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

    // ── NOVOS MÉTODOS (importação, novo mês, edição, remoção) ──

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
        String arquivoHistorico = repository.salvarHistorico(lista);
        repository.limparDados();
        lista.clear();
        return arquivoHistorico;
    }

    public Funcionario buscarPorMatricula(int matricula) {
        return lista.stream()
                .filter(f -> f.getMatricula() == matricula)
                .findFirst()
                .orElse(null);
    }

    // Métodos auxiliares para obter campos específicos (usados na edição)
    public double getVendasFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioComissionado) {
            return ((FuncionarioComissionado) f).getVendas();
        }
        return 0;
    }

    public double getPercentualFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioComissionado) {
            return ((FuncionarioComissionado) f).getPercentualComissao();
        }
        return 0;
    }

    public int getQuantidadePecasFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioProducao) {
            return ((FuncionarioProducao) f).getQuantidadeProduzida();
        }
        return 0;
    }

    public double getValorPecaFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioProducao) {
            return ((FuncionarioProducao) f).getValorPorPeca();
        }
        return 0;
    }

    public void editarFuncionario(int matricula, String novoNome,
                                  Double novasVendas, Double novoPercentual,
                                  Integer novaQtd, Double novoValorPeca) throws Exception {
        Funcionario antigo = buscarPorMatricula(matricula);
        if (antigo == null) throw new Exception("Matricula nao encontrada.");

        String nomeFinal = (novoNome != null && !novoNome.trim().isEmpty())
                ? Funcionario.normalizarNome(novoNome)
                : antigo.getNome();

        int idx = lista.indexOf(antigo);

        if (antigo instanceof FuncionarioPadrao) {
            lista.set(idx, new FuncionarioPadrao(nomeFinal, matricula));
        }
        else if (antigo instanceof FuncionarioComissionado) {
            double vendas = (novasVendas != null && novasVendas >= 0) ? novasVendas : getVendasFuncionario(matricula);
            double perc   = (novoPercentual != null && novoPercentual >= 0) ? novoPercentual : getPercentualFuncionario(matricula);
            lista.set(idx, new FuncionarioComissionado(nomeFinal, matricula, vendas, perc));
        }
        else if (antigo instanceof FuncionarioProducao) {
            int qtd = (novaQtd != null && novaQtd >= 0) ? novaQtd : getQuantidadePecasFuncionario(matricula);
            double valor = (novoValorPeca != null && novoValorPeca >= 0) ? novoValorPeca : getValorPecaFuncionario(matricula);
            if (bonusUltrapassaTeto(qtd, valor)) {
                throw new Exception("Bonus ultrapassa o teto de 200% do salario base.");
            }
            lista.set(idx, new FuncionarioProducao(nomeFinal, matricula, qtd, valor));
        }
        salvar();
    }

    public void removerFuncionario(int matricula) {
        lista.removeIf(f -> f.getMatricula() == matricula);
        salvar();
    }
}