import { useState } from 'react'
import { HomePage } from './pages/HomePage'
import { SleepGuidePage } from './pages/SleepGuidePage'
import { FeedingGuidePage } from './pages/FeedingGuidePage'

type Page = 'home' | 'sleep' | 'feeding'

function App() {
  const [page, setPage] = useState<Page>('home')

  return (
    <>
      {page === 'home' && (
        <HomePage onNavigate={(p) => setPage(p)} />
      )}
      {page === 'sleep' && (
        <SleepGuidePage onBack={() => setPage('home')} />
      )}
      {page === 'feeding' && (
        <FeedingGuidePage onBack={() => setPage('home')} />
      )}
    </>
  )
}

export default App
