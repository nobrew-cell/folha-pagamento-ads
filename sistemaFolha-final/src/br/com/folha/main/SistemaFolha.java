package br.com.folha.main;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import br.com.folha.repository.FuncionarioRepository;
import br.com.folha.service.FolhaService;
import br.com.folha.ui.ConsoleUI;
import br.com.folha.ui.SeletorPerfil;
import br.com.folha.ui.SeletorPerfil.Perfil;
import br.com.folha.util.LoggerUtil;

public class SistemaFolha {

    public static void main(String[] args) {
        Reader stdin = criarStdin();

        // ── Detecção de estado do banco de dados ─────────────────────────────
        // Três cenários possíveis pensados:
        //   1. Sem database + sem logs → primeiro acesso real → abre no ADM
        //   2. Sem database + com logs → provavelmente, database apagado acidentalmente → bloqueia
        //   3. Com database           → fluxo normal
        boolean temDatabase = new File("database.tsv").exists();
        boolean temLog      = LoggerUtil.contemPrimeiroAcesso();

        if (!temDatabase && temLog) {
            System.out.println("\n======================================================");
            System.out.println("                 DATABASE INEXISTENTE");
            System.out.println("======================================================");
            System.out.println(" O arquivo database.tsv nao foi encontrado,");
            System.out.println(" mas ha registros de sessoes anteriores no log.");
            System.out.println(" Isso indica que o banco de dados pode ter sido");
            System.out.println(" apagado acidentalmente.");
            System.out.println("------------------------------------------------------");
            System.out.println(" Contate o administrador do sistema.");
            System.out.println("======================================================\n");
            return;
        }

        boolean primeiraVez = !temDatabase;

        // ── Registro de início no log ─────────────────────────────────────
        if (primeiraVez) {
            LoggerUtil.logPrimeiroAcesso("3.x");
        }

        FuncionarioRepository repository = new FuncionarioRepository();
        FolhaService service = null;
        try {
            service = new FolhaService(repository);
        } catch (Exception e) {
            System.out.println("\n======================================================");
            System.out.println("            ERRO CRITICO NA INICIALIZACAO");
            System.out.println("======================================================");
            System.out.println(e.getMessage());
            System.out.println("======================================================");
            System.out.println(" O sistema NAO sera iniciado.");
            System.out.println(" Corrija o arquivo database.tsv ou remova-o para recomecar.");
            System.out.println("======================================================\n");
            return;
        }

        Perfil perfil = SeletorPerfil.selecionar(primeiraVez, stdin);

        if (!primeiraVez) {
            try {
                int total = repository.carregar().funcionarios.size();
                LoggerUtil.logInicializacao("3.x", total);
            } catch (Exception e) {
                LoggerUtil.logInicializacao("3.x", 0);
            }
        }

        ConsoleUI ui = new ConsoleUI(service, primeiraVez, perfil, stdin);
        ui.iniciar();
    }

    private static Reader criarStdin() {
        Console console = System.console();
        if (console != null) {
            return console.reader();
        }
        return new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8)
        );
    }
}