package br.com.folha.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import br.com.folha.model.Funcionario;
import br.com.folha.model.FuncionarioComissionado;
import br.com.folha.model.FuncionarioPadrao;
import br.com.folha.model.FuncionarioProducao;
import br.com.folha.repository.FuncionarioRepository;
import br.com.folha.repository.FuncionarioRepository.DadosCarregados;

/**
 * Camada de negócio.
 *
 * Salário base e teto de bônus vivem aqui como campos de instância —
 * sem duplicação em Funcionario nem em classes Configuracao separadas.
 * São persistidos na linha #CONFIG do database.tsv pelo repositório.
 */
public class FolhaService {

    // ── Configurações ─────────────────────────────────────────────────────
    private double salarioBase;
    private double tetoBonusPercentual; // percentual do salárioBase (ex: 200 = 200%)

    // ── Dados ─────────────────────────────────────────────────────────────
    private final FuncionarioRepository repository;
    private final List<Funcionario>      lista;

    // ── Inicialização ─────────────────────────────────────────────────────

    public FolhaService(FuncionarioRepository repository) throws Exception {
        this.repository = repository;
        DadosCarregados dados = repository.carregar();
        this.lista              = dados.funcionarios;
        this.salarioBase        = dados.salarioBase;
        this.tetoBonusPercentual = dados.tetoBonusPercentual;
    }

    // ── Configurações — getters/setters ───────────────────────────────────

    public double getSalarioBase()        { return salarioBase; }
    public double getTetoPercentual()     { return tetoBonusPercentual; }
    public double getTetoBonusAbsoluto()  { return salarioBase * (tetoBonusPercentual / 100.0); }

    public void setSalarioBase(double novoSalario) {
        this.salarioBase = novoSalario;
        salvar(); // persiste imediatamente
    }

    public void setTetoPercentual(double novoPercentual) {
        this.tetoBonusPercentual = novoPercentual;
        salvar();
    }

    // ── Listagem ──────────────────────────────────────────────────────────

    public List<Funcionario> listar() {
        lista.sort(Comparator.comparingInt(Funcionario::getMatricula));
        return Collections.unmodifiableList(lista);
    }

    // ── Buscas ────────────────────────────────────────────────────────────

    public boolean matriculaExiste(int matricula) {
        return lista.stream().anyMatch(f -> f.getMatricula() == matricula);
    }

    public Funcionario buscarPorMatricula(int matricula) {
        return lista.stream()
                .filter(f -> f.getMatricula() == matricula)
                .findFirst().orElse(null);
    }

    public List<Funcionario> buscarPorNomeSimilar(String nome, int matriculaIgnorar) {
        String nomeNorm = Funcionario.normalizarComparacao(nome);
        return lista.stream()
                .filter(f -> f.getMatricula() != matriculaIgnorar)
                .filter(f -> Funcionario.normalizarComparacao(f.getNome()).equals(nomeNorm))
                .collect(Collectors.toList());
    }

    // ── Verificação de teto ───────────────────────────────────────────────

    public boolean bonusUltrapassaTeto(int quantidade, double valorPorPeca) {
        return (quantidade * valorPorPeca) > getTetoBonusAbsoluto();
    }

    // ── Cálculo de salário com teto (usado na folha e na exportação) ──────

    /**
     * Calcula o salário final de um funcionário aplicando o teto de produção
     * com os valores de configuração atuais.
     */
    public double calcularSalarioFinalCompleto(Funcionario f) {
        if (f instanceof FuncionarioProducao) {
            return ((FuncionarioProducao) f)
                    .calcularSalarioFinal(salarioBase, getTetoBonusAbsoluto());
        }
        return f.calcularSalarioFinal(salarioBase);
    }

    public double calcularTotalFolha() {
        return lista.stream()
                .mapToDouble(this::calcularSalarioFinalCompleto)
                .sum();
    }

    // ── Cadastros ─────────────────────────────────────────────────────────

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

    // ── Persistência ──────────────────────────────────────────────────────

    /**
     * Salva a lista e as configurações no database.tsv.
     *
     * @return true se a escrita foi bem-sucedida
     */
    public boolean salvar() {
        return repository.salvar(lista, salarioBase, tetoBonusPercentual);
    }

    public String[] exportar() {
        return repository.exportar(lista, salarioBase, tetoBonusPercentual);
    }

