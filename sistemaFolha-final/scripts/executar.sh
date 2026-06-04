#!/usr/bin/env bash
# executar.sh — Compila (se necessário) e inicia o Sistema de Folha de Pagamento
# Lógica: gera o .jar caso ele não exista; caso já exista, pula a compilação.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$ROOT_DIR"

JAR_PATH="bin/SistemaFolha.jar"

echo ""
echo "  ============================================"
echo "   Sistema Folha de Pagamento — Iniciando..."
echo "  ============================================"
echo ""

# Verifica Java
if ! command -v java &>/dev/null; then
    echo "  [ERRO] Java não encontrado! Instale o JDK 17 ou superior."
    echo "         Download: https://adoptium.net/"
    exit 1
fi

if [ ! -f "$JAR_PATH" ]; then
    echo "  [1/2] Compilando o projeto..."

    if ! command -v javac &>/dev/null; then
        echo "  [ERRO] javac não encontrado! Instale o JDK 17 (não apenas o JRE)."
        exit 1
    fi

    mkdir -p bin

    # Coleta todos os .java com suporte a nomes com espaço/acento
    find src -name "*.java" -print0 | xargs -0 \
        javac -encoding UTF-8 -d bin

    if [ $? -ne 0 ]; then
        echo ""
        echo "  [ERRO] Falha na compilação. Verifique os arquivos .java."
        exit 1
    fi

    # Empacota em JAR para que iniciar.sh possa reutilizar
    jar cf "$JAR_PATH" -C bin .

    echo "  [2/2] Compilado com sucesso! Iniciando sistema..."
else
    echo "  [JAR encontrado] Pulando compilação. Iniciando sistema..."
fi

echo ""
echo "  ============================================"
echo ""

java -cp bin br.com.folha.main.SistemaFolha

echo ""
read -rp "Pressione ENTER para sair"
