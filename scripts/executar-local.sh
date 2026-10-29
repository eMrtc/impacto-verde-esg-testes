#!/usr/bin/env bash
# Sobe a API de exemplo, roda a suíte Cucumber e derruba a API. Relatórios ficam em testes/target/cucumber-reports.
# Uso: scripts/executar-local.sh [tags]   ex.: scripts/executar-local.sh "@esg and @positivo"
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TAGS="${1:-}"
PORTA="${SERVER_PORT:-8080}"

"${RAIZ}/scripts/configurar-ambiente.sh"
set -a; source "${RAIZ}/.env"; set +a
export API_BASE_URL="http://127.0.0.1:${PORTA}"

echo "[QA-ESG] Compilando a API de exemplo..."
mvn -B -q -f "${RAIZ}/api-exemplo/pom.xml" -DskipTests clean package

echo "[QA-ESG] Subindo a API em ${API_BASE_URL} (somente loopback)..."
IMPACTO_VERDE_TOKEN_OPERADOR="${API_TOKEN_OPERADOR}" \
IMPACTO_VERDE_TOKEN_ADMIN="${API_TOKEN_ADMIN}" \
SERVER_PORT="${PORTA}" \
  java -jar "${RAIZ}/api-exemplo/target/impacto-verde-api.jar" > "${RAIZ}/api-exemplo/target/api.log" 2>&1 &
PID_API=$!
trap 'echo "[QA-ESG] Desligando a API (pid ${PID_API})..."; kill "${PID_API}" 2>/dev/null || true' EXIT

echo "[QA-ESG] Executando os testes..."
ARGS=()
[[ -n "${TAGS}" ]] && ARGS+=("-Dcucumber.filter.tags=${TAGS}")
mvn -B -f "${RAIZ}/testes/pom.xml" clean test ${ARGS[@]+"${ARGS[@]}"}

echo "[QA-ESG] Relatório HTML: testes/target/cucumber-reports/cucumber.html"
