import { useState } from 'react'
import { PageHeader } from '../components/PageHeader'
import { ChildSelector } from '../components/ChildSelector'
import { CategoryGrid, Category } from '../components/CategoryGrid'
import { FilterTabs } from '../components/FilterTabs'
import { UsefulButtons } from '../components/UsefulButtons'
import {
  milkFeedingCards,
  solidFoodCards,
  scheduleCards,
  foodsToAvoid,
  feedingTips,
} from '../data/feedingData'
import { children } from '../data/sleepData'

const categories: Category[] = [
  { id: 'milk', label: 'Milk Feeding', emoji: '🍼' },
  { id: 'solids', label: 'Solid Foods', emoji: '🥣' },
  { id: 'schedule', label: 'Sample Schedule', emoji: '📅' },
  { id: 'avoid', label: 'Foods to Avoid', emoji: '🚫' },
  { id: 'tips', label: 'Feeding Tips', emoji: '💡' },
]

const milkTabs = [
  { id: 'all', label: 'All' },
  { id: 'breastfeeding', label: 'Breastfeeding' },
  { id: 'formula', label: 'Formula Feeding' },
  { id: 'combination', label: 'Combination' },
]

const solidTabs = [
  { id: 'all', label: 'All' },
  { id: 'starter', label: 'Starter' },
  { id: 'advancing', label: 'Advancing' },
  { id: 'finger-foods', label: 'Finger Foods' },
]

interface FeedingGuidePageProps {
  onBack: () => void
}

