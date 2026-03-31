interface FilterTabsProps {
  tabs: { id: string; label: string }[]
  selected: string
  onSelect: (id: string) => void
}

export function FilterTabs({ tabs, selected, onSelect }: FilterTabsProps) {
  return (
    <div className="flex gap-2 overflow-x-auto pb-1 mb-4 no-scrollbar">
      {tabs.map(tab => (
        <button
          key={tab.id}
          onClick={() => onSelect(tab.id)}
          className={`flex-shrink-0 px-4 py-1.5 rounded-full text-xs font-semibold transition-all border ${
            selected === tab.id
              ? 'bg-primary text-white border-primary shadow-sm'
              : 'bg-white/70 text-foreground border-pink-200 hover:border-primary'
          }`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  )
}
