package br.com.folha.ui;

import java.awt.HeadlessException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.Reader;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import br.com.folha.model.Funcionario;
import br.com.folha.service.FolhaService;
import br.com.folha.util.LoggerUtil;

public class ConsoleUI {
    // SEP (Separator): Utilizada em Menus Principais para delimitar cabeçalhos e rodapés de navegação.
    private static final String SEP = "======================================================";
    // LIN (Line): Linha divisora estática, aplicada majoritariamente em tabelas e listagens de dados (leitura).
    private static final String LIN = "------------------------------------------------------";
    // SEA (Wave): Linha dinâmica (ondas), exclusiva para áreas de edição, alertas críticos ou entrada de dados (escrita).
    private static final String SEA = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

    private final Scanner      sc;
    private final FolhaService service;
    private final boolean      primeiraVez;

    public ConsoleUI(FolhaService service, boolean primeiraVez, Reader stdin) {
        this.sc          = new Scanner(stdin);
        this.service     = service;
        this.primeiraVez = primeiraVez;
    }

    public void iniciar() {
        if (primeiraVez) exibirBoasVindas();

        int opcao = -1;
        try {
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
        } finally {
            sc.close();
        }
    }

    // ── Menu principal ────────────────────────────────────────────────────

    private void exibirMenu() {
        System.out.println("\n" + SEP);
        System.out.println("        FOLHA DE PAGAMENTO  (salarios mensais)");
        System.out.println(SEP);
        System.out.println("  [1] - Cadastrar Funcionario Padrao");
        System.out.println("  [2] - Cadastrar Funcionario Comissionado");
        System.out.println("  [3] - Cadastrar Funcionario de Producao");
        System.out.println("  [4] - Gerar Folha de Pagamento");
        System.out.println("  [5] - Menu ADM (Manutencao de Dados)");
        System.out.println("  [0] - Sair");
        System.out.println(SEP);
        System.out.print("  Opcao: ");
    }

    private void exibirBoasVindas() {
        System.out.println("\n" + SEP);
        System.out.println("      Bem-vindo ao Sistema de Folha de Pagamento");
        System.out.println("           Versao 4.1  |  Salarios mensais");
        System.out.println(SEP);
        System.out.println("  Este e o seu primeiro acesso.");
        System.out.println("  Nenhum funcionario cadastrado ainda.");
        System.out.println(LIN);
        aguardar("  Pressione ENTER para continuar...");
    }

    // ── Menu ADM ──────────────────────────────────────────────────────────
    private void menuADM() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n" + SEP);
            System.out.println("                  MENU ADMINISTRATIVO");
            System.out.println(SEP);
            System.out.println("  (1) - Exportar TSV e XLS");
            System.out.println("  (2) - Importar arquivo TSV (substitui todos os dados)");
            System.out.println("  (3) - Novo mes (arquiva atual e limpa a base)");
            System.out.println("  (4) - Editar funcionario");
            System.out.println("  (5) - Remover funcionario");
            System.out.println("  (6) - Resetar sistema (backup automatico)");
            System.out.println("  (7) - Configuracoes do sistema");
            System.out.println("  (8) - Editar funcionarios em lote (por tipo)");
            System.out.println("  (9) - Abrir Dashboard Analitico");
            System.out.println("  (0) - Voltar ao menu principal");
            System.out.println(SEP);
            System.out.print("  Opcao: ");

