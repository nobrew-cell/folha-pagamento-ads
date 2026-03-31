# 💼 Sistema de Folha de Pagamento

> Projeto desenvolvido como requisito de avaliação para a disciplina de **Algoritmos e Programação** — UC Dual em parceria com a **Oracle Academy**.  
> Curso: Análise e Desenvolvimento de Sistemas (ADS)

---

## 📌 Sobre o projeto

Sistema de folha de pagamento via terminal (console) que gerencia três perfis de funcionários, escolhidos pela UC Dual. Ele calcula salários automaticamente e persiste/exporta os dados localmente em CSV — sem banco de dados externo, sem dependências, sem internet.

A arquitetura foi organizada em camadas bem definidas para separar interface, regras de negócio e persistência. O projeto brinca bastante com pacotes em JAVA. Não é uma mera maquiagem: cada camada faz exatamente o que o nome dela diz, e nada além disso.

---

## ⚙️ Funcionalidades

- **Três perfis de funcionário** com cálculo automático de salário:
  - **Padrão** — recebe apenas o salário base mensal
  - **Comissionado** — salário base + comissão sobre vendas
  - **Produção** — salário base + bônus por peças produzidas
- **Matrícula única** — o sistema rejeita duplicatas na hora
- **Nome normalizado automaticamente** — pode digitar em maiúsculo, minúsculo ou misturado
- **Geração de folha** — exibe todos os funcionários ordenados por matrícula
- **Exportação CSV com timestamp** — cada exportação gera um arquivo novo, nunca sobrescreve
- **Backup automático antes do reset** — nenhum dado é apagado sem rastro
- **Persistência automática** — ao sair pela opção 0, os dados são salvos; ao abrir, são restaurados

---

## 💻 Tecnologias e padrões

| Item | Detalhe |
|---|---|
| Linguagem | Java (JDK 17+) |
| Paradigma | POO — herança, polimorfismo, classes abstratas |
| Arquitetura | Camadas: `model / service / repository / ui / main` |
| Persistência | CSV local (sem banco de dados externo) |
| Versionamento | Git + GitHub |

---

## 📂 Estrutura do projeto

```
sistemaFolha/
├── src/
│   └── br/com/folha/
│       ├── main/
│       │   └── SistemaFolha.java          ← ponto de entrada
│       ├── model/
│       │   ├── Funcionario.java           ← classe base abstrata
│       │   ├── FuncionarioPadrao.java
│       │   ├── FuncionarioComissionado.java
│       │   └── FuncionarioProducao.java
│       ├── service/
│       │   └── FolhaService.java          ← regras de negócio
│       ├── repository/
│       │   └── FuncionarioRepository.java ← leitura e escrita em CSV
│       └── ui/
│           └── ConsoleUI.java             ← toda a interface do terminal
├── docs/
│   └── fluxograma-sistema-folha.html      ← documentação visual (ver abaixo)
└── README.md
```

Para adicionar um novo tipo de funcionário: crie a classe em `model/`, implemente os quatro métodos abstratos e adicione o `case` correspondente em `FuncionarioRepository`. Só isso. Parece simples, não é?

---

## 📊 Fluxograma do sistema

O fluxo completo da aplicação — do menu principal até cada operação — está documentado visualmente em:

📄 [`docs/fluxograma-sistema-folha.html`](./docs/fluxograma-sistema-folha.html)

Abra o arquivo diretamente no navegador. Não precisa de servidor, não precisa de nada instalado — é HTML puro, funciona offline.

> **Por que HTML e não uma imagem?**  
> HTML, hoje em dia, é universal: todo dispositivo tem navegador. O fluxograma foi mantido como arquivo separado em `docs/` para não misturar documentação com código-fonte — o projeto continua sendo 100% Java na essência, com o HTML servindo como material de apoio.

---

## 🚀 Como executar

> Antes de qualquer coisa, um aviso tranquilizador:  
> você não precisa saber programar para usar este sistema.  
> Só precisa saber digitar e pressionar ENTER.

### 1. Abrir o terminal

Abra o **PowerShell** (ou o terminal integrado do VS Code) dentro da pasta do projeto.

### 2. Compilar

> Só precisa fazer isso uma vez — ou quando alterar algum arquivo `.java`.

**Windows (PowerShell):**
```powershell
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
```

**Linux / macOS:**
```bash
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
```

### 3. Executar

```bash
java -cp bin br.com.folha.main.SistemaFolha
```

Pronto. O menu vai aparecer na tela.

---

## 🖥️ Interface do sistema

Ao abrir, você verá:

```
===========================================
        FOLHA DE PAGAMENTO  (salarios mensais)
===========================================
  1 - Cadastrar Funcionario Padrao
  2 - Cadastrar Funcionario Comissionado
  3 - Cadastrar Funcionario de Producao
  4 - Gerar Folha de Pagamento
  5 - Exportar CSV  (copia com data e hora)
  6 - Resetar sistema  [ADM]
  0 - Sair
===========================================
  Opcao:
```

Digite o número da opção desejada e pressione ENTER.

---

## 📋 Guia de uso — opção por opção

<details>
<summary><strong>Opção 1 — Funcionário Padrão</strong></summary>

