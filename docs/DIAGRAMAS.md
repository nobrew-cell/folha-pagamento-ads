# 📊 Diagramas do Sistema

Diagramas de casos de uso e fluxograma do Sistema de Folha de Pagamento.  
Todos os diagramas de casos de uso foram elaborados manualmente e exibem versões distintas para modo claro e escuro do GitHub.

> Para o fluxograma interativo com simbologia ANSI completa, abra [`docs/fluxograma-sistema-folha.html`](./fluxograma-sistema-folha.html) diretamente no navegador — sem servidor, sem dependências.

---

## Fluxograma do sistema

```mermaid
flowchart TD
    START([Início · main])
    DB{database.tsv\nexiste?}
    BOAS[/Boas-vindas\nprimeiro acesso/]
    ERR[ERRO CRÍTICO\nBD apagado sem logs]
    FIM_ERR([Encerra])
    LOAD[[Repository.carregar\nFolhaService.init]]
    SEL{Selecionar\nPerfil}

    MENU[Menu Principal\n1–4 · 5 ADM · 0 Sair]
    D_MENU{opção?}

    CAD_PAD[/Cadastrar Padrão\nnome + matrícula/]
    CAD_COM[/Cadastrar Comissionado\nnome · matrícula · vendas · %/]
    CAD_PRO_CHK{bônus ultrapassa\nteto configurado?}
    BLOQUEIO[BLOQUEIO\nConsulte a diretoria]
    CAD_PRO[/Cadastrar Produção\nnome · matrícula · peças · R$/peça/]
    FOLHA[/Gerar Folha\ncalcularSalarioFinal p/ cada funcionário/]

    MENU_ADM[Menu Administrativo\n1–9 · 0 Voltar]
    D_ADM{sub-opção?}

    OP1[/Exportar\nTSV + XLS com timestamp/]
    OP2[/Importar TSV\nvalida → substitui base/]
    OP3[Novo Mês\narquiva em historico/ · zera variáveis]
    OP4[Editar Funcionário\nmatrícula · tipo · campos/]
    OP5[/Remover Funcionário\nconfirmação S/N/]
    OP6[Resetar Sistema\ndigite CONFIRMAR]
    OP7[Configurações\nsal. base · teto bônus · matrícula · sequência]
    OP8[Editar em Lote\nfiltro por tipo · E/N/Q por registro]
    OP9[Dashboard Analítico\nSwing · thread paralela]
    BCK[(backup_timestamp.tsv)]

    SAVE[[service.salvar\nRepository.escreverTSV]]
    DB_MAIN[(database.tsv\nestado persistido)]
    FIM([Fim · scanner.close])

    START --> DB
    DB -- não --> BOAS --> LOAD
    DB -- não + logs --> ERR --> FIM_ERR
    DB -- sim --> LOAD
    LOAD --> SEL

    SEL -- Funcionário --> MENU
    SEL -- ADM --> MENU

    MENU --> D_MENU
    D_MENU -- 1 --> CAD_PAD --> MENU
    D_MENU -- 2 --> CAD_COM --> MENU
    D_MENU -- 3 --> CAD_PRO_CHK
    CAD_PRO_CHK -- sim --> BLOQUEIO --> MENU
    CAD_PRO_CHK -- não --> CAD_PRO --> MENU
    D_MENU -- 4 --> FOLHA --> MENU
    D_MENU -- 5 ADM --> MENU_ADM
    D_MENU -- 0 --> SAVE

    MENU_ADM --> D_ADM
    D_ADM -- 1 --> OP1 --> MENU_ADM
    D_ADM -- 2 --> OP2 --> MENU_ADM
    D_ADM -- 3 --> OP3 --> MENU_ADM
    D_ADM -- 4 --> OP4 --> MENU_ADM
    D_ADM -- 5 --> OP5 --> MENU_ADM
    D_ADM -- 6 --> OP6 --> BCK --> MENU_ADM
    D_ADM -- 7 --> OP7 --> MENU_ADM
    D_ADM -- 8 --> OP8 --> MENU_ADM
    D_ADM -- 9 --> OP9 --> MENU_ADM
    D_ADM -- 0 --> MENU

    SAVE --> DB_MAIN --> FIM

    classDef terminal  fill:#E1F5EE,stroke:#0F6E56,color:#085041,font-weight:600
    classDef processo  fill:#E6F1FB,stroke:#185FA5,color:#0C447C
    classDef io        fill:#EEEDFE,stroke:#534AB7,color:#3C3489
    classDef decisao   fill:#FAEEDA,stroke:#854F0B,color:#633806,font-weight:500
    classDef storage   fill:#F1EFE8,stroke:#5F5E5A,color:#2C2C2A
    classDef sub       fill:#FBEAF0,stroke:#993556,color:#4B1528
    classDef erro      fill:#FDE8E8,stroke:#C0392B,color:#7B1A1A,font-weight:600

    class START,FIM,FIM_ERR terminal
    class MENU,MENU_ADM,OP3,OP4,OP6,OP7,OP8,OP9 processo
    class CAD_PAD,CAD_COM,CAD_PRO,FOLHA,OP1,OP2,OP5 io
    class DB,SEL,D_MENU,D_ADM,CAD_PRO_CHK decisao
    class BCK,DB_MAIN storage
    class LOAD,SAVE sub
    class ERR,BLOQUEIO erro
```

---

## Diagramas dos releases

