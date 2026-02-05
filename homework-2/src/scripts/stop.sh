#!/bin/bash

# Stop script for Ticket System

echo "Stopping Ticket System..."

# Stop Docker containers
docker-compose down

echo "Ticket System stopped."
