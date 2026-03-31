interface ShieldIconProps {
  emoji: string
  size?: 'sm' | 'md' | 'lg'
  active?: boolean
}

export function ShieldIcon({ emoji, size = 'md', active = false }: ShieldIconProps) {
  const sizeClasses = {
    sm: 'w-12 h-14 text-lg',
    md: 'w-16 h-18 text-2xl',
    lg: 'w-20 h-24 text-3xl',
  }

  return (
    <div className={`relative flex items-center justify-center ${sizeClasses[size]}`}>
      <svg
        viewBox="0 0 64 72"
        className="absolute inset-0 w-full h-full"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M32 2L6 12V36C6 52 18 64 32 70C46 64 58 52 58 36V12L32 2Z"
          fill={active ? 'hsl(330, 81%, 50%)' : 'hsl(330, 81%, 88%)'}
          stroke={active ? 'hsl(330, 81%, 40%)' : 'hsl(330, 81%, 70%)'}
          strokeWidth="2"
        />
        <path
          d="M32 8L10 17V36C10 50 20 60 32 65C44 60 54 50 54 36V17L32 8Z"
          fill={active ? 'hsl(330, 81%, 60%)' : 'hsl(330, 81%, 93%)'}
          opacity="0.6"
        />
      </svg>
      <span className="relative z-10 select-none" style={{ fontSize: size === 'sm' ? '1rem' : size === 'md' ? '1.4rem' : '1.8rem' }}>
        {emoji}
      </span>
    </div>
  )
}
