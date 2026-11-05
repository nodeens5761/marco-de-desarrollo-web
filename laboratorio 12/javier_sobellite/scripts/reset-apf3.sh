#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f compose.yaml || ! -f .env ]]; then
    echo "Faltan compose.yaml o .env en la raíz del proyecto."
    exit 1
fi

set -a
source .env
set +a
: "${TECHLAB_DB_PASSWORD:?Defina TECHLAB_DB_PASSWORD en .env}"
: "${TECHLAB_ROOT_PASSWORD:?Defina TECHLAB_ROOT_PASSWORD en .env}"

if [[ "${CONFIRM_RESET:-}" != "SI" ]]; then
    echo "Este proceso eliminará el volumen MySQL de TechLab y todos sus datos."
    echo "Confirma con: CONFIRM_RESET=SI ./scripts/reset-apf3.sh"
    exit 1
fi

docker compose config --services | grep -qx mysql
docker compose down -v
docker compose up -d mysql

echo "Esperando a que MySQL esté healthy..."
for attempt in $(seq 1 60); do
    status="$(docker inspect --format='{{.State.Health.Status}}' techlab-mysql 2>/dev/null || true)"
    if [[ "$status" == "healthy" ]]; then
        docker compose ps
        ./mvnw clean test
        exit 0
    fi
    sleep 5
done

docker compose logs --tail=80 mysql
echo "MySQL no alcanzó el estado healthy."
exit 1