export function FeedingGuidePage({ onBack }: FeedingGuidePageProps) {
  const [selectedChild, setSelectedChild] = useState(children[0])
  const [selectedCategory, setSelectedCategory] = useState('milk')
  const [milkFilter, setMilkFilter] = useState('all')
  const [solidFilter, setSolidFilter] = useState('all')

  return (
    <div className="min-h-screen bg-background px-4 py-4 max-w-md mx-auto">
      <PageHeader title="Feeding Guide" onBack={onBack} />

      <ChildSelector
        children={children}
        selected={selectedChild}
        onChange={setSelectedChild}
        title={`Help ${selectedChild} Eat Well 🍽️`}
        subtitle="Personalized feeding guidance for your little one"
      />

      <CategoryGrid
        categories={categories}
        selected={selectedCategory}
        onSelect={setSelectedCategory}
      />

      {/* ── MILK FEEDING ─────────────────────────────────────────────────── */}
      {selectedCategory === 'milk' && (
        <div className="animate-fade-in">
          <FilterTabs tabs={milkTabs} selected={milkFilter} onSelect={setMilkFilter} />
          <div className="space-y-3">
            {milkFeedingCards
              .filter(c => milkFilter === 'all' || c.tab === milkFilter)
              .map(card => (
                <div key={card.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-lg">{card.emoji}</span>
                    <h3 className="text-sm font-bold text-foreground">{card.title}</h3>
                  </div>

                  {card.fields.map((field, fi) => (
                    <div key={fi} className="mb-2">
                      <p className="text-xs font-bold text-foreground uppercase tracking-wide">{field.label}</p>
                      {field.value && (
                        <p className="text-xs text-muted-foreground leading-relaxed whitespace-pre-line pl-1">
                          {field.value}
                        </p>
                      )}
                      {field.items && (
                        <ul className="pl-1 space-y-0.5">
                          {field.items.map((item, ii) => (
                            <li key={ii} className="text-xs text-muted-foreground flex items-start gap-1">
                              <span className="text-primary mt-0.5">•</span>
                              {item}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  ))}

                  {card.tip && (
                    <div className="bg-yellow-50 rounded-lg p-2 border border-yellow-200 mt-2">
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

      {/* ── SOLID FOODS ──────────────────────────────────────────────────── */}
      {selectedCategory === 'solids' && (
        <div className="animate-fade-in">
          <FilterTabs tabs={solidTabs} selected={solidFilter} onSelect={setSolidFilter} />
          <div className="space-y-3">
            {solidFoodCards
              .filter(c => solidFilter === 'all' || c.tab === solidFilter)
              .map(card => (
                <div key={card.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-lg">{card.emoji}</span>
                    <div>
                      <h3 className="text-sm font-bold text-foreground">{card.title}</h3>
                      <p className="text-xs text-muted-foreground">Age: {card.ageRange}</p>
                    </div>
                  </div>

                  <p className="text-xs font-bold text-foreground mb-1 uppercase tracking-wide">Recommended Foods:</p>
                  <ul className="space-y-0.5 mb-2">
                    {card.foods.map((food, fi) => (
                      <li key={fi} className="text-xs text-foreground flex items-start gap-1">
                        <span className="text-green-500 mt-0.5">✓</span>
                        {food}
                      </li>
                    ))}
                  </ul>

                  {card.tip && (
                    <div className="bg-yellow-50 rounded-lg p-2 border border-yellow-200">
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

      {/* ── SAMPLE SCHEDULE ──────────────────────────────────────────────── */}
      {selectedCategory === 'schedule' && (
        <div className="animate-fade-in space-y-3">
          <h2 className="text-xs font-bold tracking-widest uppercase text-muted-foreground">Sample Feeding Schedules</h2>
          {scheduleCards.map(sc => (
            <div key={sc.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
              <h3 className="text-sm font-bold text-primary mb-3">{sc.ageRange}</h3>

              <div className="space-y-2 mb-3">
                {sc.schedule.map((entry, i) => (
                  <div key={i} className="flex items-center gap-3 py-1.5 border-b border-pink-100 last:border-0">
                    <span className="text-xs font-bold text-primary w-16 flex-shrink-0">{entry.time}</span>
                    <div className="flex-1">
                      <span className="text-xs text-foreground">{entry.feeding}</span>
                      {entry.amount && (
                        <span className="text-xs text-muted-foreground ml-2">({entry.amount})</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              <div className="bg-white/60 rounded-xl p-2.5 mb-1">
                <p className="text-xs font-bold text-foreground mb-1">Notes:</p>
                <ul className="space-y-0.5">
                  {sc.notes.map((note, i) => (
                    <li key={i} className="text-xs text-muted-foreground flex items-start gap-1">
                      <span className="text-primary mt-0.5">•</span>
                      {note}
                    </li>
                  ))}
                </ul>
              </div>

              <UsefulButtons usefulCount={sc.usefulCount} />
            </div>
          ))}
        </div>
      )}

      {/* ── FOODS TO AVOID ───────────────────────────────────────────────── */}
      {selectedCategory === 'avoid' && (
        <div className="animate-fade-in space-y-3">
          <div className="bg-red-50 rounded-xl p-3 border border-red-200 mb-3">
            <p className="text-xs text-red-700 font-semibold">
              ⚠️ Important: Always consult your pediatrician before introducing new foods.
            </p>
          </div>
          {foodsToAvoid.map(food => (
            <div key={food.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xl">{food.emoji}</span>
                <h3 className="text-sm font-bold text-foreground">{food.title}</h3>
              </div>

              <p className="text-xs font-bold text-foreground mb-1 uppercase tracking-wide">Why to Avoid:</p>
              <p className="text-xs text-muted-foreground leading-relaxed mb-2">{food.reason}</p>

              <p className="text-xs font-bold text-foreground mb-1 uppercase tracking-wide">Examples:</p>
              <ul className="space-y-0.5 mb-1">
                {food.examples.map((ex, i) => (
                  <li key={i} className="text-xs text-foreground flex items-start gap-1">
                    <span className="text-red-400 mt-0.5">✗</span>
                    {ex}
                  </li>
                ))}
              </ul>

              <UsefulButtons usefulCount={food.usefulCount} />
            </div>
          ))}
        </div>
      )}

      {/* ── FEEDING TIPS ─────────────────────────────────────────────────── */}
      {selectedCategory === 'tips' && (
        <div className="animate-fade-in space-y-3">
          <h2 className="text-xs font-bold tracking-widest uppercase text-muted-foreground">Feeding Tips for {selectedChild}</h2>
          {feedingTips.map(tip => (
            <div key={tip.id} className="bg-primary/10 rounded-2xl p-4 border border-pink-200">
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xl">{tip.emoji}</span>
                <h3 className="text-sm font-bold text-foreground">{tip.title}</h3>
              </div>
              <p className="text-xs text-muted-foreground leading-relaxed">{tip.description}</p>
              <UsefulButtons usefulCount={tip.usefulCount} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
