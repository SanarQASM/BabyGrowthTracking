package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.example.project.babygrowthtrackingapplication.ui.components.AdaptiveLayout
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// ═══════════════════════════════════════════════════════════════════════════
// FeedingGuideScreen.kt
// ═══════════════════════════════════════════════════════════════════════════

private const val CAT_MILK     = "milk_feeding"
private const val CAT_SOLID    = "solid_foods"
private const val CAT_SCHEDULE = "sample_schedule"
private const val CAT_AVOID    = "foods_to_avoid"
private const val CAT_TIPS     = "feeding_tips"

@Composable
fun FeedingGuideScreen(
    babies   : List<BabyResponse>,
    viewModel: GuideViewModel,
    language : String,
    onBack   : () -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(Unit) { viewModel.loadFeedingGuide() }

    var selectedCategory  by remember { mutableStateOf(CAT_MILK) }
    var selectedTabId     by remember { mutableStateOf("all") }
    var selectedBabyIndex by remember { mutableStateOf(0) }

    val selectedBaby  = babies.getOrNull(selectedBabyIndex)
    val ageInMonths   = selectedBaby?.ageInMonths ?: 8

    val guide           = state.feedingGuide
    val currentStrategy = guide?.strategyById(selectedCategory)

    // FIX: Key on `currentStrategy` (not just selectedCategory + selectedBabyIndex)
    // so this effect fires once the guide finishes loading asynchronously.
    // loadFeedbackForStrategy loads ALL age-ranges in the strategy, not just
    // the currently visible age, so every card has counts pre-fetched.
    LaunchedEffect(currentStrategy, selectedBabyIndex) {
        val strategy = currentStrategy ?: return@LaunchedEffect
        viewModel.loadFeedbackForStrategy(strategy, "FEEDING")
    }

    val childName = selectedBaby?.fullName ?: ""
    val subtitle  = if (childName.isNotBlank())
        guide?.subtitle?.get(language)?.replace("{childName}", childName)
            ?: stringResource(Res.string.feeding_guide_title)
    else
        guide?.title?.get(language) ?: stringResource(Res.string.feeding_guide_title)

    val categories = listOf(
        GuideCategoryItem(CAT_MILK,     "🥛", stringResource(Res.string.feeding_guide_cat_milk)),
        GuideCategoryItem(CAT_SOLID,    "🥣", stringResource(Res.string.feeding_guide_cat_solid)),
        GuideCategoryItem(CAT_SCHEDULE, "📅", stringResource(Res.string.feeding_guide_cat_schedule)),
        GuideCategoryItem(CAT_AVOID,    "🚫", stringResource(Res.string.feeding_guide_cat_avoid)),
        GuideCategoryItem(CAT_TIPS,     "💡", stringResource(Res.string.feeding_guide_cat_tips))
    )

    Scaffold(
        topBar         = {
            GuideTopBar(
                title  = guide?.title?.get(language) ?: stringResource(Res.string.feeding_guide_title),
                onBack = onBack
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        AdaptiveLayout(
            modifier  = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            portrait  = {
                FeedingGuidePortrait(
                    babies            = babies,
                    selectedBabyIndex = selectedBabyIndex,
                    onSelectBaby      = { selectedBabyIndex = it; selectedTabId = "all" },
                    subtitle          = subtitle,
                    categories        = categories,
                    selectedCategory  = selectedCategory,
                    onSelectCategory  = { selectedCategory = it; selectedTabId = "all" },
                    selectedTabId     = selectedTabId,
                    onSelectTab       = { selectedTabId = it },
                    currentStrategy   = currentStrategy,
                    ageInMonths       = ageInMonths,
                    language          = language,
                    isLoading         = state.isLoadingFeeding,
                    feedbackMap       = state.feedbackMap,
                    onUseful          = { id -> viewModel.castVote(id, "FEEDING", UserVote.USEFUL) },
                    onUseless         = { id -> viewModel.castVote(id, "FEEDING", UserVote.USELESS) }
                )
            },
            landscape = {
                FeedingGuideLandscape(
                    babies            = babies,
                    selectedBabyIndex = selectedBabyIndex,
                    onSelectBaby      = { selectedBabyIndex = it; selectedTabId = "all" },
                    subtitle          = subtitle,
                    categories        = categories,
                    selectedCategory  = selectedCategory,
                    onSelectCategory  = { selectedCategory = it; selectedTabId = "all" },
                    selectedTabId     = selectedTabId,
                    onSelectTab       = { selectedTabId = it },
                    currentStrategy   = currentStrategy,
                    ageInMonths       = ageInMonths,
                    language          = language,
                    isLoading         = state.isLoadingFeeding,
                    feedbackMap       = state.feedbackMap,
                    onUseful          = { id -> viewModel.castVote(id, "FEEDING", UserVote.USEFUL) },
                    onUseless         = { id -> viewModel.castVote(id, "FEEDING", UserVote.USELESS) }
                )
            }
        )
    }
}

// ── Portrait layout ───────────────────────────────────────────────────────

@Composable
private fun FeedingGuidePortrait(
    babies           : List<BabyResponse>,
    selectedBabyIndex: Int,
    onSelectBaby     : (Int) -> Unit,
    subtitle         : String,
    categories       : List<GuideCategoryItem>,
    selectedCategory : String,
    onSelectCategory : (String) -> Unit,
    selectedTabId    : String,
    onSelectTab      : (String) -> Unit,
    currentStrategy  : GuideStrategy?,
    ageInMonths      : Int,
    language         : String,
    isLoading        : Boolean,
    feedbackMap      : Map<String, CardFeedbackState>,
    onUseful         : (String) -> Unit,
    onUseless        : (String) -> Unit
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        GuideSubtitleHeader(
            subtitle      = subtitle,
            babies        = babies,
            selectedIndex = selectedBabyIndex,
            onSelectBaby  = onSelectBaby
        )

        GuideCategorySelector(
            categories       = categories,
            selectedId       = selectedCategory,
            onSelectCategory = onSelectCategory,
            modifier         = Modifier.padding(top = dimensions.spacingSmall)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = dimensions.spacingSmall),
            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        if (isLoading) {
            Box(
                Modifier.fillMaxWidth().padding(dimensions.spacingXLarge),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (currentStrategy == null) {
            FeedingEmptyState()
        } else {
            FeedingStrategyContent(
                strategy      = currentStrategy,
                ageInMonths   = ageInMonths,
                language      = language,
                selectedTabId = selectedTabId,
                onSelectTab   = onSelectTab,
                feedbackMap   = feedbackMap,
                onUseful      = onUseful,
                onUseless     = onUseless,
                modifier      = Modifier.padding(bottom = dimensions.spacingLarge)
            )
        }
    }
}

// ── Landscape layout ──────────────────────────────────────────────────────

@Composable
private fun FeedingGuideLandscape(
    babies           : List<BabyResponse>,
    selectedBabyIndex: Int,
    onSelectBaby     : (Int) -> Unit,
    subtitle         : String,
    categories       : List<GuideCategoryItem>,
    selectedCategory : String,
    onSelectCategory : (String) -> Unit,
    selectedTabId    : String,
    onSelectTab      : (String) -> Unit,
    currentStrategy  : GuideStrategy?,
    ageInMonths      : Int,
    language         : String,
    isLoading        : Boolean,
    feedbackMap      : Map<String, CardFeedbackState>,
    onUseful         : (String) -> Unit,
    onUseless        : (String) -> Unit
) {
    val dimensions = LocalDimensions.current

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            GuideSubtitleHeader(
                subtitle      = subtitle,
                babies        = babies,
                selectedIndex = selectedBabyIndex,
                onSelectBaby  = onSelectBaby
            )
            GuideCategorySelector(
                categories       = categories,
                selectedId       = selectedCategory,
                onSelectCategory = onSelectCategory
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(bottom = dimensions.spacingLarge)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(dimensions.spacingXLarge),
                    contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (currentStrategy == null) {
                FeedingEmptyState()
            } else {
                FeedingStrategyContent(
                    strategy      = currentStrategy,
                    ageInMonths   = ageInMonths,
                    language      = language,
                    selectedTabId = selectedTabId,
                    onSelectTab   = onSelectTab,
                    feedbackMap   = feedbackMap,
                    onUseful      = onUseful,
                    onUseless     = onUseless
                )
            }
        }
    }
}

// ── Strategy content dispatcher ───────────────────────────────────────────

@Composable
private fun FeedingStrategyContent(
    strategy     : GuideStrategy,
    ageInMonths  : Int,
    language     : String,
    selectedTabId: String,
    onSelectTab  : (String) -> Unit,
    feedbackMap  : Map<String, CardFeedbackState>,
    onUseful     : (String) -> Unit,
    onUseless    : (String) -> Unit,
    modifier     : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = modifier) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(customColors.accentGradientStart.copy(alpha = 0.08f))
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall)
        ) {
            Text(
                text          = strategy.title.get(language).uppercase(),
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.Bold,
                color         = customColors.accentGradientStart,
                letterSpacing = 1.sp
            )
        }

        when (strategy.id) {

            CAT_MILK -> {
                val rawTabs = strategy.tabs ?: emptyList()
                val tabs = if (rawTabs.any { it.id == "all" }) {
                    rawTabs
                } else {
                    buildList {
                        add(GuideTab("all", buildLocalizedAll()))
                        addAll(rawTabs)
                    }
                }
                GuidePillTabs(
                    tabs        = tabs,
                    selectedId  = selectedTabId,
                    langCode    = language,
                    onSelectTab = onSelectTab
                )
                val items = strategy.itemsForAgeAndTab(ageInMonths, selectedTabId)
                FeedingCardsColumn(items, language, feedbackMap, onUseful, onUseless)
            }

            else -> {
                val items = strategy.itemsForAge(ageInMonths)
                if (items.isEmpty()) {
                    FeedingNoDataForAge(strategy.id)
                } else {
                    FeedingCardsColumn(items, language, feedbackMap, onUseful, onUseless)
                }
            }
        }
    }
}

