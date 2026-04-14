# Voll.med API

API REST para gerenciamento de uma clínica médica fictícia chamada **Voll.med**. Permite o cadastro e gerenciamento de médicos, pacientes e consultas, com autenticação JWT e controle de acesso por perfis.

---

## Tecnologias Utilizadas

| Tecnologia | Versão | Descrição |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.5.4 | Framework base |
| Spring Web | — | Criação da API REST |
| Spring Data JPA | — | Camada de persistência |
| Spring Security | — | Autenticação e autorização |
| Spring Validation | — | Validação de dados |
| Flyway | — | Migrations do banco de dados |
| MySQL | 8.0 | Banco de dados relacional |
| Lombok | — | Redução de código boilerplate |
| Docker / Docker Compose | — | Containerização |
| Maven | 3.9 | Gerenciador de build |

---

## Estrutura do Projeto

```
src/
└── main/
    ├── java/med/voll/api/
    │   ├── ApiApplication.java
    │   ├── controller/              # Endpoints da API
    │   ├── domain/                  # Entidades e regras de negócio
    │   │   ├── consulta/
    │   │   ├── endereco/
    │   │   ├── medico/              # inclui DisponibilidadeMedico
    │   │   ├── paciente/
    │   │   ├── prontuario/
    │   │   └── usuario/
    │   ├── exception/               # Exceções de domínio
    │   ├── infra/
    │   │   ├── exception/           # Handler global de erros
    │   │   └── security/            # Filtros JWT e configuração Spring Security
    │   └── service/                 # Camada de serviços
    └── resources/
        ├── application.properties
        └── db/migration/            # Scripts Flyway (V1 a V13)
```

---

## Pré-requisitos

- **Java 17** ou superior
- **Maven 3.9+**
- **Docker** e **Docker Compose**

---

## Configuração

### 1. Variáveis de ambiente

Copie o arquivo de exemplo e preencha os valores:

```bash
cp .env.example .env
```

| Variável | Obrigatória | Padrão | Descrição |
|----------|-------------|--------|-----------|
| `DB_HOST` | Não | `localhost` | Host do banco de dados |
| `DB_PORT` | Não | `3307` | Porta do banco de dados |
| `DB_NAME` | Não | `vollmed_api` | Nome do banco de dados |
| `DB_USER` | Não | `root` | Usuário do banco |
| `DB_PASSWORD` | **Sim** | — | Senha do banco |
| `MYSQL_ROOT_PASSWORD` | **Sim** | — | Senha root do MySQL (Docker Compose) |
| `JWT_SECRET` | **Sim** | — | Chave secreta para assinar tokens JWT (min. 32 chars) |
| `TOKEN_EXPIRACAO_HORAS` | Não | `2` | Expiração do JWT em horas |
| `ADMIN_LOGIN` | Não | `admin@vollmed.com` | Login do admin inicial |
| `ADMIN_PASSWORD` | **Sim** | — | Senha do admin inicial (min. 8 chars) |

Para gerar um `JWT_SECRET` seguro:

```bash
openssl rand -base64 32
```

### 2. Subir o banco de dados

```bash
docker-compose up -d
```

Inicia um container MySQL 8.0 na porta **3307**.

---

## Como Executar

```bash
# Clone o repositório
git clone <url-do-repositorio>
cd api-voll.med

# Configure as variáveis de ambiente
cp .env.example .env
# edite o .env com seus valores

# Suba o banco
docker-compose up -d

# Execute a aplicação
./mvnw spring-boot:run
```

A API ficará disponível em: **http://localhost:8080**

A documentação Swagger estará em: **http://localhost:8080/swagger-ui.html**

---

## Perfis de Usuário

| Perfil | Descrição |
|--------|-----------|
| `ROLE_ADMIN` | Administrador — pode cadastrar novos usuários |
| `ROLE_FUNCIONARIO` | Funcionário — acesso completo à API |
| `ROLE_MEDICO` | Médico — vê apenas suas próprias consultas |

### Primeiro acesso (admin inicial)

Na primeira inicialização, a aplicação cria automaticamente um usuário admin com as credenciais definidas em `ADMIN_LOGIN` e `ADMIN_PASSWORD`. Se `ADMIN_PASSWORD` não estiver definido, o admin não é criado e um aviso é exibido no log.

Esse processo é idempotente: se já existir um admin no banco, nada acontece.

---

## Endpoints da API

### Autenticação

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/auth/login` | Público | Autentica o usuário e retorna o JWT |
| `POST` | `/auth/cadastro` | `ROLE_ADMIN` | Cria um novo usuário (`ROLE_FUNCIONARIO` ou `ROLE_MEDICO`) |

**Login:**
```json
POST /auth/login
{
  "login": "admin@vollmed.com",
  "senha": "suaSenha"
}
```

**Cadastro de usuário (requer token de admin):**
```json
POST /auth/cadastro
Authorization: Bearer <token>

{
  "login": "funcionario@vollmed.com",
  "senha": "Senha123!",
  "role": "ROLE_FUNCIONARIO"
}
```

> `role` aceita: `ROLE_FUNCIONARIO` ou `ROLE_MEDICO`. Não é possível criar outro `ROLE_ADMIN` por esta rota.

---

### Médicos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/medicos` | Cadastra um novo médico |
| `GET` | `/medicos` | Lista médicos ativos (paginado) |
| `GET` | `/medicos/{id}` | Detalha um médico |
| `PUT` | `/medicos` | Atualiza dados de um médico |
| `DELETE` | `/medicos/{id}` | Inativa um médico (exclusão lógica) |

