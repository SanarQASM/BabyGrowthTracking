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
// FeedingGuideScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FeedingGuideScreen(
    babies   : List<BabyResponse>,
    viewModel: GuideViewModel,
    language : String,
    onBack   : () -> Unit
) {
    val dimensions = LocalDimensions.current

    LaunchedEffect(language) {
        if (!viewModel.isLoaded) viewModel.loadGuides(language)
    }

    var selectedBabyIndex by remember { mutableStateOf(0) }
    val babyInfoList = babies.map { b ->
        BabyInfo(id = b.babyId, name = b.fullName, gender = b.gender, ageMonths = b.ageInMonths)
    }
    val selectedBaby = babyInfoList.getOrNull(selectedBabyIndex)
    val ageMonths    = selectedBaby?.ageMonths ?: 0
    val ageRange     = viewModel.findFeedingRangeForAge(ageMonths)

    var selectedCategory by remember { mutableStateOf(FeedingCategory.MILK_FEEDING) }
    // Sub-tabs for milk feeding: 0=All, 1=Breastfeeding, 2=Formula, 3=Combination
    var milkTabIndex by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            FeedingGuideTopBar(onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            FeedingGuideHeaderCard(babyName = selectedBaby?.name ?: "")

            Column(
                modifier = Modifier.padding(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingMedium
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                // Child selector
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

                // Category grid (5 items: 2 rows - 2+2+1)
                Text(
                    text       = stringResource(Res.string.sleep_guide_select_category),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                    letterSpacing = 1.sp
                )
                val feedingCategories = listOf(
                    "🥛" to stringResource(Res.string.feeding_guide_cat_milk),
                    "🥣" to stringResource(Res.string.feeding_guide_cat_solid),
                    "📅" to stringResource(Res.string.feeding_guide_cat_schedule),
                    "🚫" to stringResource(Res.string.feeding_guide_cat_avoid),
                    "💡" to stringResource(Res.string.feeding_guide_cat_tips)
                )
                GuideCategoryGrid(
                    categories    = feedingCategories,
                    selectedIndex = selectedCategory.ordinal,
                    onSelect      = { selectedCategory = FeedingCategory.entries[it] }
                )

                // Content
                if (!viewModel.isLoaded) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.spacingXLarge),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = GuidePink) }
                } else if (ageRange == null) {
                    Text(
                        text  = stringResource(Res.string.feeding_guide_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                    )
                } else {
                    when (selectedCategory) {
                        FeedingCategory.MILK_FEEDING -> MilkFeedingContent(
                            ageRange      = ageRange,
                            viewModel     = viewModel,
                            selectedTab   = milkTabIndex,
                            onSelectTab   = { milkTabIndex = it }
                        )
                        FeedingCategory.SOLID_FOODS -> SolidFoodsContent(
                            ageRange  = ageRange,
                            viewModel = viewModel
                        )
                        FeedingCategory.SAMPLE_SCHEDULE -> SampleScheduleContent(
                            ageRange  = ageRange,
                            viewModel = viewModel
                        )
                        FeedingCategory.FOODS_TO_AVOID -> FoodsToAvoidContent(
                            ageRange  = ageRange,
                            viewModel = viewModel
                        )
                        FeedingCategory.FEEDING_TIPS -> FeedingTipsContent(
                            ageRange  = ageRange,
                            viewModel = viewModel
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
private fun FeedingGuideTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(Res.string.feeding_guide_title), fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Header Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeedingGuideHeaderCard(babyName: String) {
    val dimensions = LocalDimensions.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GuidePurple, GuidePurpleLight)))
            .padding(dimensions.spacingMedium + dimensions.spacingSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = if (babyName.isNotBlank()) stringResource(Res.string.feeding_guide_header, babyName) else stringResource(Res.string.feeding_guide_title) + " 🍼",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Milk Feeding Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MilkFeedingContent(
    ageRange   : FeedingAgeRange,
    viewModel  : GuideViewModel,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val dimensions = LocalDimensions.current
    val milkTabs   = listOf(
        stringResource(Res.string.feeding_guide_milk_tab_all),
        stringResource(Res.string.feeding_guide_milk_tab_breast),
        stringResource(Res.string.feeding_guide_milk_tab_formula),
        stringResource(Res.string.feeding_guide_milk_tab_combo)
    )

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        GuideTabRow(tabs = milkTabs, selectedIndex = selectedTab, onSelect = onSelectTab)

        val filterType = when (selectedTab) {
            1 -> "breastfeeding"  // compare against original English subType values from JSON data
            2 -> "formula"
            3 -> "combination"
            else -> null
        }
        val filtered = if (filterType == null) ageRange.milkFeeding
                       else ageRange.milkFeeding.filter { it.subType == filterType }

        filtered.forEach { item ->
            val vote = viewModel.getVote(item.id)
            MilkFeedingCard(item = item, vote = vote, viewModel = viewModel)
        }
    }
}

@Composable
private fun MilkFeedingCard(
    item     : MilkFeeding,
    vote     : GuideVote,
    viewModel: GuideViewModel
) {
    val dimensions = LocalDimensions.current
    GuideSectionCard(title = item.title) {
        FeedingInfoRow(stringResource(Res.string.feeding_guide_frequency), item.frequency)
        Spacer(Modifier.height(4.dp))
        FeedingInfoRow(stringResource(Res.string.feeding_guide_duration), item.duration)

        if (item.hungerSigns.isNotEmpty()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(stringResource(Res.string.feeding_guide_hunger_signs), style = MaterialTheme.typography.labelMedium,
                 fontWeight = FontWeight.Bold, color = GuidePink)
            item.hungerSigns.forEach { sign ->
                BulletPoint(sign)
            }
        }
        if (item.fullnessSigns.isNotEmpty()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(stringResource(Res.string.feeding_guide_fullness_signs), style = MaterialTheme.typography.labelMedium,
                 fontWeight = FontWeight.Bold, color = GuidePink)
            item.fullnessSigns.forEach { sign ->
                BulletPoint(sign)
            }
        }
        if (item.tip.isNotBlank()) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GuidePink.copy(0.15f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("💡", fontSize = 14.sp)
                Text("${stringResource(Res.string.feeding_guide_tip_label)} ${item.tip}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.9f))
            }
        }
        Spacer(Modifier.height(dimensions.spacingSmall))
        HorizontalDivider(color = Color.White.copy(0.2f))
        Spacer(Modifier.height(dimensions.spacingSmall))
        GuideFeedbackRow(
            vote      = vote,
            onUseful  = { viewModel.voteFeedingItem(item.id, VoteType.USEFUL) },
            onUseless = { viewModel.voteFeedingItem(item.id, VoteType.USELESS) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solid Foods Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SolidFoodsContent(
    ageRange : FeedingAgeRange,
    viewModel: GuideViewModel
) {
    val dimensions = LocalDimensions.current
    if (ageRange.solidFoods.isEmpty()) {
        GuideSectionCard(title = "SOLID FOODS") {
            Text(
                text  = stringResource(Res.string.feeding_guide_no_solid),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.85f)
            )
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        ageRange.solidFoods.forEach { item ->
            val vote = viewModel.getVote(item.id)
            GuideSectionCard(title = item.title) {
                Text(item.description, style = MaterialTheme.typography.bodySmall,
                     color = Color.White.copy(0.85f))
                if (item.foods.isNotEmpty()) {
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    Text(stringResource(Res.string.feeding_guide_recommended_foods), style = MaterialTheme.typography.labelMedium,
                         fontWeight = FontWeight.Bold, color = GuidePink)
                    item.foods.forEach { food -> BulletPoint(food) }
                }
                if (item.tip.isNotBlank()) {
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    TipBox(item.tip)
                }
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(color = Color.White.copy(0.2f))
                Spacer(Modifier.height(dimensions.spacingSmall))
                GuideFeedbackRow(
                    vote      = vote,
                    onUseful  = { viewModel.voteFeedingItem(item.id, VoteType.USEFUL) },
                    onUseless = { viewModel.voteFeedingItem(item.id, VoteType.USELESS) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sample Schedule Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SampleScheduleContent(
    ageRange : FeedingAgeRange,
    viewModel: GuideViewModel
) {
    val dimensions = LocalDimensions.current
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        ageRange.sampleSchedule.forEach { item ->
            val vote = viewModel.getVote(item.id)
            GuideSectionCard(title = item.title) {
                item.entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", color = GuidePink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(entry, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.85f))
                    }
                }
                if (item.tip.isNotBlank()) {
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    TipBox(item.tip)
                }
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(color = Color.White.copy(0.2f))
                Spacer(Modifier.height(dimensions.spacingSmall))
                GuideFeedbackRow(
                    vote      = vote,
                    onUseful  = { viewModel.voteFeedingItem(item.id, VoteType.USEFUL) },
                    onUseless = { viewModel.voteFeedingItem(item.id, VoteType.USELESS) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Foods To Avoid Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FoodsToAvoidContent(
    ageRange : FeedingAgeRange,
    viewModel: GuideViewModel
) {
    val dimensions = LocalDimensions.current
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        ageRange.foodsToAvoid.forEach { item ->
            val vote = viewModel.getVote(item.id)
            GuideSectionCard(title = "🚫 ${item.title}") {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(item.icon, fontSize = 24.sp)
                    Column {
                        Text(
                            text       = item.title,
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = item.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.85f)
                        )
                    }
                }
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(color = Color.White.copy(0.2f))
                Spacer(Modifier.height(dimensions.spacingSmall))
                GuideFeedbackRow(
                    vote      = vote,
                    onUseful  = { viewModel.voteFeedingItem(item.id, VoteType.USEFUL) },
                    onUseless = { viewModel.voteFeedingItem(item.id, VoteType.USELESS) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Feeding Tips Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeedingTipsContent(
    ageRange : FeedingAgeRange,
    viewModel: GuideViewModel
) {
    val dimensions = LocalDimensions.current
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
        ageRange.feedingTips.forEach { item ->
            val vote = viewModel.getVote(item.id)
            GuideSectionCard(title = "${item.icon} ${item.title}") {
                Text(item.description, style = MaterialTheme.typography.bodySmall,
                     color = Color.White.copy(0.85f))
                if (item.tip.isNotBlank()) {
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    TipBox(item.tip)
                }
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(color = Color.White.copy(0.2f))
                Spacer(Modifier.height(dimensions.spacingSmall))
                GuideFeedbackRow(
                    vote      = vote,
                    onUseful  = { viewModel.voteFeedingItem(item.id, VoteType.USEFUL) },
                    onUseless = { viewModel.voteFeedingItem(item.id, VoteType.USELESS) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared composable helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeedingInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall,
             fontWeight = FontWeight.Bold, color = GuidePink)
        Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.85f))
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier  = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("•", color = GuidePink, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.85f))
    }
}

@Composable
private fun TipBox(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GuidePink.copy(0.15f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("💡", fontSize = 14.sp)
        Text(tip, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.9f))
    }
}
