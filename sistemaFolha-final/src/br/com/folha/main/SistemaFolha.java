package br.com.folha.main;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import br.com.folha.repository.FuncionarioRepository;
import br.com.folha.service.FolhaService;
import br.com.folha.ui.ConsoleUI;

/**
 * Ponto de entrada. Monta as dependências e dá a partida.
 * Detecta se é a primeira execução pela ausência do database.tsv.
 */
public class SistemaFolha {

    public static void main(String[] args) {
        // Força saída e entrada em UTF-8 para acentos no CMD
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setIn(new java.io.BufferedInputStream(System.in));

        boolean primeiraVez = !new File("database.tsv").exists();

        FuncionarioRepository repository = new FuncionarioRepository();
        FolhaService service = null;
        try {
            service = new FolhaService(repository);
        } catch (Exception e) {
            // Isso aqui é uma gambiarra com propósito, vou deixar ela aqui, em vez do ConsoleUI;
            System.out.println("\n======================================================");
            System.out.println("  ERRO CRÍTICO NA INICIALIZAÇÃO");
            System.out.println("======================================================");
            System.out.println(e.getMessage());  // mensagem vinda do repository
            System.out.println("======================================================");
            System.out.println("O sistema NÃO será iniciado.");
            System.out.println("Corrija o arquivo database.tsv ou remova-o para recomeçar.");
            System.out.println("======================================================\n");
            return;  // encerra o programa sem entrar no menu
        }

        ConsoleUI ui = new ConsoleUI(service, primeiraVez);
        ui.iniciar();
    }
}