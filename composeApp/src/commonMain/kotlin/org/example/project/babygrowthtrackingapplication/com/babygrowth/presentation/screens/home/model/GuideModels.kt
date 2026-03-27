package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

// ── Vote / Feedback ──────────────────────────────────────────────────────────

data class GuideVote(
    val itemId      : String,
    val usefulCount : Int      = 0,
    val uselessCount: Int      = 0,
    val userVote    : VoteType = VoteType.NONE
)

enum class VoteType { NONE, USEFUL, USELESS }

// ── Sleep Guide models ────────────────────────────────────────────────────────

data class SleepStrategy(
    val id         : String,
    val title      : String,
    val description: String,
    val tip        : String
)

data class NapEntry(
    val name    : String,
    val time    : String,
    val duration: String
)

data class SleepNeed(
    val id          : String,
    val totalSleep  : String,
    val nightSleep  : String,
    val daytimeSleep: String,
    val napSchedule : List<NapEntry>,
    val tips        : List<String>,
    val tip         : String
)

data class EnvironmentItem(
    val id   : String,
    val type : String,   // "bedtime" | "nap"
    val title: String,
    val icon : String,
    val value: String,
    val why  : String,
    val tips : List<String>,
    val tip  : String
)

data class Lullaby(
    val id         : String,
    val title      : String,
    val language   : String,   // "Kurdish" | "Arabic" | "English"
    val duration   : String,
    val audioUrl   : String,
    val description: String
)

data class SleepAgeRange(
    val id              : String,
    val label           : String,
    val minMonths       : Int,
    val maxMonths       : Int,
    val sleepStrategies : List<SleepStrategy>,
    val sleepNeed       : SleepNeed,
    val environments    : List<EnvironmentItem>,
    val lullabies       : List<Lullaby>
)

// ── Feeding Guide models ──────────────────────────────────────────────────────

data class MilkFeeding(
    val id           : String,
    val subType      : String,
    val title        : String,
    val frequency    : String,
    val duration     : String,
    val hungerSigns  : List<String>,
    val fullnessSigns: List<String>,
    val tip          : String
)

data class SolidFood(
    val id         : String,
    val title      : String,
    val description: String,
    val foods      : List<String>,
    val tip        : String
)

data class ScheduleEntry(
    val id      : String,
    val subType : String,
    val title   : String,
    val entries : List<String>,
    val tip     : String
)

data class FoodToAvoid(
    val id    : String,
    val title : String,
    val reason: String,
    val icon  : String
)

data class FeedingTip(
    val id         : String,
    val title      : String,
    val description: String,
    val tip        : String,
    val icon       : String
)

data class FeedingAgeRange(
    val id            : String,
    val label         : String,
    val minMonths     : Int,
    val maxMonths     : Int,
    val milkFeeding   : List<MilkFeeding>,
    val solidFoods    : List<SolidFood>,
    val sampleSchedule: List<ScheduleEntry>,
    val foodsToAvoid  : List<FoodToAvoid>,
    val feedingTips   : List<FeedingTip>
)

// ── Guide UI enums ────────────────────────────────────────────────────────────

enum class SleepCategory   { SLEEP_STRATEGIES, SLEEP_NEEDS, ENVIRONMENT, LULLABIES }
enum class FeedingCategory { MILK_FEEDING, SOLID_FOODS, SAMPLE_SCHEDULE, FOODS_TO_AVOID, FEEDING_TIPS }
