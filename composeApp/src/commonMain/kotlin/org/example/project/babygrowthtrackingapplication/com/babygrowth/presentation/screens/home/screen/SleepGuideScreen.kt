package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// SleepGuideScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SleepGuideScreen(
    babies         : List<BabyResponse>,
    viewModel      : GuideViewModel,
    language       : String,
    onBack         : () -> Unit
) {
    val dimensions = LocalDimensions.current

    // ── Load data ─────────────────────────────────────────────────────────
    LaunchedEffect(language) {
        if (!viewModel.isLoaded) viewModel.loadGuides(language)
    }

    // ── Baby selection ────────────────────────────────────────────────────
    var selectedBabyIndex by remember { mutableStateOf(0) }
    val babyInfoList = babies.map { b ->
        BabyInfo(
            id        = b.babyId,
            name      = b.fullName,
            gender    = b.gender,
            ageMonths = b.ageInMonths
        )
    }
    val selectedBaby = babyInfoList.getOrNull(selectedBabyIndex)
    val ageMonths    = selectedBaby?.ageMonths ?: 0
    val ageRange     = viewModel.findSleepRangeForAge(ageMonths)

    // ── Category selection ────────────────────────────────────────────────
    var selectedCategory by remember { mutableStateOf(SleepCategory.SLEEP_STRATEGIES) }

    // ── Env tab ───────────────────────────────────────────────────────────
    var envTabIndex by remember { mutableStateOf(0) }  // 0=All,1=Bedtime,2=Naps

    // ── Lullaby language tab ──────────────────────────────────────────────
    var lullabyLangIndex by remember { mutableStateOf(0) } // 0=All,1=Kurdish,2=Arabic,3=English

    // ── Scrollable content ────────────────────────────────────────────────
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SleepGuideTopBar(
                babyName   = selectedBaby?.name ?: "",
                onBack     = onBack
            )
        },
        bottomBar = {
            if (viewModel.currentPlayingLullabyId != null) {
                val lullabyId = viewModel.currentPlayingLullabyId!!
                val lullaby   = ageRange?.lullabies?.firstOrNull { it.id == lullabyId }
                if (lullaby != null) {
                    LullabyPlayerBar(
                        lullabyTitle = lullaby.title,
                        isPlaying    = viewModel.isPlaying,
                        progress     = viewModel.playbackPosition,
                        duration     = lullaby.duration,
                        onPlayPause  = { viewModel.onPlayPause(lullaby.id, lullaby.audioUrl) },
                        onStop       = { viewModel.onStop() },
                        onSeek       = { viewModel.onSeek(it) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            // ── Guide header card ─────────────────────────────────────────
            SleepGuideHeaderCard(babyName = selectedBaby?.name ?: "")

            Column(
                modifier = Modifier.padding(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingMedium
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                // ── Child selector ────────────────────────────────────────
                if (babyInfoList.isNotEmpty()) {
                    Text(
                        text       = stringResource(Res.string.sleep_guide_select_child),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                        letterSpacing = 1.sp
                    )
                    GuideChildSelector(
                        babies        = babyInfoList,
                        selectedIndex = selectedBabyIndex,
                        onSelect      = { selectedBabyIndex = it }
                    )
                }

                // ── Category grid ─────────────────────────────────────────
                Text(
                    text       = stringResource(Res.string.sleep_guide_select_category),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                    letterSpacing = 1.sp
                )

                val sleepCategories = listOf(
                    "💤" to stringResource(Res.string.sleep_guide_cat_strategies),
                    "🌙" to stringResource(Res.string.sleep_guide_cat_needs),
                    "🛏️" to stringResource(Res.string.sleep_guide_cat_environment),
                    "🎵" to stringResource(Res.string.sleep_guide_cat_lullabies)
                )
                GuideCategoryGrid(
                    categories    = sleepCategories,
                    selectedIndex = selectedCategory.ordinal,
                    onSelect      = { selectedCategory = SleepCategory.entries[it] }
                )

                // ── Content section ───────────────────────────────────────
                if (!viewModel.isLoaded) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.spacingXLarge),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = GuidePink) }
                } else if (ageRange == null) {
                    Text(
                        text  = stringResource(Res.string.sleep_guide_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                    )
                } else {
                    when (selectedCategory) {
                        SleepCategory.SLEEP_STRATEGIES -> SleepStrategiesContent(
                            ageRange  = ageRange,
                            viewModel = viewModel
                        )
                        SleepCategory.SLEEP_NEEDS -> SleepNeedsContent(
                            ageRange  = ageRange,
                            viewModel = viewModel
                        )
                        SleepCategory.ENVIRONMENT -> EnvironmentContent(
                            ageRange      = ageRange,
                            viewModel     = viewModel,
                            selectedTab   = envTabIndex,
                            onSelectTab   = { envTabIndex = it }
                        )
                        SleepCategory.LULLABIES -> LullabiesContent(
                            ageRange        = ageRange,
                            viewModel       = viewModel,
                            selectedLangTab = lullabyLangIndex,
                            onSelectLangTab = { lullabyLangIndex = it }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepGuideTopBar(babyName: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(Res.string.sleep_guide_title), fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor       = MaterialTheme.colorScheme.surface,
            titleContentColor    = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Header Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SleepGuideHeaderCard(babyName: String) {
    val dimensions = LocalDimensions.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GuidePurple, GuidePurpleLight)))
            .padding(dimensions.spacingMedium + dimensions.spacingSmall),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = if (babyName.isNotBlank()) stringResource(Res.string.sleep_guide_header, babyName) else stringResource(Res.string.sleep_guide_title) + " 💤",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sleep Strategies Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SleepStrategiesContent(
    ageRange : SleepAgeRange,
    viewModel: GuideViewModel
) {
    val dimensions = LocalDimensions.current
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        ageRange.sleepStrategies.forEach { strategy ->
            val vote = viewModel.getVote(strategy.id)
            GuideStrategyCard(
                sectionLabel = stringResource(Res.string.sleep_guide_cat_strategies).uppercase(),
                title        = strategy.title,
                description  = strategy.description,
                tip          = strategy.tip,
                vote         = vote,
                onUseful     = { viewModel.voteSleepItem(strategy.id, VoteType.USEFUL) },
                onUseless    = { viewModel.voteSleepItem(strategy.id, VoteType.USELESS) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sleep Needs Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SleepNeedsContent(
    ageRange : SleepAgeRange,
    viewModel: GuideViewModel
) {
    val dimensions = LocalDimensions.current
    val need       = ageRange.sleepNeed
    val vote       = viewModel.getVote(need.id)

    GuideSectionCard(title = "How Much Sleep Does ${ageRange.label}?") {
        // Age note inside card
        Text(
            text  = "Age: ${ageRange.label}",
            style = MaterialTheme.typography.labelSmall,
            color = GuidePink,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(dimensions.spacingSmall))
        // Sleep duration
        NeedInfoRow(stringResource(Res.string.sleep_guide_total_sleep), need.totalSleep)
        NeedInfoRow("  ${stringResource(Res.string.sleep_guide_night)}", need.nightSleep)
        NeedInfoRow("  ${stringResource(Res.string.sleep_guide_daytime)}", need.daytimeSleep)

        if (need.napSchedule.isNotEmpty()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            HorizontalDivider(color = Color.White.copy(0.2f))
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text       = stringResource(Res.string.sleep_guide_total_nap),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
            need.napSchedule.forEach { nap ->
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "${nap.name}: ${nap.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.85f),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text  = "Duration: ${nap.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f),
                    modifier = Modifier.padding(start = dimensions.spacingSmall)
                )
            }
        }

        if (need.tips.isNotEmpty()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            HorizontalDivider(color = Color.White.copy(0.2f))
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text       = stringResource(Res.string.sleep_guide_tips_for_age),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                letterSpacing = 0.5.sp
            )
            need.tips.forEach { tip ->
                Row(
                    modifier  = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("•", color = GuidePink, fontWeight = FontWeight.Bold)
                    Text(tip, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.85f))
                }
            }
        }

        if (need.tip.isNotBlank()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GuidePink.copy(0.15f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("💡", fontSize = 14.sp)
                Text(need.tip, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.9f))
            }
        }

        Spacer(Modifier.height(dimensions.spacingSmall))
        HorizontalDivider(color = Color.White.copy(0.2f))
        Spacer(Modifier.height(dimensions.spacingSmall))
        GuideFeedbackRow(
            vote      = vote,
            onUseful  = { viewModel.voteSleepItem(need.id, VoteType.USEFUL) },
            onUseless = { viewModel.voteSleepItem(need.id, VoteType.USELESS) }
        )
    }
}

@Composable
private fun NeedInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold, color = Color.White)
        Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.85f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Environment Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EnvironmentContent(
    ageRange   : SleepAgeRange,
    viewModel  : GuideViewModel,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val dimensions = LocalDimensions.current
    val envTabs    = listOf(
        stringResource(Res.string.sleep_guide_env_tab_all),
        stringResource(Res.string.sleep_guide_env_tab_bedtime),
        stringResource(Res.string.sleep_guide_env_tab_naps)
    )

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        GuideTabRow(tabs = envTabs, selectedIndex = selectedTab, onSelect = onSelectTab)

        val envType = when (selectedTab) {
            1    -> "bedtime"
            2    -> "nap"
            else -> null
        }
        val filtered = if (envType == null) ageRange.environments
        else ageRange.environments.filter { it.type == envType }

        filtered.forEach { item ->
            val vote = viewModel.getVote(item.id)
            GuideEnvironmentCard(
                item      = item,
                vote      = vote,
                onUseful  = { viewModel.voteSleepItem(item.id, VoteType.USEFUL) },
                onUseless = { viewModel.voteSleepItem(item.id, VoteType.USELESS) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lullabies Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LullabiesContent(
    ageRange       : SleepAgeRange,
    viewModel      : GuideViewModel,
    selectedLangTab: Int,
    onSelectLangTab: (Int) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val langTabs     = listOf(
        stringResource(Res.string.sleep_guide_lang_all),
        stringResource(Res.string.sleep_guide_lang_kurdish),
        stringResource(Res.string.sleep_guide_lang_arabic),
        stringResource(Res.string.sleep_guide_lang_english)
    )

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        Text(
            text       = stringResource(Res.string.sleep_guide_lullabies_title),
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )

        GuideTabRow(tabs = langTabs, selectedIndex = selectedLangTab, onSelect = onSelectLangTab)

        val selectedLang = when (selectedLangTab) {
            1 -> "Kurdish"   // compare against original English values from JSON data
            2 -> "Arabic"
            3 -> "English"
            else -> null
        }
        val filtered = if (selectedLang == null) ageRange.lullabies
        else ageRange.lullabies.filter { it.language.equals(selectedLang, ignoreCase = true) }

        filtered.forEach { lullaby ->
            val vote = viewModel.getVote(lullaby.id)
            LullabyCard(
                lullaby    = lullaby,
                isPlaying  = viewModel.currentPlayingLullabyId == lullaby.id && viewModel.isPlaying,
                vote       = vote,
                onPlayPause = { viewModel.onPlayPause(lullaby.id, lullaby.audioUrl) },
                onDownload  = { /* trigger platform download */ },
                onUseful    = { viewModel.voteSleepItem(lullaby.id, VoteType.USEFUL) },
                onUseless   = { viewModel.voteSleepItem(lullaby.id, VoteType.USELESS) }
            )
        }
    }
}
