#!/usr/bin/env bash
set -euo pipefail

# Configuration
DOCKER_USERNAME="${DOCKER_USERNAME:-jsoehner}"
IMAGE_NAME="enterprise-command-center"
TAG="${TAG:-main}"
BIND_HOST="${BIND_HOST:-127.0.0.1}"

ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-}"

if [ -z "$ADMIN_PASSWORD" ] || [ "$ADMIN_PASSWORD" = "admin123" ]; then
    echo "ERROR: Secure ADMIN_PASSWORD must be provided via environment variable." >&2
    echo "Example: ADMIN_PASSWORD='my_secure_password' ./docker-run.sh" >&2
    exit 1
fi

FULL_IMAGE_NAME="${DOCKER_USERNAME}/${IMAGE_NAME}:${TAG}"

echo "Pulling image: ${FULL_IMAGE_NAME}..."
docker pull "${FULL_IMAGE_NAME}"

# Stop and remove existing container if it is running
echo "Cleaning up any existing container..."
docker stop "${IMAGE_NAME}" 2>/dev/null || true
docker rm "${IMAGE_NAME}" 2>/dev/null || true

echo "Running hardened container..."
docker run -d \
  --name "${IMAGE_NAME}" \
  --read-only \
  --tmpfs /tmp:rw,noexec,nosuid,size=64m \
  --cap-drop=ALL \
  --security-opt=no-new-privileges:true \
  --user 65532:65532 \
  --memory=1g \
  --cpus=2 \
  --pids-limit=256 \
  --restart=unless-stopped \
  -p "${BIND_HOST}:8080:8080" \
  -p "${BIND_HOST}:8081:8081" \
  -e "ADMIN_USERNAME=${ADMIN_USERNAME}" \
  -e "ADMIN_PASSWORD=${ADMIN_PASSWORD}" \
  "${FULL_IMAGE_NAME}"

echo "Container started successfully!"
echo "Check logs with: docker logs -f ${IMAGE_NAME}"
