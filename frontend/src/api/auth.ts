import api from './axios'
import type { TokenResponse, LoginPayload } from '@/types/auth'

export const authApi = {
  login: (dados: LoginPayload) =>
    api.post<TokenResponse>('/auth/login', dados).then((r) => r.data),
}