            opcao = lerInteiro();
            switch (opcao) {
                // Operações únicas/destrutivas — fecham o menu ADM após sucesso
                case 1 -> { if (exportar())        { msgConcluida(); return; } }
                case 2 -> { if (importarArquivo()) { msgConcluida(); return; } }
                case 3 -> { if (novoMes())         { msgConcluida(); return; } }
                case 6 -> { if (resetar())         { msgConcluida(); return; } }

                // Operações repetíveis — permanecem no menu ADM
                case 4 -> editarFuncionario();
                case 5 -> removerFuncionario();
                case 7 -> configuracoes();
                case 8 -> editarLote();
                case 9 -> abrirDashboard();

                case 0 -> System.out.println("  Voltando ao menu principal...");
                default -> System.out.println("  Opcao invalida.");
            }
        }
    }

    private void msgConcluida() {
        System.out.println("\n  Operacao concluida. Retornando ao menu principal...");
    }

    // ── Importar ──────────────────────────────────────────────────────────
    private boolean importarArquivo() {
        System.out.println("\n" + SEA);
        System.out.println("            IMPORTAR DADOS DE ARQUIVO TSV");
        System.out.println(SEA);

        String caminho = null;

        try {
            System.out.println("  Abrindo seletor de arquivos...");
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileFilter(new FileNameExtensionFilter("Arquivos TSV", "tsv"));
            int resultado = chooser.showOpenDialog(null);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                System.out.println("  Importacao cancelada.");
                return false;
            }
            caminho = chooser.getSelectedFile().getAbsolutePath();
        } catch (HeadlessException e) {
            System.out.print("  Caminho do arquivo TSV: ");
            caminho = sc.nextLine().trim();
            if (caminho.isEmpty()) {
                System.out.println("  Importacao cancelada.");
                return false;
            }
        }

        System.out.println("  Arquivo selecionado: " + caminho);
        System.out.println("\n  Validando arquivo...");

        try {
            List<Funcionario> importados = service.validarArquivoImportacao(caminho);
            System.out.println("  Arquivo valido. " + importados.size() + " funcionario(s) encontrado(s).");
            System.out.print("  Isso substituira TODOS os dados atuais. \n  Confirmar? (S/N): ");
            String conf = sc.nextLine().trim().toUpperCase();
            if (conf.equals("S")) {
                service.importarArquivo(caminho);
                System.out.println("  (backup do estado anterior criado automaticamente)");
                LoggerUtil.logImport(caminho, importados.size());
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

    // ── Novo mês ──────────────────────────────────────────────────────────
    private boolean novoMes() {
        System.out.println("\n" + SEA);
        System.out.println("                  INICIAR NOVO MES");
        System.out.println(SEA);
        System.out.println("  Isso salvara o mes atual na pasta 'historico/'");
        System.out.print("  Deseja copiar os funcionarios do mes anterior \n  (zerando vendas/pecas)? (S/N): ");
        String copiar = sc.nextLine().trim().toUpperCase();
        boolean copiarFuncionarios = copiar.equals("S");

        System.out.print("  Deseja continuar? (S/N): ");
        String conf = sc.nextLine().trim().toUpperCase();
        if (conf.equals("S")) {
            try {
                String arquivoHistorico = service.iniciarNovoMes(copiarFuncionarios);
                LoggerUtil.logNovoMes(copiarFuncionarios, arquivoHistorico);
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

    // ── Editar funcionário (individual) ───────────────────────────────────
    private void editarFuncionario() {
        System.out.println("\n" + SEA);
        System.out.println("                 EDITAR FUNCIONARIO");
        System.out.println(SEA);
        System.out.print("  Digite a matricula (0 para cancelar): ");
        int mat = lerInteiro();
        if (mat == 0) { System.out.println("  Operacao cancelada."); return; }

        Funcionario f = service.buscarPorMatricula(mat);
        if (f == null) {
            System.out.println("  Matricula nao encontrada.");
            aguardar("  Pressione ENTER para continuar...");
            return;
        }

        editarFuncionarioComDados(mat, f);
    }

    private boolean editarFuncionarioComDados(int mat, Funcionario f) {
        System.out.println("\n  Dados atuais:");
        System.out.println("  Nome: " + f.getNomeExibicao());
        System.out.println("  Tipo: " + f.getTipo());
        if (f.getTipo().equals("Comissionado")) {
            System.out.println("  Vendas: " + Funcionario.moeda(service.getVendasFuncionario(mat)));
            System.out.println("  Percentual: " + service.getPercentualFuncionario(mat) + "%");
        } else if (f.getTipo().equals("Producao")) {
            System.out.println("  Pecas produzidas: " + service.getQuantidadePecasFuncionario(mat));
            System.out.println("  Bonus por peca: " + Funcionario.moeda(service.getValorPecaFuncionario(mat)));
        }

        System.out.println("------------------ ANTES DISSO... --------------------");
        System.out.println("  Deseja alterar o tipo do funcionario?");
        System.out.println("------------------------------------------------------");
        System.out.println("    1 - Padrao       |     2 - Comissionado             ");
        System.out.println("    3 - Producao     |     ENTER - Manter (" + f.getTipo() + ")");
        System.out.println("------------------------------------------------------");

        System.out.print("  Opcao: ");
        String tipoInput = sc.nextLine().trim();
        String novoTipo  = null;
        boolean tipoAlterado = false;

        if (!tipoInput.isEmpty()) {
            try {
                int tipoOpcao = Integer.parseInt(tipoInput);
                switch (tipoOpcao) {
                    case 1 -> novoTipo = "Padrao";
                    case 2 -> novoTipo = "Comissionado";
                    case 3 -> novoTipo = "Producao";
                    default -> System.out.println("  Opcao invalida. Tipo mantido.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  Opcao invalida. Tipo mantido.");
            }

            if (novoTipo != null) {
                if (novoTipo.equals(f.getTipo())) {
                    System.out.println("  Esse e o tipo atual. Nenhuma alteracao realizada.");
                    novoTipo = null;
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

        if (!novoNome.equals(f.getNome())) {
            List<Funcionario> similares = service.buscarPorNomeSimilar(novoNome, mat);
            if (!similares.isEmpty()) {
                System.out.println("\n  Atencao: nome semelhante ja cadastrado:");
                for (Funcionario s : similares) {
                    System.out.println("    " + s.getNomeExibicao() + " (matricula " + s.getMatricula() + ")");
                }
                System.out.print("  Continuar mesmo assim? (S/N): ");
                if (!sc.nextLine().trim().toUpperCase().equals("S")) {
                    System.out.println("  Operacao cancelada.");
                    return false;
                }
            }
        }

        Double  novasVendas    = null;
        Double  novoPercentual = null;
        Integer novaQtd        = null;
        Double  novoValorPeca  = null;

        String tipoEfetivo = (novoTipo != null) ? novoTipo : f.getTipo();

        if (tipoAlterado) {
            System.out.println(LIN);
            System.out.println("  Novo tipo: " + tipoEfetivo);
            System.out.println("  Preencha os campos abaixo (ENTER pula - pode editar depois):");
            if (tipoEfetivo.equals("Comissionado")) {
                System.out.print("  Total de vendas (R$): ");
                double v = lerDoubleEdicao();
                if (v >= 0) novasVendas = v;
                System.out.print("  Percentual de comissao (%): ");
                double p = lerDoubleEdicao();
                if (p >= 0) novoPercentual = p;
            } else if (tipoEfetivo.equals("Producao")) {
                System.out.print("  Quantidade de pecas: ");
                int q = lerInteiroEdicao();
                if (q >= 0) novaQtd = q;
                System.out.print("  Bonus por peca (R$): ");
                double vp = lerDoubleEdicao();
                if (vp >= 0) novoValorPeca = vp;
            }
        } else {
            if (tipoEfetivo.equals("Comissionado")) {
                System.out.print("  Novo total de vendas (R$) (ENTER para manter): ");
                double v = lerDoubleEdicao();
                if (v >= 0) novasVendas = v;
                System.out.print("  Novo percentual de comissao (%) (ENTER para manter): ");
                double p = lerDoubleEdicao();
                if (p >= 0) novoPercentual = p;
            } else if (tipoEfetivo.equals("Producao")) {
                System.out.print("  Nova quantidade de pecas (ENTER para manter): ");
                int q = lerInteiroEdicao();
                if (q >= 0) novaQtd = q;
                System.out.print("  Novo bonus por peca (R$) (ENTER para manter): ");
                double vp = lerDoubleEdicao();
                if (vp >= 0) novoValorPeca = vp;
            }
        }

        try {
            service.editarFuncionarioCompleto(mat, novoNome, novoTipo,
                    novasVendas, novoPercentual, novaQtd, novoValorPeca);
            LoggerUtil.logEdicao(mat,
                "Nome: " + novoNome + " | Tipo: " + tipoEfetivo);
            System.out.println("\n  [OK] Funcionario editado com sucesso.");
            return true;
        } catch (Exception e) {
            System.out.println("\n  [ERRO] " + e.getMessage());
            aguardar("\n  Pressione ENTER para continuar...");
            return false;
        }
    }

    // ── Dashboard analítico ───────────────────────────────────────────────
    private void abrirDashboard() {
        System.out.println("\n" + SEA);
        System.out.println("              DASHBOARD ANALITICO v5.1");
        System.out.println(SEA);
        System.out.println("  Abrindo janela grafica em segundo plano...");
        System.out.println("  O terminal continua disponivel normalmente.");
        System.out.println("  Feche a janela do dashboard para libera-la.");
        System.out.println(SEA);
        try {
            DashboardBI.abrir();
        } catch (Exception e) {
            System.out.println("  [ERRO] Nao foi possivel abrir o dashboard.");
            System.out.println("  Detalhe: " + e.getMessage());
            System.out.println("  Verifique se o ambiente suporta interface grafica (Swing/AWT).");
        }
    }

    // ── Edição em lote ────────────────────────────────────────────────────
    private void editarLote() {
        System.out.println("\n" + SEA);
        System.out.println("              EDICAO EM LOTE POR TIPO");
        System.out.println(SEA);
        System.out.println("  Escolha o tipo a editar em lote:");
        System.out.println("------------------------------------------------------");
        System.out.println("    |    1 - Padrao     |    2 - Comissionado    |");
        System.out.println("    |    3 - Producao   |    0 - Cancelar        |");
        System.out.println("------------------------------------------------------");
        System.out.print("  Opcao: ");

        int opcaoTipo = lerInteiro();
        if (opcaoTipo == 0) { System.out.println("  Cancelado."); return; }

        String tipoFiltro;
        switch (opcaoTipo) {
            case 1 -> tipoFiltro = "Padrao";
            case 2 -> tipoFiltro = "Comissionado";
            case 3 -> tipoFiltro = "Producao";
            default -> { System.out.println("  Opcao invalida."); return; }
        }

        List<Funcionario> todos   = new ArrayList<>(service.listar());
        List<Funcionario> filtrados = new ArrayList<>();
        for (Funcionario f : todos) {
            if (f.getTipo().equals(tipoFiltro)) filtrados.add(f);
        }

        if (filtrados.isEmpty()) {
            System.out.println("  Nenhum funcionario do tipo " + tipoFiltro + " encontrado.");
            return;
        }

        System.out.println("\n  " + filtrados.size() + " funcionario(s) do tipo " + tipoFiltro + " encontrado(s).");
        System.out.println("  Durante o lote: [E] Editar  [N] Pular  [Q] Sair");

        int editados = 0;
        int pulados  = 0;

        for (int i = 0; i < filtrados.size(); i++) {
            Funcionario f = filtrados.get(i);
            System.out.println("\n" + SEA);
            System.out.printf("  (%d/%d) %s  |  Matricula: %d%n",
                    i + 1, filtrados.size(), f.getNomeExibicao(), f.getMatricula());
            System.out.print("  Acao [E/N/Q]: ");
            String acao = sc.nextLine().trim().toUpperCase();

            switch (acao) {
                case "E" -> {
                    if (editarFuncionarioComDados(f.getMatricula(), f)) editados++;
                    else pulados++;
                }
                case "N" -> {
                    System.out.println("  Pulado.");
                    pulados++;
                }
                case "Q" -> {
                    System.out.println("  Saindo do lote.");
                    pulados += filtrados.size() - i - 1;
                    i = filtrados.size();
                }
                default -> {
                    System.out.println("  Opcao invalida. Pulando.");
                    pulados++;
                }
            }
        }

        LoggerUtil.logEdicaoLote(tipoFiltro, editados, pulados);
        System.out.println("\n" + LIN);
        System.out.println("  Edicao em lote concluida.");
        System.out.println("  Editados: " + editados + "  |  Pulados: " + pulados);
    }

    // ── Remover funcionário ───────────────────────────────────────────────
    private void removerFuncionario() {
        System.out.println("\n" + SEA);
        System.out.println("                REMOVER FUNCIONARIO");
        System.out.println(SEA);
        System.out.print("  Digite a matricula (0 para cancelar): ");
        int mat = lerInteiro();
        if (mat == 0) { System.out.println("  Operacao cancelada."); return; }

        Funcionario f = service.buscarPorMatricula(mat);
        if (f == null) {
            System.out.println("  Matricula nao encontrada.");
            aguardar("  Pressione ENTER para continuar...");
            return;
        }
        System.out.println("  Funcionario encontrado: " + f.getNomeExibicao() + " (" + f.getTipo() + ")");
        System.out.print("  Confirmar remocao? (S/N): ");
        if (sc.nextLine().trim().toUpperCase().equals("S")) {
            LoggerUtil.logRemocao(f.getNome(), mat);
            service.removerFuncionario(mat);
            System.out.println("\n  [OK] Funcionario removido.");
        } else {
            System.out.println("  Operacao cancelada.");
        }
    }

    // ── Configurações ─────────────────────────────────────────────────────
    private void configuracoes() {
        int op = -1;
        while (op != 0) {
            System.out.println("\n" + SEA);
            System.out.println("           CONFIGURACOES DO SISTEMA");
            System.out.println(SEA);
            System.out.println("  Salario base atual : " + Funcionario.moeda(service.getSalarioBase()));
            System.out.println("  Teto de bonus      : " + service.getTetoPercentual()
                    + "% do salario base  (" + Funcionario.moeda(service.getTetoBonusAbsoluto()) + ")");
            System.out.println(LIN);
            System.out.println("  1 - Alterar salario base");
            System.out.println("  2 - Alterar teto de bonus (% do salario base)");
            System.out.println("  0 - Voltar");
            System.out.print("  Opcao: ");
            op = lerInteiro();

            switch (op) {
                case 1 -> {
                    System.out.print("  Novo salario base (R$): ");
                    double novo = lerDouble();
                    if (novo > 0) {
                        String anterior = Funcionario.moeda(service.getSalarioBase());
                        service.setSalarioBase(novo);
                        LoggerUtil.logConfig("Salario base", anterior, Funcionario.moeda(novo));
                        System.out.println("  [OK] Salario base alterado.");
                    } else {
                        System.out.println("  Valor invalido. Operacao cancelada.");
                    }
                }
                case 2 -> {
                    System.out.print("  Novo percentual do teto de bonus (%% do salario base): ");
                    double novoPct = lerDouble();
                    if (novoPct > 0) {
                        String anterior = service.getTetoPercentual() + "%";
                        service.setTetoPercentual(novoPct);
                        LoggerUtil.logConfig("Teto bonus (%)", anterior, novoPct + "%");
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

    // ── Gerar folha ───────────────────────────────────────────────────────
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
            double salarioFinal = service.calcularSalarioFinalCompleto(f);
            System.out.println(LIN);
            System.out.println("  Nome:         " + f.getNomeExibicao());
            System.out.println("  Matricula:    " + f.getMatricula());
            System.out.println("  Tipo:         " + f.getTipo());
            System.out.println("  Salario base: " + Funcionario.moeda(service.getSalarioBase()) + " / mes");
            System.out.println("  " + f.getDetalheExtra());
            System.out.println("  Total mensal: " + Funcionario.moeda(salarioFinal));
        }

        System.out.println(LIN);
        System.out.println("  TOTAL DA FOLHA: " + Funcionario.moeda(service.calcularTotalFolha()));
        System.out.println(SEP);
    }

    // ── Exportar ──────────────────────────────────────────────────────────
    private boolean exportar() {
        System.out.println("\n" + SEA);
        System.out.println("                 EXPORTAR TSV E XLS");
        System.out.println(SEA);
        System.out.println("\n  Exportando dados...");
        String[] caminhos = service.exportar();
        System.out.println();
        if (caminhos[0] != null)
            System.out.println("  [OK] TSV gerado: " + caminhos[0]);
        else
            System.out.println("  [ERRO] Falha ao gerar TSV. Verifique permissoes da pasta 'exportados'.");

        if (caminhos[1] != null)
            System.out.println("  [OK] XLS gerado: " + caminhos[1]);
        else
            System.out.println("  [ERRO] Falha ao gerar XLS. Verifique permissoes da pasta 'exportados'.");

        boolean ok = caminhos[0] != null;
        if (ok) LoggerUtil.logExport(
                caminhos[0] != null ? caminhos[0] : "ERRO",
                caminhos[1] != null ? caminhos[1] : "ERRO");
        return ok;
    }

    // ── Reset ─────────────────────────────────────────────────────────────
    private boolean resetar() {
        System.out.println("\n" + SEA);
        System.out.println("              RESET DE DADOS DO SISTEMA");
        System.out.println(SEA);
        System.out.println("  ATENCAO: Esta acao apagara todos os funcionarios.");
        System.out.println("  Um backup automatico sera salvo antes.");
        System.out.println(SEA);
        System.out.print("  Digite CONFIRMAR para prosseguir: ");
        String confirm = sc.nextLine().trim();

        if (!confirm.equals("CONFIRMAR")) {
            System.out.println("  Operacao cancelada.");
            return false;
        }

        try {
            String backup = service.resetar();
            LoggerUtil.logReset(backup);
            System.out.println("\n  [OK] Sistema resetado.");
            System.out.println("  Backup salvo em: " + backup);
            return true;
        } catch (IOException e) {
            System.out.println("\n  [ERRO] Nao foi possivel limpar o banco de dados.");
            System.out.println("  Detalhes: " + e.getMessage());
            System.out.println("  Os dados em memoria foram preservados. Nenhum dado foi perdido.");
            aguardar("  Pressione ENTER para continuar...");
            return false;
        }
    }

    // ── Cadastro Padrão (cancelamento só com 0 no nome/matrícula) ──────────
    private void cadastrarPadrao() {
        System.out.println("\n" + SEA);
        System.out.println("               NOVO FUNCIONARIO PADRAO");
        System.out.println("      (nome/matricula: digite 0 para cancelar)");
        System.out.println(SEA);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        List<Funcionario> similares = service.buscarPorNomeSimilar(nome, -1);
        if (!similares.isEmpty()) {
            System.out.println("\n  Atencao: nome semelhante ja cadastrado:");
            for (Funcionario s : similares) {
                System.out.println("    " + s.getNomeExibicao() + " (matricula " + s.getMatricula() + ")");
            }
            System.out.print("  Continuar mesmo assim? (S/N): ");
            if (!sc.nextLine().trim().toUpperCase().equals("S")) {
                System.out.println("  Cadastro cancelado.");
                return;
            }
        }

        service.cadastrarPadrao(nome, mat);
        LoggerUtil.logCadastro("Padrao", nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── Cadastro Comissionado (campos numéricos obrigatórios; ENTER não cancela) ──
    private void cadastrarComissionado() {
        System.out.println("\n" + SEA);
        System.out.println("            NOVO FUNCIONARIO COMISSIONADO");
        System.out.println("             (0 cancela campos de texto)");
        System.out.println(SEA);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        List<Funcionario> similares = service.buscarPorNomeSimilar(nome, -1);
        if (!similares.isEmpty()) {
            System.out.println("\n  Atencao: nome semelhante ja cadastrado:");
            for (Funcionario s : similares) {
                System.out.println("    " + s.getNomeExibicao() + " (matricula " + s.getMatricula() + ")");
            }
            System.out.print("  Continuar mesmo assim? (S/N): ");
            if (!sc.nextLine().trim().toUpperCase().equals("S")) {
                System.out.println("  Cadastro cancelado.");
                return;
            }
        }

        double vendas = lerDoubleObrigatorio("  Total de vendas mensais (R$): ");
        double perc   = lerDoubleObrigatorio("  Percentual de comissao (%): ");

        service.cadastrarComissionado(nome, mat, vendas, perc);
        LoggerUtil.logCadastro("Comissionado", nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── Cadastro Produção (campos numéricos obrigatórios; ENTER não cancela) ──
    private void cadastrarProducao() {
        System.out.println("\n" + SEA);
        System.out.println("             NOVO FUNCIONARIO DE PRODUCAO");
        System.out.println("             (0  cancela campos de texto)");
        System.out.println(SEA);

        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }

        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }

        List<Funcionario> similares = service.buscarPorNomeSimilar(nome, -1);
        if (!similares.isEmpty()) {
            System.out.println("\n  Atencao: nome semelhante ja cadastrado:");
            for (Funcionario s : similares) {
                System.out.println("    " + s.getNomeExibicao() + " (matricula " + s.getMatricula() + ")");
            }
            System.out.print("  Continuar mesmo assim? (S/N): ");
            if (!sc.nextLine().trim().toUpperCase().equals("S")) {
                System.out.println("  Cadastro cancelado.");
                return;
            }
        }

        int qtd = lerInteiroObrigatorio("  Pecas produzidas no mes: ");
        double vpeca = lerDoubleObrigatorio("  Bonus por peca - valor liquido (R$): ");

        if (service.bonusUltrapassaTeto(qtd, vpeca)) {
            System.out.println("\n  [BLOQUEIO] Bonus de " + Funcionario.moeda(qtd * vpeca) +
                            " ultrapassa o teto de " + Funcionario.moeda(service.getTetoBonusAbsoluto()));
            System.out.println("  Cadastro bloqueado. Consulte a diretoria para casos excepcionais.");
            aguardar("\n  Pressione ENTER para voltar ao menu...");
            return;
        }

        service.cadastrarProducao(nome, mat, qtd, vpeca);
        LoggerUtil.logCadastro("Producao", nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── NOVOS MÉTODOS DE LEITURA OBRIGATÓRIA (sem cancelamento por ENTER) ──
    private double lerDoubleObrigatorio(String prompt) {
        System.out.print(prompt);
        while (true) {
            String linha = sc.nextLine().trim().replace(",", ".");
            try {
                double v = Double.parseDouble(linha);
                if (v >= 0) return v;
                System.out.print("  Nao pode ser negativo. Digite novamente: ");
            } catch (NumberFormatException e) {
                System.out.print("  Numero invalido. Digite novamente: ");
            }
        }
    }

    private int lerInteiroObrigatorio(String prompt) {
        System.out.print(prompt);
        while (true) {
            String linha = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(linha);
                if (v >= 0) return v;
                System.out.print("  Nao pode ser negativo. Digite novamente: ");
            } catch (NumberFormatException e) {
                System.out.print("  Numero invalido. Digite novamente: ");
            }
        }
    }

    // ── MÉTODOS EXISTENTES (preservados e inalterados) ────────────────────
    private String lerTexto(String prompt) {
        System.out.print(prompt);
        String v = sc.nextLine().trim();
        while (v.isEmpty()) {
            System.out.print("  Campo obrigatorio (ou 0 para cancelar): ");
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
                    System.out.print("  Matricula deve ser maior que zero (ou 0 para cancelar): ");
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

    private void encerrar() {
        boolean salvo = service.salvar();
        System.out.println("\n" + SEP);
        if (salvo) {
            System.out.println("              Dados salvos. Volte sempre!");
        } else {
            System.out.println("  [AVISO] Falha ao salvar os dados!");
            System.out.println("  Os dados desta sessao podem ter sido perdidos.");
            System.out.println("  Verifique permissoes de escrita na pasta do sistema.");
        }
        System.out.println(SEP + "\n");
    }

    private void cancelado() {
        System.out.println("\n  Cadastro cancelado. Voltando ao menu.");
    }
}