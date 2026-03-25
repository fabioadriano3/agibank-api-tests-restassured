#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

ENV_NAME="${1:-dev}"
shift || true

MVN_CMD="${MVN_CMD:-}"
if [[ -n "$MVN_CMD" ]]; then
  :
elif [[ -x "./mvnw" ]]; then
  MVN_CMD="./mvnw"
else
  MVN_CMD="mvn"
fi

if [[ "$MVN_CMD" == ./* ]]; then
  if [[ ! -x "$MVN_CMD" ]]; then
    echo "Erro: mvnw encontrado, mas nao executavel." >&2
    exit 1
  fi
else
  if ! command -v "${MVN_CMD%% *}" >/dev/null 2>&1; then
    echo "Erro: Maven nao encontrado (ou nao ha mvnw no projeto)." >&2
    echo "Instale Maven ou adicione Maven Wrapper (mvnw) no repositorio." >&2
    exit 1
  fi
fi

echo "Executando testes (env=$ENV_NAME) ..."
echo "Comando: ${MVN_CMD} -B -ntp clean test verify -Ddog.api.env=${ENV_NAME} $*"

"${MVN_CMD}" -B -ntp clean test verify -Ddog.api.env="${ENV_NAME}" "$@"

echo "Concluido."
echo "Allure report: target/allure-report/index.html"

