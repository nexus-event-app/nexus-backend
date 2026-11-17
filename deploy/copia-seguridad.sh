#!/usr/bin/env bash
# Copia de la base de datos y de las imágenes subidas en /opt/primercommit/copias. Guarda las de los últimos 14 días.
# Restaurar una copia (borra lo que haya ahora en la base de datos):
#   gunzip -c /opt/primercommit/copias/db-FECHA.sql.gz | docker compose exec -T db psql -U nexus -d nexus
set -euo pipefail
cd "$(dirname "$0")"

DEST=/opt/primercommit/copias
STAMP=$(date +%Y-%m-%d_%H%M)
mkdir -p "$DEST"
chmod 700 "$DEST"

docker compose exec -T db pg_dump -U nexus -d nexus --clean --if-exists | gzip > "$DEST/db-$STAMP.sql.gz"
docker compose exec -T backend tar -czf - -C /app uploads > "$DEST/uploads-$STAMP.tar.gz"

find "$DEST" -type f -mtime +14 -delete
echo "$(date '+%F %T') copia hecha: db-$STAMP.sql.gz"
