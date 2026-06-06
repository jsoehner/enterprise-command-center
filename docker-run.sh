#!/bin/bash

# Configuration
# Replace these with your actual Docker Hub username and image name
DOCKER_USERNAME="jsoehner"
IMAGE_NAME="enterprise-command-center"
TAG="main"

FULL_IMAGE_NAME="${DOCKER_USERNAME}/${IMAGE_NAME}:${TAG}"

echo "Pulling image: ${FULL_IMAGE_NAME}..."
docker pull "${FULL_IMAGE_NAME}"

# Stop and remove existing container if it's running
echo "Cleaning up any existing container..."
docker stop ${IMAGE_NAME} 2>/dev/null || true
docker rm ${IMAGE_NAME} 2>/dev/null || true

echo "Running container..."
# -d runs in detached mode
# --rm removes the container when it stops
# -p maps the ports exposed in your Dockerfile (8080 and 8081)
docker run -d \
  --name ${IMAGE_NAME} \
  -p 8080:8080 \
  -p 8081:8081 \
  "${FULL_IMAGE_NAME}"

echo "Container started successfully!"
echo "Check logs with: docker logs -f ${IMAGE_NAME}"
