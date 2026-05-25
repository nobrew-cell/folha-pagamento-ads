# 💼 Sistema de Folha de Pagamento

> <img src="https://github.com/nobrew-cell/folha-pagamento-ads/blob/evolution/sistemaFolha-final/config/logo-CLI.png?raw=true" alt="Logo do Projeto" width="75px" align="left" style="margin-right: 15px;"> **Projeto desenvolvido como requisito de avaliação para a disciplina de Algoritmos e Programação — UC Dual em parceria com a Oracle Academy.**
> <br><span style="display: block; clear: left;">Curso: Análise e Desenvolvimento de Sistemas (ADS) · Versão atual: **7.1**</span>

---

## 👥 Equipe e contribuições

O projeto foi desenvolvido por **Gabriel Conceição da Silva**, **Eduardo Santos Cruz** e **Pedro Alonso Martins Fernandes**.
*Gabriel* foi responsável pela maior parte da implementação e arquitetura do sistema, enquanto *Pedro* e *Eduardo* contribuíram principalmente com testes, sugestões e validações durante o desenvolvimento.

Reflexões individuais de cada membro sobre o desenvolvimento estão em [`docs/COMENTARIOS.md`](./docs/COMENTARIOS.md).

---

## 📌 Sobre o projeto

Sistemas de folha de pagamento no mercado tendem a ser caros, dependentes de internet ou complexos demais para o que entregam. Na outra ponta, o Excel é flexível e amplamente usado — mas não tem regras de negócio, depende do usuário não errar e não escala bem.

Este sistema é uma alternativa simples no meio do caminho: roda localmente no terminal, sem banco de dados externo, sem internet, sem instalação de dependências além do JDK. Calcula salários automaticamente, aplica regras de negócio e exporta os dados em formatos compatíveis com Excel e ferramentas de BI. O foco não é ser completo — é ser útil naquilo que se propõe.

Critério de qualidade central: **um funcionário mal-intencionado não deve ter facilidade; um bem-intencionado não deve ter dificuldade alguma.** A segurança do ambiente — acesso à máquina, controle de quem opera o sistema — fica a cargo da empresa.

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

## 📁 Estrutura do projeto

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
├── assets/                                ← diagramas SVG (dark/light)
├── docs/
│   ├── DIAGRAMAS.md                       ← fluxograma e casos de uso
│   ├── COMENTARIOS.md                     ← reflexões da equipe sobre o desenvolvimento
│   ├── ARQUITETURA.html                   ← documentação técnica navegável
│   └── fluxograma-sistema-folha.html      ← fluxograma interativo offline
├── executar.bat                           ← execução com um clique (Windows)
├── scripts/
│   └── executar.ps1
└── COMO_EXECUTAR.txt                      ← guia detalhado de todas as telas e menus
```

Cada pasta dentro de `br/com/folha/` é um pacote Java. Não são simples diretórios: pacotes dividem o sistema em camadas confortavelmente isoladas, e ficam declarados sempre no topo de cada arquivo `.java`. Cada camada faz exatamente o que o nome dela diz, e nada além disso.

Para adicionar um novo tipo de funcionário: crie a classe em `model/`, implemente os quatro métodos abstratos e adicione o `case` correspondente em `FuncionarioRepository`.

---

## 🚀 Como executar

**Pré-requisito:** JDK 17+ instalado.

### Opção 1 — `.bat` (Windows, recomendada)

Dê dois cliques em `executar.bat`. O sistema compila e abre automaticamente.

> Na primeira execução, o Windows pode exibir um aviso do SmartScreen — clique em **"Mais informações"** e depois **"Executar assim mesmo"**.

### Opção 2 — IDE

Abra a pasta do projeto na sua IDE, navegue até `src/br/com/folha/main/SistemaFolha.java` e execute pelo botão **Run** acima do método `main`.

### Opção 3 — Terminal

**Compilar:**

```powershell
# Windows (PowerShell)
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src | % { $_.FullName })
```

```bash
# Linux / macOS
javac -encoding UTF-8 -d bin $(find src -name "*.java")
```

**Executar:**

```bash
java -cp bin br.com.folha.main.SistemaFolha
```

> Instaladores `.exe` (Windows), `.deb` (Linux) e `.dmg` (macOS) estão em desenvolvimento — veja [`INSTALL.md`](./INSTALL.md) quando disponível.

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
| [`docs/DIAGRAMAS.md`](./docs/DIAGRAMAS.md) | Fluxograma Mermaid e diagramas de casos de uso (dark/light) |
| [`docs/ARQUITETURA.html`](./docs/ARQUITETURA.html) | Documentação técnica navegável das camadas e classes |
| [`docs/fluxograma-sistema-folha.html`](./docs/fluxograma-sistema-folha.html) | Fluxograma interativo offline com simbologia ANSI completa |
| [`COMO_EXECUTAR.txt`](./sistemaFolha-final/COMO_EXECUTAR.txt) | Guia de todas as telas, menus, submenus e opções do console |
| [`LICENSE.md`](./LICENSE.md) | Termos de uso e restrições do projeto |

---

## 🛠️ Uso de ferramentas externas

Ao longo do desenvolvimento, recorreu-se pontualmente ao **Claude** como ferramenta de acompanhamento — não como substituto de raciocínio, mas como um par de olhos externo.

Os usos foram cirúrgicos:

- **Dashboard analítico** — feito em JavaFX, uma tecnologia ainda em estudo à época. A IA gerou um molde inicial; a lógica, os parâmetros e os ajustes finais foram feitos manualmente.
- **Comentários no código-fonte** — a empolgação falou mais alto no início, e o código saiu antes da documentação.
- **Partes do `model/` e do `repository/`** — orientação a objetos ainda era território em construção. A ferramenta ajudou a validar decisões de herança e polimorfismo, não a gerá-las.
- **Documentação do repositório** — os arquivos `.md` e `.html` foram redigidos com suporte da ferramenta a partir de anotações e rascunhos próprios.

Arquitetura, lógica de negócio, estrutura de pacotes, `ConsoleUI.java` e decisões de design foram desenvolvidos manualmente. O material da **Oracle Academy** serviu como base de estudo ao longo de todo o projeto.

---

*Projeto desenvolvido para fins acadêmicos — UC Dual Algoritmos e Programação | ADS · Oracle Academy*
