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
import br.com.folha.ui.SeletorPerfil.Perfil;
import br.com.folha.util.LoggerUtil;

public class ConsoleUI {
    private static final String SEP = "======================================================";
    private static final String LIN = "------------------------------------------------------";
    private static final String SEA = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

    private final Scanner      sc;
    private final FolhaService service;
    private final boolean      primeiraVez;
    private final boolean      modoADM;

    public ConsoleUI(FolhaService service, boolean primeiraVez, Perfil perfil, Reader stdin) {
        this.sc          = new Scanner(stdin);
        this.service     = service;
        this.primeiraVez = primeiraVez;
        this.modoADM     = (perfil == Perfil.ADM);
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
                    case 5 -> { if (modoADM) menuADM(); else System.out.println("  Opcao invalida. Tente novamente."); }
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
        String labelPerfil = modoADM ? "[ ADMINISTRADOR ]" : "[ FUNCIONARIO ]";
        System.out.println("\n" + SEP);
        System.out.println("        FOLHA DE PAGAMENTO  (salarios mensais)");
        System.out.println("              Perfil: " + labelPerfil);
        System.out.println(SEP);
        System.out.println("  [1] - Cadastrar Funcionario Padrao");
        System.out.println("  [2] - Cadastrar Funcionario Comissionado");
        System.out.println("  [3] - Cadastrar Funcionario de Producao");
        System.out.println("  [4] - Gerar Folha de Pagamento");
        if (modoADM) System.out.println("  [5] - Menu ADM (Manutencao de Dados)");
        System.out.println("  [0] - Sair");
        System.out.println(SEP);
        System.out.print("  Opcao: ");
    }

