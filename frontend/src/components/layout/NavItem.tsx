import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'

interface NavItemProps {
  to: string
  icon: ReactNode
  label: string
}

export function NavItem({ to, icon, label }: NavItemProps) {
  return (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) =>
        isActive
          ? 'flex items-center gap-3 px-3 py-2 rounded-md bg-surface-hover text-text-primary font-medium border-l-2 border-accent transition-colors duration-150'
          : 'flex items-center gap-3 px-3 py-2 rounded-md text-text-secondary hover:bg-surface-hover hover:text-text-primary transition-colors duration-150'
      }
    >
      {icon}
      <span className="text-sm">{label}</span>
    </NavLink>
  )
}