**Exemplo de cadastro:**
```json
{
  "nome": "Dr. João Silva",
  "email": "joao.silva@voll.med",
  "telefone": "11999999999",
  "crm": "123456",
  "especialidade": "CARDIOLOGIA",
  "endereco": {
    "logradouro": "Rua das Flores",
    "bairro": "Centro",
    "cep": "00000000",
    "cidade": "São Paulo",
    "uf": "SP"
  }
}
```

---

### Pacientes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/pacientes` | Cadastra um novo paciente |
| `GET` | `/pacientes` | Lista pacientes ativos (paginado) |
| `GET` | `/pacientes/{id}` | Detalha um paciente |
| `PUT` | `/pacientes` | Atualiza dados de um paciente |
| `DELETE` | `/pacientes/{id}` | Inativa um paciente (exclusão lógica) |

**Exemplo de cadastro:**
```json
{
  "nome": "Maria Oliveira",
  "email": "maria@email.com",
  "telefone": "11988888888",
  "cpf": "12345678909",
  "endereco": {
    "logradouro": "Av. Paulista",
    "bairro": "Bela Vista",
    "cep": "01310100",
    "cidade": "São Paulo",
    "uf": "SP"
  }
}
```

---

### Consultas

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/consultas` | `ROLE_FUNCIONARIO` | Agenda uma nova consulta |
| `GET` | `/consultas` | Autenticado | Lista consultas ativas (paginado) |
| `DELETE` | `/consultas` | `ROLE_FUNCIONARIO` | Cancela uma consulta |

**Agendamento:**
```json
{
  "idPaciente": 1,
  "idMedico": 1,
  "data": "2025-12-10T10:00:00"
}
```

> `idMedico` é opcional — se omitido, o sistema escolhe automaticamente um médico com disponibilidade cadastrada no horário.

**Cancelamento:**
```json
{
  "idConsulta": 1,
  "motivo": "PACIENTE_DESISTIU"
}
```

> Motivos aceitos: `PACIENTE_DESISTIU`, `MEDICO_CANCELOU`, `OUTROS`.
> `ROLE_MEDICO` vê apenas as consultas vinculadas ao seu próprio cadastro.

---

### Disponibilidade de Médicos

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/medicos/{id}/disponibilidade` | `ROLE_FUNCIONARIO` | Cadastra um horário de disponibilidade |
| `GET` | `/medicos/{id}/disponibilidade` | Autenticado | Lista horários do médico |
| `DELETE` | `/medicos/{id}/disponibilidade/{dispId}` | `ROLE_FUNCIONARIO` | Remove um horário |

**Cadastro de disponibilidade:**
```json
{
  "diaSemana": "MONDAY",
  "horaInicio": "08:00:00",
  "horaFim": "17:00:00"
}
```

> Valores aceitos para `diaSemana`: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`.
> O agendamento de consultas valida se o médico possui disponibilidade no dia e horário solicitados.

---

### Prontuários

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/prontuarios` | `ROLE_MEDICO` | Cria prontuário vinculado a uma consulta |
| `GET` | `/prontuarios` | Autenticado | Lista prontuários ativos (paginado) |
| `GET` | `/prontuarios/{id}` | Autenticado | Detalha um prontuário |
| `GET` | `/prontuarios/paciente/{id}` | Autenticado | Histórico clínico do paciente |
| `PUT` | `/prontuarios` | `ROLE_MEDICO` | Atualiza prontuário (janela de 24h) |
| `DELETE` | `/prontuarios/{id}` | `ROLE_ADMIN` | Inativa um prontuário |

**Criação de prontuário:**
```json
{
  "consultaId": 1,
  "anamnese": "Paciente relata dor de cabeça há 3 dias...",
  "diagnostico": "Enxaqueca tensional",
  "cid10": "G43",
  "observacoes": "Repouso e hidratação recomendados"
}
```

> `ROLE_MEDICO` acessa apenas prontuários de consultas que realizou.
> Prontuário não pode ser editado após 24h do registro.

---

## Banco de Dados — Migrations Flyway

| Migration | Descrição |
|-----------|-----------|
| `V1` | Criação da tabela `medicos` |
| `V2` | Adição da coluna `telefone` em médicos |
| `V3` | Criação da tabela `pacientes` |
| `V4` | Adição da coluna `ativo` em médicos |
| `V5` | Adição da coluna `ativo` em pacientes |
| `V6` | Criação da tabela `consultas` |
| `V7` | Criação da tabela `usuarios` |
| `V8` | Constraint `UNIQUE` na coluna `login` de usuários |
| `V9` | Correção do tipo da coluna `ativo` em consultas (`TINYINT`) |
| `V10` | Conversão da coluna `uf` de `CHAR(2)` para `VARCHAR(2)` em médicos e pacientes |
| `V11` | Adição da coluna `usuario_id` (FK) em médicos |
| `V12` | Criação da tabela `prontuarios` |
| `V13` | Criação da tabela `disponibilidade_medico` |

---

## Segurança

- Autenticação via **JWT** (Bearer Token)
- Senhas hasheadas com **BCrypt**
- **Rate limiting** em `/auth/*`: máximo 10 tentativas por IP em 15 minutos (HTTP 429)
- Headers de segurança: `X-Frame-Options: DENY`, `X-Content-Type-Options`, `Cache-Control`, `Referrer-Policy`
- Mensagens de erro sem stack trace ou detalhes internos
- Swagger desabilitado em produção (`springdoc.swagger-ui.enabled=false`)

---

## Observações

- A exclusão de médicos, pacientes e consultas é **lógica** (soft delete via campo `ativo`).
- A listagem padrão retorna **10 registros por página**.
- O `JWT_SECRET` sem valor configurado impede a inicialização da aplicação (falha intencional).

---

## Licença

Projeto desenvolvido para fins de aprendizado no curso da **Alura**.
