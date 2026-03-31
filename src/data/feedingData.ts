export interface FeedingCard {
  id: string
  tab: 'breastfeeding' | 'formula' | 'combination' | 'all'
  emoji: string
  title: string
  fields: { label: string; value?: string; items?: string[] }[]
  tip?: string
  usefulCount: number
}

export interface SolidFoodCard {
  id: string
  tab: 'starter' | 'advancing' | 'finger-foods' | 'all'
  title: string
  emoji: string
  ageRange: string
  foods: string[]
  tip?: string
  usefulCount: number
}

export interface ScheduleCard {
  id: string
  ageRange: string
  schedule: { time: string; feeding: string; amount?: string }[]
  notes: string[]
  usefulCount: number
}

export interface FoodsToAvoid {
  id: string
  title: string
  emoji: string
  reason: string
  examples: string[]
  usefulCount: number
}

export interface FeedingTip {
  id: string
  title: string
  emoji: string
  description: string
  usefulCount: number
}

export const milkFeedingCards: FeedingCard[] = [
  {
    id: 'mf1',
    tab: 'breastfeeding',
    emoji: '🤱',
    title: 'Exclusive Breastfeeding',
    fields: [
      { label: 'FREQUENCY:', value: '8–12 feeds per day' },
      { label: 'DURATION:', value: '10–45 minutes per feed\nUntil baby seems satisfied' },
      { label: 'SIGNS OF HUNGER:', items: ['Rooting (turning head)', 'Sucking on hands/fists', 'Smacking lips', 'Crying (late sign)'] },
      { label: 'SIGNS OF FULLNESS:', items: ['Turns head away', 'Closes mouth', 'Relaxed hands', 'Falls asleep'] },
    ],
    tip: 'Feed on demand, not by schedule',
    usefulCount: 18,
  },
  {
    id: 'mf2',
    tab: 'breastfeeding',
    emoji: '🌸',
    title: 'Breastfeeding Positions',
    fields: [
      { label: 'CRADLE HOLD:', value: 'Classic position, baby across your front' },
      { label: 'FOOTBALL HOLD:', value: 'Baby tucked under arm, great after C-section' },
      { label: 'SIDE-LYING:', value: 'Both mother and baby lie on their sides — great for night feeds' },
      { label: 'TIPS:', items: ['Ensure deep latch', "Baby's belly should face your belly", 'Support the breast if needed'] },
    ],
    tip: 'A good latch prevents soreness and ensures effective feeding',
    usefulCount: 14,
  },
  {
    id: 'mf3',
    tab: 'formula',
    emoji: '🍼',
    title: 'Formula Feeding Basics',
    fields: [
      { label: 'FREQUENCY:', value: 'Every 3–4 hours (8 feeds/day for newborns)' },
      { label: 'AMOUNT PER FEED:', value: 'Newborn: 60–90 ml\n1 month: 90–120 ml\n6 months: 180–240 ml' },
      { label: 'PREPARATION:', items: ['Use cooled boiled water (if under 2 months)', 'Follow formula instructions precisely', 'Discard unused formula after 1 hour'] },
    ],
    tip: 'Never add extra formula powder — it can cause kidney strain',
    usefulCount: 22,
  },
  {
    id: 'mf4',
    tab: 'formula',
    emoji: '🧪',
    title: 'Choosing the Right Formula',
    fields: [
      { label: 'STANDARD FORMULA:', value: 'Cow\'s milk-based — suitable for most babies' },
      { label: 'SENSITIVE FORMULA:', value: 'For gassiness, fussiness, or mild intolerance' },
      { label: 'HYPOALLERGENIC:', value: 'For confirmed cow\'s milk protein allergy (prescribed)' },
      { label: 'SIGNS TO SWITCH:', items: ['Persistent crying after feeding', 'Rash or eczema', 'Blood in stool', 'Vomiting frequently'] },
    ],
    tip: 'Always consult your pediatrician before switching formulas',
    usefulCount: 17,
  },
  {
    id: 'mf5',
    tab: 'combination',
    emoji: '🔄',
    title: 'Combination Feeding',
    fields: [
      { label: 'WHAT IT IS:', value: 'Mixing breastfeeding with formula to supplement' },
      { label: 'WHEN TO CONSIDER:', items: ['Low milk supply', 'Returning to work', 'Supplementing for weight gain', 'Sharing feeding with partner'] },
      { label: 'TIPS:', items: ['Establish breastfeeding first (4–6 weeks)', 'Introduce bottle gradually', 'Use a slow-flow nipple to avoid bottle preference', 'Maintain breast stimulation to keep supply'] },
    ],
    tip: 'Pumping when skipping a breastfeed helps maintain milk supply',
    usefulCount: 13,
  },
]

export const solidFoodCards: SolidFoodCard[] = [
  {
    id: 'sf1',
    tab: 'starter',
    title: 'Starting Solids (4–6 months)',
    emoji: '🥣',
    ageRange: '4–6 months',
    foods: ['Iron-fortified rice cereal', 'Pureed sweet potato', 'Pureed peas', 'Pureed carrots', 'Mashed banana', 'Pureed apple'],
    tip: 'Start with single-ingredient purees. Wait 3–5 days before introducing a new food.',
    usefulCount: 20,
  },
  {
    id: 'sf2',
    tab: 'advancing',
    title: 'Advancing Textures (7–9 months)',
    emoji: '🥦',
    ageRange: '7–9 months',
    foods: ['Mashed avocado', 'Soft cooked vegetables', 'Minced meat', 'Soft fruit chunks', 'Yogurt (plain)', 'Soft tofu'],
    tip: 'Introduce lumpy textures to prepare for table foods.',
    usefulCount: 16,
  },
  {
    id: 'sf3',
    tab: 'finger-foods',
    title: 'Finger Foods (10–12 months)',
    emoji: '🫐',
    ageRange: '10–12 months',
    foods: ['Soft pasta pieces', 'Small cheese cubes', 'Ripe banana chunks', 'Well-cooked carrot sticks', 'Scrambled eggs', 'Small rice cakes'],
    tip: 'Foods should be soft enough to squish between your fingers.',
    usefulCount: 18,
  },
]

