# AGENTS.md

## Run Commands

```bash
# Start MySQL (required) - wait for "healthy" status before running app
docker-compose up -d

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

## Architecture

- **Framework**: Spring Boot 3.5.4, Java 17, Maven
- **Database**: MySQL 8.0 on port 3307 (via Docker)
- **Migrations**: Flyway in `src/main/resources/db/migration/`
- **Auth**: JWT via `/login` (public); other endpoints require Bearer token
- **Soft deletes**: Entities use `ativo` field, not physical deletes
- **Pagination**: 10 records per page (default)
- **API Docs**: Swagger UI at `/swagger-ui.html`

## Entry Points

- `ApiApplication.java` - main class
- Controllers: `AutenticacaoController`, `MedicoController`, `PacientesController`, `ConsultaController`

## Gotchas

- `.env` file must exist for DB connection (DB_PASSWORD=root)
- JWT_SECRET defaults to weak `12345678` - override in production
- MySQL container needs password: `DB_PASSWORD=root` in `.env`
- Test database uses H2 in-memory (configured in spring-boot-starter-test)
