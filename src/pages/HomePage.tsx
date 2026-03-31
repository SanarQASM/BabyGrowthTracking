import { Moon, UtensilsCrossed } from 'lucide-react'
import { ShieldIcon } from '../components/ShieldIcon'

interface HomePageProps {
  onNavigate: (page: 'sleep' | 'feeding') => void
}

export function HomePage({ onNavigate }: HomePageProps) {
  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center px-6 py-10">
      <div className="text-center mb-10">
        <div className="flex justify-center mb-4">
          <ShieldIcon emoji="👶" size="lg" active />
        </div>
        <h1 className="text-2xl font-bold text-foreground mb-2">Baby Guide</h1>
        <p className="text-sm text-muted-foreground">Personalized guidance for your little one</p>
      </div>

      <div className="w-full max-w-sm space-y-4">
        <button
          onClick={() => onNavigate('sleep')}
          className="w-full flex items-center gap-4 bg-primary/10 hover:bg-primary/20 border-2 border-pink-200 hover:border-primary rounded-2xl p-5 transition-all group"
        >
          <div className="flex-shrink-0">
            <div className="w-12 h-12 bg-primary/20 rounded-full flex items-center justify-center group-hover:bg-primary/30 transition-all">
              <Moon size={22} className="text-primary" />
            </div>
          </div>
          <div className="text-left">
            <h2 className="text-base font-bold text-foreground">Sleep Guide</h2>
            <p className="text-xs text-muted-foreground">Strategies, schedules & lullabies</p>
          </div>
          <div className="ml-auto text-primary text-lg">→</div>
        </button>

        <button
          onClick={() => onNavigate('feeding')}
          className="w-full flex items-center gap-4 bg-primary/10 hover:bg-primary/20 border-2 border-pink-200 hover:border-primary rounded-2xl p-5 transition-all group"
        >
          <div className="flex-shrink-0">
            <div className="w-12 h-12 bg-primary/20 rounded-full flex items-center justify-center group-hover:bg-primary/30 transition-all">
              <UtensilsCrossed size={22} className="text-primary" />
            </div>
          </div>
          <div className="text-left">
            <h2 className="text-base font-bold text-foreground">Feeding Guide</h2>
            <p className="text-xs text-muted-foreground">Milk, solids, schedules & tips</p>
          </div>
          <div className="ml-auto text-primary text-lg">→</div>
        </button>
      </div>
    </div>
  )
}
