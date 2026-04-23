# Arquitetura Frontend — Voll.med

React 18 + TypeScript + Vite. SPA com roteamento client-side. Comunicação com backend Spring Boot via Axios.

---

## Estrutura de Pastas

```
frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/
│   │   ├── axios.ts              # instância Axios com interceptores
│   │   ├── auth.ts               # login, logout
│   │   ├── medicos.ts            # CRUD médicos
│   │   ├── pacientes.ts          # CRUD pacientes
│   │   ├── consultas.ts          # agendamento, cancelamento
│   │   ├── prontuarios.ts        # prontuários
│   │   ├── prescricoes.ts        # prescrições
│   │   ├── atestados.ts          # atestados
│   │   ├── especialidades.ts     # especialidades
│   │   ├── convenios.ts          # convênios
│   │   └── ia.ts                 # IA clínica
│   ├── components/
│   │   ├── ui/                   # primitivos reutilizáveis
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Spinner.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Pagination.tsx
│   │   │   └── EmptyState.tsx
│   │   └── layout/
│   │       ├── Layout.tsx        # wrapper sidebar + main
│   │       ├── Sidebar.tsx
│   │       └── NavItem.tsx
│   ├── contexts/
│   │   └── AuthContext.tsx       # token JWT, user, login(), logout()
│   ├── hooks/
│   │   ├── useAuth.ts            # acesso ao AuthContext
│   │   └── usePageTitle.ts       # seta document.title
│   ├── pages/
│   │   ├── Login.tsx
│   │   ├── Dashboard.tsx
│   │   ├── medicos/
│   │   │   ├── MedicosPage.tsx   # listagem
│   │   │   └── MedicoForm.tsx    # cadastro/edição
│   │   ├── pacientes/
│   │   ├── consultas/
│   │   ├── prontuarios/
│   │   ├── prescricoes/
│   │   ├── atestados/
│   │   ├── especialidades/
│   │   ├── convenios/
│   │   └── ia/
│   ├── routes/
│   │   ├── AppRouter.tsx         # todas as rotas
│   │   └── PrivateRoute.tsx      # guard: redireciona para /login sem token
│   ├── types/
│   │   ├── auth.ts
│   │   ├── medico.ts
│   │   ├── paciente.ts
│   │   ├── consulta.ts
│   │   ├── prontuario.ts
│   │   ├── prescricao.ts
│   │   ├── atestado.ts
│   │   ├── especialidade.ts
│   │   ├── convenio.ts
│   │   └── common.ts             # Page<T>, ApiError
│   ├── utils/
│   │   ├── jwt.ts                # decodificar payload do JWT
│   │   ├── date.ts               # formatação de datas (pt-BR)
│   │   └── masks.ts              # CPF, telefone, CEP
│   ├── main.tsx
│   └── index.css
├── docs/                         # esta pasta
├── tailwind.config.ts
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## Camada de API (`src/api/`)

### axios.ts

```ts
import axios from 'axios'

const api = axios.create({ baseURL: 'http://localhost:8080' })

// Injeta token em toda requisição
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 401 → logout
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
```

### Padrão de função de API

```ts
// src/api/medicos.ts
import api from './axios'
import type { Medico, DadosCadastroMedico, Page } from '../types/medico'

export const medicos = {
  listar: (page = 0, size = 10) =>
    api.get<Page<Medico>>(`/medicos?page=${page}&size=${size}`).then(r => r.data),

  detalhar: (id: number) =>
    api.get<Medico>(`/medicos/${id}`).then(r => r.data),

  cadastrar: (dados: DadosCadastroMedico) =>
    api.post<Medico>('/medicos', dados).then(r => r.data),

  atualizar: (dados: Partial<DadosCadastroMedico>) =>
    api.put<Medico>('/medicos', dados).then(r => r.data),

  excluir: (id: number) =>
    api.delete(`/medicos/${id}`),
}
```

---

## AuthContext

```ts
interface AuthContextType {
  token: string | null
  user: { login: string; role: 'ROLE_ADMIN' | 'ROLE_FUNCIONARIO' | 'ROLE_MEDICO' } | null
  isAuthenticated: boolean
  login: (login: string, senha: string) => Promise<void>
  logout: () => void
}
```

Token salvo em `localStorage`. Role extraída do payload JWT (base64 decode, campo `role`).

---

## Tipagem (`src/types/`)

### common.ts — tipos globais

```ts
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number   // página atual
  size: number
}

export interface ApiError {
  message?: string
  [field: string]: string | undefined
}
```

### Padrão por entidade

Cada arquivo de tipo exporta:
- Interface da entidade completa (response da API)
- Interface de cadastro (`Dados*Cadastro`)
- Interface de atualização (`Dados*Atualizacao`)
- Interface de listagem simplificada (`Dados*Listagem`) quando necessário

---

## Roteamento

```tsx
// AppRouter.tsx
<Routes>
  <Route path="/login" element={<Login />} />
  <Route element={<PrivateRoute />}>
    <Route element={<Layout />}>
      <Route path="/" element={<Dashboard />} />
      <Route path="/medicos" element={<MedicosPage />} />
      <Route path="/pacientes" element={<PacientesPage />} />
      <Route path="/consultas" element={<ConsultasPage />} />
      <Route path="/prontuarios" element={<ProntuariosPage />} />
      <Route path="/especialidades" element={<EspecialidadesPage />} />
      <Route path="/convenios" element={<ConveniosPage />} />
      <Route path="/ia" element={<IaPage />} />
    </Route>
  </Route>
  <Route path="*" element={<Navigate to="/" replace />} />
</Routes>
```

---

## Gerenciamento de Estado

**Sem Redux/Zustand.** Estado local com `useState` + contexto apenas para auth.

| Dado | Onde fica |
|------|-----------|
| Token JWT | `AuthContext` + `localStorage` |
| Lista paginada de médicos | `useState` na `MedicosPage` |
| Form de cadastro | `react-hook-form` local |
| Loading/error de fetch | `useState` local |

Se a complexidade crescer, adotar **TanStack Query** (não Zustand — o backend tem paginação e cache server-side faz mais sentido).

---

## Paginação

O backend retorna `Page<T>` com `content`, `totalElements`, `totalPages`, `number`.

O componente `<Pagination />` recebe:
```ts
interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}
```

---

## Formatação

```ts
// src/utils/date.ts
export const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('pt-BR')

export const formatDateTime = (iso: string) =>
  new Date(iso).toLocaleString('pt-BR')

// src/utils/masks.ts
export const formatCPF = (cpf: string) =>
  cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
```

---

## Convenções de Código

- Componentes: PascalCase (`MedicosPage.tsx`)
- Funções/hooks: camelCase (`useAuth`, `formatDate`)
- Tipos/interfaces: PascalCase (`Medico`, `DadosCadastroMedico`)
- Arquivos de API: camelCase plural (`medicos.ts`, `consultas.ts`)
- Props de componentes: sempre tipar com interface explícita (nunca `any`)
- Imports: absolutos via `@/` (configurar no tsconfig + vite.config)