export const scheduleCards: ScheduleCard[] = [
  {
    id: 'sc1',
    ageRange: '0–3 Months',
    schedule: [
      { time: '6:00 AM', feeding: 'Breastfeed / Formula', amount: '60–90 ml' },
      { time: '8:30 AM', feeding: 'Breastfeed / Formula', amount: '60–90 ml' },
      { time: '11:00 AM', feeding: 'Breastfeed / Formula', amount: '90 ml' },
      { time: '1:30 PM', feeding: 'Breastfeed / Formula', amount: '90 ml' },
      { time: '4:00 PM', feeding: 'Breastfeed / Formula', amount: '90 ml' },
      { time: '6:30 PM', feeding: 'Breastfeed / Formula', amount: '90 ml' },
      { time: '9:00 PM', feeding: 'Dream Feed', amount: '90–120 ml' },
    ],
    notes: ['Feed on demand — this is just a guide', '8–12 feeds per day is normal', 'Night feeds are expected at this age'],
    usefulCount: 15,
  },
  {
    id: 'sc2',
    ageRange: '6–8 Months',
    schedule: [
      { time: '7:00 AM', feeding: 'Breastfeed / Formula', amount: '180 ml' },
      { time: '8:00 AM', feeding: 'Solids — breakfast', },
      { time: '11:00 AM', feeding: 'Breastfeed / Formula', amount: '180 ml' },
      { time: '12:00 PM', feeding: 'Solids — lunch' },
      { time: '3:00 PM', feeding: 'Breastfeed / Formula', amount: '180 ml' },
      { time: '5:30 PM', feeding: 'Solids — dinner' },
      { time: '7:00 PM', feeding: 'Breastfeed / Formula (bedtime)', amount: '210 ml' },
    ],
    notes: ['Milk is still the main nutrition at this age', 'Offer solids after milk, not before', 'Start with 1–2 tablespoons of puree'],
    usefulCount: 21,
  },
]

export const foodsToAvoid: FoodsToAvoid[] = [
  {
    id: 'fa1',
    title: 'Honey',
    emoji: '🍯',
    reason: 'Risk of infant botulism — a rare but serious illness. Honey can contain Clostridium botulinum spores.',
    examples: ['Raw honey', 'Processed honey', 'Foods baked with honey (under 12 months)'],
    usefulCount: 24,
  },
  {
    id: 'fa2',
    title: 'Cow\'s Milk (as main drink)',
    emoji: '🥛',
    reason: 'Cow\'s milk lacks the right balance of iron and other nutrients for babies under 12 months and can cause intestinal bleeding.',
    examples: ['Whole cow\'s milk as a drink', 'Toddler formula made from cow\'s milk as a sole drink'],
    usefulCount: 19,
  },
  {
    id: 'fa3',
    title: 'Choking Hazards',
    emoji: '⚠️',
    reason: 'Hard, small, round, or sticky foods are choking risks for babies and toddlers.',
    examples: ['Whole grapes', 'Nuts', 'Raw carrots', 'Popcorn', 'Hard candy', 'Large chunks of meat'],
    usefulCount: 30,
  },
  {
    id: 'fa4',
    title: 'Added Sugar & Salt',
    emoji: '🧂',
    reason: 'Babies\' kidneys cannot handle much sodium. Added sugar contributes to tooth decay and poor eating habits.',
    examples: ['Processed snacks', 'Salty crackers', 'Sweet cereals', 'Fruit juices'],
    usefulCount: 22,
  },
  {
    id: 'fa5',
    title: 'Unpasteurized Foods',
    emoji: '🧀',
    reason: 'Risk of harmful bacteria such as Listeria, E. coli, and Salmonella.',
    examples: ['Raw milk', 'Soft unpasteurized cheeses', 'Raw or undercooked eggs', 'Raw fish/meat'],
    usefulCount: 17,
  },
]

export const feedingTips: FeedingTip[] = [
  {
    id: 'ft1',
    title: 'Watch for Hunger Cues',
    emoji: '👀',
    description: 'Don\'t wait for crying. Look for early cues: rooting, sucking on hands, opening and closing mouth. Crying is a late hunger sign.',
    usefulCount: 28,
  },
  {
    id: 'ft2',
    title: 'Responsive Feeding',
    emoji: '💞',
    description: 'Let baby lead the pace. Don\'t force the last bit of formula or insist on finishing the breast. Babies self-regulate when respected.',
    usefulCount: 22,
  },
  {
    id: 'ft3',
    title: 'Burping Techniques',
    emoji: '🫧',
    description: 'Burp baby mid-feed and after. Try the shoulder hold, sitting upright with chin support, or face-down across your lap.',
    usefulCount: 19,
  },
  {
    id: 'ft4',
    title: 'Track Wet Diapers',
    emoji: '📊',
    description: 'A well-fed newborn should have 6+ wet diapers a day by day 5. This is the best indicator that baby is getting enough milk.',
    usefulCount: 25,
  },
  {
    id: 'ft5',
    title: 'Growth Spurts = More Feeding',
    emoji: '📈',
    description: 'Expect feeding increases at 2–3 weeks, 6 weeks, 3 months, and 6 months. This is temporary — your supply will adjust.',
    usefulCount: 16,
  },
]