Diagrama de planejamento das releases do projeto, divididas entre o escopo <b>Local</b> (desenvolvimentos e testes que podem ser retidos ou unificados) e o escopo <b>GitHub</b> (entregas oficiais e versionadas via Git)

---

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/rg_releases_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/evolution/sistemaFolha-final/assets/rg_releases_white.svg">
  <img src="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/evolution/sistemaFolha-final/assets/rg_releases_black.svg" alt="Diagrama de Releases do Projeto" style="max-width: 100%;">
</picture>

---

## Diagramas de casos de uso

Os diagramas abaixo cobrem cada área funcional do sistema. Cada um identifica os atores envolvidos, as operações disponíveis e as regras de negócio aplicadas.

---

### UC-Perfis — Seleção de perfil de acesso

Tela exibida no início de cada sessão. No primeiro acesso (sem `database.tsv`), o sistema entra direto como Administrador. Nas sessões seguintes, o usuário escolhe entre **Funcionário** e **Administrador**.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc_perfis_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc_perfis_white.svg">
  <img alt="UC-Perfis — Seleção de perfil de acesso" src="../assets/uc_perfis_white.svg">
</picture>

---

### UC-00 — Visão geral dos menus

Mapa completo da navegação: menu de seleção de perfil, menu principal e menu administrativo com todas as suas opções.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc00_menus_principais_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc00_menus_principais_white.svg">
  <img alt="UC-00 — Visão geral dos menus principais" src="../assets/uc00_menus_principais_white.svg">
</picture>

---

### UC-01 — Cadastros de funcionários `[1] [2] [3]`

Cobre as três opções de cadastro do menu principal. Detalha os tipos disponíveis, as validações aplicadas e as regras de negócio — incluindo verificação de matrícula duplicada, alerta de nome similar e bloqueio de bônus acima do teto configurado.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc01_cadastros_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc01_cadastros_white.svg">
  <img alt="UC-01 — Cadastros de funcionários" src="../assets/uc01_cadastros_white.svg">
</picture>

---

### UC-02 — Gerar folha de pagamento `[4]`

Exibe todos os funcionários ordenados por matrícula com seus respectivos cálculos. Detalha as fórmulas por tipo e a aplicação do teto de bônus para funcionários de produção.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc02_folha_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc02_folha_white.svg">
  <img alt="UC-02 — Gerar folha de pagamento" src="../assets/uc02_folha_white.svg">
</picture>

---

### UC-03 — Exportar e importar dados `(1) (2)`

Operações do menu administrativo para movimentação de dados. A exportação gera TSV (dados brutos) e XLS (relatório formatado), ambos com timestamp. A importação valida o formato antes de substituir a base, com backup automático obrigatório.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc03_export_import_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc03_export_import_white.svg">
  <img alt="UC-03 — Exportar e importar dados" src="../assets/uc03_export_import_white.svg">
</picture>

---

### UC-04 — Editar e remover funcionário `(4) (5)`

Operações individuais restritas ao Administrador. A edição permite trocar o tipo do funcionário (Padrão ↔ Comissionado ↔ Produção), com revalidação das regras de negócio. A remoção exige confirmação explícita.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc04_edicao_remocao_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc04_edicao_remocao_white.svg">
  <img alt="UC-04 — Editar e remover funcionário" src="../assets/uc04_edicao_remocao_white.svg">
</picture>

---

### UC-05 — Novo mês e reset do sistema `(3) (6)`

Duas operações de ciclo de vida dos dados. O fechamento de mês arquiva o `database.tsv` em `/historico` com nomenclatura por data e zera variáveis mensais, mantendo o cadastro base. O reset apaga tudo com backup automático e requer a digitação de `CONFIRMAR`.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc05_novo_mes_resetar_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc05_novo_mes_resetar_white.svg">
  <img alt="UC-05 — Novo mês e reset do sistema" src="../assets/uc05_novo_mes_resetar_white.svg">
</picture>

---

### UC-06 — Configurações do sistema `(7)`

Parâmetros configuráveis pelo Administrador: salário-base, teto de bônus (produção), limite de matrícula e modo de sequência (Rígido/Flexível). Todos são persistidos no `database.tsv` via linha `#CONFIG` e recarregados a cada inicialização.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc06_configuracoes_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc06_configuracoes_white.svg">
  <img alt="UC-06 — Configurações do sistema" src="../assets/uc06_configuracoes_white.svg">
</picture>

---

### UC-07 — Edição em lote por tipo `(8)`

Permite atualizar parâmetros (como percentual de comissão ou valor por peça) para todos os funcionários de um mesmo tipo de uma só vez. O processamento acontece em memória e o TSV é reescrito apenas após validação individual de cada registro.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc07_edicao_lote_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc07_edicao_lote_white.svg">
  <img alt="UC-07 — Edição em lote por tipo" src="../assets/uc07_edicao_lote_white.svg">
</picture>

---

### UC-08 — Dashboard analítico `(9)`

Janela gráfica independente (JavaFX/Swing) que roda em paralelo ao console. Carrega os dados do `database.tsv` no startup, opera em modo leitura (não altera dados) e pode ser fechada sem encerrar o sistema principal. Contém abas de visão geral, listagem de funcionários com filtros e gráficos de relatório.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc08_dashboard_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/uc08_dashboard_white.svg">
  <img alt="UC-08 — Dashboard analítico" src="../assets/uc08_dashboard_white.svg">
</picture>

---
