export interface SleepStrategy {
  id: string
  title: string
  description: string
  usefulCount: number
}

export interface SleepNeed {
  ageLabel: string
  totalSleep: string
  nightSleep: string
  daytimeSleep: string
  naps: { name: string; time: string; duration: string }[]
  tips: string[]
}

export interface EnvironmentCard {
  id: string
  category: 'bedtime' | 'naps' | 'all'
  title: string
  icon: string
  subtitle: string
  whyItMatters: string
  tips: string[]
  tip?: string
  usefulCount: number
}

export interface Lullaby {
  id: string
  language: 'kurdish' | 'arabic' | 'english' | 'all'
  title: string
  genre: string
  duration: string
  usefulCount: number
}

export const children = ['Sara', 'Ali', 'Layla', 'Omar']

export const sleepStrategies: SleepStrategy[] = [
  {
    id: 's1',
    title: 'Consistent Bedtime Routine',
    description: 'Put Sara to bed at the same time every night (7–8 PM). A predictable routine signals sleep time.',
    usefulCount: 18,
  },
  {
    id: 's2',
    title: 'White Noise or Soft Music',
    description: 'Put Sara to bed at the same time every night (7–8 PM). Gentle background sounds help block sudden noises.',
    usefulCount: 22,
  },
  {
    id: 's3',
    title: 'Drowsy but Awake',
    description: 'Put Sara to bed at the same time every night (7–8 PM). Place baby in the crib drowsy so they learn to self-soothe.',
    usefulCount: 15,
  },
  {
    id: 's4',
    title: 'Gradual Check-ins',
    description: 'Check on Sara at increasing intervals to reassure without fully waking. Helps develop independent sleep.',
    usefulCount: 12,
  },
  {
    id: 's5',
    title: 'Swaddle Transition',
    description: 'Transition from a full swaddle to arms-out to no swaddle gradually as Sara develops motor skills.',
    usefulCount: 9,
  },
]

export const sleepNeeds: Record<string, SleepNeed> = {
  Sara: {
    ageLabel: '8 months',
    totalSleep: '12–15 hours',
    nightSleep: '10–12 hrs',
    daytimeSleep: '2–3 hrs',
    naps: [
      { name: 'Morning Nap', time: '9:00–10:30 AM', duration: '1–1.5 hours' },
      { name: 'Afternoon Nap', time: '1:00–2:30 PM', duration: '1–1.5 hours' },
    ],
    tips: [
      'Most babies drop to 2 naps',
      'Night sleep becomes longer',
      'May sleep through the night',
      'Avoid naps after 4 PM',
    ],
  },
  Ali: {
    ageLabel: '3 months',
    totalSleep: '14–17 hours',
    nightSleep: '8–9 hrs',
    daytimeSleep: '6–7 hrs',
    naps: [
      { name: 'Morning Nap', time: '8:30–10:00 AM', duration: '1–1.5 hours' },
      { name: 'Midday Nap', time: '12:00–1:30 PM', duration: '1–2 hours' },
      { name: 'Afternoon Nap', time: '3:30–4:30 PM', duration: '45 min–1 hour' },
    ],
    tips: [
      'Sleep is very fragmented at this age',
      'Watch for sleepy cues (yawning, rubbing eyes)',
      'Short wake windows of 60–90 min',
      'Swaddling can help extend sleep',
    ],
  },
  Layla: {
    ageLabel: '12 months',
    totalSleep: '12–14 hours',
    nightSleep: '11–12 hrs',
    daytimeSleep: '2–3 hrs',
    naps: [
      { name: 'Afternoon Nap', time: '12:30–2:30 PM', duration: '1.5–2 hours' },
    ],
    tips: [
      'Transitioning to 1 nap around 12–18 months',
      'Night sleep is consolidated',
      'May resist bedtime due to FOMO',
      'Keep a consistent wake-up time',
    ],
  },
  Omar: {
    ageLabel: '6 months',
    totalSleep: '12–15 hours',
    nightSleep: '9–10 hrs',
    daytimeSleep: '3–4 hrs',
    naps: [
      { name: 'Morning Nap', time: '9:00–10:00 AM', duration: '1 hour' },
      { name: 'Afternoon Nap', time: '1:00–2:30 PM', duration: '1.5 hours' },
      { name: 'Catnap', time: '4:00–4:30 PM', duration: '30 min' },
    ],
    tips: [
      '3 naps transitioning to 2',
      'Sleep regressions common at 6 months',
      'Start sleep training if desired',
      'Solid foods may affect sleep',
    ],
  },
}

