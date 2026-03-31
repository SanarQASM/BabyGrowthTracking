import { useState } from 'react'
import { ThumbsUp, ThumbsDown } from 'lucide-react'

interface UsefulButtonsProps {
  usefulCount: number
  label?: string
}

export function UsefulButtons({ usefulCount, label }: UsefulButtonsProps) {
  const [vote, setVote] = useState<'useful' | 'useless' | null>(null)
  const [count, setCount] = useState(usefulCount)

  const handleVote = (v: 'useful' | 'useless') => {
    if (vote === v) {
      setVote(null)
      if (v === 'useful') setCount(c => c - 1)
    } else {
      if (vote === 'useful' && v === 'useless') setCount(c => c - 1)
      if (vote === 'useless' && v === 'useful') setCount(c => c + 1)
      if (!vote && v === 'useful') setCount(c => c + 1)
      setVote(v)
    }
  }

  return (
    <div className="flex items-center gap-2 mt-3 pt-3 border-t border-pink-100">
      <span className="text-xs text-muted-foreground flex-1">
        Useful for {count} Users
      </span>
      <button
        onClick={() => handleVote('useful')}
        className={`flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium transition-all ${
          vote === 'useful'
            ? 'bg-primary text-white'
            : 'bg-pink-100 text-primary hover:bg-pink-200'
        }`}
      >
        <ThumbsUp size={12} />
        {label === undefined ? 'Useful' : label}
      </button>
      <button
        onClick={() => handleVote('useless')}
        className={`flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium transition-all ${
          vote === 'useless'
            ? 'bg-gray-400 text-white'
            : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
        }`}
      >
        <ThumbsDown size={12} />
        Useless
      </button>
    </div>
  )
}
