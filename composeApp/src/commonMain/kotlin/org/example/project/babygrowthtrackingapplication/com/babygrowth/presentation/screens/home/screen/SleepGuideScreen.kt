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
// SleepGuideScreen.kt
// ═══════════════════════════════════════════════════════════════════════════

private const val CAT_STRATEGIES  = "sleep_strategies"
private const val CAT_NEEDS       = "sleep_needs"
private const val CAT_ENVIRONMENT = "environment"
private const val CAT_LULLABIES   = "lullabies"

@Composable
fun SleepGuideScreen(
    babies   : List<BabyResponse>,
    viewModel: GuideViewModel,
    language : String,
    onBack   : () -> Unit
) {
    val state = viewModel.uiState

    // Load JSON once
    LaunchedEffect(Unit) { viewModel.loadSleepGuide() }

    // Category & tab selection state
    var selectedCategory  by remember { mutableStateOf(CAT_STRATEGIES) }
    var selectedTabId     by remember { mutableStateOf("all") }
    var selectedBabyIndex by remember { mutableStateOf(0) }

    val selectedBaby   = babies.getOrNull(selectedBabyIndex)
    val ageInMonths    = selectedBaby?.ageInMonths ?: 8

    val guide          = state.sleepGuide
    val currentStrategy= guide?.strategyById(selectedCategory)

    // Preload feedback counts when strategy or baby changes
    LaunchedEffect(selectedCategory, selectedBabyIndex) {
        val items = currentStrategy?.itemsForAge(ageInMonths)?.map { it.id } ?: return@LaunchedEffect
        if (items.isNotEmpty()) viewModel.loadFeedbackCounts(items, "SLEEP")
    }

    // Subtitle with child name
    val childName = selectedBaby?.fullName ?: ""
    val subtitle  = if (childName.isNotBlank())
        guide?.subtitle?.get(language)?.replace("{childName}", childName)
            ?: stringResource(Res.string.sleep_guide_title)
    else
        guide?.title?.get(language) ?: stringResource(Res.string.sleep_guide_title)

    // Categories
    val categories = listOf(
        GuideCategoryItem(CAT_STRATEGIES,  "🛏️", stringResource(Res.string.sleep_guide_cat_strategies)),
        GuideCategoryItem(CAT_NEEDS,       "😴", stringResource(Res.string.sleep_guide_cat_needs)),
        GuideCategoryItem(CAT_ENVIRONMENT, "🌙", stringResource(Res.string.sleep_guide_cat_environment)),
        GuideCategoryItem(CAT_LULLABIES,   "🎵", stringResource(Res.string.sleep_guide_cat_lullabies))
    )

    Scaffold(
        topBar         = {
            GuideTopBar(
                title  = guide?.title?.get(language) ?: stringResource(Res.string.sleep_guide_title),
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
                SleepGuidePortrait(
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
                    isLoading         = state.isLoadingSleep,
                    feedbackMap       = state.feedbackMap,
                    playerState       = state.playerState,
                    onUseful          = { id -> viewModel.castVote(id, "SLEEP", UserVote.USEFUL) },
                    onUseless         = { id -> viewModel.castVote(id, "SLEEP", UserVote.USELESS) },
                    onPlay            = { item ->
                        viewModel.playLullaby(item.id, item.media?.duration_seconds ?: 180)
                    },
                    onTogglePause     = { viewModel.togglePlayPause() },
                    onStop            = { viewModel.stopLullaby() },
                    onDownload        = { /* platform download */ }
                )
            },
            landscape = {
                SleepGuideLandscape(
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
                    isLoading         = state.isLoadingSleep,
                    feedbackMap       = state.feedbackMap,
                    playerState       = state.playerState,
                    onUseful          = { id -> viewModel.castVote(id, "SLEEP", UserVote.USEFUL) },
                    onUseless         = { id -> viewModel.castVote(id, "SLEEP", UserVote.USELESS) },
                    onPlay            = { item ->
                        viewModel.playLullaby(item.id, item.media?.duration_seconds ?: 180)
                    },
                    onTogglePause     = { viewModel.togglePlayPause() },
                    onStop            = { viewModel.stopLullaby() },
                    onDownload        = { /* platform download */ }
                )
            }
        )
    }
}

// ── Portrait layout ───────────────────────────────────────────────────────

@Composable
private fun SleepGuidePortrait(
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
    playerState      : LullabyPlayerState,
    onUseful         : (String) -> Unit,
    onUseless        : (String) -> Unit,
    onPlay           : (GuideItem) -> Unit,
    onTogglePause    : () -> Unit,
    onStop           : () -> Unit,
    onDownload       : (GuideItem) -> Unit
) {
    val scrollState = rememberScrollState()
    val dimensions  = LocalDimensions.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header + child selector
        GuideSubtitleHeader(
            subtitle      = subtitle,
            babies        = babies,
            selectedIndex = selectedBabyIndex,
            onSelectBaby  = onSelectBaby
        )

        // Category selector
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
                modifier         = Modifier.fillMaxWidth().padding(dimensions.spacingXLarge),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (currentStrategy == null) {
            GuideEmptyState()
        } else {
            SleepStrategyContent(
                strategy      = currentStrategy,
                ageInMonths   = ageInMonths,
                language      = language,
                selectedTabId = selectedTabId,
                onSelectTab   = onSelectTab,
                feedbackMap   = feedbackMap,
                playerState   = playerState,
                onUseful      = onUseful,
                onUseless     = onUseless,
                onPlay        = onPlay,
                onTogglePause = onTogglePause,
                onStop        = onStop,
                onDownload    = onDownload,
                modifier      = Modifier.padding(bottom = dimensions.spacingLarge)
            )
        }
    }
}

