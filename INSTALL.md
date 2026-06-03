# Instalação — Sistema de Folha de Pagamento

<div align="left">

[![Release v8.1](https://img.shields.io/badge/release-v8.1--atual-gold?style=for-the-badge&logo=github)](https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v8.1)
[![Release v7.1](https://img.shields.io/badge/release-v7.1-blue?style=for-the-badge&logo=github)](https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v7.1)
[![Release v5.1](https://img.shields.io/badge/release-v5.1-blue?style=for-the-badge&logo=github)](https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v5.1)
[![Release v3.1](https://img.shields.io/badge/release-v3.1-blue?style=for-the-badge&logo=github)](https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v3.1)
[![Release v2.1](https://img.shields.io/badge/release-v2.1-blue?style=for-the-badge&logo=github)](https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v2.1)
[![Release v1.1](https://img.shields.io/badge/release-v1.1-blue?style=for-the-badge&logo=github)](https://github.com/nobrew-cell/folha-pagamento-ads/releases/tag/v1.1)

</div>

>*💡 Dica: Clique nos botões acima para ir direto para a página de download de cada versão.*

---

## Releases

<!-- Espaço reservado para os ícones de cada release -->
<!-- Insira aqui os badges/ícones das versões anteriores -->

| Release | Foco Principal | Disponível no GitHub |
| :--- | :--- | :---: |
| **v8.1** — *Atual* | Distribuição multiplataforma, executáveis nativos e scripts utilitários. | ✅ Sim |
| **v7.1** | Consolidação da engenharia de documentação e `ARQUITETURA.html`. | ✅ Sim |
| **v6.1** | *Pulada intencionalmente para focar na reestruturação de documentação.* | ❌ Não |
| **v5.1** | Interface de gestão com Dashboard integrado e arquivos de teste (`.tsv`). | ✅ Sim |
| **v4.1** | *Pulada intencionalmente devido à evolução massiva do Dashboard.* | ❌ Não |
| **v3.1** | Estabilização do formato híbrido (TSV & XLS) e saída dupla. | ✅ Sim |
| **v2.1** | Estabilização do legado em TSV (migração e consistência de delimitadores). | ✅ Sim |
| **v1.1** | Estabilização do legado em CSV (padrão de ponto-e-vírgula regional 🇧🇷). | ✅ Sim |

---

## Sobre a v8.1

A `v8.1` entrega a camada de distribuição prática do projeto. Depois de consolidar toda a engenharia de documentação no `ARQUITETURA.html` da versão anterior, este release transforma o código funcional em executáveis acessíveis e simples de rodar em qualquer sistema operacional — sem dependências manuais, sem configuração de ambiente.

**O que foi entregue:**

- **Executável `.exe`** para Windows — portátil, com terminal de comandos habilitado.
- **Aplicativo `.app`** nativo para macOS — compilado via GitHub Actions em ambiente limpo.
- **Binário `.tar.gz`** para Linux — estrutura pronta para execução direta.
- **Scripts utilitários** dentro do código-fonte para validação e testes locais.

> Os executáveis incluem a JRE estática integrada — não é necessário ter Java instalado na máquina para rodá-los.

---

## Opção 1 — Executável nativo (recomendado, sem Java necessário)

Baixe o release correspondente ao seu sistema operacional na [página de releases](https://github.com/nobrew-cell/folha-pagamento-ads/releases/latest), ou aqui no topo.

### Windows

Baixe `SistemaFolha-Windows.zip`, extraia e execute `SistemaFolha.exe` com duplo clique.

> O Windows SmartScreen pode exibir um aviso na primeira execução — clique em **"Mais informações"** e depois **"Executar assim mesmo"**. O executável não tem assinatura de código pois é um projeto acadêmico.

### macOS

Baixe `SistemaFolha-Mac.zip`, extraia e abra `SistemaFolha.app`.

> O macOS pode bloquear a abertura por ser de um desenvolvedor não identificado. Para liberar: abra o **Terminal**, navegue até a pasta onde o `.app` está e execute:
>
> ```bash
> xattr -cr SistemaFolha.app
> ```
>
> Depois disso, clique duas vezes normalmente.

### Linux

Baixe `SistemaFolha-Linux.tar.gz`, extraia e execute:

```bash
tar -xzf SistemaFolha-Linux.tar.gz
cd SistemaFolha-Linux
./SistemaFolha
```

> Caso o sistema reporte "permissão negada":
>
> ```bash
> chmod +x SistemaFolha
> ./SistemaFolha
> ```

---

## Opção 2 — Scripts (código-fonte, requer JDK 17+)

Use esta opção se quiser rodar direto do código-fonte, contribuir com o projeto ou validar o sistema localmente.

**Pré-requisito:** JDK 17 ou superior instalado.

- Windows: [Adoptium Temurin](https://adoptium.net/) ou `winget install EclipseAdoptium.Temurin.17.JDK`
- macOS: [Adoptium Temurin](https://adoptium.net/) ou `brew install --cask temurin`
- Linux: `sudo apt install openjdk-17-jdk` (Debian/Ubuntu) ou equivalente da sua distro

Verifique a instalação:

```bash
java -version
javac -version
```

### Lógica dos scripts

Os scripts funcionam de forma **inversa e complementar**:

- **`executar`** — compila o projeto e gera o `.jar` caso ele ainda não exista, depois abre o sistema. Faça isso na primeira vez que baixar o código.
- **`iniciar`** — abre o sistema diretamente, pulando a recompilação. Use nos testes seguintes para economizar tempo.

### Windows

Na raiz do projeto, dê **duplo clique** em `executar.bat`.

O `.bat` chama o `executar.ps1` automaticamente — isso garante suporte correto a acentos e formatação no terminal.

> Para rodar direto no PowerShell:
>
> ```powershell
> powershell -ExecutionPolicy Bypass -File scripts\executar.ps1
> ```

### Linux

Na raiz do projeto:

```bash
# Primeira execução (compila + abre)
bash scripts/executar.sh

# Execuções seguintes (abre direto, sem recompilar)
bash scripts/iniciar.sh
```

> Se os scripts já tiverem permissão de execução (commits preservam isso no Git):
>
> ```bash
> ./scripts/executar.sh
> ./scripts/iniciar.sh
> ```

### macOS

Na raiz do projeto, dê **duplo clique** em `scripts/executar.command` pelo Finder.

Ou pelo Terminal:

```bash
# Primeira execução
bash scripts/executar.command

# Execuções seguintes
bash scripts/iniciar.command
```

> **Permissão negada no Finder?** Execute uma vez pelo Terminal para que o macOS reconheça o script como executável confiável:
>
> ```bash
> chmod +x scripts/executar.command scripts/iniciar.command
> open scripts/executar.command
> ```

---

## Estrutura gerada em execução

Após a primeira execução, o sistema cria automaticamente na pasta onde está sendo rodado:

| Pasta / Arquivo | Conteúdo |
|-----------------|----------|
| `database.tsv` | Base de dados principal — carregado ao abrir, salvo ao sair |
| `exportados/dados/` | Exportações TSV com timestamp |
| `exportados/relatorios/` | Exportações XLS com timestamp |
| `backups/` | Backups automáticos antes de reset ou fechamento de mês |
| `historico/` | Snapshots mensais gerados pelo "Novo mês" |
| `logs/` | Registro de sessões — usado para detectar primeiro acesso |

Esses arquivos não estão no repositório (`.gitignore`) pois podem conter dados de funcionários.

---

*Veja também: [`COMO_EXECUTAR.txt`](./sistemaFolha-final/COMO_EXECUTAR.txt) — guia completo de todas as telas, menus e mensagens do sistema.*