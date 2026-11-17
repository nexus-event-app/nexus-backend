#!/usr/bin/env bash
# Descarga lo último de la rama dev y vuelve a levantar la web. Sirve para el primer arranque y para cada cambio.
# Antes hace una copia de seguridad, por si algo sale mal.
set -euo pipefail
cd "$(dirname "$0")"

[ -f .env ] || { echo "Falta .env: cópialo de .env.example y rellénalo (o ejecuta preparar-servidor.sh)."; exit 1; }
grep -qE '^DOMAIN=.+' .env || { echo "Falta DOMAIN en .env"; exit 1; }

if docker compose ps --status running --services 2>/dev/null | grep -qx db; then
  ./copia-seguridad.sh
fi

git -C .. pull --ff-only
git -C ../../nexus-frontend pull --ff-only

docker compose up -d --build --remove-orphans
docker image prune -f >/dev/null

echo "Esperando a que el backend arranque..."
for _ in $(seq 1 60); do
  if docker compose exec -T backend curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "Listo: https://$(grep -E '^DOMAIN=' .env | cut -d= -f2)"
    exit 0
  fi
  sleep 3
done
echo "El backend no responde. Mira qué pasa con: docker compose logs --tail 100 backend"
exit 1
