# Testes Automatizados — API Voll.med

Suite com **89 testes**, 0 falhas. JUnit 5 + Mockito + Spring Boot Test.

---

## Como executar

```bash
# Suite completa
./mvnw test

# Classe específica
./mvnw test -Dtest=AgendaDeConsultasTest

# Múltiplas classes
./mvnw test -Dtest="ConsultaControllerTest,AgendaDeConsultasTest"
```

---

## Estratégia

### Testes unitários (`@ExtendWith(MockitoExtension.class)`)

Sem contexto Spring. Testam lógica isolada com dependências mockadas via Mockito.

- **Quando usar:** services com regras de negócio complexas, validadores de domínio
- **Velocidade:** rápidos (< 1s por classe)

### Testes de controller (`@WebMvcTest`)

Carregam apenas a camada web (controller + segurança). Services são `@MockBean`. Sem JPA, sem banco.

- **Quando usar:** validar rotas, roles (`@PreAuthorize`), status HTTP, serialização JSON
- **Configurações necessárias:** `@Import(MethodSecurityTestConfig.class)` + `@MockBean(JpaMetamodelMappingContext.class)` (ver `docs/DECISOES_TECNICAS.md`)

---

## Classes de teste

### Testes de domínio / service

| Classe | Testes | O que cobre |
|--------|--------|-------------|
| `AgendaDeConsultasTest` | 17 | Todas as validações de agendamento (horário, antecedência, disponibilidade, convênio) e cancelamento |
| `ProntuarioServiceTest` | 7 | Criação (403/409/400), edição (422 janela expirada, 403 médico errado), 404 |
| `EspecialidadeServiceTest` | 8 | CRUD, nome duplicado (409), inativação |
| `IaServiceTest` | 4 | Mock do `RestClient`, sem prontuários, pré-diagnóstico, laudo, resumo histórico |

### Testes de controller

| Classe | Testes | O que cobre |
|--------|--------|-------------|
| `ConsultaControllerTest` | 6 | Agendar (FUNCIONARIO), cancelar (FUNCIONARIO), listar, 403 para MEDICO |
| `MedicoControllerTest` | 6 | CRUD, 401 sem auth, 403 para MEDICO |
| `PacientesControllerTest` | 8 | CRUD, 401 sem auth, 403 para MEDICO em endpoints restritos |
| `ProntuarioControllerTest` | 9 | Criar/editar (MEDICO), deletar (ADMIN), listar/detalhar (qualquer auth), 403 |
| `PrescricaoControllerTest` | 5 | Criar (MEDICO), detalhar/listar (qualquer auth), 403, 401 |
| `AtestadoControllerTest` | 5 | Emitir (MEDICO), detalhar/listar (qualquer auth), 403, 401 |
| `EspecialidadeControllerTest` | 7 | CRUD (ADMIN), listar/detalhar (qualquer auth), 403 para FUNCIONARIO |
| `AutenticacaoControllerTest` | 5 | Login (200+token), cadastro (ADMIN), bloquear ADMIN duplicado (403), login duplicado (409), 403 para FUNCIONARIO |

---

## Configurações de teste

### `MethodSecurityTestConfig`

```java
@TestConfiguration
@EnableMethodSecurity
public class MethodSecurityTestConfig {}
```

Necessário porque `@WebMvcTest` não garante que `@EnableMethodSecurity` seja ativado. Sem isso, `@PreAuthorize` é ignorado.

### `src/test/resources/application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.flyway.enabled=false
spring.jpa.hibernate.ddl-auto=create-drop
```

H2 em memória. Flyway desabilitado — algumas migrations usam sintaxe MySQL incompatível com H2. O schema é criado pelo Hibernate a partir das entidades JPA.

---

## Padrões de autenticação nos testes

### `@WebMvcTest` com `@AuthenticationPrincipal Usuario`

Usar `.with(user(new Usuario(id, login, senha, Perfil.ROLE_XXX, null)))` — não `@WithMockUser`.

`@WithMockUser` cria um `User` padrão do Spring Security, não assignável ao `Usuario` customizado. O parâmetro `@AuthenticationPrincipal Usuario` receberia `null`.

```java
mvc.perform(post("/consultas")
    .with(user(new Usuario(1L, "func@test.com", "senha", Perfil.ROLE_FUNCIONARIO, null)))
    .with(csrf())
    ...)
```

### Exceção: `AutenticacaoControllerTest.deveRetornarTokenAoFazerLogin`

Usa `@WithMockUser` porque o endpoint `/auth/login` é público mas `@WebMvcTest` não carrega a config de segurança real. Ver `docs/DECISOES_TECNICAS.md` para detalhes.

### Requisições mutantes (POST/PUT/DELETE)

Sempre adicionar `.with(csrf())`. Sem CSRF token, Spring Security retorna 403 mesmo com usuário autenticado.
