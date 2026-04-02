@echo off
chcp 65001 > nul
title Sistema Folha de Pagamento

echo.
echo  ============================================
echo   Sistema Folha de Pagamento - Iniciando...
echo  ============================================
echo.

:: Verifica se o Java esta instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Java nao encontrado! Instale o JDK 17 ou superior.
    echo        Download: https://adoptium.net/
    pause
    exit /b 1
)

:: Cria a pasta bin se nao existir
if not exist "bin" mkdir bin

echo [1/2] Compilando o projeto...
javac -encoding UTF-8 -d bin -sourcepath src -cp src src\br\com\folha\main\SistemaFolha.java

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Falha na compilacao. Verifique os arquivos .java.
    pause
    exit /b 1
)

echo [2/2] Compilado com sucesso! Iniciando sistema...
echo.
echo  ============================================
echo.

java -cp bin br.com.folha.main.SistemaFolha

echo.
pause
