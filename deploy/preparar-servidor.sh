#!/usr/bin/env bash
set -euo pipefail

BASE=/opt/primercommit
REPO=https://github.com/nexus-event-app

echo "==> Actualizaciones de seguridad automáticas"
export DEBIAN_FRONTEND=noninteractive
apt-get update -q
apt-get install -y -q git curl ufw unattended-upgrades openssl
dpkg-reconfigure -f noninteractive unattended-upgrades

echo "==> Memoria de reserva (swap de 4 GB)"
if ! swapon --show | grep -q /swapfile; then
  fallocate -l 4G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
echo 'vm.swappiness=10' > /etc/sysctl.d/99-primercommit.conf
sysctl -q -p /etc/sysctl.d/99-primercommit.conf

echo "==> Docker"
if ! command -v docker >/dev/null; then
  curl -fsSL https://get.docker.com | sh
fi

echo "==> Cortafuegos: solo SSH, http y https"
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp
ufw --force enable

echo "==> Código en $BASE (rama dev)"
mkdir -p "$BASE"
for repo in nexus-backend nexus-frontend; do
  if [ -d "$BASE/$repo/.git" ]; then
    git -C "$BASE/$repo" pull --ff-only
  else
    git clone --branch dev "$REPO/$repo.git" "$BASE/$repo"
  fi
done

echo "==> Configuración (deploy/.env)"
cd "$BASE/nexus-backend/deploy"
if [ ! -f .env ]; then
  cp .env.example .env
  chmod 600 .env
fi
fill_secret() {
  if grep -qE "^$1=$" .env; then
    sed -i "s|^$1=$|$1=$(openssl rand -base64 "$2" | tr -d '\n/+=')|" .env
    echo "    $1 generado"
  fi
}
fill_secret POSTGRES_PASSWORD 32
fill_secret JWT_SECRET 48

echo "==> Copia de seguridad diaria (03:30)"
chmod +x copia-seguridad.sh actualizar.sh
echo "30 3 * * * root $BASE/nexus-backend/deploy/copia-seguridad.sh >> /var/log/primercommit-copias.log 2>&1" \
  > /etc/cron.d/primercommit-copias

cat <<MSG

Listo. Ahora:
  1. nano $BASE/nexus-backend/deploy/.env   (dominio, tu cuenta de admin, email y datos legales)
  2. $BASE/nexus-backend/deploy/actualizar.sh
MSG
