package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import babygrowthtrackingapplication.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
class GuideViewModel {

    // ── Public State ─────────────────────────────────────────────────────────

    var sleepAgeRanges     by mutableStateOf<List<SleepAgeRange>>(emptyList())
    var feedingAgeRanges   by mutableStateOf<List<FeedingAgeRange>>(emptyList())
    var votes              by mutableStateOf<Map<String, GuideVote>>(emptyMap())
    var currentPlayingLullabyId by mutableStateOf<String?>(null)
    var isPlaying          by mutableStateOf(false)
    var playbackPosition   by mutableStateOf(0f)
    var isLoaded           by mutableStateOf(false)
    var loadError          by mutableStateOf<String?>(null)

    private val scope      = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val repository = GuideRepository()

    // ── Loading ──────────────────────────────────────────────────────────────

    fun loadGuides(language: String) {
        scope.launch {
            try {
                val lang = language.lowercase()
                val sleepBytes   = Res.readBytes("files/sleep_guide_content.json")
                val feedingBytes = Res.readBytes("files/feeding_guide_content.json")

                val sleepJson   = sleepBytes.decodeToString()
                val feedingJson = feedingBytes.decodeToString()

                val parsedSleep   = parseSleepGuide(sleepJson, lang)
                val parsedFeeding = parseFeedingGuide(feedingJson, lang)

                withContext(Dispatchers.Main) {
                    sleepAgeRanges   = parsedSleep
                    feedingAgeRanges = parsedFeeding
                    isLoaded = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadError = e.message
                    isLoaded  = true
                }
            }
        }
    }

    // ── Age Range Lookup ─────────────────────────────────────────────────────

    fun findSleepRangeForAge(ageMonths: Int): SleepAgeRange? =
        sleepAgeRanges.firstOrNull { ageMonths in it.minMonths..it.maxMonths }
            ?: sleepAgeRanges.lastOrNull()

    fun findFeedingRangeForAge(ageMonths: Int): FeedingAgeRange? =
        feedingAgeRanges.firstOrNull { ageMonths in it.minMonths..it.maxMonths }
            ?: feedingAgeRanges.lastOrNull()

    // ── Voting ───────────────────────────────────────────────────────────────

    fun vote(itemId: String, type: VoteType) {
        repository.vote(itemId, type)
        votes = repository.getAllVotes()
    }

    fun voteSleepItem(itemId: String, type: VoteType)   = vote(itemId, type)
    fun voteFeedingItem(itemId: String, type: VoteType) = vote(itemId, type)

    fun getVote(itemId: String): GuideVote = repository.getVote(itemId)

    // ── Playback ─────────────────────────────────────────────────────────────

    fun onPlayPause(lullabyId: String, audioUrl: String) {
        if (currentPlayingLullabyId == lullabyId) {
            isPlaying = !isPlaying
        } else {
            currentPlayingLullabyId = lullabyId
            playbackPosition        = 0f
            isPlaying               = true
        }
    }

    fun onStop() {
        isPlaying               = false
        currentPlayingLullabyId = null
        playbackPosition        = 0f
    }

    fun onSeek(position: Float) {
        playbackPosition = position.coerceIn(0f, 1f)
    }

    fun onProgressUpdate(progress: Float) {
        playbackPosition = progress.coerceIn(0f, 1f)
    }

    fun onPlaybackCompleted() {
        isPlaying        = false
        playbackPosition = 0f
    }

    fun onDestroy() {
        scope.cancel()
        repository.clearAll()
    }

    // ── Private JSON Parsers ─────────────────────────────────────────────────

    private fun JsonObject.str(key: String, lang: String, fallback: String = ""): String =
        this["${key}_$lang"]?.jsonPrimitive?.contentOrNull
            ?: this["${key}_en"]?.jsonPrimitive?.contentOrNull
            ?: fallback

    private fun JsonObject.strList(key: String, lang: String): List<String> {
        val arr = this["${key}_$lang"]?.jsonArray
            ?: this["${key}_en"]?.jsonArray
            ?: return emptyList()
        return arr.mapNotNull { it.jsonPrimitive.contentOrNull }
    }

