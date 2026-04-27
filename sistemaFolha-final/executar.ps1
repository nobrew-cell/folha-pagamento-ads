# executar.ps1 teste
$host.UI.RawUI.BackgroundColor = "Black"
Clear-Host

Write-Host ""
Write-Host "  ============================================" -ForegroundColor Cyan
Write-Host "   Sistema Folha de Pagamento - Iniciando..." -ForegroundColor Cyan
Write-Host "  ============================================" -ForegroundColor Cyan
Write-Host ""

# Verifica se o Java está instalado
try {
    java -version 2>&1 | Out-Null
} catch {
    Write-Host "  [ERRO] Java nao encontrado! Instale o JDK 17 ou superior." -ForegroundColor Red
    Write-Host "         Download: https://www.java.com/pt-BR/" -ForegroundColor Yellow
    Read-Host "Pressione ENTER para sair"
    exit 1
}

# Cria a pasta bin se não existir
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "  [1/2] Compilando o projeto..." -ForegroundColor Yellow
javac -encoding UTF-8 -d bin -sourcepath src -cp src src\br\com\folha\main\SistemaFolha.java

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "  [ERRO] Falha na compilacao. Verifique os arquivos .java." -ForegroundColor Red
    Read-Host "Pressione ENTER para sair"
    exit 1
}

Write-Host "  [2/2] Compilado com sucesso! Iniciando sistema..." -ForegroundColor Green
Write-Host ""
Write-Host "  ============================================" -ForegroundColor Cyan
Write-Host ""

# Executa o sistema
java -cp bin br.com.folha.main.SistemaFolha

Write-Host ""
Read-Host "Pressione ENTER para sair"