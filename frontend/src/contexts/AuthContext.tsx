import { createContext, useCallback, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '@/api/auth'
import type { User, Role, JwtPayload } from '@/types/auth'

interface AuthContextType {
  token: string | null
  user: User | null
  isAuthenticated: boolean
  login: (login: string, senha: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextType | null>(null)

function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    )
    return JSON.parse(json) as JwtPayload
  } catch {
    return null
  }
}

function userFromToken(token: string): User | null {
  const payload = decodeJwtPayload(token)
  if (!payload) return null
  return { login: payload.sub, role: payload.role as Role }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'))
  const [user, setUser] = useState<User | null>(() => {
    const t = localStorage.getItem('token')
    return t ? userFromToken(t) : null
  })

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token)
      setUser(userFromToken(token))
    } else {
      localStorage.removeItem('token')
      setUser(null)
    }
  }, [token])

  const login = useCallback(async (loginStr: string, senha: string) => {
    const { tokenJWT } = await authApi.login({ login: loginStr, senha })
    setToken(tokenJWT)
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    window.location.href = '/login'
  }, [])

  return (
    <AuthContext.Provider value={{ token, user, isAuthenticated: !!token, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
