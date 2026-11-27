$ErrorActionPreference = "Stop"

# Configuration
$DOCKER_USERNAME = if ($env:DOCKER_USERNAME) { $env:DOCKER_USERNAME } else { "jsoehner" }
$IMAGE_NAME = "enterprise-command-center"
$TAG = if ($env:TAG) { $env:TAG } else { "main" }
$BIND_HOST = if ($env:BIND_HOST) { $env:BIND_HOST } else { "127.0.0.1" }

$ADMIN_USERNAME = if ($env:ADMIN_USERNAME) { $env:ADMIN_USERNAME } else { "admin" }
$ADMIN_PASSWORD = if ($env:ADMIN_PASSWORD) { $env:ADMIN_PASSWORD } else { "" }

if (-not $ADMIN_PASSWORD -or $ADMIN_PASSWORD -eq "admin123") {
    Write-Error "ERROR: Secure ADMIN_PASSWORD must be provided via environment variable."
    exit 1
}

$FULL_IMAGE_NAME = "${DOCKER_USERNAME}/${IMAGE_NAME}:${TAG}"

Write-Host "Pulling image: ${FULL_IMAGE_NAME}..."
docker pull "${FULL_IMAGE_NAME}"

# Stop and remove existing container if it is running
Write-Host "Cleaning up any existing container..."
docker stop "${IMAGE_NAME}" 2>$null
docker rm "${IMAGE_NAME}" 2>$null

Write-Host "Running hardened container..."
docker run -d `
  --name "${IMAGE_NAME}" `
  --read-only `
  --tmpfs /tmp:rw,noexec,nosuid,size=64m `
  --cap-drop=ALL `
  --security-opt=no-new-privileges:true `
  --user 65532:65532 `
  --memory=1g `
  --cpus=2 `
  --pids-limit=256 `
  --restart=unless-stopped `
  -p "${BIND_HOST}:8080:8080" `
  -p "${BIND_HOST}:8081:8081" `
  -e "ADMIN_USERNAME=${ADMIN_USERNAME}" `
  -e "ADMIN_PASSWORD=${ADMIN_PASSWORD}" `
  "${FULL_IMAGE_NAME}"

Write-Host "Container started successfully!"
Write-Host "Check logs with: docker logs -f ${IMAGE_NAME}"
