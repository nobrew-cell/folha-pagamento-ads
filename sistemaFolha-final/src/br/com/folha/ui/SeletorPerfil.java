package br.com.folha.ui;

import java.io.Reader;
import java.util.Scanner;

/**
 * Tela de seleção de perfil exibida no início de cada sessão.
 * Retorna o perfil escolhido pelo usuário: ADM ou FUNCIONARIO, só essas opções por enquanto.
 *
 * Regra especial: se for o primeiro acesso (sem database.tsv),
 * entra direto como ADM sem perguntar, pois não há dados ainda.
 */
public class SeletorPerfil {

    public enum Perfil { ADM, FUNCIONARIO }

    private static final String SEP = "======================================================";
    private static final String LIN = "------------------------------------------------------";

    /**
     * Determina o perfil da sessão atual.
     *
     * @param primeiraVez true se database.tsv não existe
     * @param stdin       reader compartilhado com ConsoleUI
     * @return Perfil selecionado
     */
    public static Perfil selecionar(boolean primeiraVez, Reader stdin) {
        if (primeiraVez) {
            System.out.println("\n" + SEP);
            System.out.println("      Bem-vindo ao Sistema de Folha de Pagamento");
            System.out.println("           Versao 7.1  |  Salarios mensais");
            System.out.println(SEP);
            System.out.println("  Primeiro acesso detectado. Nenhum dado cadastrado.");
            System.out.println("  Entrando automaticamente como ADMINISTRADOR.");
            System.out.println(LIN);
            System.out.println("  Pressione ENTER para continuar...");
            aguardar(stdin);
            return Perfil.ADM;
        }

        try (Scanner sc = new Scanner(stdin)) {
            while (true) {
                System.out.println("\n" + SEP);
                System.out.println("         Sistema de Folha de Pagamento  v7.1");
                System.out.println(SEP);
                System.out.println("  Como deseja acessar?");
                System.out.println(LIN);
                System.out.println("  [1] - Funcionario");
                System.out.println("  [2] - Administrador");
                System.out.println(SEP);
                System.out.print("  Opcao: ");

                String entrada = sc.nextLine().trim();
                switch (entrada) {
                    case "1" -> { return Perfil.FUNCIONARIO; }
                    case "2" -> { return Perfil.ADM; }
                    default  -> System.out.println("  Opcao invalida. Digite 1 ou 2: ");
                }
            }
        }
    }

    private static void aguardar(Reader stdin) {
        try { stdin.read(); } catch (Exception ignored) {}
    }
}