#!/usr/bin/env bash
# Gera o arquivo .env com tokens ALEATÓRIOS para uso local/Docker.
# O .env está no .gitignore: segredo não é lixo reciclável, não vai pro repositório.
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ARQUIVO="${RAIZ}/.env"

if [[ -f "${ARQUIVO}" && "${1:-}" != "--forcar" ]]; then
  echo "[QA-ESG] .env já existe. Use --forcar para gerar tokens novos."
  exit 0
fi

gerar_token() { openssl rand -hex 24; }

umask 077
cat > "${ARQUIVO}" <<ENV
# Gerado por scripts/configurar-ambiente.sh (valores aleatórios, não comite este arquivo)
API_BASE_URL=http://127.0.0.1:8080
API_TOKEN_OPERADOR=$(gerar_token)
API_TOKEN_ADMIN=$(gerar_token)
ENV

echo "[QA-ESG] .env criado com tokens aleatórios (permissão 600). Ambiente pronto para reciclar bugs!"
