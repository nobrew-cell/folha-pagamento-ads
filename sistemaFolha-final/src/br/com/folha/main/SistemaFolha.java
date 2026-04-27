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

public class SistemaFolha {

    public static void main(String[] args) {
        Reader stdin = criarStdin();

        boolean primeiraVez = !new File("database.tsv").exists();

        FuncionarioRepository repository = new FuncionarioRepository();
        FolhaService service = null;
        try {
            service = new FolhaService(repository);
        } catch (Exception e) {
            System.out.println("\n======================================================");
            System.out.println("  ERRO CRITICO NA INICIALIZACAO");
            System.out.println("======================================================");
            System.out.println(e.getMessage());
            System.out.println("======================================================");
            System.out.println("O sistema NAO sera iniciado.");
            System.out.println("Corrija o arquivo database.tsv ou remova-o para recomecar.");
            System.out.println("======================================================\n");
            return;
        }

        ConsoleUI ui = new ConsoleUI(service, primeiraVez, stdin);
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