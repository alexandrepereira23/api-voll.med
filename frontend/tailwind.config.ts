import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        background: '#F5F0EB',
        surface: '#EDE8E3',
        'surface-hover': '#E5DED7',
        border: '#D4CCC4',
        'text-primary': '#1C1917',
        'text-secondary': '#78716C',
        'text-muted': '#A8A29E',
        accent: {
          DEFAULT: '#6B7F6A',
          hover: '#5A6E59',
        },
        danger: {
          DEFAULT: '#C4714F',
          hover: '#B0623F',
        },
        success: '#4A7C59',
        warning: '#C4964F',
        white: '#FDFCFB',
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'sans-serif'],
      },
      borderRadius: {
        sm: '6px',
        md: '10px',
        lg: '16px',
        xl: '24px',
      },
      boxShadow: {
        sm: '0 1px 3px rgba(28,25,23,0.06)',
        md: '0 4px 12px rgba(28,25,23,0.08)',
        lg: '0 8px 24px rgba(28,25,23,0.10)',
      },
    },
  },
  plugins: [],
} satisfies Config
