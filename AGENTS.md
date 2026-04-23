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
- Controllers: `AutenticacaoController`, `MedicoController`, `PacientesController`, `ConsultaController`, `DisponibilidadeMedicoController`, `ProntuarioController`, `PrescricaoController`, `AtestadoController`, `ConvenioController`, `ConvenioPacienteController`, `AuditoriaController`, `EspecialidadeController`, `IaController`

## Migrations applied

V1–V22 applied (next: V23)

## Tests

89 tests passing. Run: `./mvnw test`

- Unit: `AgendaDeConsultasTest` (17), `ProntuarioServiceTest` (7), `EspecialidadeServiceTest` (8), `IaServiceTest` (4)
- Controller (`@WebMvcTest`): `ConsultaControllerTest`, `MedicoControllerTest`, `PacientesControllerTest`, `ProntuarioControllerTest`, `PrescricaoControllerTest`, `AtestadoControllerTest`, `EspecialidadeControllerTest`, `AutenticacaoControllerTest`
- See `docs/TESTES.md` for full strategy

## Gotchas

- `.env` file must exist for DB connection (DB_PASSWORD=root)
- JWT_SECRET defaults to weak `12345678` - override in production
- MySQL container needs password: `DB_PASSWORD=root` in `.env`
- Test database uses H2 in-memory (configured in spring-boot-starter-test)
- `SecurityFillter` and `RateLimitFilter` have `FilterRegistrationBean` disabling auto-registration — never remove them (Spring Security 6.5+ requirement)
- `ANTHROPIC_API_KEY` required for `/ia/*` endpoints — app starts without it but calls fail at runtime
- IA endpoints are `ROLE_MEDICO` only — do not change to broader roles
