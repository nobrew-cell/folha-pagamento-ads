#!/usr/bin/env bash
# iniciar.sh — Abre o sistema diretamente se o .jar já estiver pronto.
# Para compilar do zero, use executar.sh.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$ROOT_DIR"

JAR_PATH="bin/SistemaFolha.jar"

echo ""
echo "  ============================================"
echo "   Sistema Folha de Pagamento — Iniciando..."
echo "  ============================================"
echo ""

if ! command -v java &>/dev/null; then
    echo "  [ERRO] Java não encontrado! Instale o JDK 17 ou superior."
    echo "         Download: https://adoptium.net/"
    exit 1
fi

if [ ! -f "$JAR_PATH" ]; then
    echo "  [AVISO] JAR não encontrado em bin/SistemaFolha.jar"
    echo "          Execute executar.sh primeiro para compilar o projeto."
    exit 1
fi

java -cp bin br.com.folha.main.SistemaFolha

echo ""
read -rp "Pressione ENTER para sair"
