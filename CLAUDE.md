# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Subir o banco de dados (MySQL 8.0 na porta 3307)
docker-compose up -d

# Executar a aplicação
./mvnw spring-boot:run

# Build completo (usar este após mudanças em @Value ou configuração — não confiar apenas no devtools)
./mvnw clean package

# Rodar testes
./mvnw test

# Rodar um único teste
./mvnw test -Dtest=NomeDaClasseTest
```

## Variáveis de ambiente (.env)

A aplicação lê `.env` via `spring.config.import=optional:file:.env[.properties]`. O padrão do projeto é: variável no `.env` → mapeada em `application.properties` → lida via `@Value`.

| Variável no .env | Propriedade Spring | Descrição |
|------------------|--------------------|-----------|
| `JWT_SECRET` | `api.security.token.secret` | Chave JWT (mín. 32 chars) — `openssl rand -base64 32` |
| `TOKEN_EXPIRACAO_HORAS` | `api.security.token.expiracao-horas` | Expiração do JWT (padrão: 2h) |
| `DB_PASSWORD` | `spring.datasource.password` | Senha do MySQL (padrão: `root`) |
| `ADMIN_LOGIN` | `admin.login` | Login do admin inicial (padrão: `admin@vollmed.com`) |
| `ADMIN_PASSWORD` | `admin.password` | Senha do admin inicial — **obrigatório para criar o admin** |

> **Importante:** Nunca usar `@Value("${NOME_VARIAVEL_ENV}")` diretamente. Sempre mapear em `application.properties` primeiro e usar `@Value("${propriedade.spring}")`. Este é o padrão adotado no projeto.

## Arquitetura

### Camadas

- **`controller/`** — endpoints REST; delegam para services ou domain services
- **`domain/<entidade>/`** — entidade JPA, repository, DTOs e regras de negócio internas
- **`service/`** — serviços de aplicação (`MedicoService`, `PacienteService`, `ConsultaService`)
- **`infra/security/`** — filtro JWT (`SecurityFillter`), rate limiting (`RateLimitFilter`), configuração Spring Security (`SecurityConfigurations`), criação do admin inicial (`AdminInitializer`)
- **`infra/exception/`** — handler global de erros (`TratadorDeErros`)
- **`docs/PLANEJAMENTO.md`** — roadmap de funcionalidades planejadas

### Fluxo de autenticação

1. `POST /auth/login` → `AutenticacaoController` → Spring Security autentica → `TokenService` gera JWT
2. Cada requisição passa por `RateLimitFilter` (bloqueia abusos) → `SecurityFillter` (valida JWT, carrega `Usuario` no `SecurityContext`)
3. `RateLimitFilter` atua apenas em `/auth/*` — máx. 10 req/IP em 15 min → HTTP 429

### Registro de filtros customizados (Spring Security 6.5+)

`SecurityFillter` e `RateLimitFilter` são `@Component`, mas **não devem ser registrados automaticamente como Servlet filters** pelo Spring Boot — apenas a security chain deve gerenciá-los. Por isso, `SecurityConfigurations` desabilita o auto-registro:

```java
@Bean
public FilterRegistrationBean<SecurityFillter> securityFillterRegistration(SecurityFillter f) {
    var reg = new FilterRegistrationBean<>(f);
    reg.setEnabled(false);
    return reg;
}
```

**Nunca remover esses `FilterRegistrationBean`** — sem eles, o Spring Security 6.5+ lança `IllegalArgumentException: does not have a registered order`.

### Perfis de usuário

- `ROLE_ADMIN` — cria usuários via `POST /auth/cadastro`
- `ROLE_FUNCIONARIO` — acesso completo à API
- `ROLE_MEDICO` — deve ver apenas consultas vinculadas ao seu cadastro (vínculo `Medico.usuario_id` existe no banco, mas o filtro por médico logado ainda não está implementado — ver `docs/PLANEJAMENTO.md`)

### Admin inicial (AdminInitializer)

Ao iniciar, `AdminInitializer` (implementa `ApplicationRunner`) verifica se existe algum `ROLE_ADMIN` no banco. Se não existir e `ADMIN_PASSWORD` estiver configurado, cria o admin automaticamente. O processo é idempotente.

Logs para acompanhar:
- `"AdminInitializer: verificando admin. Login configurado: X"` → método chamado, propriedade lida
- `"admin já existe no banco"` → já há um ROLE_ADMIN, nenhuma ação
- `"ADMIN_PASSWORD não configurado"` → variável de ambiente não lida
- `"Admin inicial criado com login: X"` → sucesso

### Banco de dados

- `ddl-auto=validate` — Hibernate valida o schema mas não o altera; toda mudança estrutural exige migration Flyway
- Migrations em `src/main/resources/db/migration/` com prefixo `V{n}__descricao.sql`
- Próxima migration a criar: `V12`
- Exclusão lógica via campo `ativo` em médicos, pacientes e consultas

### Regras de negócio de consultas (AgendaDeConsultas)

- Clínica funciona Seg–Sáb, 07h–19h
- Agendamento com mínimo 30 min de antecedência; cancelamento com mínimo 24h
- Paciente não pode ter duas consultas no mesmo dia
- Médico pode ser omitido — sistema escolhe aleatoriamente um disponível na data (sem agenda de disponibilidade real ainda — ver planejamento)

## Planejamento de evolução

Ver `docs/PLANEJAMENTO.md` para o roadmap completo. Resumo das prioridades:

1. **Prontuário eletrônico** — sem ele o sistema não tem memória clínica
2. **Agenda de disponibilidade do médico** — o agendamento atual é fictício sem isso
3. **Prescrição / receita médica**
4. **Triagem com prioridade**, retorno de consulta, atestado médico
5. **Convênios / planos de saúde**
6. **Auditoria LGPD** de acesso a prontuários
