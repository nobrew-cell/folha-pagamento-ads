@echo off
chcp 65001 >nul
title Limpeza do Projeto - Sistema Folha

echo.
echo ============================================
echo    Limpeza de arquivos gerados pelo run
echo ============================================
echo.

:: Define a raiz do projeto relativa ao .bat (coloque o .bat na pasta sistemaFolha-final)
set "RAIZ=%~dp0"

:: -- Compilacao --
echo [1/6] Apagando pasta bin...
if exist "%RAIZ%bin" (
    rd /s /q "%RAIZ%bin"
    echo       OK - bin apagado.
) else (
    echo       Nao encontrado, pulando.
)

echo [2/6] Apagando arquivos sources.txt e sources.log...
if exist "%RAIZ%sources.txt" del /q "%RAIZ%sources.txt" && echo       OK - sources.txt apagado.
if exist "%RAIZ%sources.log" del /q "%RAIZ%sources.log" && echo       OK - sources.log apagado.

:: -- Dados gerados em execucao --
echo [3/6] Apagando database.tsv...
if exist "%RAIZ%database.tsv" (
    del /q "%RAIZ%database.tsv"
    echo       OK - database.tsv apagado.
) else (
    echo       Nao encontrado, pulando.
)

echo [4/6] Apagando pasta backups...
if exist "%RAIZ%backups" (
    rd /s /q "%RAIZ%backups"
    echo       OK - backups apagado.
) else (
    echo       Nao encontrado, pulando.
)

echo [5/6] Apagando pasta exportados...
if exist "%RAIZ%exportados" (
    rd /s /q "%RAIZ%exportados"
    echo       OK - exportados apagado.
) else (
    echo       Nao encontrado, pulando.
)

echo [6/6] Apagando pasta logs...
if exist "%RAIZ%logs" (
    rd /s /q "%RAIZ%logs"
    echo       OK - logs apagado.
) else (
    echo       Nao encontrado, pulando.
)

echo.
echo ============================================
echo   Limpeza concluida! Projeto no estado limpo.
echo ============================================
echo.
pause