@Composable
private fun FeedingCardsColumn(
    items      : List<GuideItem>,
    language   : String,
    feedbackMap: Map<String, CardFeedbackState>,
    onUseful   : (String) -> Unit,
    onUseless  : (String) -> Unit
) {
    val dimensions = LocalDimensions.current
    if (items.isEmpty()) {
        FeedingEmptyState()
        return
    }
    Column(
        modifier            = Modifier.padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        items.forEach { item ->
            // FIX: always provide a fallback CardFeedbackState so the card's
            // buttons are never in a "no state" limbo.
            GuideContentCard(
                item          = item,
                langCode      = language,
                feedbackState = feedbackMap[item.id] ?: CardFeedbackState(item.id),
                onUseful      = { onUseful(item.id) },
                onUseless     = { onUseless(item.id) }
            )
        }
    }
}

// ── Empty / no-data states ────────────────────────────────────────────────

@Composable
private fun FeedingEmptyState() {
    val dimensions = LocalDimensions.current
    Box(
        modifier         = Modifier.fillMaxWidth().padding(dimensions.spacingXLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍼", fontSize = 48.sp)
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                text  = stringResource(Res.string.feeding_guide_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun FeedingNoDataForAge(strategyId: String) {
    val dimensions = LocalDimensions.current
    val msg = when (strategyId) {
        CAT_SOLID -> stringResource(Res.string.feeding_guide_no_solid)
        else      -> stringResource(Res.string.feeding_guide_no_data)
    }
    Box(
        modifier         = Modifier.fillMaxWidth().padding(dimensions.spacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = msg,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ── Helper: build localised "All" tab ────────────────────────────────────
private fun buildLocalizedAll() =
    LocalizedString(
        en        = "All",
        ku_sorani = "هەموو",
        ku_badini = "هەمی",
        ar        = "الكل"
    )