Para quem recebe apenas o salário fixo mensal, sem comissão ou bônus.

O sistema pede:
- **Nome** (pode digitar em qualquer caixa: `jose`, `JOSE` ou `Jose` — o sistema normaliza)
- **Matrícula** (número único de identificação)

</details>

<details>
<summary><strong>Opção 2 — Funcionário Comissionado</strong></summary>

Para quem recebe salário fixo + comissão sobre vendas.

O sistema pede:
- Nome
- Matrícula
- Total de vendas do mês (ex: `15000`)
- Percentual de comissão (ex: `5` para 5%)

**Cálculo:**
```
salário final = salário base + (vendas × percentual ÷ 100)
```

</details>

<details>
<summary><strong>Opção 3 — Funcionário de Produção</strong></summary>

Para quem recebe salário fixo + bônus por peças produzidas.

O sistema pede:
- Nome
- Matrícula
- Quantidade de peças produzidas no mês
- Valor pago por cada peça (ex: `3.50`)

**Cálculo:**
```
salário final = salário base + (quantidade × valor por peça)
```

</details>

<details>
<summary><strong>Opção 4 — Gerar Folha de Pagamento</strong></summary>

Exibe no terminal todos os funcionários cadastrados, ordenados por matrícula (do menor para o maior).

Para cada funcionário, você vê:
- Nome e matrícula
- Tipo (Padrão, Comissionado ou Produção)
- Salário base mensal
- Extras (comissão ou bônus, dependendo do tipo)
- **Total mensal a receber**

</details>

<details>
<summary><strong>Opção 5 — Exportar CSV</strong></summary>

Gera uma cópia do cadastro atual em formato CSV, que abre direto no Excel.

O arquivo é salvo em:
```
exportados/folha_AAAA-MM-DD_HH-MM-SS.csv
```

A data e hora no nome do arquivo garantem que você nunca sobrescreve uma exportação anterior. Cada vez que exportar, um novo arquivo é criado.

</details>

<details>
<summary><strong>Opção 6 — Resetar sistema [ADM]</strong></summary>

Apaga todos os funcionários e zera o sistema.

**Antes de apagar qualquer coisa**, um backup automático é salvo em:
```
backups/backup_AAAA-MM-DD_HH-MM-SS.csv
```

Para confirmar o reset, você precisa digitar exatamente a palavra: `CONFIRMAR`

Qualquer outra entrada cancela a operação.

</details>

<details>
<summary><strong>Opção 0 — Sair</strong></summary>

Salva tudo automaticamente no arquivo `database.csv` e encerra o programa.

Na próxima vez que abrir, todos os dados estarão lá, exatamente como você deixou.

</details>

---

## 🗂️ Arquivos gerados em execução

| Arquivo / Pasta | Função |
|---|---|
| `database.csv` | Banco de dados principal — carregado ao abrir, salvo ao sair |
| `exportados/` | Cópias manuais com data e hora (opção 5) |
| `backups/` | Backups automáticos gerados antes de qualquer reset |

> Esses arquivos não fazem parte do repositório (estão no `.gitignore`).  
> Eles podem conter dados de funcionários — nomes, matrículas, salários.  
> Manter isso fora do Git é uma decisão de **privacidade e segurança**, mesmo que modesta.

O sistema usa o relógio do próprio computador para marcar data e hora nos arquivos. Não precisa de conexão com a internet.

---

## 🔢 Decisão de design — o `0` como cancelamento universal

Em qualquer campo, na hora do cadastro, digitar `0` cancela a operação e volta ao menu.

Essa escolha é inspirada em interfaces que qualquer pessoa já usou ou pelo menos conhece: sistemas de atendimento telefônico (como telemarketing) e controles remotos — onde `0` historicamente significa "voltar" ou "cancelar". É uma convenção que dispensa explicação.

Dentro deste projeto, a decisão é segura: todos os valores numéricos válidos respeitam limites mínimos definidos pelas regras de negócio (matrícula deve ser maior que zero, valores monetários são tratados como não-negativos com sinal de cancelamento separado).

Isso transforma uma escolha simples em **decisão de design consciente**.

**Onde mora o risco aqui? — No caso, não é no agora, é no futuro.**

Imagine que o sistema seja expandido:
- um novo campo aceita `0` como valor legítimo (desconto zero, horas extras zero, bônus zero);
- outro desenvolvedor mexe no código sem saber da convenção;
- ou, meses depois, a regra acaba sendo esquecida.

De repente, `0` volta a ser válido como dado — e o cancelamento vira uma armadilha silenciosa e uma dor de cabeça na certa.

Se o projeto for escalonado, a recomendação é substituir o `0` por uma entrada textual explícita, como `c` ou `cancelar`. Mais verboso, mas inequívoco.

---

## 🔧 Informações técnicas

**Salário base mensal atual:** `R$ 2.000,00`

Para alterar, edite a constante em:
```
src/br/com/folha/model/Funcionario.java
```
```java
public static final double SALARIO_BASE = 2000.00;
```

---

*Projeto desenvolvido para fins acadêmicos — UC Dual Algoritmos e Programação | ADS*
