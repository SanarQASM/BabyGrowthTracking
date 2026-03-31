# Baby Growth Tracker

## Overview
A React + Vite web application for tracking baby growth metrics, built with TypeScript.

## Tech Stack
- **Frontend**: React 18, TypeScript, Vite 7
- **UI**: @blinkdotnew/ui (BlinkUI), Tailwind CSS, Framer Motion
- **Routing**: @tanstack/react-router
- **Data Fetching**: @tanstack/react-query
- **Forms**: react-hook-form + zod
- **Charts**: Recharts
- **3D**: @react-three/fiber + @react-three/drei
- **Package Manager**: npm

## Project Structure
- `src/` — React frontend source code
  - `main.tsx` — App entry point with QueryClient and BlinkUIProvider
  - `App.tsx` — Root app component
  - `lib/utils.ts` — Utility functions
- `public/` — Static assets
- `backend-side/` — Kotlin Multiplatform backend (not active in Replit)
- `composeApp/` — Kotlin Multiplatform Compose app (not active in Replit)
- `iosApp/` — iOS app entry (not active in Replit)

## Configuration
- **Dev port**: 5000 (configured in vite.config.ts)
- **Host**: 0.0.0.0 (all hosts allowed for Replit proxy)
- **Environment**: `.env.local` contains VITE_BLINK_PROJECT_ID and VITE_BLINK_PUBLISHABLE_KEY
- **Deployment**: Static site — builds to `dist/` with `npm run build`

## Workflows
- **Start application**: `npm run dev` — serves on port 5000

## Deployment
- Target: Static
- Build: `npm run build`
- Public directory: `dist`
