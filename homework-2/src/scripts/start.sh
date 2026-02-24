#!/bin/bash

# Start script for Ticket System

set -e

echo "Starting Ticket System..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Start PostgreSQL
echo "Starting PostgreSQL..."
docker-compose up -d postgres

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker-compose exec -T postgres pg_isready -U postgres -d tickets > /dev/null 2>&1; do
    echo "PostgreSQL is not ready yet. Waiting..."
    sleep 2
done
echo "PostgreSQL is ready!"

# Run the Spring Boot application
echo "Starting Spring Boot application..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

echo "Application started successfully!"