    private fun parseSleepGuide(json: String, lang: String): List<SleepAgeRange> {
        val root       = Json.parseToJsonElement(json).jsonObject
        val ageRanges  = root["age_ranges"]?.jsonArray ?: return emptyList()

        return ageRanges.map { rangeEl ->
            val range = rangeEl.jsonObject

            val strategies = range["sleep_strategies"]?.jsonArray?.map { el ->
                val s = el.jsonObject
                SleepStrategy(
                    id          = s["id"]?.jsonPrimitive?.content ?: "",
                    title       = s.str("title", lang),
                    description = s.str("description", lang),
                    tip         = s.str("tip", lang)
                )
            } ?: emptyList()

            val needObj  = range["sleep_needs"]?.jsonObject ?: JsonObject(emptyMap())
            val napArr   = needObj["nap_schedule"]?.jsonArray ?: JsonArray(emptyList())
            val naps     = napArr.map { n ->
                val no = n.jsonObject
                NapEntry(
                    name     = no.str("name", lang),
                    time     = no.str("time", lang),
                    duration = no.str("duration", lang)
                )
            }
            val sleepNeed = SleepNeed(
                id           = needObj["id"]?.jsonPrimitive?.content ?: "",
                totalSleep   = needObj.str("total_sleep", lang),
                nightSleep   = needObj.str("night_sleep", lang),
                daytimeSleep = needObj.str("daytime_sleep", lang),
                napSchedule  = naps,
                tips         = needObj.strList("tips", lang),
                tip          = needObj.str("tip", lang)
            )

            val envItems = range["environments"]?.jsonArray?.map { el ->
                val e = el.jsonObject
                EnvironmentItem(
                    id    = e["id"]?.jsonPrimitive?.content ?: "",
                    type  = e["type"]?.jsonPrimitive?.content ?: "bedtime",
                    title = e.str("title", lang),
                    icon  = e["icon"]?.jsonPrimitive?.content ?: "",
                    value = e.str("value", lang),
                    why   = e.str("why", lang),
                    tips  = e.strList("tips", lang),
                    tip   = e.str("tip", lang)
                )
            } ?: emptyList()

            val lullabies = range["lullabies"]?.jsonArray?.map { el ->
                val l = el.jsonObject
                Lullaby(
                    id          = l["id"]?.jsonPrimitive?.content ?: "",
                    title       = l.str("title", lang),
                    language    = l["language"]?.jsonPrimitive?.content ?: "English",
                    duration    = l["duration"]?.jsonPrimitive?.content ?: "",
                    audioUrl    = l["audio_url"]?.jsonPrimitive?.content ?: "",
                    description = l.str("description", lang)
                )
            } ?: emptyList()

            SleepAgeRange(
                id              = range["id"]?.jsonPrimitive?.content ?: "",
                label           = range.str("label", lang),
                minMonths       = range["min_months"]?.jsonPrimitive?.intOrNull ?: 0,
                maxMonths       = range["max_months"]?.jsonPrimitive?.intOrNull ?: 99,
                sleepStrategies = strategies,
                sleepNeed       = sleepNeed,
                environments    = envItems,
                lullabies       = lullabies
            )
        }
    }

    private fun parseFeedingGuide(json: String, lang: String): List<FeedingAgeRange> {
        val root      = Json.parseToJsonElement(json).jsonObject
        val ageRanges = root["age_ranges"]?.jsonArray ?: return emptyList()

        return ageRanges.map { rangeEl ->
            val range = rangeEl.jsonObject

            val milkFeeding = range["milk_feeding"]?.jsonArray?.map { el ->
                val m = el.jsonObject
                MilkFeeding(
                    id            = m["id"]?.jsonPrimitive?.content ?: "",
                    subType       = m["sub_type"]?.jsonPrimitive?.content ?: "",
                    title         = m.str("title", lang),
                    frequency     = m.str("frequency", lang),
                    duration      = m.str("duration", lang),
                    hungerSigns   = m.strList("hunger_signs", lang),
                    fullnessSigns = m.strList("fullness_signs", lang),
                    tip           = m.str("tip", lang)
                )
            } ?: emptyList()

            val solidFoods = range["solid_foods"]?.jsonArray?.map { el ->
                val s = el.jsonObject
                SolidFood(
                    id          = s["id"]?.jsonPrimitive?.content ?: "",
                    title       = s.str("title", lang),
                    description = s.str("description", lang),
                    foods       = s.strList("foods", lang),
                    tip         = s.str("tip", lang)
                )
            } ?: emptyList()

            val sampleSchedule = range["sample_schedule"]?.jsonArray?.map { el ->
                val s = el.jsonObject
                ScheduleEntry(
                    id      = s["id"]?.jsonPrimitive?.content ?: "",
                    subType = s["sub_type"]?.jsonPrimitive?.content ?: "",
                    title   = s.str("title", lang),
                    entries = s.strList("entries", lang),
                    tip     = s.str("tip", lang)
                )
            } ?: emptyList()

            val foodsToAvoid = range["foods_to_avoid"]?.jsonArray?.map { el ->
                val f = el.jsonObject
                FoodToAvoid(
                    id     = f["id"]?.jsonPrimitive?.content ?: "",
                    title  = f.str("title", lang),
                    reason = f.str("reason", lang),
                    icon   = f["icon"]?.jsonPrimitive?.content ?: ""
                )
            } ?: emptyList()

            val feedingTips = range["feeding_tips"]?.jsonArray?.map { el ->
                val t = el.jsonObject
                FeedingTip(
                    id          = t["id"]?.jsonPrimitive?.content ?: "",
                    title       = t.str("title", lang),
                    description = t.str("description", lang),
                    tip         = t.str("tip", lang),
                    icon        = t["icon"]?.jsonPrimitive?.content ?: ""
                )
            } ?: emptyList()

            FeedingAgeRange(
                id             = range["id"]?.jsonPrimitive?.content ?: "",
                label          = range.str("label", lang),
                minMonths      = range["min_months"]?.jsonPrimitive?.intOrNull ?: 0,
                maxMonths      = range["max_months"]?.jsonPrimitive?.intOrNull ?: 99,
                milkFeeding    = milkFeeding,
                solidFoods     = solidFoods,
                sampleSchedule = sampleSchedule,
                foodsToAvoid   = foodsToAvoid,
                feedingTips    = feedingTips
            )
        }
    }
}
