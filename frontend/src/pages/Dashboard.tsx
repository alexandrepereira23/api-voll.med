import { LayoutDashboard } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'

export function Dashboard() {
  const { user } = useAuth()

  return (
    <div>
      <div className="flex items-center gap-3 mb-8">
        <LayoutDashboard size={20} className="text-text-secondary" />
        <h1 className="text-2xl font-bold text-text-primary">Dashboard</h1>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-border p-6">
        <p className="text-text-secondary text-sm leading-relaxed">
          Bem-vindo, <span className="font-medium text-text-primary">{user?.login}</span>.
          O sistema está pronto para uso.
        </p>
      </div>
    </div>
  )
}
