package br.com.folha.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.folha.model.Funcionario;
import br.com.folha.model.FuncionarioComissionado;
import br.com.folha.model.FuncionarioPadrao;
import br.com.folha.model.FuncionarioProducao;
import br.com.folha.repository.FuncionarioRepository;

/**
 * Regras de negócio. Não sabe nada de console nem de arquivo.
 */
public class FolhaService {

    private final FuncionarioRepository repository;
    private final List<Funcionario>     lista;

    /** Teto de bonus de producao: 200 % do salario base (R$ 4.000,00). */
    private static final double TETO_BONUS = Funcionario.SALARIO_BASE * 2;

    public FolhaService(FuncionarioRepository repository) {
        this.repository = repository;
        this.lista      = repository.carregar();
    }

    // ── Consultas ────────────────────────────────────────────────────────────

    /** Retorna visão imutável da lista, ordenada por matrícula. */
    public List<Funcionario> listar() {
        lista.sort(Comparator.comparingInt(Funcionario::getMatricula));
        return Collections.unmodifiableList(lista);
    }

    /** Verifica se a matrícula já está em uso. */
    public boolean matriculaExiste(int matricula) {
        return lista.stream().anyMatch(f -> f.getMatricula() == matricula);
    }

    /** Retorna true se o bonus calculado ultrapassa o teto permitido. */
    public boolean bonusUltrapassaTeto(int quantidade, double valorPorPeca) {
        return (quantidade * valorPorPeca) > TETO_BONUS;
    }

    /** Retorna o valor do teto de bonus para exibicao na UI. */
    public double getTetoBonusProducao() {
        return TETO_BONUS;
    }
    // ── Cadastros ────────────────────────────────────────────────────────────

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

    // ── Persistência ─────────────────────────────────────────────────────────

    public void salvar() {
        repository.salvar(lista);
    }

    /** Retorna array com caminho TSV [0] e XLS [1], ou null em cada posicao em falha. */
    public String[] exportar() {
        return repository.exportar(lista);
    }

    /**
     * Faz backup automático e limpa o sistema.
     * Retorna o caminho do backup gerado.
     */
    public String resetar() {
        String backup = repository.resetar(lista);
        lista.clear();
        return backup;
    }
}
