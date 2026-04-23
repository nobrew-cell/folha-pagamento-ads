package br.com.folha.ui;

import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import br.com.folha.model.Funcionario;
import br.com.folha.service.FolhaService;

public class ConsoleUI {

    private static final String SEP = "======================================================";
    private static final String LIN = "------------------------------------------------------";

    private final Scanner sc;
    private final FolhaService service;
    private final boolean primeiraVez;

    public ConsoleUI(FolhaService service, boolean primeiraVez) {
        this.sc = new Scanner(System.in);
        this.service = service;
        this.primeiraVez = primeiraVez;
    }

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
                case 5 -> menuADM();
                case 0 -> encerrar();
                default -> System.out.println("  Opcao invalida. Tente novamente.");
            }
        }
        sc.close();
    }

    private void exibirMenu() {
        System.out.println("\n" + SEP);
        System.out.println("        FOLHA DE PAGAMENTO  (salarios mensais)");
        System.out.println(SEP);
        System.out.println("  [1] - Cadastrar Funcionario Padrao");
        System.out.println("  [2] - Cadastrar Funcionario Comissionado");
        System.out.println("  [3] - Cadastrar Funcionario de Producao");
        System.out.println("  [4] - Gerar Folha de Pagamento");
        System.out.println("  [5] - Menu ADM (Manutenção de Dados)");
        System.out.println("  [0] - Sair");
        System.out.println(SEP);
        System.out.print("  Opcao: ");
    }

    private void exibirBoasVindas() {
        System.out.println("\n" + SEP);
        System.out.println("      Bem-vindo ao Sistema de Folha de Pagamento");
        System.out.println("           Versao 5.0  |  Salarios mensais");
        System.out.println(SEP);
        System.out.println("  Este e o seu primeiro acesso.");
        System.out.println("  Nenhum funcionario cadastrado ainda.");
        System.out.println(LIN);
        aguardar("  Pressione ENTER para continuar...");
    }

    // ── SUBMENU ADM com navegação inteligente ─────────────────────────────────
    private void menuADM() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n" + SEP);
            System.out.println("               MENU ADMINISTRATIVO");
            System.out.println(SEP);
            System.out.println("  1 - Exportar TSV e XLS");
            System.out.println("  2 - Importar arquivo TSV (substitui todos os dados)");
            System.out.println("  3 - Novo mes (arquiva atual e limpa a base)");
            System.out.println("  4 - Editar funcionario");
            System.out.println("  5 - Remover funcionario");
            System.out.println("  6 - Resetar sistema (backup automatico)");
            System.out.println("  7 - Configuracoes do sistema");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println(SEP);
            System.out.print("  Opcao: ");

            opcao = lerInteiro();
            boolean success = false;
            switch (opcao) {
                case 1 -> success = exportar();
                case 2 -> success = importarArquivo();
                case 3 -> success = novoMes();
                case 4 -> success = editarFuncionario();
                case 5 -> success = removerFuncionario();
                case 6 -> success = resetar();
                case 7 -> configuracoes();
                case 0 -> System.out.println("  Voltando ao menu principal...");
                default -> System.out.println("  Opcao invalida.");
            }
            if (success) {
                System.out.println("\n  Operacao concluida. Retornando ao menu principal...");
                return;
            }
        }
    }

    // ── IMPORTAR com JFileChooser (retorna true se sucesso) ───────────────────
    private boolean importarArquivo() {
        System.out.println("\n" + LIN);
        System.out.println("            IMPORTAR DADOS DE ARQUIVO TSV");
        System.out.println(LIN);
        System.out.println("  Abrindo seletor de arquivos...");

        JFileChooser chooser = new JFileChooser(".");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos TSV", "tsv");
        chooser.setFileFilter(filter);
        int resultado = chooser.showOpenDialog(null);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            System.out.println("  Importacao cancelada.");
            return false;
        }
        String caminho = chooser.getSelectedFile().getAbsolutePath();
        System.out.println("  Arquivo selecionado: " + caminho);

        System.out.println("\n  Validando arquivo...");
        try {
            List<Funcionario> importados = service.validarArquivoImportacao(caminho);
            System.out.println("  Arquivo valido. " + importados.size() + " funcionario(s) encontrado(s).");
            System.out.print("  Isso substituira TODOS os dados atuais. Confirmar? (S/N): ");
            String conf = sc.nextLine().trim().toUpperCase();
            if (conf.equals("S")) {
                service.importarArquivo(caminho);
                System.out.println("\n  [OK] Importacao concluida com sucesso.");
                return true;
            } else {
                System.out.println("  Importacao cancelada.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("\n  ERRO: " + e.getMessage());
            System.out.println("  Importacao cancelada.");
            return false;
        }
    }

    // ── NOVO MÊS (retorna true se sucesso) ───────────────────────────────────
    private boolean novoMes() {
        System.out.println("\n" + LIN);
        System.out.println("                  INICIAR NOVO MES");
        System.out.println(LIN);
        System.out.println("  Isso salvara o mes atual na pasta 'historico/'");
        System.out.print("  Deseja copiar os funcionarios do mes anterior (zerando vendas/pecas)? (S/N): ");
        String copiar = sc.nextLine().trim().toUpperCase();
        boolean copiarFuncionarios = copiar.equals("S");

        System.out.print("  Deseja continuar? (S/N): ");
        String conf = sc.nextLine().trim().toUpperCase();
        if (conf.equals("S")) {
            try {
                String arquivoHistorico = service.iniciarNovoMes(copiarFuncionarios);
                System.out.println("\n  [OK] Historico salvo em: " + arquivoHistorico);
                if (copiarFuncionarios) {
                    System.out.println("  Funcionarios copiados (com valores zerados).");
                } else {
                    System.out.println("  Base limpa. Novo mes iniciado.");
                }
                return true;
            } catch (Exception e) {
                System.out.println("\n  [ERRO] " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("  Operacao cancelada.");
            return false;
        }
    }

    // ── EDIÇÃO com troca de tipo (retorna true se sucesso) ────────────────────
    private boolean editarFuncionario() {
        System.out.println("\n" + LIN);
        System.out.println("                 EDITAR FUNCIONARIO");
        System.out.println(LIN);
        System.out.print("  Digite a matricula do funcionario: ");
        int mat = lerInteiro();
        Funcionario f = service.buscarPorMatricula(mat);
        if (f == null) {
            System.out.println("  Matricula nao encontrada.");
            aguardar("  Pressione ENTER para continuar...");
            return false;
        }

        System.out.println("\n  Dados atuais:");
        System.out.println("  Nome: " + f.getNome());
        System.out.println("  Tipo: " + f.getTipo());
        if (f.getTipo().equals("Comissionado")) {
            double vendas = service.getVendasFuncionario(mat);
            double perc = service.getPercentualFuncionario(mat);
            System.out.println("  Vendas: R$ " + vendas);
            System.out.println("  Percentual: " + perc + "%");
        } else if (f.getTipo().equals("Producao")) {
            int qtd = service.getQuantidadePecasFuncionario(mat);
            double valor = service.getValorPecaFuncionario(mat);
            System.out.println("  Pecas produzidas: " + qtd);
            System.out.println("  Valor por peca (bonus liquido por unidade): R$ " + valor);
        }

        System.out.println(LIN);
        System.out.println("  Deseja alterar o tipo do funcionario?");
        System.out.println("  1 - Padrao");
        System.out.println("  2 - Comissionado");
        System.out.println("  3 - Producao");
        System.out.println("  ENTER - manter atual");
        System.out.print("  Opcao: ");
        String tipoInput = sc.nextLine().trim();
        String novoTipo = null;
        boolean tipoAlterado = false;

        if (!tipoInput.isEmpty()) {
            int tipoOpcao = Integer.parseInt(tipoInput);
            switch (tipoOpcao) {
                case 1 -> novoTipo = "Padrao";
                case 2 -> novoTipo = "Comissionado";
                case 3 -> novoTipo = "Producao";
                default -> System.out.println("  Opcao invalida. Tipo mantido.");
            }
            if (novoTipo != null) {
                if (novoTipo.equals(f.getTipo())) {
                    System.out.println("  Esse e o tipo atual. Nenhuma alteracao realizada.");
                } else {
                    tipoAlterado = true;
                    System.out.println("  Tipo alterado para: " + novoTipo);
                }
            }
        }

        System.out.println(LIN);
        System.out.println("  Deixe em branco para manter o valor atual.");
        System.out.print("  Novo nome: ");
        String novoNome = sc.nextLine().trim();
        if (novoNome.isEmpty()) novoNome = f.getNome();

        Double novasVendas = null;
        Double novoPercentual = null;
        Integer novaQtd = null;
        Double novoValorPeca = null;

        if (tipoAlterado) {
            System.out.println(LIN);
            System.out.println("  Novo tipo: " + novoTipo);
            if (novoTipo.equals("Comissionado")) {
                System.out.println("  Preencha os campos abaixo (ENTER pula - pode editar depois):");
                System.out.print("  Total de vendas (R$): ");
                double v = lerDoubleEdicao();
                if (v >= 0) novasVendas = v;
                System.out.print("  Percentual de comissao (%): ");
                double p = lerDoubleEdicao();
                if (p >= 0) novoPercentual = p;
            } else if (novoTipo.equals("Producao")) {
                System.out.println("  Preencha os campos abaixo (ENTER pula - pode editar depois):");
                System.out.print("  Quantidade de pecas: ");
                int q = lerInteiroEdicao();
                if (q >= 0) novaQtd = q;
                System.out.print("  Valor por peca (bonus liquido por unidade) (R$): ");
                double vp = lerDoubleEdicao();
                if (vp >= 0) novoValorPeca = vp;
            }
        } else {
            if (f.getTipo().equals("Comissionado")) {
                System.out.print("  Novo total de vendas (R$) (0 para manter): ");
                double v = lerDoubleEdicao();
                if (v >= 0) novasVendas = v;
                System.out.print("  Novo percentual de comissao (%) (0 para manter): ");
                double p = lerDoubleEdicao();
                if (p >= 0) novoPercentual = p;
            } else if (f.getTipo().equals("Producao")) {
                System.out.print("  Nova quantidade de pecas (0 para manter): ");
                int q = lerInteiroEdicao();
                if (q >= 0) novaQtd = q;
                System.out.print("  Novo valor por peca (bonus liquido por unidade) (R$) (0 para manter): ");
                double vp = lerDoubleEdicao();
                if (vp >= 0) novoValorPeca = vp;
            }
        }

        try {
            service.editarFuncionarioCompleto(mat, novoNome, novoTipo,
                    novasVendas, novoPercentual, novaQtd, novoValorPeca);
            System.out.println("\n  [OK] Funcionario editado com sucesso.");
            return true;
        } catch (Exception e) {
            System.out.println("\n  [ERRO] " + e.getMessage());
            aguardar("\n  Pressione ENTER para continuar...");
            return false;
        }
    }

    // ── REMOVER (retorna true se sucesso) ─────────────────────────────────────
    private boolean removerFuncionario() {
        System.out.println("\n" + LIN);
        System.out.println("                REMOVER FUNCIONARIO");
        System.out.println(LIN);
        System.out.print("  Digite a matricula: ");
        int mat = lerInteiro();
        Funcionario f = service.buscarPorMatricula(mat);
        if (f == null) {
            System.out.println("  Matricula nao encontrada.");
            aguardar("  Pressione ENTER para continuar...");
            return false;
        }
        System.out.println("  Funcionario encontrado: " + f.getNome() + " (" + f.getTipo() + ")");
        System.out.print("  Confirmar remocao? (S/N): ");
        String conf = sc.nextLine().trim().toUpperCase();
        if (conf.equals("S")) {
            service.removerFuncionario(mat);
            System.out.println("\n  [OK] Funcionario removido.");
            return true;
        } else {
            System.out.println("  Operacao cancelada.");
            return false;
        }
    }

    // ── CONFIGURAÇÕES (loop interno) ─────────────────────────────────────────
    private void configuracoes() {
        int op = -1;
        while (op != 0) {
            System.out.println("\n" + LIN);
            System.out.println("           CONFIGURACOES DO SISTEMA");
            System.out.println(LIN);
            System.out.println("  Salario base atual: " + Funcionario.moeda(service.getSalarioBase()));
            System.out.println("  Teto de bonus (%%) do salario base: " + service.getTetoPercentual() + "%");
            System.out.println("  (Teto absoluto: " + Funcionario.moeda(service.getTetoBonusProducao()) + ")");
            System.out.println(LIN);
            System.out.println("  1 - Alterar salario base");
            System.out.println("  2 - Alterar teto de bonus (%)");
            System.out.println("  0 - Voltar");
            System.out.print("  Opcao: ");
            op = lerInteiro();

            switch (op) {
                case 1 -> {
                    System.out.print("  Novo salario base (R$): ");
                    double novo = lerDouble();
                    if (novo > 0) {
                        service.setSalarioBase(novo);
                        System.out.println("  [OK] Salario base alterado. Todos os calculos serao atualizados.");
                    } else {
                        System.out.println("  Valor invalido. Operacao cancelada.");
                    }
                }
                case 2 -> {
                    System.out.print("  Novo percentual do teto de bonus (%% do salario base): ");
                    double novoPct = lerDouble();
                    if (novoPct > 0) {
                        service.setTetoPercentual(novoPct);
                        System.out.println("  [OK] Teto de bonus alterado.");
                    } else {
                        System.out.println("  Percentual invalido. Operacao cancelada.");
                    }
                }
                case 0 -> System.out.println("  Voltando...");
                default -> System.out.println("  Opcao invalida.");
            }
        }
    }

    // ── GERAR FOLHA COM TOTAL ────────────────────────────────────────────────
    private void gerarFolha() {
        var lista = service.listar();

        System.out.println("\n" + SEP);
        System.out.println("              FOLHA DE PAGAMENTO MENSAL");
        System.out.printf ("              Total de funcionarios: %d%n", lista.size());
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
            System.out.println("  Salario base: " + Funcionario.moeda(Funcionario.getSalarioBase()) + " / mes");
            System.out.println("  " + f.getDetalheExtra());
            System.out.println("  Total mensal: " + Funcionario.moeda(f.calcularSalarioFinal()));
        }

        double total = service.calcularTotalFolha();
        System.out.println(LIN);
        System.out.println("  TOTAL DA FOLHA: " + Funcionario.moeda(total));
        System.out.println(SEP);
    }

    // ── EXPORTAR (retorna true se sucesso) ────────────────────────────────────
    private boolean exportar() {
        System.out.println("\n  Exportando dados...");
        String[] caminhos = service.exportar();
        System.out.println();
        if (caminhos[0] != null)
            System.out.println("  ✔ TSV gerado: " + caminhos[0]);
        else
            System.out.println("  ❌ Falha ao gerar TSV.");

        if (caminhos[1] != null)
            System.out.println("  ✔ XLS gerado: " + caminhos[1]);
        else
            System.out.println("  ❌ Falha ao gerar XLS. Verifique as permissoes da pasta 'exportados'.");

        return caminhos[0] != null;
    }

    // ── RESET (retorna true se sucesso) ───────────────────────────────────────
    private boolean resetar() {
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
            return true;
        } else {
            System.out.println("  Operacao cancelada.");
            return false;
        }
    }

    // ── CADASTRO PADRÃO (original) ───────────────────────────────────────────
    private void cadastrarPadrao() {
        System.out.println("\n" + LIN);
        System.out.println("               NOVO FUNCIONARIO PADRAO");
        System.out.println("      (digite 0 em qualquer campo para cancelar)");
        System.out.println(LIN);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        service.cadastrarPadrao(nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── CADASTRO COMISSIONADO (original) ─────────────────────────────────────
    private void cadastrarComissionado() {
        System.out.println("\n" + LIN);
        System.out.println("            NOVO FUNCIONARIO COMISSIONADO");
        System.out.println("      (digite 0 em qualquer campo para cancelar)");
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

    // ── CADASTRO PRODUÇÃO (original com mensagem clara) ──────────────────────
    private void cadastrarProducao() {
        System.out.println("\n" + LIN);
        System.out.println("             NOVO FUNCIONARIO DE PRODUCAO");
        System.out.println("      (digite 0 em qualquer campo para cancelar)");
        System.out.println(LIN);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        System.out.print("  Pecas produzidas no mes: ");
        int qtd = lerInteiroPositivoOuZero();
        if (qtd == 0) { cancelado(); return; }

        System.out.print("  Valor por peca (bonus liquido por unidade) (R$): ");
        double vpeca = lerDoubleNaoNegativo();
        if (vpeca < 0) { cancelado(); return; }
        if (service.bonusUltrapassaTeto(qtd, vpeca)) {
            System.out.println("\n  [BLOQUEIO] Bonus de " + Funcionario.moeda(qtd * vpeca) +
                            " ultrapassa o teto de " + Funcionario.moeda(service.getTetoBonusProducao()));
            System.out.println("  Cadastro bloqueado. Consulte a diretoria para casos excepcionais.");
            aguardar("\n  Pressione ENTER para voltar ao menu...");
            return;
        }
        service.cadastrarProducao(nome, mat, qtd, vpeca);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── ENCERRAR (original) ──────────────────────────────────────────────────
    private void encerrar() {
        service.salvar();
        System.out.println("\n" + SEP);
        System.out.println("              Dados salvos. Volte sempre!");
        System.out.println(SEP + "\n");
    }

    // ── UTILITÁRIOS DE LEITURA (idênticos aos originais) ─────────────────────
    private String lerTexto(String prompt) {
        System.out.print(prompt);
        String v = sc.nextLine().trim();
        while (v.isEmpty()) {
            System.out.print("  Campo obrigatorio. " + prompt.stripLeading());
            v = sc.nextLine().trim();
        }
        return v;
    }

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
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("  Inteiro invalido: ");
            }
        }
    }

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

    private double lerDoubleNaoNegativo() {
        while (true) {
            String linha = sc.nextLine().trim().replace(",", ".");
            if (linha.equals("0")) return -1;
            try {
                double v = Double.parseDouble(linha);
                if (v > 0) return v;
                System.out.print("  Valor deve ser maior que zero (ou 0 para cancelar): ");
            } catch (NumberFormatException e) {
                System.out.print("  Decimal invalido: ");
            }
        }
    }

    private double lerDouble() {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.print("  Numero invalido: ");
            }
        }
    }

    private double lerDoubleEdicao() {
        String linha = sc.nextLine().trim().replace(",", ".");
        if (linha.isEmpty()) return -1;
        try {
            return Double.parseDouble(linha);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int lerInteiroEdicao() {
        String linha = sc.nextLine().trim();
        if (linha.isEmpty()) return -1;
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return -1;
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