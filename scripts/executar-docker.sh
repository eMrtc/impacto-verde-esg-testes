#!/usr/bin/env bash
# Executa API + testes em contêineres e copia os relatórios para ./relatorios
# Pré-requisito: docker compose e login no registry.redhat.io (docker login registry.redhat.io).
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${RAIZ}"

./scripts/configurar-ambiente.sh

STATUS=0
docker compose --env-file .env up --build --abort-on-container-exit --exit-code-from testes || STATUS=$?

mkdir -p relatorios
docker compose --env-file .env cp testes:/home/default/build/target/cucumber-reports/. relatorios/ || true
docker compose --env-file .env down --volumes --remove-orphans

echo "[QA-ESG] Relatórios em ./relatorios (abra relatorios/cucumber.html). Status dos testes: ${STATUS}"
exit "${STATUS}"
