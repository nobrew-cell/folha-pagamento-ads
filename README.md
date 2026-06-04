# 💼 Sistema de Folha de Pagamento

> <img src="https://github.com/nobrew-cell/folha-pagamento-ads/blob/239319e28b0db7eb913b3c2e20806c62ffd37501/sistemaFolha-final/config/logo-CLI.png" alt="Logo do Projeto" width="75px" align="left" style="margin-right: 15px;"> **Projeto desenvolvido como requisito de avaliação para a disciplina de Algoritmos e Programação — UC Dual em parceria com a Oracle Academy.**
> <br><span style="display: block; clear: left;">Curso: Análise e Desenvolvimento de Sistemas (ADS) · Versão atual: **8.1**</span>

---

> [!IMPORTANT]
> ### 🎓 Painel de Avaliação Acadêmica
> **Professor, selecione uma das opções abaixo para ir direto ao ponto e otimizar o seu tempo de correção:**
> * 🚀 **[Rodar o Sistema Direto](#-como-testar-e-executar)** — Baixe o executável nativo do seu sistema operacional e rode o app pronto, sem precisar instalar nada na máquina.
> * 💻 **[Avaliar o Código-Fonte](#-execução-via-código-fonte-avaliação-acadêmica)** — Instruções rápidas para compilar manualmente ou rodar com um clique usando os scripts automatizados (`.bat`, `.sh` e `.command`).
> * 📊 **[Análise Técnica e Documentos](#-documentação-complementar)** — Índice completo com os diagramas de fluxo interativos, mapa da arquitetura e as reflexões da equipe. Recomendamos que acesse o [`docs/COMENTARIOS.md`](./docs/COMENTARIOS.md).

---

## 👥 Equipe e contribuições

O projeto foi desenvolvido por **`Gabriel Conceição da Silva`**, **`Eduardo Santos Cruz`** e **`Pedro Alonso Martins Fernandes`**.
*Gabriel* foi responsável pela maior parte da implementação e arquitetura do sistema, enquanto *Pedro* e *Eduardo* contribuíram principalmente com testes, sugestões e validações durante o desenvolvimento.

Reflexões individuais de cada membro sobre o desenvolvimento estão em [`docs/COMENTARIOS.md`](./docs/COMENTARIOS.md).

---

## 📌 Sobre o projeto

Sistemas de folha de pagamento no mercado tendem a ser caros, dependentes de internet ou complexos demais para o que entregam. Na outra ponta, o Excel é flexível e amplamente usado — mas não tem regras de negócio, depende do usuário não errar e não escala bem.

Este sistema é uma alternativa simples no meio do caminho: roda localmente no terminal, sem banco de dados externo, sem internet, sem instalação de dependências além do JDK. Calcula salários automaticamente, aplica regras de negócio e exporta os dados em formatos compatíveis com Excel e ferramentas de BI. O foco não é ser completo — é ser útil naquilo que se propõe.

Critério de qualidade central: **um funcionário mal-intencionado não deve ter facilidade; um bem-intencionado não deve ter dificuldade alguma.** A segurança do ambiente — acesso à máquina, controle de quem opera o sistema — fica a cargo da empresa.

---
## 🚀 Como testar e executar

O projeto cresceu significativamente desde os seus primeiros rascunhos. Para facilitar a distribuição e avaliação, a **v8.1** conta com **executáveis multiplataforma** (`.exe` para Windows, `.app` para macOS e `.tar.gz` para Linux) que rodam o sistema de forma nativa e sem dependências.

Para baixar os executáveis e visualizar o histórico completo de releases (com seus respectivos ícones e destaques), acesse nossa central de documentação:

<p align="center">
  <a href="./INSTALL.md">
    <img src="https://img.shields.io/badge/Acessar-Central%20de%20Instalação-gold?style=for-the-badge&logo=readme&logoColor=white" height="45" alt="Central de Instalação">
  </a>
  <a href="https://github.com/nobrew-cell/folha-pagamento-ads/releases/latest">
    <img src="https://img.shields.io/badge/Download-Último%20Release-blue?style=for-the-badge&logo=github" height="45" alt="Download Release">
  </a>
</p>

> **Nota:** As instruções completas para rodar os binários e liberar permissões de sistema (como o SmartScreen no Windows ou a quarentena no macOS) estão detalhadas no [`INSTALL.md`](./INSTALL.md).

---

### Galeria de Releases:

<table align="center" border="0">
  <tr>
    <td align="center" valign="bottom">
      <a href="https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v8.1">
        <img src="https://github.com/nobrew-cell/folha-pagamento-ads/blob/5bac7c3bf03d5eb5166cca2b22710ba9b794cf55/sistemaFolha-final/assets/CLI.png" width="110" alt="v8.1" title="Versão 8.1"><br>
        <code>v8.1</code>
      </a>
    </td>
    <td align="center" valign="bottom">
      <a href="https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v7.1">
        <img src="https://github.com/nobrew-cell/folha-pagamento-ads/blob/5bac7c3bf03d5eb5166cca2b22710ba9b794cf55/sistemaFolha-final/assets/DOC.png" width="110" alt="v7.1" title="Versão 7.1"><br>
        <code>v7.1</code>
      </a>
    </td>
    <td align="center" valign="bottom">
      <a href="https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v5.1">
        <img src="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/9fe73e9e781d76d743566a86eace93b1c6dfaabd/sistemaFolha-final/assets/BI.png" width="110" alt="v5.1" title="Versão 5.1"><br>
        <code>v5.1</code>
      </a>
    </td>
    <td align="center" valign="bottom">
      <a href="https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v3.1">
        <img src="https://github.com/nobrew-cell/folha-pagamento-ads/blob/5bac7c3bf03d5eb5166cca2b22710ba9b794cf55/sistemaFolha-final/assets/XLS.png" width="110" alt="v3.1" title="Versão 3.1"><br>
        <code>v3.1</code>
      </a>
    </td>
    <td align="center" valign="bottom">
      <a href="https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v2.1">
        <img src="https://github.com/nobrew-cell/folha-pagamento-ads/blob/5bac7c3bf03d5eb5166cca2b22710ba9b794cf55/sistemaFolha-final/assets/TSV.png" width="110" alt="v2.1" title="Versão 2.1"><br>
        <code>v2.1</code>
      </a>
    </td>
    <td align="center" valign="bottom">
      <a href="https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v1.1">
        <img src="https://github.com/nobrew-cell/folha-pagamento-ads/blob/5bac7c3bf03d5eb5166cca2b22710ba9b794cf55/sistemaFolha-final/assets/CSV.png" width="110" alt="v1.1" title="Versão 1.1"><br>
        <code>v1.1</code>
      </a>
    </td>
  </tr>
</table>

---

## 💻 Execução via Código-Fonte (Avaliação Acadêmica)

Caso você tenha feito o download do código-fonte cru (`.zip`) e queira rodar o projeto diretamente sem usar os executáveis, é necessário ter o **JDK 17+** instalado na máquina.

**Opção 1 — Automático:**

| Sistema | Script | Como abrir |
|---|---|---|
| Windows | `executar.bat` | Duplo clique na raiz do projeto |
| Windows (PowerShell) | `scripts/executar.ps1` | Chamado automaticamente pelo `.bat` |
| Linux | `scripts/executar.sh` | `bash scripts/executar.sh` no terminal |
| macOS | `scripts/executar.command` | Duplo clique no Finder ou `bash scripts/executar.command` |

Todos os scripts compilam o projeto automaticamente (se necessário) e iniciam o sistema. Após a primeira compilação, use `scripts/iniciar.sh` ou `scripts/iniciar.command` para pular a etapa de compilação e abrir mais rápido.

**Opção 2 — Compilação Manual (Terminal):**

**Windows (PowerShell):**
```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src | % { $_.FullName })
```

**Linux / macOS:**
```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
```

**Executar (Qualquer SO):**
```bash
java -cp bin br.com.folha.main.SistemaFolha
```
---

## ⚙️ Funcionalidades

**Perfil Funcionário**

- Cadastrar os três tipos de funcionário com cálculo automático de salário
- Consultar a folha de pagamento do mês, ordenada por matrícula

**Perfil Administrador** *(acesso via seleção de perfil na abertura)*

- Exportar dados em TSV e relatório em XLS com timestamp
- Importar arquivo TSV externo para substituir a base atual
- Fechar o mês — arquiva os dados e inicia um ciclo limpo
- Editar e remover funcionários individualmente ou em lote por tipo
- Resetar o sistema com backup automático antes de apagar qualquer coisa
- Configurar salário-base, teto de bônus, limite de matrícula e modo de sequência
- Abrir o Dashboard Analítico integrado

**Comportamentos gerais**

- Matrícula única — duplicatas são rejeitadas na hora
- Nomes normalizados automaticamente (aceita qualquer capitalização)
- Persistência automática ao sair; restauração automática ao abrir
- Proteção contra database ausente com histórico de sessões no log
- Em qualquer campo de cadastro, `0` cancela e volta ao menu

---

## 💻 Tecnologias e padrões

| Item | Detalhe |
|---|---|
| Linguagem | Java (JDK 17+) |
| Paradigma | POO — herança, polimorfismo, classes abstratas |
| Arquitetura | Camadas: `model / service / repository / ui / main / util` |
| Persistência | TSV local (fonte da verdade) + XLS para relatórios |
| Versionamento | Git + GitHub |

---

## 📁 Estrutura do projeto base

```
sistemaFolha-final/
├── src/
│   └── br/com/folha/
│       ├── main/
│       │   └── SistemaFolha.java          ← ponto de entrada e detecção de estado
│       ├── model/
│       │   ├── Funcionario.java           ← classe base abstrata
│       │   ├── FuncionarioPadrao.java
│       │   ├── FuncionarioComissionado.java
│       │   └── FuncionarioProducao.java
│       ├── service/
│       │   └── FolhaService.java          ← regras de negócio e configurações
│       ├── repository/
│       │   └── FuncionarioRepository.java ← leitura e escrita em TSV e XLS
│       ├── ui/
│       │   ├── ConsoleUI.java             ← interface do terminal
│       │   ├── SeletorPerfil.java         ← tela de seleção ADM / Funcionário
│       │   └── DashboardBI.java           ← dashboard analítico integrado
│       └── util/
│           └── LoggerUtil.java            ← log de sessões e detecção de primeiro acesso
├── assets/                                ← diagramas SVG (dark/light) e capturas de tela
├── bin/                                   ← classes compiladas (gerado automaticamente)
├── config/                                ← ícones do aplicativo (.icns, .ico, .png)
├── docs/
│   ├── DIAGRAMAS.md                       ← fluxograma e casos de uso
│   ├── COMENTARIOS.md                     ← reflexões da equipe sobre o desenvolvimento
│   ├── ARQUITETURA.html                   ← documentação técnica navegável
│   └── fluxograma-sistema-folha.html      ← fluxograma interativo offline
├── logs/                                  ← logs de sessão gerados em execução
├── scripts/
│   ├── executar.ps1                       ← compila e executa (Windows / PowerShell)
│   ├── executar.sh                        ← compila e executa (Linux)
│   ├── executar.command                   ← compila e executa (macOS, duplo clique no Finder)
│   ├── iniciar.sh                         ← executa direto se o JAR já existir (Linux)
│   └── iniciar.command                    ← executa direto se o JAR já existir (macOS)
├── tests/
│   ├── dados_teste.xlsx
│   └── database_teste.tsv
├── executar.bat                           ← chama o .ps1 (Windows)
├── limpar.bat                             ← remove bin/, logs/, backups/, exportados/ e database.tsv
└── COMO_EXECUTAR.txt                      ← guia detalhado de todas as telas e menus
```

Cada pasta dentro de `br/com/folha/` é um pacote Java. Não são simples diretórios: pacotes dividem o sistema em camadas confortavelmente isoladas, e ficam declarados sempre no topo de cada arquivo `.java`. Cada camada faz exatamente o que o nome dela diz, e nada além disso.

Para adicionar um novo tipo de funcionário: crie a classe em `model/`, implemente os quatro métodos abstratos e adicione o `case` correspondente em `FuncionarioRepository`.

---

## 🗂️ Arquivos gerados em execução

| Arquivo / Pasta | Função |
|---|---|
| `database.tsv` | Fonte da verdade — carregado ao abrir, salvo ao sair |
| `exportados/dados/` | Exportações TSV com timestamp (integração BI / Excel) |
| `exportados/relatorios/` | Exportações XLS com timestamp (leitura humana) |
| `backups/` | Backups automáticos antes de qualquer reset ou fechamento de mês |
| `historico/` | Snapshots mensais gerados pelo "Novo mês", com nomenclatura `yyyy-MM-dd_database.tsv` |
| `logs/` | Registro de sessões mensais — usado para detectar database ausente |

Esses arquivos não aparecem agora pois estão no `.gitignore`. Podem conter dados de funcionários — nomes, matrículas, salários. Mantê-los fora do repositório é uma decisão de privacidade.

---

## 📎 Documentação complementar

| Documento | Conteúdo |
|---|---|
| [`INSTALL.md`](./INSTALL.md) | Central de instalação, uso dos executáveis e permissões de sistema |
| [`docs/ARQUITETURA.html`](./docs/ARQUITETURA.html) | Documentação técnica navegável das camadas e classes |
| [`docs/DIAGRAMAS.md`](./docs/DIAGRAMAS.md) | Fluxograma Mermaid e diagramas de casos de uso (dark/light) |
| [`docs/fluxograma-sistema-folha.html`](./docs/fluxograma-sistema-folha.html) | Fluxograma interativo offline com simbologia ANSI completa |
| [`docs/COMENTARIOS.md`](./docs/COMENTARIOS.md) | Reflexões individuais da equipe sobre o processo de desenvolvimento |
| [`COMO_EXECUTAR.txt`](./sistemaFolha-final/COMO_EXECUTAR.txt) | Guia detalhado de todas as telas, menus e opções do console |
| [`LICENSE.md`](./LICENSE.md) | Termos de uso e restrições do projeto |

---

## 🛠️ Uso de ferramentas externas

Ao longo do desenvolvimento, recorreu-se pontualmente ao **Claude Code** como ferramenta de acompanhamento — não como substituto de raciocínio, mas como um par de olhos externo.

Os usos foram cirúrgicos:

- **Dashboard analítico** — feito em Swing, uma tecnologia ainda em estudo à época. A IA gerou um molde inicial; a lógica, os parâmetros e os ajustes finais foram feitos manualmente.
- **Comentários no código-fonte** — a empolgação falou mais alto no início, e o código saiu bem antes da documentação.
- **Partes do `model/` e do `repository/`** — orientação a objetos no `Java`, também era território em construção. A ferramenta ajudou a validar decisões de herança e polimorfismo, não a gerá-las.
- **Documentação do repositório** — alguns dos arquivos `.md` e `.html` foram redigidos com suporte da ferramenta a partir de anotações e rascunhos próprios. 

Arquitetura, lógica de negócio, estrutura de pacotes, `ConsoleUI.java` e decisões de design foram desenvolvidos manualmente. O material da **Oracle Academy** serviu como base de estudo ao longo de todo o projeto.

---

*Projeto desenvolvido para fins acadêmicos — UC Dual Algoritmos e Programação | ADS · Oracle Academy*