export const environmentCards: EnvironmentCard[] = [
  {
    id: 'e1',
    category: 'bedtime',
    title: 'Room Temperature',
    icon: '🌡️',
    subtitle: '18–21°C (65–70°F)',
    whyItMatters:
      'Babies sleep best in cool rooms. Overheating increases the risk of sleep-related issues and can cause frequent night wakings.',
    tips: [
      "Check the baby's neck — it should feel warm, not sweaty.",
      'Do not rely on hands or feet, as they are naturally cooler.',
      'Remove one layer of clothing if the room feels warm.',
    ],
    usefulCount: 18,
  },
  {
    id: 'e2',
    category: 'bedtime',
    title: 'Lighting',
    icon: '🌙',
    subtitle: 'Recommended: Complete darkness',
    whyItMatters:
      'Even small amounts of light can disrupt melatonin, the hormone that helps the baby sleep.',
    tips: [
      'Blackout curtains.',
      'Cover any LED lights.',
      'No night lights.',
    ],
    tip: 'Use a red-spectrum nightlight if you need light for night feeds — red light has the least impact on melatonin.',
    usefulCount: 14,
  },
  {
    id: 'e3',
    category: 'bedtime',
    title: 'Sound Environment',
    icon: '🔊',
    subtitle: 'White noise at 50–60 dB',
    whyItMatters:
      'Consistent background sound masks household noises that can wake a sleeping baby.',
    tips: [
      'Place white noise machine at least 2m from the crib.',
      'Use a fan, white noise machine, or app.',
      'Keep volume consistent through the night.',
    ],
    usefulCount: 20,
  },
  {
    id: 'e4',
    category: 'naps',
    title: 'Nap Environment',
    icon: '☀️',
    subtitle: 'Slightly lighter than night',
    whyItMatters:
      'Nap rooms can be slightly brighter than nighttime environments to help maintain circadian rhythm.',
    tips: [
      'Use light-filtering curtains (not blackout) for naps.',
      'Keep room temperature the same as night sleep.',
      'Use white noise to block daytime sounds.',
    ],
    usefulCount: 11,
  },
  {
    id: 'e5',
    category: 'naps',
    title: 'Nap Timing',
    icon: '⏰',
    subtitle: 'Watch for sleepy cues',
    whyItMatters:
      'Napping too late in the day can interfere with nighttime sleep and make bedtime harder.',
    tips: [
      'Last nap should end at least 2 hours before bedtime.',
      'Watch for yawning, eye-rubbing, or fussiness.',
      'Keep nap times consistent day to day.',
    ],
    usefulCount: 16,
  },
]

export const lullabies: Lullaby[] = [
  { id: 'l1', language: 'kurdish', title: 'Lale Lale Kurdan', genre: 'Traditional Kurdish', duration: '3:45', usefulCount: 18 },
  { id: 'l2', language: 'kurdish', title: 'Şîna Dayê', genre: 'Traditional Kurdish', duration: '4:12', usefulCount: 12 },
  { id: 'l3', language: 'arabic', title: 'Ya Rait', genre: 'Traditional Arabic', duration: '3:30', usefulCount: 20 },
  { id: 'l4', language: 'arabic', title: 'Numi Numi', genre: 'Arabic Lullaby', duration: '2:58', usefulCount: 15 },
  { id: 'l5', language: 'english', title: 'Twinkle Twinkle', genre: 'Classic English', duration: '2:15', usefulCount: 25 },
  { id: 'l6', language: 'english', title: 'Rock-a-Bye Baby', genre: 'Classic English', duration: '1:50', usefulCount: 19 },
]