// ── Landscape layout ──────────────────────────────────────────────────────

@Composable
private fun SleepGuideLandscape(
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
    playerState      : LullabyPlayerState,
    onUseful         : (String) -> Unit,
    onUseless        : (String) -> Unit,
    onPlay           : (GuideItem) -> Unit,
    onTogglePause    : () -> Unit,
    onStop           : () -> Unit,
    onDownload       : (GuideItem) -> Unit
) {
    val dimensions = LocalDimensions.current

    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane: header + categories
        Column(
            modifier = Modifier
                .width(280.dp)
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

        // Right pane: content
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
                GuideEmptyState()
            } else {
                SleepStrategyContent(
                    strategy      = currentStrategy,
                    ageInMonths   = ageInMonths,
                    language      = language,
                    selectedTabId = selectedTabId,
                    onSelectTab   = onSelectTab,
                    feedbackMap   = feedbackMap,
                    playerState   = playerState,
                    onUseful      = onUseful,
                    onUseless     = onUseless,
                    onPlay        = onPlay,
                    onTogglePause = onTogglePause,
                    onStop        = onStop,
                    onDownload    = onDownload
                )
            }
        }
    }
}

// ── Strategy content dispatcher ───────────────────────────────────────────

@Composable
private fun SleepStrategyContent(
    strategy     : GuideStrategy,
    ageInMonths  : Int,
    language     : String,
    selectedTabId: String,
    onSelectTab  : (String) -> Unit,
    feedbackMap  : Map<String, CardFeedbackState>,
    playerState  : LullabyPlayerState,
    onUseful     : (String) -> Unit,
    onUseless    : (String) -> Unit,
    onPlay       : (GuideItem) -> Unit,
    onTogglePause: () -> Unit,
    onStop       : () -> Unit,
    onDownload   : (GuideItem) -> Unit,
    modifier     : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = modifier) {
        when (strategy.id) {

            // ── Sleep Strategies: no tabs, plain cards ─────────────────────
            CAT_STRATEGIES -> {
                val items = strategy.itemsForAge(ageInMonths)
                SleepStrategySection(label = strategy.title.get(language))
                if (items.isEmpty()) {
                    GuideNoDataForAge()
                } else {
                    Column(
                        modifier            = Modifier.padding(dimensions.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {
                        items.forEach { item ->
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
            }

            // ── Sleep Needs: no tabs, special layout showing totals ─────────
            CAT_NEEDS -> {
                val items = strategy.itemsForAge(ageInMonths)
                SleepStrategySection(label = strategy.title.get(language))
                if (items.isEmpty()) {
                    GuideNoDataForAge()
                } else {
                    Column(
                        modifier            = Modifier.padding(dimensions.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {
                        items.forEach { item ->
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
            }

            // ── Environment: All / Bedtime / Naps tabs ─────────────────────
            CAT_ENVIRONMENT -> {
                val tabs = buildList {
                    add(GuideTab("all", buildLocalizedAll()))
                    addAll(strategy.tabs ?: emptyList())
                }
                GuidePillTabs(
                    tabs       = tabs,
                    selectedId = selectedTabId,
                    langCode   = language,
                    onSelectTab = onSelectTab
                )
                val items = strategy.itemsForAgeAndTab(ageInMonths, selectedTabId)
                if (items.isEmpty()) {
                    GuideNoDataForAge()
                } else {
                    Column(
                        modifier            = Modifier.padding(dimensions.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {
                        items.forEach { item ->
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
            }

            // ── Lullabies: All / Kurdish / Arabic / English tabs ───────────
            CAT_LULLABIES -> {
                val tabs = buildList {
                    add(GuideTab("all", buildLocalizedAll()))
                    addAll(strategy.tabs ?: emptyList())
                }
                GuidePillTabs(
                    tabs        = tabs,
                    selectedId  = selectedTabId,
                    langCode    = language,
                    onSelectTab = onSelectTab
                )
                val items = strategy.itemsForAgeAndTab(ageInMonths, selectedTabId)
                LullabiesContent(
                    items         = items,
                    langCode      = language,
                    playerState   = playerState,
                    feedbackMap   = feedbackMap,
                    guideType     = "SLEEP",
                    onPlay        = onPlay,
                    onTogglePause = onTogglePause,
                    onStop        = onStop,
                    onDownload    = onDownload,
                    onUseful      = onUseful,
                    onUseless     = onUseless,
                    modifier      = Modifier.padding(bottom = dimensions.spacingLarge)
                )
            }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────

@Composable
private fun SleepStrategySection(label: String) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(customColors.accentGradientStart.copy(alpha = 0.08f))
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall)
    ) {
        Text(
            text       = label.uppercase(),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = customColors.accentGradientStart,
            letterSpacing = 1.sp
        )
    }
}

// ── Empty states ──────────────────────────────────────────────────────────

@Composable
private fun GuideEmptyState() {
    val dimensions = LocalDimensions.current
    Box(
        modifier         = Modifier.fillMaxWidth().padding(dimensions.spacingXLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📖", fontSize = 48.sp)
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                text      = stringResource(Res.string.sleep_guide_no_data),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun GuideNoDataForAge() {
    val dimensions = LocalDimensions.current
    Box(
        modifier         = Modifier.fillMaxWidth().padding(dimensions.spacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = stringResource(Res.string.sleep_guide_no_data),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ── Helper: build "All" tab in all languages ──────────────────────────────

private fun buildLocalizedAll() = org.example.project.babygrowthtrackingapplication
    .com.babygrowth.presentation.screens.home.model.LocalizedString(
        en        = "All",
        ku_sorani = "هەموو",
        ku_badini = "Hemû",
        ar        = "الكل"
    )