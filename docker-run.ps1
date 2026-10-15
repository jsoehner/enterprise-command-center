# Configuration
# Replace these with your actual Docker Hub username and image name
$DOCKER_USERNAME = "jsoehner"
$IMAGE_NAME = "enterprise-command-center"
$TAG = "main"

$ADMIN_USERNAME = if ($env:ADMIN_USERNAME) { $env:ADMIN_USERNAME } else { "admin" }
$ADMIN_PASSWORD = if ($env:ADMIN_PASSWORD) { $env:ADMIN_PASSWORD } else { "" }

if (-not $ADMIN_PASSWORD -or $ADMIN_PASSWORD -eq "admin123") {
    Write-Error "ERROR: Secure ADMIN_PASSWORD must be provided via environment variable."
    exit 1
}

$FULL_IMAGE_NAME = "${DOCKER_USERNAME}/${IMAGE_NAME}:${TAG}"

Write-Host "Pulling image: ${FULL_IMAGE_NAME}..."
docker pull "${FULL_IMAGE_NAME}"

# Stop and remove existing container if it's running
Write-Host "Cleaning up any existing container..."
docker stop ${IMAGE_NAME} 2>$null
docker rm ${IMAGE_NAME} 2>$null

Write-Host "Running container..."
# -d runs in detached mode
# --rm removes the container when it stops
# -p maps the ports exposed in your Dockerfile (8080 and 8081)
docker run -d `
  --name ${IMAGE_NAME} `
  -p 8080:8080 `
  -p 8081:8081 `
  -e ADMIN_USERNAME=${ADMIN_USERNAME} `
  -e ADMIN_PASSWORD=${ADMIN_PASSWORD} `
  "${FULL_IMAGE_NAME}"

Write-Host "Container started successfully!"
Write-Host "Check logs with: docker logs -f ${IMAGE_NAME}"
