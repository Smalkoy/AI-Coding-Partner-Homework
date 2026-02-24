#!/bin/bash

# Reset database script

set -e

echo "Resetting database..."

# Stop containers
docker-compose down -v

# Start PostgreSQL fresh
docker-compose up -d postgres

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker-compose exec -T postgres pg_isready -U postgres -d tickets > /dev/null 2>&1; do
    echo "PostgreSQL is not ready yet. Waiting..."
    sleep 2
done

echo "Database reset complete!"
