import { useState } from 'react'
import { Music, Play, Download } from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { ChildSelector } from '../components/ChildSelector'
import { CategoryGrid, Category } from '../components/CategoryGrid'
import { FilterTabs } from '../components/FilterTabs'
import { UsefulButtons } from '../components/UsefulButtons'
import {
  children,
  sleepStrategies,
  sleepNeeds,
  environmentCards,
  lullabies,
} from '../data/sleepData'

const categories: Category[] = [
  { id: 'strategies', label: 'Sleep Strategies', emoji: '🛡️' },
  { id: 'needs', label: 'Sleep Needs', emoji: '💤' },
  { id: 'environment', label: 'Environment', emoji: '🌙' },
  { id: 'lullabies', label: 'Lullabies', emoji: '🎵' },
]

const envTabs = [
  { id: 'all', label: 'All' },
  { id: 'bedtime', label: 'Bedtime' },
  { id: 'naps', label: 'Naps' },
]

const lullabyTabs = [
  { id: 'all', label: 'All' },
  { id: 'kurdish', label: 'Kurdish' },
  { id: 'arabic', label: 'Arabic' },
  { id: 'english', label: 'English' },
]

interface SleepGuidePageProps {
  onBack: () => void
}

export function SleepGuidePage({ onBack }: SleepGuidePageProps) {
  const [selectedChild, setSelectedChild] = useState(children[0])
  const [selectedCategory, setSelectedCategory] = useState('strategies')
  const [envFilter, setEnvFilter] = useState('all')
  const [lullabyFilter, setLullabyFilter] = useState('all')
  const [nowPlaying, setNowPlaying] = useState<string | null>(null)

  const childNeeds = sleepNeeds[selectedChild]

  return (
    <div className="min-h-screen bg-background px-4 py-4 max-w-md mx-auto">
      <PageHeader title="Sleep Guide" onBack={onBack} />

      <ChildSelector
        children={children}
        selected={selectedChild}
        onChange={setSelectedChild}
        title={`Help ${selectedChild} Sleep Better 💤`}
        subtitle="Personalized guidance for your little one"
      />

      <CategoryGrid
        categories={categories}
        selected={selectedCategory}
        onSelect={setSelectedCategory}
      />

      {/* ── SLEEP STRATEGIES ─────────────────────────────────────────────── */}
      {selectedCategory === 'strategies' && (
        <div className="space-y-3 animate-fade-in">
          <h2 className="text-xs font-bold tracking-widest uppercase text-muted-foreground">Sleep Strategies</h2>
          {sleepStrategies.map(s => (
            <div key={s.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
              <h3 className="text-sm font-bold text-primary uppercase tracking-wide mb-1">Sleep Strategies</h3>
              <p className="text-sm font-semibold text-foreground mb-1">{s.title}</p>
              <p className="text-xs text-muted-foreground leading-relaxed">
                <span className="font-medium text-foreground">Description: </span>
                {s.description}
              </p>
              <UsefulButtons usefulCount={s.usefulCount} />
            </div>
          ))}
        </div>
      )}

      {/* ── SLEEP NEEDS ──────────────────────────────────────────────────── */}
      {selectedCategory === 'needs' && (
        <div className="animate-fade-in space-y-3">
          <div className="bg-white/60 rounded-2xl p-4 border border-pink-200 shadow-sm">
            <h2 className="text-base font-bold text-foreground mb-1">
              How Much Sleep Does {selectedChild} Need?
            </h2>
            <p className="text-xs text-muted-foreground italic mb-3">Age: {childNeeds.ageLabel}</p>

            <div className="space-y-2 mb-4">
              <div className="flex items-start gap-2">
                <span className="text-sm font-bold text-foreground w-28">Total Sleep:</span>
                <span className="text-sm text-foreground">{childNeeds.totalSleep}</span>
              </div>
              <div className="flex items-start gap-2 pl-4">
                <span className="text-xs text-muted-foreground w-24">Night:</span>
                <span className="text-xs text-foreground">{childNeeds.nightSleep}</span>
              </div>
              <div className="flex items-start gap-2 pl-4">
                <span className="text-xs text-muted-foreground w-24">Daytime:</span>
                <span className="text-xs text-foreground">{childNeeds.daytimeSleep}</span>
              </div>

              <div className="mt-2">
                <span className="text-sm font-bold text-foreground">Total Nap</span>
                {childNeeds.naps.map((nap, i) => (
                  <div key={i} className="pl-4 mt-1">
                    <div className="flex items-start gap-2">
                      <span className="text-xs text-muted-foreground w-28">{nap.name}:</span>
                      <div>
                        <div className="text-xs text-foreground">{nap.time}</div>
                        <div className="text-xs text-muted-foreground">Duration: {nap.duration}</div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-primary/10 rounded-xl p-3 border border-pink-200">
              <p className="text-xs font-bold text-primary uppercase tracking-wide mb-2">Tips for this age</p>
              <ul className="space-y-1">
                {childNeeds.tips.map((tip, i) => (
                  <li key={i} className="text-xs text-foreground flex items-start gap-1">
                    <span className="text-primary mt-0.5">•</span>
                    {tip}
                  </li>
                ))}
              </ul>
            </div>

            <UsefulButtons usefulCount={18} />
          </div>
        </div>
      )}

      {/* ── ENVIRONMENT ──────────────────────────────────────────────────── */}
      {selectedCategory === 'environment' && (
        <div className="animate-fade-in">
          <FilterTabs tabs={envTabs} selected={envFilter} onSelect={setEnvFilter} />

          <div className="mb-3">
            <h2 className="text-xs font-bold tracking-widest uppercase text-muted-foreground">
              {envFilter === 'bedtime' ? 'BEDTIME ENVIRONMENT 🌙' : envFilter === 'naps' ? 'NAP ENVIRONMENT ☀️' : 'SLEEP ENVIRONMENT'}
            </h2>
            <p className="text-xs text-muted-foreground">Optimal setup for {selectedChild}:</p>
          </div>

          <div className="space-y-3">
            {environmentCards
              .filter(c => envFilter === 'all' || c.category === envFilter)
              .map(card => (
                <div key={card.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-base">{card.icon}</span>
                    <h3 className="text-sm font-bold text-foreground uppercase tracking-wide">{card.title}</h3>
                  </div>
                  <p className="text-xs font-semibold text-primary mb-2">{card.subtitle}</p>

                  <p className="text-xs font-semibold text-foreground mb-1">Why it matters:</p>
                  <p className="text-xs text-muted-foreground leading-relaxed mb-2">{card.whyItMatters}</p>

                  <p className="text-xs font-semibold text-foreground mb-1">Tips:</p>
                  <ul className="space-y-0.5 mb-2">
                    {card.tips.map((tip, i) => (
                      <li key={i} className="text-xs text-foreground flex items-start gap-1">
                        <span className="text-green-500 mt-0.5">✓</span>
                        {tip}
                      </li>
                    ))}
                  </ul>

                  {card.tip && (
                    <div className="bg-yellow-50 rounded-lg p-2 border border-yellow-200 mb-1">
                      <p className="text-xs text-yellow-800">
                        <span className="font-semibold">💡 TIP: </span>
                        {card.tip}
                      </p>
                    </div>
                  )}

                  <UsefulButtons usefulCount={card.usefulCount} />
                </div>
              ))}
          </div>
        </div>
      )}

      {/* ── LULLABIES ────────────────────────────────────────────────────── */}
      {selectedCategory === 'lullabies' && (
        <div className="animate-fade-in">
          <FilterTabs tabs={lullabyTabs} selected={lullabyFilter} onSelect={setLullabyFilter} />

          <div className="mb-3">
            <h2 className="text-xs font-bold tracking-widest uppercase text-muted-foreground">LULLABIES & SOUNDS 🎵</h2>
            <p className="text-xs text-muted-foreground">Soothing sounds for {selectedChild}:</p>
          </div>

          <div className="space-y-3">
            {lullabies
              .filter(l => lullabyFilter === 'all' || l.language === lullabyFilter)
              .map(lullaby => (
                <div key={lullaby.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
                  <div className="flex items-center gap-2 mb-1">
                    <Music size={14} className="text-primary" />
                    <h3 className="text-sm font-bold text-foreground">{lullaby.title}</h3>
                  </div>
                  <p className="text-xs text-muted-foreground">{lullaby.genre}</p>
                  <p className="text-xs text-muted-foreground mb-3">Duration: {lullaby.duration}</p>

                  <div className="flex gap-2 mb-1">
                    <button
                      onClick={() => setNowPlaying(nowPlaying === lullaby.id ? null : lullaby.id)}
                      className={`flex items-center gap-1.5 px-4 py-1.5 rounded-full text-xs font-semibold transition-all ${
                        nowPlaying === lullaby.id
                          ? 'bg-primary text-white'
                          : 'bg-white text-primary border border-primary hover:bg-pink-50'
                      }`}
                    >
                      <Play size={11} />
                      {nowPlaying === lullaby.id ? 'Playing...' : 'Play'}
                    </button>
                    <button className="flex items-center gap-1.5 px-4 py-1.5 rounded-full text-xs font-semibold bg-white text-primary border border-primary hover:bg-pink-50 transition-all">
                      <Download size={11} />
                      Download
                    </button>
                  </div>

                  <UsefulButtons usefulCount={lullaby.usefulCount} />
                </div>
              ))}
          </div>

          {nowPlaying && (
            <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-pink-200 shadow-xl p-4 animate-slide-up">
              <p className="text-xs font-bold text-center text-foreground uppercase tracking-wide mb-2">NOW PLAYING:</p>
              <div className="flex items-center gap-2 justify-center mb-2">
                <Music size={14} className="text-primary" />
                <span className="text-sm font-semibold text-foreground">
                  {lullabies.find(l => l.id === nowPlaying)?.title}
                </span>
              </div>
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xs text-muted-foreground">0:00</span>
                <div className="flex-1 h-1.5 bg-pink-100 rounded-full">
                  <div className="h-full w-1/3 bg-primary rounded-full" />
                </div>
                <span className="text-xs text-muted-foreground">
                  {lullabies.find(l => l.id === nowPlaying)?.duration}
                </span>
              </div>
              <div className="flex items-center justify-center gap-4">
                <button className="text-lg">⏮</button>
                <button className="text-lg">⏪</button>
                <button onClick={() => setNowPlaying(null)} className="text-xl bg-primary text-white w-9 h-9 rounded-full flex items-center justify-center">⏸</button>
                <button className="text-lg">⏩</button>
                <button className="text-lg">⏭</button>
              </div>
            </div>
          )}
        </div>
      )}

      {nowPlaying && <div className="h-32" />}
    </div>
  )
}
