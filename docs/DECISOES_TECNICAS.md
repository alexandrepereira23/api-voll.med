# Decisões Técnicas — API Voll.med

Registro de decisões arquiteturais e de design adotadas no projeto, com justificativa. Serve como referência para novos desenvolvedores e para revisões futuras.

---

## Segurança

### Registro de filtros desabilitado (Spring Security 6.5+)

`SecurityFillter` e `RateLimitFilter` são `@Component`, mas são registrados **apenas** via security chain — o auto-registro como Servlet filter está desabilitado via `FilterRegistrationBean` em `SecurityConfigurations`.

**Por quê:** No Spring Security 6.5+, filtros `@Component` com `@Order` causam `IllegalArgumentException: does not have a registered order` se registrados nos dois contextos simultaneamente. Sem os `FilterRegistrationBean`, a aplicação não inicializa.

**Nunca remover** os beans `FilterRegistrationBean` em `SecurityConfigurations`.

---

### Variáveis de ambiente via `application.properties`

O padrão adotado é: variável no `.env` → mapeada em `application.properties` → lida via `@Value("${propriedade.spring}")`.

**Por quê:** Evita acoplamento direto entre código Java e nomes de variáveis de ambiente. Se a variável mudar de nome, só o `application.properties` precisa ser atualizado. Facilita também o uso de valores default via `${prop:default}`.

**Nunca usar** `@Value("${NOME_VARIAVEL_ENV}")` diretamente no código.

---

## Banco de Dados

### `ddl-auto=validate`

O Hibernate valida o schema mas não o altera. Toda mudança estrutural exige migration Flyway.

**Por quê:** Previne alterações acidentais no schema em produção. Garante que as migrations são a fonte de verdade do schema.

---

### Exclusão lógica (soft delete)

Médicos, pacientes, consultas, prontuários e disponibilidades usam o campo `ativo` em vez de `DELETE` físico.

**Por quê:** Preserva histórico clínico e integridade referencial. Um médico inativo ainda aparece em consultas passadas.

---

### Tipo `DayOfWeek` do Java em `disponibilidade_medico`

O campo `dia_semana` usa `VARCHAR(20)` no banco armazenando o nome do enum Java (`MONDAY`, `TUESDAY`, ...).

**Por quê:** Reutiliza o enum padrão do Java sem criar um enum customizado. A query nativa usa `DayOfWeek.name()` para converter.

---

## API e Controllers

### `@ParameterObject` em todos os endpoints com `Pageable`

Todos os parâmetros `Pageable` nos controllers usam `@ParameterObject` do SpringDoc.

**Por quê:** Sem a anotação, o Swagger UI renderiza o `sort` como `array[string]` com valor padrão `["string"]`, que é enviado literalmente ao JPA e causa `InvalidDataAccessApiUsageException`. Com `@ParameterObject`, os parâmetros são expandidos individualmente (`page`, `size`, `sort`).

---

### `ResponseStatusException` no `ProntuarioService`

O service de prontuários usa `ResponseStatusException` em vez de `ValidacaoException` customizada.

**Por quê:** Os erros de prontuário têm códigos HTTP distintos (403, 404, 409, 422) que não se encaixam no padrão 400 da `ValidacaoException`. `ResponseStatusException` permite especificar o status exato sem criar múltiplas classes de exceção.

---

### Controller sem Service para `DisponibilidadeMedico`

O `DisponibilidadeMedicoController` acessa os repositories diretamente, sem uma camada de service separada.

**Por quê:** A lógica é simples (buscar médico → criar/inativar disponibilidade) e não há regras de negócio complexas que justifiquem uma classe extra. Seguindo o princípio de não criar abstrações desnecessárias.

---

## Migrations

### Sequência de migrations

| Faixa | Conteúdo |
|-------|----------|
| V1–V8 | Base: médicos, pacientes, consultas, usuários |
| V9–V11 | Correções e ajustes na base |
| V12 | Prontuário eletrônico |
| V13 | Disponibilidade de médicos |
| V14+ | Funcionalidades planejadas (ver `PLANEJAMENTO.md`) |

Migrations são imutáveis após aplicadas em qualquer ambiente. Para corrigir uma migration já aplicada, criar uma nova.
