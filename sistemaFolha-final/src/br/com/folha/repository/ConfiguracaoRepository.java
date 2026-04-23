package br.com.folha.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import br.com.folha.model.Configuracao;

public class ConfiguracaoRepository {
    private static final String ARQUIVO = "config.properties";

    public static void carregar() {
        File file = new File(ARQUIVO);
        if (!file.exists()) {
            salvar(); // cria com valores padrão
            return;
        }
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            double salarioBase = Double.parseDouble(props.getProperty("salarioBase", "2000.00"));
            double tetoPercent = Double.parseDouble(props.getProperty("tetoBonusPercentual", "200.0"));
            Configuracao.setSalarioBase(salarioBase);
            Configuracao.setTetoBonusPercentual(tetoPercent);
        } catch (Exception e) {
            System.out.println("Erro ao carregar configurações. Usando valores padrão.");
        }
    }

    public static void salvar() {
        Properties props = new Properties();
        props.setProperty("salarioBase", String.valueOf(Configuracao.getSalarioBase()));
        props.setProperty("tetoBonusPercentual", String.valueOf(Configuracao.getTetoBonusPercentual()));
        try (FileOutputStream fos = new FileOutputStream(ARQUIVO)) {
            props.store(fos, "Configurações do Sistema de Folha");
        } catch (IOException e) {
            System.out.println("Erro ao salvar configurações: " + e.getMessage());
        }
    }
}