import { ShieldIcon } from './ShieldIcon'

export interface Category {
  id: string
  label: string
  emoji: string
}

interface CategoryGridProps {
  categories: Category[]
  selected: string
  onSelect: (id: string) => void
}

export function CategoryGrid({ categories, selected, onSelect }: CategoryGridProps) {
  return (
    <div className="mb-4">
      <p className="text-[10px] font-semibold tracking-widest text-muted-foreground uppercase mb-3">Select Category</p>
      <div className="grid grid-cols-2 gap-3">
        {categories.map(cat => (
          <button
            key={cat.id}
            onClick={() => onSelect(cat.id)}
            className={`flex flex-col items-center gap-1 p-3 rounded-2xl transition-all border-2 ${
              selected === cat.id
                ? 'border-primary bg-pink-50 shadow-sm'
                : 'border-transparent bg-white/40 hover:bg-white/70'
            }`}
          >
            <ShieldIcon emoji={cat.emoji} size="md" active={selected === cat.id} />
            <span className={`text-xs font-semibold text-center leading-tight ${selected === cat.id ? 'text-primary' : 'text-foreground'}`}>
              {cat.label}
            </span>
          </button>
        ))}
      </div>
    </div>
  )
}
