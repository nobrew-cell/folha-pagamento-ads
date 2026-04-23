package br.com.folha.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

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

    // Método existente (agora chama o novo com false para manter compatibilidade)
    public String iniciarNovoMes() throws Exception {
        return iniciarNovoMes(false);
    }

    // Novo método com opção de copiar funcionários (zerando valores)
    public String iniciarNovoMes(boolean copiarFuncionarios) throws Exception {
        // 1. Salva histórico do mês atual
        String arquivoHistorico = repository.salvarHistorico(lista);
        
        if (copiarFuncionarios) {
            // Cria uma nova lista com os mesmos funcionários, mas zerando os campos variáveis
            List<Funcionario> novaLista = new ArrayList<>();
            for (Funcionario f : lista) {
                Funcionario copia = clonarComZeros(f);
                novaLista.add(copia);
            }
            // Substitui a lista atual pela nova lista zerada
            lista.clear();
            lista.addAll(novaLista);
            repository.limparDados();          // limpa o arquivo
            repository.salvar(lista);          // salva a nova lista
        } else {
            // Comportamento antigo: limpa completamente
            repository.limparDados();
            lista.clear();
        }
        return arquivoHistorico;
    }

    // Método auxiliar para clonar funcionário com valores zerados
    private Funcionario clonarComZeros(Funcionario original) {
        String nome = original.getNome();
        int matricula = original.getMatricula();
        String tipo = original.getTipo();
        
        if (tipo.equals("Padrao")) {
            return new FuncionarioPadrao(nome, matricula);
        } else if (tipo.equals("Comissionado")) {
            // Zera vendas e percentual
            return new FuncionarioComissionado(nome, matricula, 0.0, 0.0);
        } else if (tipo.equals("Producao")) {
            // Zera quantidade e valor por peça
            return new FuncionarioProducao(nome, matricula, 0, 0.0);
        }
        return original; // fallback
    }

    // ── Métodos auxiliares para obter campos específicos ──
    public Funcionario buscarPorMatricula(int matricula) {
        return lista.stream()
                .filter(f -> f.getMatricula() == matricula)
                .findFirst()
                .orElse(null);
    }

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

    // Método de edição original (mantido para compatibilidade, mas agora chama o completo)
    public void editarFuncionario(int matricula, String novoNome,
                                  Double novasVendas, Double novoPercentual,
                                  Integer novaQtd, Double novoValorPeca) throws Exception {
        editarFuncionarioCompleto(matricula, novoNome, null, 
                novasVendas, novoPercentual, novaQtd, novoValorPeca);
    }

    // Novo método de edição que permite troca de tipo
    public void editarFuncionarioCompleto(int matricula, String novoNome, String novoTipo,
                                          Double novasVendas, Double novoPercentual,
                                          Integer novaQtd, Double novoValorPeca) throws Exception {
        Funcionario antigo = buscarPorMatricula(matricula);
        if (antigo == null) throw new Exception("Matricula nao encontrada.");

        String nomeFinal = (novoNome != null && !novoNome.trim().isEmpty())
                ? Funcionario.normalizarNome(novoNome)
                : antigo.getNome();

        // Determina o tipo final (se novoTipo for null, mantém o antigo)
        String tipoFinal = (novoTipo != null) ? novoTipo : antigo.getTipo();
        int idx = lista.indexOf(antigo);

        Funcionario novoFuncionario = null;

        switch (tipoFinal) {
            case "Padrao":
                novoFuncionario = new FuncionarioPadrao(nomeFinal, matricula);
                break;
            case "Comissionado":
                double vendas = (novasVendas != null && novasVendas >= 0) ? novasVendas : getVendasFuncionario(matricula);
                double perc   = (novoPercentual != null && novoPercentual >= 0) ? novoPercentual : getPercentualFuncionario(matricula);
                novoFuncionario = new FuncionarioComissionado(nomeFinal, matricula, vendas, perc);
                break;
            case "Producao":
                int qtd = (novaQtd != null && novaQtd >= 0) ? novaQtd : getQuantidadePecasFuncionario(matricula);
                double valor = (novoValorPeca != null && novoValorPeca >= 0) ? novoValorPeca : getValorPecaFuncionario(matricula);
                if (bonusUltrapassaTeto(qtd, valor)) {
                    throw new Exception("Bonus ultrapassa o teto de 200% do salario base.");
                }
                novoFuncionario = new FuncionarioProducao(nomeFinal, matricula, qtd, valor);
                break;
            default:
                throw new Exception("Tipo invalido: " + tipoFinal);
        }

        lista.set(idx, novoFuncionario);
        salvar();
    }

    public void removerFuncionario(int matricula) {
        lista.removeIf(f -> f.getMatricula() == matricula);
        salvar();
    }
}