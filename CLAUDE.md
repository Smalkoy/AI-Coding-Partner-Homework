# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is a homework repository for the **AI-Assisted Development Course**. It contains multiple homework projects demonstrating progressively advanced AI-assisted software development practices. Each homework is isolated in its own directory.

## Repository Structure

```
ai-assisted-dev-homework/
├── homework-1/     # Banking Transactions API (Java/Spring Boot)
├── homework-2/     # Customer Support Ticket System (Java/Spring Boot)
├── homework-3/     # Specification-Driven Design (FinTech Spending Caps - Specification Only)
├── homework-4/     # Multi-Agent System (TBD)
├── homework-5/     # MCP Server Configuration (TBD)
└── homework-6/     # Capstone Project (TBD)
```

## Homework Projects

### Homework 1: Banking Transactions API
- **Location**: `homework-1/`
- **Tech Stack**: Java 17+, Spring Boot, PostgreSQL
- **Status**: Basic implementation (minimal documentation)
- **Note**: This is an early homework with limited structure

### Homework 2: Customer Support Ticket System
- **Location**: `homework-2/src/`
- **Tech Stack**: Java 17, Spring Boot 3.2.x, PostgreSQL 15+, Maven
- **Features**: Multi-format file import (CSV/JSON/XML), auto-classification, REST API
- **Key Commands**:
  ```bash
  cd homework-2/src

  # Start everything (database + application)
  ./scripts/start.sh

  # Run tests with coverage
  ./mvnw test jacoco:report

  # Run specific test class
  ./mvnw test -Dtest=TicketControllerTest

  # Stop all services
  ./scripts/stop.sh

  # Reset database
  ./scripts/reset-db.sh
  ```
- **Project Structure**:
  - `src/main/java/com/support/ticketsystem/controller/` - REST endpoints
  - `src/main/java/com/support/ticketsystem/service/` - Business logic
  - `src/main/java/com/support/ticketsystem/repository/` - Data access
  - `src/main/java/com/support/ticketsystem/parser/` - File parsers (CSV/JSON/XML)
  - `src/main/java/com/support/ticketsystem/domain/entity/` - JPA entities
  - `src/main/java/com/support/ticketsystem/domain/dto/` - DTOs
  - `src/test/java/` - Unit and integration tests
- **Configuration**: Uses `.env` files (copy from `.env.example`)
- **API Documentation**: Available at http://localhost:8080/swagger-ui.html
- **Testing**: JUnit 5, Mockito, Testcontainers; target coverage: 85%

### Homework 3: Specification-Driven Design
- **Location**: `homework-3/`
- **Focus**: FinTech Spending Caps feature specification
- **Deliverables**:
  - `specification.md` - Three-tier specification (high/mid/low-level objectives)
  - `agents.md` - AI agent configuration for Claude Code/Cursor/Aider
  - `.github/copilot-instructions.md` - GitHub Copilot rules
  - `README.md` - Rationale and industry best practices
- **Tech Stack Specified**: Python 3.12+, FastAPI, SQLAlchemy 2.0, Pydantic v2, pytest
- **Key Practices**: Decimal for money, immutable audit trails, soft-deletes, PII encryption, RBAC, row-level locking

## Development Workflow

### Creating New Homework Branches

When working on homework assignments:
```bash
# Create a new branch for homework N
git checkout -b homework-N-submission

# Work on assignment, then commit
git add .
git commit -m "Complete homework N"
git push origin homework-N-submission
```

### Pull Request Workflow
- Base repository: Your personal fork
- Base branch: `main`
- Compare branch: `homework-N-submission`
- Include screenshots demonstrating AI usage in `docs/screenshots/`

## AI Agent Configuration

### Global Coding Standards (from .github/copilot-instructions.md)

These rules apply when working on **FinTech/Regulated projects** (Homework 3+):

