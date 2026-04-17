package br.com.folha.ui;

import java.util.Scanner;

import br.com.folha.model.Funcionario;
import br.com.folha.service.FolhaService;

/**
 * Toda a interface do terminal fica aqui.
 * Não contém nenhuma regra de negócio. Conversa exclusivamente com FolhaService.
 */
public class ConsoleUI {

    private static final String SEP = "======================================================";
    private static final String LIN = "------------------------------------------------------";

    private final Scanner     sc;
    private final FolhaService service;
    private final boolean      primeiraVez;

    public ConsoleUI(FolhaService service, boolean primeiraVez) {
        this.sc          = new Scanner(System.in);
        this.service     = service;
        this.primeiraVez = primeiraVez;
    }

    // ── Loop principal ───────────────────────────────────────────────────────

    public void iniciar() {
        if (primeiraVez) exibirBoasVindas();

        int opcao = -1;
        while (opcao != 0) {
            exibirMenu();
            opcao = lerInteiro();

            switch (opcao) {
                case 1 -> cadastrarPadrao();
                case 2 -> cadastrarComissionado();
                case 3 -> cadastrarProducao();
                case 4 -> gerarFolha();
                case 5 -> exportar();
                case 6 -> resetar();
                case 0 -> encerrar();
                default -> System.out.println("  Opcao invalida. Tente novamente.");
            }
        }
        sc.close();
    }

    // ── Boas-vindas (só na primeira execução) ────────────────────────────────

