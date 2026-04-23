export type Role = 'ROLE_ADMIN' | 'ROLE_FUNCIONARIO' | 'ROLE_MEDICO'

export interface User {
  login: string
  role: Role
}

export interface LoginPayload {
  login: string
  senha: string
}

export interface TokenResponse {
  tokenJWT: string
}

export interface JwtPayload {
  sub: string
  role: string
  exp: number
}
