import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Stethoscope, AlertCircle } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import type { AxiosError } from 'axios'

const schema = z.object({
  login: z.string().min(1, 'Informe o login'),
  senha: z.string().min(1, 'Informe a senha'),
})

type FormData = z.infer<typeof schema>

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [authError, setAuthError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  async function onSubmit(data: FormData) {
    setAuthError(null)
    try {
      await login(data.login, data.senha)
      navigate('/', { replace: true })
    } catch (err) {
      const status = (err as AxiosError)?.response?.status
      if (status === 401 || status === 403) {
        setAuthError('Credenciais inválidas. Verifique seu login e senha.')
      } else {
        setAuthError('Erro ao conectar com o servidor.')
      }
    }
  }

  return (
    <div className="min-h-screen flex bg-background">
      {/* Lado esquerdo — branding */}
      <div className="hidden lg:flex flex-col justify-between w-[480px] flex-shrink-0 bg-surface px-14 py-16 border-r border-border">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-accent flex items-center justify-center">
            <Stethoscope size={20} className="text-white" />
          </div>
          <span className="text-xl font-bold text-text-primary">Voll.med</span>
        </div>

        <div>
          <h1 className="text-3xl font-bold text-text-primary leading-snug mb-4">
            Cuidado clínico<br />
            com mais precisão
          </h1>
          <p className="text-text-secondary leading-relaxed text-sm">
            Gerencie consultas, prontuários, prescrições e mais — em um sistema
            pensado para o fluxo real de uma clínica.
          </p>
        </div>

        <div className="flex flex-col gap-3">
          {[
            'Prontuário eletrônico com janela de edição segura',
            'Agendamento inteligente com disponibilidade do médico',
            'IA clínica para pré-diagnóstico e laudos',
          ].map((item) => (
            <div key={item} className="flex items-start gap-2.5">
              <div className="w-1.5 h-1.5 rounded-full bg-accent mt-1.5 flex-shrink-0" />
              <p className="text-sm text-text-secondary leading-relaxed">{item}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Lado direito — formulário */}
      <div className="flex-1 flex items-center justify-center px-6">
        <div className="w-full max-w-sm">
          {/* Logo mobile */}
          <div className="flex items-center gap-2.5 mb-10 lg:hidden">
            <div className="w-9 h-9 rounded-md bg-accent flex items-center justify-center">
              <Stethoscope size={18} className="text-white" />
            </div>
            <span className="text-lg font-bold text-text-primary">Voll.med</span>
          </div>

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-text-primary">Entrar</h2>
            <p className="text-sm text-text-secondary mt-1">
              Acesse sua conta para continuar
            </p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <Input
              label="Login"
              placeholder="seu@email.com"
              autoComplete="username"
              {...register('login')}
              error={errors.login?.message}
            />

            <Input
              label="Senha"
              type="password"
              placeholder="••••••••"
              autoComplete="current-password"
              {...register('senha')}
              error={errors.senha?.message}
            />

            {authError && (
              <div className="flex items-start gap-2 p-3 rounded-md bg-danger/10 border border-danger/20">
                <AlertCircle size={15} className="text-danger mt-0.5 flex-shrink-0" />
                <p className="text-xs text-danger leading-relaxed">{authError}</p>
              </div>
            )}

            <Button type="submit" loading={isSubmitting} className="w-full mt-1">
              Entrar
            </Button>
          </form>
        </div>
      </div>
    </div>
  )
}
