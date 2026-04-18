# Pontos Pendentes — API Voll.med

Registro de itens identificados como incompletos ou não implementados após a conclusão do roadmap principal.

---

## ✅ 1. CRUD completo de Especialidades — IMPLEMENTADO

**Problema:** apenas `GET /especialidades` existia. Adicionar ou renomear especialidades exigia migration manual.

**Solução implementada:**

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/especialidades` | `ROLE_ADMIN` | Cadastrar nova especialidade |
| `GET` | `/especialidades` | Autenticado | Listar especialidades ativas |
| `GET` | `/especialidades/{id}` | Autenticado | Detalhar especialidade |
| `PUT` | `/especialidades/{id}` | `ROLE_ADMIN` | Atualizar nome |
| `DELETE` | `/especialidades/{id}` | `ROLE_ADMIN` | Inativar (soft delete) |

**Arquivos criados/modificados:**
- `domain/medico/DadosCadastroEspecialidade.java`
- `domain/medico/DadosAtualizacaoEspecialidade.java`
- `domain/medico/DadosDetalhamentoEspecialidade.java`
- `domain/medico/EspecialidadeEntity.java` — adicionado `atualizar()`, `inativar()`, construtor com nome
- `domain/medico/EspecialidadeRepository.java` — adicionado `existsByNomeIgnoreCase()`
- `service/EspecialidadeService.java` — criado com validação de nome duplicado (HTTP 409)
- `controller/EspecialidadeController.java` — CRUD completo

**Regra de negócio:** não é possível cadastrar duas especialidades com o mesmo nome (case-insensitive) — retorna HTTP 409.

---

## ✅ 2. Validação de convênio no médico — IMPLEMENTADO (V22)

**Solução implementada:**

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/medicos/{id}/convenios` | `ROLE_ADMIN` / `ROLE_FUNCIONARIO` | Vincular convênio ao médico |
| `GET` | `/medicos/{id}/convenios` | Autenticado | Listar convênios do médico |
| `DELETE` | `/medicos/{id}/convenios/{convenioId}` | `ROLE_ADMIN` / `ROLE_FUNCIONARIO` | Desvincular convênio |

**Validação no agendamento:** `AgendaDeConsultas.agendar()` agora rejeita com HTTP 400 se `convenioId` for informado mas o médico selecionado não aceitar o convênio.

**Arquivos criados/modificados:**
- `domain/medico/MedicoConvenio.java` — entidade join
- `domain/medico/MedicoConvenioRepository.java`
- `domain/medico/DadosVinculoConvenioMedico.java`
- `domain/medico/DadosDetalhamentoConvenioMedico.java`
- `controller/MedicoConvenioController.java`
- `domain/consulta/AgendaDeConsultas.java` — nova dependência + validação
- `V22__create-table-medico-convenios.sql`

---

## ✅ 3. Testes automatizados — IMPLEMENTADO

**Estratégia:** testes unitários com JUnit 5 + Mockito (sem contexto Spring); testes de controller com `@WebMvcTest` + serviços mockados; H2 para `@SpringBootTest`.

### Testes unitários criados
- `AgendaDeConsultasTest` — 13 cenários cobrindo todas as validações de agendamento e cancelamento, incluindo a nova regra de convênio (V22)
- `EspecialidadeServiceTest` — nome duplicado (409), CRUD, inativação
- `ProntuarioServiceTest` — janela de 24h (422), restrição por médico (403), conflito (409)
- `IaServiceTest` — mock do `RestClient`, sem prontuários, geração de pré-diagnóstico/laudo/resumo

### Testes de controller criados (`@WebMvcTest`)
- `MedicoControllerTest` — CRUD com controle de acesso por role
- `ConsultaControllerTest` — agendar, cancelar, listar com roles
- `EspecialidadeControllerTest` — CRUD completo com controle de acesso por role

### Como rodar
```bash
./mvnw test
./mvnw test -Dtest=NomeDaClasseTest
```