    private void exibirBoasVindas() {
        System.out.println("\n" + SEP);
        System.out.println("      Bem-vindo ao Sistema de Folha de Pagamento");
        System.out.println("           Versao 2.0  |  Salarios mensais");
        System.out.println(SEP);
        System.out.println("  Este e o seu primeiro acesso.");
        System.out.println("  Nenhum funcionario cadastrado ainda.");
        System.out.println(LIN);
        aguardar("  Pressione ENTER para continuar...");
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    private void exibirMenu() {
        System.out.println("\n" + SEP);
        System.out.println("        FOLHA DE PAGAMENTO  (salarios mensais)");
        System.out.println(SEP);
        System.out.println("  1 - Cadastrar Funcionario Padrao");
        System.out.println("  2 - Cadastrar Funcionario Comissionado");
        System.out.println("  3 - Cadastrar Funcionario de Producao");
        System.out.println("  4 - Gerar Folha de Pagamento");
        System.out.println("  5 - Exportar TSV  (copia com data e hora)");
        System.out.println("  6 - Resetar sistema  [ADM]");
        System.out.println("  0 - Sair");
        System.out.println(SEP);
        System.out.print("  Opcao: ");
    }

    // ── Cadastros ────────────────────────────────────────────────────────────

    private void cadastrarPadrao() {
        System.out.println("\n" + LIN);
        System.out.println("  NOVO FUNCIONARIO PADRAO");
        System.out.println("  (digite 0 em qualquer campo para cancelar)");
        System.out.println(LIN);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        service.cadastrarPadrao(nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    private void cadastrarComissionado() {
        System.out.println("\n" + LIN);
        System.out.println("  NOVO FUNCIONARIO COMISSIONADO");
        System.out.println("  (digite 0 em qualquer campo para cancelar)");
        System.out.println(LIN);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        System.out.print("  Total de vendas mensais (R$): ");
        double vendas = lerDoubleNaoNegativo();
        if (vendas < 0) { cancelado(); return; }

        System.out.print("  Percentual de comissao (%): ");
        double perc = lerDoubleNaoNegativo();
        if (perc < 0) { cancelado(); return; }

        service.cadastrarComissionado(nome, mat, vendas, perc);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    private void cadastrarProducao() {
        System.out.println("\n" + LIN);
        System.out.println("  NOVO FUNCIONARIO DE PRODUCAO");
        System.out.println("  (digite 0 em qualquer campo para cancelar)");
        System.out.println(LIN);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        System.out.print("  Pecas produzidas no mes: ");
        int qtd = lerInteiroPositivoOuZero();
        if (qtd == 0) { cancelado(); return; }

        System.out.print("  Valor por peca (R$): ");
        double vpeca = lerDoubleNaoNegativo();
        if (vpeca < 0) { cancelado(); return; }

        service.cadastrarProducao(nome, mat, qtd, vpeca);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── Folha ────────────────────────────────────────────────────────────────

    private void gerarFolha() {
        var lista = service.listar();

        System.out.println("\n" + SEP);
        System.out.println("        FOLHA DE PAGAMENTO MENSAL");
        System.out.printf ("        Total de funcionarios: %d%n", lista.size());
        System.out.println(SEP);

        if (lista.isEmpty()) {
            System.out.println("  Nenhum funcionario cadastrado ainda.");
            System.out.println(SEP);
            return;
        }

        for (Funcionario f : lista) {
            System.out.println(LIN);
            System.out.println("  Nome:         " + f.getNome());
            System.out.println("  Matricula:    " + f.getMatricula());
            System.out.println("  Tipo:         " + f.getTipo());
            System.out.println("  Salario base: " + Funcionario.moeda(Funcionario.SALARIO_BASE) + " / mes");
            System.out.println("  " + f.getDetalheExtra());
            System.out.println("  Total mensal: " + Funcionario.moeda(f.calcularSalarioFinal()));
        }

        System.out.println(SEP);
    }

    // ── Exportar ─────────────────────────────────────────────────────────────

    private void exportar() {
        String caminho = service.exportar();
        if (caminho != null) {
            System.out.println("\n  [OK] Exportado: " + caminho);
        } else {
            System.out.println("\n  [ERRO] Nao foi possivel exportar o arquivo.");
        }
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    private void resetar() {
        System.out.println("\n" + LIN);
        System.out.println("                MODO ADM - RESET TOTAL");
        System.out.println(LIN);
        System.out.println("  Isso apagara todos os funcionarios.");
        System.out.println("  Um backup automatico sera salvo antes.");
        System.out.println(LIN);
        System.out.print("  Digite CONFIRMAR para prosseguir: ");
        String confirm = sc.nextLine().trim();

        if (confirm.equals("CONFIRMAR")) {
            String backup = service.resetar();
            System.out.println("\n  [OK] Sistema resetado.");
            System.out.println("  Backup salvo em: " + backup);
        } else {
            System.out.println("  Operacao cancelada.");
        }
    }

    // ── Encerrar ─────────────────────────────────────────────────────────────

    private void encerrar() {
        service.salvar();
        System.out.println("\n" + SEP);
        System.out.println("  Dados salvos. Volte sempre!");
        System.out.println(SEP + "\n");
    }

    // ── Utilitários de leitura ───────────────────────────────────────────────

    private String lerTexto(String prompt) {
        System.out.print(prompt);
        String v = sc.nextLine().trim();
        while (v.isEmpty()) {
            System.out.print("  Campo obrigatorio. " + prompt.stripLeading());
            v = sc.nextLine().trim();
        }
        return v;
    }

    /**
     * Lê matrícula: deve ser > 0 e não pode estar duplicada.
     * Retorna 0 se o usuário digitar "0" (cancelar).
     */
    private int lerMatricula(String prompt) {
        System.out.print(prompt);
        while (true) {
            String linha = sc.nextLine().trim();
            if (linha.equals("0")) return 0;
            try {
                int v = Integer.parseInt(linha);
                if (v <= 0) {
                    System.out.print("  Matricula deve ser maior que zero: ");
                    continue;
                }
                if (service.matriculaExiste(v)) {
                    System.out.printf("  Matricula %d ja esta em uso. Informe outra: ", v);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.print("  Numero invalido. " + prompt.stripLeading());
            }
        }
    }

    private int lerInteiro() {
        while (true) {
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("  Inteiro invalido: "); }
        }
    }

    /**
     * Lê inteiro >= 0. Retorna 0 também como sinal de cancelamento
     * quando chamado de dentro de um fluxo de cadastro.
     */
    private int lerInteiroPositivoOuZero() {
        while (true) {
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= 0) return v;
                System.out.print("  Nao pode ser negativo: ");
            } catch (NumberFormatException e) {
                System.out.print("  Numero invalido: ");
            }
        }
    }

    /**
     * Lê double > 0.
     *
     * Retorna -1 se o usuário digitar exatamente "0" — sinal universal de
     * cancelamento desta interface. O zero é interceptado antes do parse,
     * então nunca chega ao cadastro como valor legítimo.
     *
     * Decisão de design: ver seção correspondente no README.
     */
    private double lerDoubleNaoNegativo() {
        while (true) {
            String linha = sc.nextLine().trim().replace(",", ".");
            if (linha.equals("0")) return -1; // cancelamento universal
            try {
                double v = Double.parseDouble(linha);
                if (v > 0) return v;
                System.out.print("  Valor deve ser maior que zero (ou 0 para cancelar): ");
            } catch (NumberFormatException e) {
                System.out.print("  Decimal invalido: ");
            }
        }
    }

    private void aguardar(String msg) {
        System.out.print(msg);
        sc.nextLine();
    }

    private void cancelado() {
        System.out.println("\n  Cadastro cancelado. Voltando ao menu.");
    }
}
