@echo off
setlocal enabledelayedexpansion

set ENV_NAME=%1
if "%ENV_NAME%"=="" set ENV_NAME=dev

if not "%~1"=="" shift

set "MVN_CMD="
if not "%MVN_CMD%"=="" goto use_mvn_cmd

if exist ".\mvnw.cmd" (
  set "MVN_CMD=.\mvnw.cmd"
) else (
  set "MVN_CMD=mvn"
)

:use_mvn_cmd
if "%MVN_CMD%"=="mvn" (
  where mvn >nul 2>nul
  if errorlevel 1 (
    echo Erro: Maven nao encontrado.
    echo Instale Maven ou adicione Maven Wrapper (mvnw) no repositorio.
    exit /b 1
  )
)

echo Executando testes (env=%ENV_NAME%) ...
echo Comando: %MVN_CMD% -B -ntp clean test verify -Ddog.api.env=%ENV_NAME% %*

%MVN_CMD% -B -ntp clean test verify -Ddog.api.env=%ENV_NAME% %*

echo.
echo Concluido.
echo Allure report: target\allure-report\index.html

endlocal