    /**
     * Reseta o sistema: cria backup e limpa o database.
     *
     * @return caminho do backup gerado
     * @throws IOException se não for possível limpar o arquivo de dados
     *         (a lista em memória NÃO é limpa nesse caso — evita inconsistência)
     */
    public String resetar() throws IOException {
        String backup = repository.resetar(lista, salarioBase, tetoBonusPercentual);
        lista.clear(); // só limpa a memória após o arquivo ter sido limpo com sucesso
        return backup;
    }

    // ── Importação ────────────────────────────────────────────────────────

    public List<Funcionario> validarArquivoImportacao(String caminho) throws Exception {
        return repository.importarDeArquivo(caminho);
    }

    public void importarArquivo(String caminho) throws Exception {
        List<Funcionario> novaLista = repository.importarDeArquivo(caminho);
        lista.clear();
        lista.addAll(novaLista);
        salvar();
    }

    // ── Novo mês ──────────────────────────────────────────────────────────

    public String iniciarNovoMes() throws Exception {
        return iniciarNovoMes(false);
    }

    public String iniciarNovoMes(boolean copiarFuncionarios) throws Exception {
        // Salva histórico antes de qualquer limpeza
        String arquivoHistorico =
                repository.salvarHistorico(lista, salarioBase, tetoBonusPercentual);

        if (copiarFuncionarios) {
            List<Funcionario> novaLista = new ArrayList<>();
            for (Funcionario f : lista) {
                novaLista.add(clonarComZeros(f));
            }
            lista.clear();
            lista.addAll(novaLista);
            repository.limparDados(salarioBase, tetoBonusPercentual);
            salvar();
        } else {
            repository.limparDados(salarioBase, tetoBonusPercentual);
            lista.clear();
        }
        return arquivoHistorico;
    }

    private Funcionario clonarComZeros(Funcionario original) {
        String nome      = original.getNome();
        int    matricula = original.getMatricula();
        String tipo      = original.getTipo();

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

    // ── Getters de campos específicos (usados pela UI de edição) ──────────

    public double getVendasFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioComissionado)
            return ((FuncionarioComissionado) f).getVendas();
        return 0;
    }

    public double getPercentualFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioComissionado)
            return ((FuncionarioComissionado) f).getPercentualComissao();
        return 0;
    }

    public int getQuantidadePecasFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioProducao)
            return ((FuncionarioProducao) f).getQuantidadeProduzida();
        return 0;
    }

    public double getValorPecaFuncionario(int matricula) {
        Funcionario f = buscarPorMatricula(matricula);
        if (f instanceof FuncionarioProducao)
            return ((FuncionarioProducao) f).getValorPorPeca();
        return 0;
    }

    // ── Edição ────────────────────────────────────────────────────────────

    public void editarFuncionario(int matricula, String novoNome,
                                  Double novasVendas, Double novoPercentual,
                                  Integer novaQtd, Double novoValorPeca) throws Exception {
        editarFuncionarioCompleto(matricula, novoNome, null,
                novasVendas, novoPercentual, novaQtd, novoValorPeca);
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
        Funcionario novo;

        switch (tipoFinal) {
            case "Padrao":
                novo = new FuncionarioPadrao(nomeFinal, matricula);
                break;
            case "Comissionado":
                double vendas = (novasVendas != null && novasVendas >= 0)
                        ? novasVendas : getVendasFuncionario(matricula);
                double perc   = (novoPercentual != null && novoPercentual >= 0)
                        ? novoPercentual : getPercentualFuncionario(matricula);
                novo = new FuncionarioComissionado(nomeFinal, matricula, vendas, perc);
                break;
            case "Producao":
                int    qtd   = (novaQtd != null && novaQtd >= 0)
                        ? novaQtd : getQuantidadePecasFuncionario(matricula);
                double valor = (novoValorPeca != null && novoValorPeca >= 0)
                        ? novoValorPeca : getValorPecaFuncionario(matricula);
                if (bonusUltrapassaTeto(qtd, valor))
                    throw new Exception("Bonus ultrapassa o teto de "
                            + Funcionario.moeda(getTetoBonusAbsoluto()));
                novo = new FuncionarioProducao(nomeFinal, matricula, qtd, valor);
                break;
            default:
                throw new Exception("Tipo invalido: " + tipoFinal);
        }

        lista.set(idx, novo);
        salvar();
    }

    // ── Remoção ───────────────────────────────────────────────────────────

    public void removerFuncionario(int matricula) {
        lista.removeIf(f -> f.getMatricula() == matricula);
        salvar();
    }
}