#### Naming Conventions
- **Files**: lowercase snake_case (e.g., `cap_service.py`)
- **Functions/variables**: snake_case (e.g., `check_transaction`)
- **Classes**: PascalCase (e.g., `SpendingCap`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_CATEGORY_LENGTH`)
- **Database tables**: plural snake_case (e.g., `spending_caps`)
- **Enum members**: UPPER_SNAKE_CASE (e.g., `Period.DAILY`)

#### Critical Rules for FinTech Projects
1. **Monetary Values**: Always use `Decimal`, never `float`
2. **Audit Logging**: Every state-changing operation must call audit service
3. **Error Responses**: Structured JSON errors, never expose stack traces or internals
4. **Authentication**: Every endpoint must use dependency injection for auth (except `GET /health`)
5. **Database Queries**: Always use parameterized queries, never raw SQL
6. **Secrets**: Read from environment variables or config, never hard-coded
7. **Deletes**: Use soft-delete (`is_active = False`), never physical deletes for financial records
8. **PII in Logs**: Mask user IDs, account numbers, names before logging
9. **Type Annotations**: Every function parameter and return type must be annotated

#### Testing Requirements
- Mirror `src/` structure in `tests/`
- At least one success test and one failure/edge-case test per feature
- Mock all external services
- Use factory fixtures in `conftest.py`
- Run: `pytest --cov=src --cov-report=term-missing`

#### Commit Message Format
Use conventional commits:
- `feat: add spending cap CRUD endpoints`
- `fix: prevent race condition in enforcement check`
- `test: add concurrent transaction tests`
- `docs: update API documentation`
- `refactor: extract audit logging into shared service`

## Skills Available

### OWASP LLM Top 10 Skill
**Usage**: `/owasp-llm-top10 [target-directory]`

Performs security verification against OWASP Top 10 for LLM Applications (2025):
- LLM01: Prompt Injection
- LLM02: Sensitive Information Disclosure
- LLM03: Supply Chain Vulnerabilities
- LLM04: Training Data Poisoning
- LLM05: Insecure Output Handling
- LLM06: Excessive Agency
- LLM07: System Prompt Leakage
- LLM08: Vector and Embedding Weaknesses
- LLM09: Misinformation
- LLM10: Unbounded Consumption

Use this skill when working on any AI/LLM integration or when security audit is needed.

## Common Patterns

### Java/Spring Boot Projects (Homework 1, 2)

#### Architecture Pattern
All Spring Boot projects follow a layered architecture:
1. **Controller Layer** - REST endpoints, request/response handling
2. **Service Layer** - Business logic
3. **Repository Layer** - Data access with Spring Data JPA
4. **Domain Layer** - Entities, DTOs, Enums
5. **Exception Layer** - Custom exceptions, global exception handler

#### Running Spring Boot Applications
```bash
# Using Maven wrapper (preferred)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# With Docker Compose for dependencies
docker-compose up -d postgres  # Start database
./mvnw spring-boot:run         # Start application
```

#### Testing Spring Boot Applications
```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
# Coverage report: target/site/jacoco/index.html

# Run specific test
./mvnw test -Dtest=ClassName

# Integration tests (uses Testcontainers)
./mvnw test -Dtest=*IntegrationTest
```

### Docker and Database Management

Most projects use PostgreSQL with Docker Compose:
```bash
# Start database
docker-compose up -d postgres

# Check database is ready
docker-compose exec postgres pg_isready -U postgres -d <database_name>

# Access PostgreSQL CLI
docker-compose exec postgres psql -U postgres -d <database_name>

# View logs
docker-compose logs -f postgres

# Stop database
docker-compose down
```

## Documentation Standards

Each homework must include:
1. **README.md** - Project overview, tech stack, setup instructions, API endpoints
2. **HOWTORUN.md** (or detailed Quick Start in README) - Step-by-step run instructions
3. **docs/screenshots/** - Screenshots of AI tool usage and application functionality
4. **Additional docs** (if applicable):
   - API_REFERENCE.md - Detailed API documentation
   - ARCHITECTURE.md - System architecture details
   - TESTING_GUIDE.md - Testing instructions
   - PLAN.md - Implementation plan

## Important Notes for AI Assistants

1. **Read before suggesting**: Always examine existing code structure before proposing changes
2. **Follow project patterns**: Each homework may use different conventions; respect the existing patterns
3. **Specification-first for Homework 3+**: When working on later homeworks, check for specification documents first
4. **Testing is critical**: All Homework 2+ projects require comprehensive test coverage
5. **Security consciousness**: Later homeworks involve FinTech/regulated domains; apply appropriate security practices
6. **Documentation matters**: This is an educational repository; maintain clear documentation for learning purposes

## Repository-Specific Rules

- **Main branch**: `main`
- **Working branches**: `homework-N-submission`
- **No direct commits to main**: Always use branches and PRs
- **Preserve homework isolation**: Each homework is independent; avoid cross-dependencies
- **Environment files**: Never commit `.env` files; always use `.env.example` templates
- **Test data**: Sample data lives in `data/` or `src/test/resources/fixtures/`
