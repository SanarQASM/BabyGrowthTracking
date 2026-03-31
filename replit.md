# Baby Growth Tracker

## Overview
A React + Vite web application providing personalized baby sleep and feeding guidance, built with TypeScript.

## Tech Stack
- **Frontend**: React 18, TypeScript, Vite 7
- **UI**: @blinkdotnew/ui (BlinkUI), Tailwind CSS, Framer Motion
- **Routing**: Simple state-based routing in App.tsx
- **Data Fetching**: @tanstack/react-query
- **Forms**: react-hook-form + zod
- **Charts**: Recharts
- **Package Manager**: npm

## Project Structure
```
src/
├── App.tsx                    # Root app with state-based routing (home/sleep/feeding)
├── main.tsx                   # Entry point with providers
├── index.css                  # Global styles, CSS variables, theme
├── data/
│   ├── sleepData.ts           # Sleep guide data (strategies, needs, environment, lullabies)
│   └── feedingData.ts         # Feeding guide data (milk, solids, schedules, avoid, tips)
├── pages/
│   ├── HomePage.tsx           # Landing page with navigation to guides
│   ├── SleepGuidePage.tsx     # Full sleep guide (4 categories + filters)
│   └── FeedingGuidePage.tsx   # Full feeding guide (5 categories + filters)
└── components/
    ├── ShieldIcon.tsx          # SVG shield icon with emoji (matches design)
    ├── UsefulButtons.tsx       # Useful/Useless voting buttons with count
    ├── ChildSelector.tsx       # Child name dropdown with guide title
    ├── CategoryGrid.tsx        # 2-column category selection grid
    ├── FilterTabs.tsx          # Horizontal filter tab bar
    └── PageHeader.tsx          # Back arrow + page title header
public/                        # Static assets
```

## Pages

### Home Page
- Navigation hub with Sleep Guide and Feeding Guide cards
- Pink/blue theme with shield logo

### Sleep Guide Page
Categories:
1. **Sleep Strategies** — Tips with Useful/Useless voting
2. **Sleep Needs** — Age-specific sleep requirements per child
3. **Environment** — Tabs: All / Bedtime / Naps, environment cards
4. **Lullabies** — Tabs: All / Kurdish / Arabic / English, with play/download + now-playing bar

### Feeding Guide Page
Categories:
1. **Milk Feeding** — Tabs: All / Breastfeeding / Formula / Combination
2. **Solid Foods** — Tabs: All / Starter / Advancing / Finger Foods
3. **Sample Schedule** — Age-based feeding schedule tables
4. **Foods to Avoid** — Safety cards with reasons and examples
5. **Feeding Tips** — General tips with voting

## Configuration
- **Dev port**: 5000 (vite.config.ts)
- **Host**: 0.0.0.0 (all hosts allowed for Replit proxy)
- **Environment**: `.env.local` — VITE_BLINK_PROJECT_ID, VITE_BLINK_PUBLISHABLE_KEY
- **Deployment**: Static site — builds to `dist/` with `npm run build`

## Color Palette (index.css)
- Background: Light blue (208 100% 97%)
- Primary: Pink/rose (330 81% 50%)
- Accent: Light pink (330 81% 65%)
- Secondary: Light blue (204 93.75% 93.72%)
