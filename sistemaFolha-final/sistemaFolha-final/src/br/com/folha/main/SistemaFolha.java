package br.com.folha.main;

import java.io.File;

import br.com.folha.repository.FuncionarioRepository;
import br.com.folha.service.FolhaService;
import br.com.folha.ui.ConsoleUI;

/**
 * Ponto de entrada. Monta as dependências e dá a partida.
 * Detecta se é a primeira execução pela ausência do database.csv.
 */
public class SistemaFolha {

    public static void main(String[] args) {
        boolean primeiraVez = !new File("database.csv").exists();

        FuncionarioRepository repository = new FuncionarioRepository();
        FolhaService          service    = new FolhaService(repository);
        ConsoleUI             ui         = new ConsoleUI(service, primeiraVez);

        ui.iniciar();
    }
}
