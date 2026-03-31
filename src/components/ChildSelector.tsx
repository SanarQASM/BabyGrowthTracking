import { ChevronDown } from 'lucide-react'

interface ChildSelectorProps {
  children: string[]
  selected: string
  onChange: (child: string) => void
  title: string
  subtitle: string
}

export function ChildSelector({ children, selected, onChange, title, subtitle }: ChildSelectorProps) {
  return (
    <div className="bg-white/60 rounded-2xl p-4 mb-4 shadow-sm border border-pink-100">
      <h2 className="text-center text-lg font-bold text-primary mb-1">{title}</h2>
      <p className="text-center text-xs text-muted-foreground mb-3">{subtitle}</p>
      <div className="flex flex-col gap-1">
        <span className="text-[10px] font-semibold tracking-widest text-muted-foreground uppercase">Select Child</span>
        <div className="relative">
          <select
            value={selected}
            onChange={e => onChange(e.target.value)}
            className="w-full appearance-none bg-pink-100 text-primary font-medium rounded-xl px-4 py-2 pr-10 text-sm cursor-pointer focus:outline-none focus:ring-2 focus:ring-primary/40 border border-pink-200"
          >
            {children.map(c => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
          <ChevronDown size={16} className="absolute right-3 top-1/2 -translate-y-1/2 text-primary pointer-events-none" />
        </div>
      </div>
    </div>
  )
}