    private void exibirBoasVindas() {
        System.out.println("\n" + SEP);
        System.out.println("      Bem-vindo ao Sistema de Folha de Pagamento");
        System.out.println("           Versao 7.1  |  Salarios mensais");
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
                case 1 -> { if (exportar())        { msgConcluida(); return; } }
                case 2 -> { if (importarArquivo()) { msgConcluida(); return; } }
                case 3 -> { if (novoMes())         { msgConcluida(); return; } }
                case 6 -> { if (resetar())         { msgConcluida(); return; } }
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
            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                System.out.println("  Importacao cancelada.");
                return false;
            }
            caminho = chooser.getSelectedFile().getAbsolutePath();
        } catch (HeadlessException e) {
            System.out.print("  Caminho do arquivo TSV: ");
            caminho = sc.nextLine().trim();
            if (caminho.isEmpty()) { System.out.println("  Importacao cancelada."); return false; }
        }
        System.out.println("  Arquivo selecionado: " + caminho);
        System.out.println("\n  Validando arquivo...");
        try {
            List<Funcionario> importados = service.validarArquivoImportacao(caminho);
            System.out.println("  Arquivo valido. " + importados.size() + " funcionario(s) encontrado(s).");
            System.out.print("  Isso substituira TODOS os dados atuais. \n  Confirmar? (S/N): ");
            if (sc.nextLine().trim().toUpperCase().equals("S")) {
                service.importarArquivo(caminho);
                System.out.println("  (backup do estado anterior criado automaticamente)");
                LoggerUtil.logImport(caminho, importados.size());
                System.out.println("\n  [OK] Importacao concluida com sucesso.");
                return true;
            } else {
                System.out.println("  Importacao cancelada."); return false;
            }
        } catch (Exception e) {
            System.out.println("\n  ERRO: " + e.getMessage());
            System.out.println("  Importacao cancelada."); return false;
        }
    }

    // ── Novo mês ──────────────────────────────────────────────────────────
    private boolean novoMes() {
        System.out.println("\n" + SEA);
        System.out.println("                  INICIAR NOVO MES");
        System.out.println(SEA);
        System.out.println("  Isso salvara o mes atual na pasta 'historico/'");
        System.out.print("  Deseja copiar os funcionarios do mes anterior \n  (zerando vendas/pecas)? (S/N): ");
        boolean copiar = sc.nextLine().trim().toUpperCase().equals("S");
        System.out.print("  Deseja continuar? (S/N): ");
        if (sc.nextLine().trim().toUpperCase().equals("S")) {
            try {
                String hist = service.iniciarNovoMes(copiar);
                LoggerUtil.logNovoMes(copiar, hist);
                System.out.println("\n  [OK] Historico salvo em: " + hist);
                System.out.println(copiar ? "  Funcionarios copiados (com valores zerados)." : "  Base limpa. Novo mes iniciado.");
                return true;
            } catch (Exception e) {
                System.out.println("\n  [ERRO] " + e.getMessage()); return false;
            }
        } else {
            System.out.println("  Operacao cancelada."); return false;
        }
    }

    // ── Editar funcionário ────────────────────────────────────────────────
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
            aguardar("  Pressione ENTER para continuar..."); return;
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
        String novoTipo = null;
        boolean tipoAlterado = false;

        if (!tipoInput.isEmpty()) {
            try {
                int to = Integer.parseInt(tipoInput);
                novoTipo = switch (to) { case 1->"Padrao"; case 2->"Comissionado"; case 3->"Producao"; default->null; };
                if (novoTipo == null) System.out.println("  Opcao invalida. Tipo mantido.");
            } catch (NumberFormatException e) { System.out.println("  Opcao invalida. Tipo mantido."); }
            if (novoTipo != null) {
                if (novoTipo.equals(f.getTipo())) { System.out.println("  Esse e o tipo atual."); novoTipo = null; }
                else { tipoAlterado = true; System.out.println("  Tipo alterado para: " + novoTipo); }
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
                for (Funcionario s : similares)
                    System.out.println("    " + s.getNomeExibicao() + " (matricula " + s.getMatricula() + ")");
                System.out.print("  Continuar mesmo assim? (S/N): ");
                if (!sc.nextLine().trim().toUpperCase().equals("S")) { System.out.println("  Operacao cancelada."); return false; }
            }
        }

        Double  novasVendas = null, novoPercentual = null, novoValorPeca = null;
        Integer novaQtd = null;
        String tipoEfetivo = (novoTipo != null) ? novoTipo : f.getTipo();

        if (tipoAlterado) {
            System.out.println(LIN);
            System.out.println("  Novo tipo: " + tipoEfetivo);
            System.out.println("  Preencha os campos abaixo (ENTER pula - pode editar depois):");
            if (tipoEfetivo.equals("Comissionado")) {
                System.out.print("  Total de vendas (R$): "); double v=lerDoubleEdicao(); if(v>=0) novasVendas=v;
                System.out.print("  Percentual de comissao (%): "); double p=lerDoubleEdicao(); if(p>=0) novoPercentual=p;
            } else if (tipoEfetivo.equals("Producao")) {
                System.out.print("  Quantidade de pecas: "); int q=lerInteiroEdicao(); if(q>=0) novaQtd=q;
                System.out.print("  Bonus por peca (R$): "); double vp=lerDoubleEdicao(); if(vp>=0) novoValorPeca=vp;
            }
        } else {
            if (tipoEfetivo.equals("Comissionado")) {
                System.out.print("  Novo total de vendas (R$) (ENTER para manter): "); double v=lerDoubleEdicao(); if(v>=0) novasVendas=v;
                System.out.print("  Novo percentual de comissao (%) (ENTER para manter): "); double p=lerDoubleEdicao(); if(p>=0) novoPercentual=p;
            } else if (tipoEfetivo.equals("Producao")) {
                System.out.print("  Nova quantidade de pecas (ENTER para manter): "); int q=lerInteiroEdicao(); if(q>=0) novaQtd=q;
                System.out.print("  Novo bonus por peca (R$) (ENTER para manter): "); double vp=lerDoubleEdicao(); if(vp>=0) novoValorPeca=vp;
            }
        }

        try {
            service.editarFuncionarioCompleto(mat, novoNome, novoTipo, novasVendas, novoPercentual, novaQtd, novoValorPeca);
            LoggerUtil.logEdicao(mat, "Nome: " + novoNome + " | Tipo: " + tipoEfetivo);
            System.out.println("\n  [OK] Funcionario editado com sucesso.");
            return true;
        } catch (Exception e) {
            System.out.println("\n  [ERRO] " + e.getMessage());
            aguardar("\n  Pressione ENTER para continuar..."); return false;
        }
    }

    // ── Dashboard ─────────────────────────────────────────────────────────
    private void abrirDashboard() {
        System.out.println("\n" + SEA);
        System.out.println("              DASHBOARD ANALITICO v7.1");
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
        String tipoFiltro = switch (opcaoTipo) { case 1->"Padrao"; case 2->"Comissionado"; case 3->"Producao"; default->null; };
        if (tipoFiltro == null) { System.out.println("  Opcao invalida."); return; }

        List<Funcionario> filtrados = new ArrayList<>();
        for (Funcionario f : service.listar()) if (f.getTipo().equals(tipoFiltro)) filtrados.add(f);
        if (filtrados.isEmpty()) { System.out.println("  Nenhum funcionario do tipo " + tipoFiltro + " encontrado."); return; }

        System.out.println("\n  " + filtrados.size() + " funcionario(s) encontrado(s). [E]ditar / [N]pular / [Q]sair");
        int editados=0, pulados=0;
        for (int i=0; i<filtrados.size(); i++) {
            Funcionario f = filtrados.get(i);
            System.out.println("\n" + SEA);
            System.out.printf("  (%d/%d) %s  |  Matricula: %d%n", i+1, filtrados.size(), f.getNomeExibicao(), f.getMatricula());
            System.out.print("  Acao [E/N/Q]: ");
            switch (sc.nextLine().trim().toUpperCase()) {
                case "E" -> { if(editarFuncionarioComDados(f.getMatricula(),f)) editados++; else pulados++; }
                case "N" -> { System.out.println("  Pulado."); pulados++; }
                case "Q" -> { System.out.println("  Saindo do lote."); pulados+=filtrados.size()-i-1; i=filtrados.size(); }
                default  -> { System.out.println("  Opcao invalida. Pulando."); pulados++; }
            }
        }
        LoggerUtil.logEdicaoLote(tipoFiltro, editados, pulados);
        System.out.println("\n" + LIN);
        System.out.println("  Edicao em lote concluida. Editados: "+editados+"  Pulados: "+pulados);
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
        if (f == null) { System.out.println("  Matricula nao encontrada."); aguardar("  Pressione ENTER..."); return; }
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
            System.out.println("              CONFIGURACOES DO SISTEMA");
            System.out.println(SEA);
            System.out.println("  Salario base : " + Funcionario.moeda(service.getSalarioBase()));
            System.out.println("  Teto de bonus: " + service.getTetoPercentual()
                    + "% do salario base  (" + Funcionario.moeda(service.getTetoBonusAbsoluto()) + ")");
            System.out.println("  Limite maximo matricula: " +
                    (service.getLimiteMaximoMatricula() == 0 ? "Sem limite" : service.getLimiteMaximoMatricula()));
            System.out.println("  Sequencia de matricula : " +
                    (service.isModoSequenciaRigido() ? "RIGIDO (sem pulos)" : "FLEXIVEL (com aviso)"));
            System.out.println(LIN);
            System.out.println("  1 - Alterar salario base");
            System.out.println("  2 - Alterar teto de bonus (% do salario base)");
            System.out.println("  3 - Alterar limite maximo de matricula");
            System.out.println("  4 - Alterar modo de sequencia de matricula");
            System.out.println("  0 - Voltar");
            System.out.print("  Opcao: ");
            op = lerInteiro();
            switch (op) {
                case 1 -> {
                    System.out.print("  Novo salario base (R$): ");
                    double novo = lerDouble();
                    if (novo > 0) {
                        String ant = Funcionario.moeda(service.getSalarioBase());
                        service.setSalarioBase(novo);
                        LoggerUtil.logConfig("Salario base", ant, Funcionario.moeda(novo));
                        System.out.println("  [OK] Salario base alterado.");
                    } else { System.out.println("  Valor invalido."); }
                }
                case 2 -> {
                    System.out.print("  Novo percentual do teto de bonus (%): ");
                    double pct = lerDouble();
                    if (pct > 0) {
                        String ant = service.getTetoPercentual() + "%";
                        service.setTetoPercentual(pct);
                        LoggerUtil.logConfig("Teto bonus (%)", ant, pct + "%");
                        System.out.println("  [OK] Teto de bonus alterado.");
                    } else { System.out.println("  Percentual invalido."); }
                }
                case 3 -> {
                    System.out.println("  Limite atual: " +
                            (service.getLimiteMaximoMatricula()==0?"Sem limite":service.getLimiteMaximoMatricula()));
                    System.out.print("  Novo limite (0 = sem limite): ");
                    try {
                        int novoLimite = Integer.parseInt(sc.nextLine().trim());
                        if (novoLimite < 0) { System.out.println("  Nao pode ser negativo."); break; }
                        String ant = String.valueOf(service.getLimiteMaximoMatricula());
                        service.setLimiteMaximoMatricula(novoLimite);
                        LoggerUtil.logConfig("Limite matricula", ant, String.valueOf(novoLimite));
                        System.out.println("  [OK] Limite alterado para " +
                                (novoLimite==0?"sem limite":novoLimite)+".");
                    } catch (NumberFormatException e) { System.out.println("  Numero invalido."); }
                }
                case 4 -> {
                    System.out.println("  Modo atual: " + (service.isModoSequenciaRigido()?"RIGIDO":"FLEXIVEL"));
                    System.out.println("  1 - Rigido (proibe pulos, exige sequencia exata)");
                    System.out.println("  2 - Flexivel (avisa sobre pulos, mas permite)");
                    System.out.print("  Opcao: ");
                    try {
                        int modo = Integer.parseInt(sc.nextLine().trim());
                        if (modo == 1) {
                            service.setModoSequenciaRigido(true);
                            LoggerUtil.logConfig("Modo sequencia", "FLEXIVEL", "RIGIDO");
                            System.out.println("  [OK] Modo RIGIDO ativado.");
                        } else if (modo == 2) {
                            service.setModoSequenciaRigido(false);
                            LoggerUtil.logConfig("Modo sequencia", "RIGIDO", "FLEXIVEL");
                            System.out.println("  [OK] Modo FLEXIVEL ativado.");
                        } else { System.out.println("  Opcao invalida."); }
                    } catch (NumberFormatException e) { System.out.println("  Numero invalido."); }
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
        System.out.printf("              Total de funcionarios: %d%n", lista.size());
        System.out.println(SEP);
        if (lista.isEmpty()) { System.out.println("  Nenhum funcionario cadastrado ainda."); System.out.println(SEP); return; }
        for (Funcionario f : lista) {
            double sal = service.calcularSalarioFinalCompleto(f);
            System.out.println(LIN);
            System.out.println("  Nome:         " + f.getNomeExibicao());
            System.out.println("  Matricula:    " + f.getMatricula());
            System.out.println("  Tipo:         " + f.getTipo());
            System.out.println("  Salario base: " + Funcionario.moeda(service.getSalarioBase()) + " / mes");
            System.out.println("  " + f.getDetalheExtra());
            System.out.println("  Total mensal: " + Funcionario.moeda(sal));
        }
        System.out.println(LIN);
        System.out.println("           TOTAL DA FOLHA: " + Funcionario.moeda(service.calcularTotalFolha()));
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
        if (caminhos[0] != null) System.out.println("  [OK] TSV gerado: " + caminhos[0]);
        else System.out.println("  [ERRO] Falha ao gerar TSV.");
        if (caminhos[1] != null) System.out.println("  [OK] XLS gerado: " + caminhos[1]);
        else System.out.println("  [ERRO] Falha ao gerar XLS.");
        boolean ok = caminhos[0] != null;
        if (ok) LoggerUtil.logExport(caminhos[0]!=null?caminhos[0]:"ERRO", caminhos[1]!=null?caminhos[1]:"ERRO");
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
        if (!sc.nextLine().trim().equals("CONFIRMAR")) { System.out.println("  Operacao cancelada."); return false; }
        try {
            String backup = service.resetar();
            LoggerUtil.logReset(backup);
            System.out.println("\n  [OK] Sistema resetado. Backup salvo em: " + backup);
            return true;
        } catch (IOException e) {
            System.out.println("\n  [ERRO] " + e.getMessage());
            aguardar("  Pressione ENTER para continuar..."); return false;
        }
    }

    // ── Cadastro Padrão ───────────────────────────────────────────────────
    private void cadastrarPadrao() {
        System.out.println("\n" + SEA);
        System.out.println("               NOVO FUNCIONARIO PADRAO");
        System.out.println("      (nome/matricula: digite 0 para cancelar)");
        System.out.println(SEA);
        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }
        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }
        verificarNomeSimilar(nome);
        service.cadastrarPadrao(nome, mat);
        LoggerUtil.logCadastro("Padrao", nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── Cadastro Comissionado ─────────────────────────────────────────────
    private void cadastrarComissionado() {
        System.out.println("\n" + SEA);
        System.out.println("            NOVO FUNCIONARIO COMISSIONADO");
        System.out.println("             (0 cancela campos de texto)");
        System.out.println(SEA);
        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }
        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }
        if (!verificarNomeSimilar(nome)) return;
        double vendas = lerDoubleObrigatorio("  Total de vendas mensais (R$): ");
        double perc   = lerDoubleObrigatorio("  Percentual de comissao (%): ");
        service.cadastrarComissionado(nome, mat, vendas, perc);
        LoggerUtil.logCadastro("Comissionado", nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    // ── Cadastro Produção ─────────────────────────────────────────────────
    private void cadastrarProducao() {
        System.out.println("\n" + SEA);
        System.out.println("             NOVO FUNCIONARIO DE PRODUCAO");
        System.out.println("             (0  cancela campos de texto)");
        System.out.println(SEA);
        String nome = lerTexto("  Nome: ");
        if (nome.equals("0")) { cancelado(); return; }
        int mat = lerMatricula("  Matricula: ");
        if (mat == 0) { cancelado(); return; }
        if (!verificarNomeSimilar(nome)) return;
        int qtd = lerInteiroObrigatorio("  Pecas produzidas no mes: ");
        double vpeca = lerDoubleObrigatorio("  Bonus por peca - valor liquido (R$): ");
        if (service.bonusUltrapassaTeto(qtd, vpeca)) {
            System.out.println("\n  [BLOQUEIO] Bonus de " + Funcionario.moeda(qtd * vpeca) +
                            " ultrapassa o teto de " + Funcionario.moeda(service.getTetoBonusAbsoluto()));
            System.out.println("  Cadastro bloqueado. Consulte a diretoria para casos excepcionais.");
            aguardar("\n  Pressione ENTER para voltar ao menu..."); return;
        }
        service.cadastrarProducao(nome, mat, qtd, vpeca);
        LoggerUtil.logCadastro("Producao", nome, mat);
        System.out.println("\n  [OK] Funcionario cadastrado com sucesso.");
    }

    /** Verifica nome similar. Retorna false se o usuário cancelou, true caso contrário. */
    private boolean verificarNomeSimilar(String nome) {
        List<Funcionario> similares = service.buscarPorNomeSimilar(nome, -1);
        if (!similares.isEmpty()) {
            System.out.println("\n  Atencao: nome semelhante ja cadastrado:");
            for (Funcionario s : similares)
                System.out.println("    " + s.getNomeExibicao() + " (matricula " + s.getMatricula() + ")");
            System.out.print("  Continuar mesmo assim? (S/N): ");
            if (!sc.nextLine().trim().toUpperCase().equals("S")) {
                System.out.println("  Cadastro cancelado."); return false;
            }
        }
        return true;
    }

    // ── Leitura de matrícula com validações de limite e sequência ─────────
    private int lerMatricula(String prompt) {
        System.out.print(prompt);
        while (true) {
            String linha = sc.nextLine().trim();
            if (linha.equals("0")) return 0;
            int v;
            try {
                v = Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.print("  Numero invalido. " + prompt.stripLeading());
                continue;
            }
            if (v <= 0) {
                System.out.print("  Matricula deve ser maior que zero (ou 0 para cancelar): ");
                continue;
            }

            // Verifica limite máximo
            String erroLimite = service.validarLimiteMatricula(v);
            if (erroLimite != null) {
                System.out.println("  [LIMITE] " + erroLimite);
                System.out.print("  Digite outra matricula (ou 0 para cancelar): ");
                continue;
            }

            // Verifica duplicata
            if (service.matriculaExiste(v)) {
                System.out.printf("  Matricula %d ja esta em uso. Informe outra: ", v);
                continue;
            }

            // Verifica sequência rígida
            if (service.isModoSequenciaRigido()) {
                String erroSeq = service.validarSequenciaRigida(v);
                if (erroSeq != null) {
                    System.out.println("  [SEQUENCIA] " + erroSeq);
                    System.out.print("  Digite outra matricula (ou 0 para cancelar): ");
                    continue;
                }
            } else {
                // Modo flexível: avisa sobre buracos
                List<Integer> livres = service.matriculasLivresAntes(v);
                if (!livres.isEmpty()) {
                    int max = Math.min(livres.size(), 8);
                    List<Integer> exibir = livres.subList(0, max);
                    System.out.println("  [AVISO] Existem " + livres.size() +
                            " matricula(s) livre(s) antes deste numero:");
                    System.out.println("  " + exibir + (livres.size() > 8 ? " ..." : ""));
                    System.out.print("  Deseja continuar com a matricula " + v + "? (S/N): ");
                    String resp = sc.nextLine().trim().toUpperCase();
                    if (!resp.equals("S")) {
                        System.out.print("  Digite outra matricula (ou 0 para cancelar): ");
                        continue;
                    }
                }
            }

            return v;
        }
    }

    // ── Métodos de leitura numérica ───────────────────────────────────────

    /**
     * Leitura de double obrigatória.
     * Aceita vírgula e ponto como separador decimal.
     * Aceita ponto como separador de milhar (ex: "26.130,41" → 26130.41).
     */
    private double lerDoubleObrigatorio(String prompt) {
        System.out.print(prompt);
        while (true) {
            String linha = sc.nextLine().trim();
            // Normaliza: remove pontos de milhar ANTES da vírgula decimal, depois troca vírgula por ponto
            linha = normalizarDecimal(linha);
            try {
                double v = Double.parseDouble(linha);
                if (v >= 0) return v;
                System.out.print("  Nao pode ser negativo. Digite novamente: ");
            } catch (NumberFormatException e) {
                System.out.print("  Numero invalido. Use virgula para decimais (ex: 1500,00): ");
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

    private String lerTexto(String prompt) {
        System.out.print(prompt);
        String v = sc.nextLine().trim();
        while (v.isEmpty()) {
            System.out.print("  Campo obrigatorio (ou 0 para cancelar): ");
            v = sc.nextLine().trim();
        }
        return v;
    }

    private int lerInteiro() {
        while (true) {
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("  Inteiro invalido: "); }
        }
    }

    private double lerDouble() {
        while (true) {
            try { return Double.parseDouble(normalizarDecimal(sc.nextLine().trim())); }
            catch (NumberFormatException e) { System.out.print("  Numero invalido: "); }
        }
    }

    private double lerDoubleEdicao() {
        String linha = normalizarDecimal(sc.nextLine().trim());
        if (linha.isEmpty()) return -1;
        try { return Double.parseDouble(linha); }
        catch (NumberFormatException e) { return -1; }
    }

    private int lerInteiroEdicao() {
        String linha = sc.nextLine().trim();
        if (linha.isEmpty()) return -1;
        try { return Integer.parseInt(linha); }
        catch (NumberFormatException e) { return -1; }
    }

    /**
     * Normaliza entrada de número decimal do usuário.
     * "26.130,41" → "26130.41"
     * "1500,00"   → "1500.00"
     * "1500.00"   → "1500.00" (já no formato correto)
     */
    private String normalizarDecimal(String entrada) {
        if (entrada == null || entrada.isEmpty()) return entrada;
        // Se tem vírgula: ela é o separador decimal → pontos anteriores são milhar
        if (entrada.contains(",")) {
            return entrada.replace(".", "").replace(",", ".");
        }
        // Se não tem vírgula mas tem ponto: pode ser decimal americano, mantém
        return entrada;
    }

    private void aguardar(String msg) { System.out.print(msg); sc.nextLine(); }
    private void encerrar() {
        boolean salvo = service.salvar();
        System.out.println("\n" + SEP);
        if (salvo) System.out.println("              Dados salvos. Volte sempre!");
        else {
            System.out.println("  [AVISO] Falha ao salvar os dados!");
            System.out.println("  Os dados desta sessao podem ter sido perdidos.");
            System.out.println("  Verifique permissoes de escrita na pasta do sistema.");
        }
        System.out.println(SEP + "\n");
    }
    private void cancelado() { System.out.println("\n  Cadastro cancelado. Voltando ao menu."); }
}