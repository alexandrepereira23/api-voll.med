# Regras de Negócio — API Voll.med

Este documento centraliza todas as regras de negócio implementadas na API, servindo como referência para desenvolvimento, revisão e testes.

---

## Consultas

### Agendamento (`AgendaDeConsultas.agendar`)

| # | Regra | Erro |
|---|-------|------|
| 1 | Paciente deve existir e estar ativo | 400 — Paciente não existe ou está inativo |
| 2 | Médico informado deve existir e estar ativo | 400 — Médico não existe ou está inativo |
| 3 | Clínica funciona Seg–Sáb, das 07h às 19h | 400 — Horário fora do funcionamento |
| 4 | Agendamento com mínimo 30 min de antecedência | 400 — Antecedência mínima não respeitada |
| 5 | Paciente não pode ter duas consultas no mesmo dia | 400 — Paciente já possui consulta no dia |
| 6 | Médico informado deve ter disponibilidade no dia/horário | 400 — Médico sem disponibilidade cadastrada |
| 7 | Médico não pode ter duas consultas no mesmo horário | 400 — Médico ocupado no horário |
| 8 | Se médico não for informado, sistema escolhe aleatório com disponibilidade real | — |
| 9 | Se não houver médico disponível, agendamento é recusado | 400 — Nenhum médico disponível |

### Cancelamento (`AgendaDeConsultas.cancelar`)

| # | Regra | Erro |
|---|-------|------|
| 1 | Consulta deve existir | 400 — ID da consulta não existe |
| 2 | Cancelamento com mínimo 24h de antecedência | 400 — Antecedência mínima não respeitada |

---

## Disponibilidade de Médicos

| # | Regra |
|---|-------|
| 1 | Médico deve estar ativo para ter disponibilidade cadastrada |
| 2 | Exclusão é lógica (campo `ativo = false`) |
| 3 | Ao agendar com médico explícito, o horário deve estar dentro de um intervalo ativo de disponibilidade |
| 4 | Ao buscar médico aleatório, apenas médicos com disponibilidade real no horário são considerados |

---

## Prontuários

| # | Regra | Erro |
|---|-------|------|
| 1 | Consulta referenciada deve existir e estar ativa | 404 / 400 |
| 2 | Cada consulta pode ter no máximo um prontuário | 409 — Prontuário já existe |
| 3 | Apenas o médico que realizou a consulta pode criar o prontuário | 403 |
| 4 | Apenas o médico que criou pode editar o prontuário | 403 |
| 5 | Edição permitida somente dentro de 24h após o registro | 422 — Janela de edição expirada |
| 6 | `ROLE_MEDICO` acessa apenas prontuários de suas próprias consultas | 403 |
| 7 | `ROLE_FUNCIONARIO` e `ROLE_ADMIN` têm acesso de leitura a todos os prontuários | — |
| 8 | Exclusão é lógica (campo `ativo = false`), restrita a `ROLE_ADMIN` | — |

---

## Usuários e Autenticação

| # | Regra |
|---|-------|
| 1 | Apenas `ROLE_ADMIN` pode criar novos usuários |
| 2 | Não é possível criar outro `ROLE_ADMIN` via endpoint de cadastro |
| 3 | Senhas armazenadas com BCrypt |
| 4 | Token JWT expira conforme `TOKEN_EXPIRACAO_HORAS` (padrão: 2h) |
| 5 | Rate limiting: máx. 10 requisições por IP em 15 min em `/auth/*` |
| 6 | Admin inicial criado automaticamente se não existir e `ADMIN_PASSWORD` estiver configurado |

---

## Perfis e Permissões por Endpoint

| Endpoint | ADMIN | FUNCIONARIO | MEDICO |
|----------|:-----:|:-----------:|:------:|
| `POST /auth/cadastro` | ✅ | — | — |
| `POST /medicos` | — | ✅ | — |
| `PUT /medicos` | — | ✅ | — |
| `DELETE /medicos/{id}` | — | ✅ | — |
| `GET /medicos` | ✅ | ✅ | ✅ |
| `POST /pacientes` | — | ✅ | — |
| `PUT /pacientes` | — | ✅ | — |
| `DELETE /pacientes/{id}` | — | ✅ | — |
| `GET /pacientes` | ✅ | ✅ | ✅ |
| `POST /consultas` | — | ✅ | — |
| `DELETE /consultas` | — | ✅ | — |
| `GET /consultas` | ✅ | ✅ | ✅ (apenas as suas) |
| `POST /medicos/{id}/disponibilidade` | — | ✅ | — |
| `GET /medicos/{id}/disponibilidade` | ✅ | ✅ | ✅ |
| `DELETE /medicos/{id}/disponibilidade/{dispId}` | — | ✅ | — |
| `POST /prontuarios` | — | — | ✅ (da sua consulta) |
| `PUT /prontuarios` | — | — | ✅ (seu, em 24h) |
| `GET /prontuarios` | ✅ | ✅ | ✅ (apenas os seus) |
| `DELETE /prontuarios/{id}` | ✅ | — | — |
