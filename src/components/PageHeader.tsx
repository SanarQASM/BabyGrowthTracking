import { ArrowLeft } from 'lucide-react'

interface PageHeaderProps {
  title: string
  onBack: () => void
}

export function PageHeader({ title, onBack }: PageHeaderProps) {
  return (
    <div className="flex items-center gap-3 mb-4">
      <button
        onClick={onBack}
        className="flex items-center justify-center w-8 h-8 rounded-full bg-white/70 hover:bg-white transition-all shadow-sm border border-pink-100"
      >
        <ArrowLeft size={16} className="text-primary" />
      </button>
      <h1 className="text-base font-bold text-foreground">{title}</h1>
    </div>
  )
}
