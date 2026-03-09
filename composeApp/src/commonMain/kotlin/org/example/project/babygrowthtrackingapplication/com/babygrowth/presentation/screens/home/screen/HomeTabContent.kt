package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.jetbrains.compose.resources.painterResource

// ─────────────────────────────────────────────────────────────────────────────
// HomeTabContent
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeTabContent(
    viewModel : HomeViewModel,
    onAddBaby : () -> Unit = {}
) {
    val state            = viewModel.uiState
    val dimensions       = LocalDimensions.current
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HomeTopBar(notificationCount = state.notificationCount)
        GenderBanner(genderTheme = state.genderTheme)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart    = dimensions.cardCornerRadius + dimensions.spacingSmall,
                        topEnd      = dimensions.cardCornerRadius + dimensions.spacingSmall,
                        bottomStart = 0.dp,
                        bottomEnd   = 0.dp
                    )
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top    = dimensions.spacingMedium + dimensions.spacingSmall,
                        start  = dimensions.screenPadding,
                        end    = dimensions.screenPadding,
                        bottom = dimensions.spacingLarge
                    )
            ) {
                WelcomeSection(
                    userName   = state.userName,
                    childCount = state.babies.size,
                    onAddChild = onAddBaby
                )

                Spacer(Modifier.height(dimensions.spacingMedium))

                when {
                    state.isLoading        -> LoadingSection()
                    state.babies.isEmpty() -> NoBabiesSection(onAddBaby = onAddBaby)
                    else -> {
                        ChildSelectorDropdown(
                            babies         = state.babies,
                            selectedIndex  = state.selectedBabyIndex,
                            expanded       = dropdownExpanded,
                            onExpandToggle = { dropdownExpanded = !dropdownExpanded },
                            onSelect       = { index ->
                                viewModel.selectBaby(index)
                                dropdownExpanded = false
                            }
                        )

                        Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

                        state.selectedBaby?.let { baby ->
                            BabyInfoCard(
                                baby            = baby,
                                nextVaccination = state.nextVaccination,
                                genderTheme     = state.genderTheme
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingLarge))

                UsefulToolsSection(
                    genderTheme    = state.genderTheme,
                    onSleepGuide   = { /* navigate */ },
                    onFeedingGuide = { /* navigate */ },
                    onMemory       = { /* navigate */ },
                    onAddBaby      = onAddBaby
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(notificationCount: Int) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = dimensions.screenPadding,
                vertical   = dimensions.spacingSmall + dimensions.spacingXSmall
            )
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(dimensions.iconXLarge - dimensions.spacingXSmall)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    customColors.accentGradientStart.copy(alpha = 0.3f),
                                    customColors.accentGradientEnd.copy(alpha = 0.15f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(Res.drawable.application_logo_without_background),
                        contentDescription = stringResource(Res.string.app_logo_description),
                        modifier           = Modifier.size(dimensions.iconLarge - dimensions.spacingXSmall)
                    )
                }

                Spacer(Modifier.width(dimensions.spacingSmall + dimensions.spacingXSmall))

                Column {
                    Text(
                        text       = stringResource(Res.string.app_name),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = stringResource(Res.string.tab_home),
                        style = MaterialTheme.typography.labelSmall,
                        color = customColors.accentGradientStart
                    )
                }
            }

            // Notification bell + badge
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = { /* navigate to notifications */ }) {
                    Icon(
                        imageVector        = Icons.Default.Notifications,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurface,
                        modifier           = Modifier.size(dimensions.iconLarge - dimensions.spacingXSmall)
                    )
                }
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(dimensions.iconSmall + dimensions.spacingXSmall)
                            .offset(x = (-2).dp, y = dimensions.spacingXSmall)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                            .border(
                                dimensions.borderWidthThin + 0.5.dp,        // WAS: 1.5.dp
                                MaterialTheme.colorScheme.surface,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = if (notificationCount > 99) "99+" else notificationCount.toString(),
                            fontSize   = dimensions.homeSmallTextSize,       // WAS: 9.sp
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onError,
                            lineHeight = dimensions.homeSmallLineHeight      // WAS: 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GENDER BANNER
// ─────────────────────────────────────────────────────────────────────────────

private data class BannerConfig(
    val bgColors  : List<Color>,
    val moonEmoji : String,
    val starColor : Color,
    val label     : String,
    val labelColor: Color,
    val subLabel  : String
)

@Composable
private fun GenderBanner(genderTheme: GenderTheme) {
    val dimensions = LocalDimensions.current

    val config = when (genderTheme) {
        GenderTheme.BOY -> BannerConfig(
            bgColors   = listOf(BoyLightColors.BabyBlue, BoyLightColors.Primary, BoyLightColors.Secondary),
            moonEmoji  = "🌙",
            starColor  = BoyLightColors.OceanBlue,
            label      = stringResource(Res.string.banner_boy_label),
            labelColor = BoyLightColors.OceanBlue,
            subLabel   = stringResource(Res.string.banner_boy_sub)
        )
        GenderTheme.GIRL -> BannerConfig(
            bgColors   = listOf(GirlLightColors.BabyPink, GirlLightColors.Primary, GirlLightColors.FlowerPink),
            moonEmoji  = "🌸",
            starColor  = GirlLightColors.HeartPink,
            label      = stringResource(Res.string.banner_girl_label),
            labelColor = GirlLightColors.HeartPink,
            subLabel   = stringResource(Res.string.banner_girl_sub)
        )
        GenderTheme.NEUTRAL -> BannerConfig(
            bgColors   = listOf(NeutralLightColors.BabyRose, NeutralLightColors.SecondaryVariant, NeutralLightColors.RosePink),
            moonEmoji  = "⭐",
            starColor  = NeutralLightColors.Primary,
            label      = stringResource(Res.string.banner_neutral_label),
            labelColor = NeutralLightColors.Primary,
            subLabel   = stringResource(Res.string.banner_neutral_sub)
        )
    }

    val bannerHeight = dimensions.logoSize * 0.6f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .background(Brush.verticalGradient(config.bgColors))
    ) {
        BannerStars(starColor = config.starColor)
        HangingStars(starColor = config.starColor)

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.avatarLarge + dimensions.spacingLarge)
                    .background(config.starColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensions.avatarLarge + dimensions.spacingSmall)
                        .background(config.starColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        config.moonEmoji,
                        fontSize = dimensions.bannerMoonEmojiSize           // WAS: (iconXLarge.value - 6).sp
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingXSmall + dimensions.borderWidthMedium))

            Text(
                text          = config.label,
                style         = MaterialTheme.typography.labelMedium,
                letterSpacing = dimensions.bannerLabelLetterSpacing,        // WAS: 1.5.sp
                color         = config.labelColor.copy(alpha = 0.8f),
                fontWeight    = FontWeight.Medium
            )
            Text(
                text       = config.subLabel,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = config.labelColor
            )
        }
    }
}

@Composable
private fun HangingStars(starColor: Color) {
    val dimensions = LocalDimensions.current
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensions.screenPadding,
                vertical   = dimensions.spacingXSmall + dimensions.borderWidthMedium
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(5) { i ->
            val size = if (i % 2 == 0) dimensions.starSizeLarge else dimensions.starSizeSmall // WAS: 16.sp / 12.sp
            Text("✦", fontSize = size, color = starColor.copy(alpha = 0.4f + (i * 0.08f)))
        }
    }
}

@Composable
private fun BannerStars(starColor: Color) {
    val dimensions = LocalDimensions.current
    Box(Modifier.fillMaxSize()) {
        Text(
            "✦", fontSize = 10.sp, color = starColor.copy(0.35f),
            modifier = Modifier.offset(
                dimensions.spacingLarge + dimensions.spacingSmall,
                dimensions.spacingXLarge + dimensions.spacingSmall
            )
        )
        Text(
            "★", fontSize = 14.sp, color = starColor.copy(0.25f),
            modifier = Modifier.offset(
                dimensions.screenPadding,
                dimensions.spacingXLarge + dimensions.spacingXXLarge
            )
        )
        Text(
            "✦", fontSize = 8.sp, color = starColor.copy(0.40f),
            modifier = Modifier.offset(
                dimensions.spacingXLarge + dimensions.spacingMedium,
                dimensions.spacingXXLarge + dimensions.spacingXLarge
            )
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Column {
                Text(
                    "★", fontSize = 12.sp, color = starColor.copy(0.30f),
                    modifier = Modifier.padding(
                        end = dimensions.spacingXLarge - dimensions.spacingSmall,
                        top = dimensions.spacingXLarge + dimensions.spacingSmall
                    )
                )
                Text(
                    "✦", fontSize = 9.sp, color = starColor.copy(0.45f),
                    modifier = Modifier.padding(
                        end = dimensions.screenPadding,
                        top = dimensions.spacingLarge + dimensions.spacingSmall
                    )
                )
                Text(
                    "★", fontSize = 16.sp, color = starColor.copy(0.20f),
                    modifier = Modifier.padding(
                        end = dimensions.spacingXLarge + dimensions.spacingMedium,
                        top = dimensions.spacingSmall
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WELCOME SECTION
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeSection(
    userName  : String,
    childCount: Int,
    onAddChild: () -> Unit
) {
    val dimensions = LocalDimensions.current

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Column {
            Text(
                text  = stringResource(Res.string.home_welcome_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = "Hello, $userName!",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text("👋", fontSize = (dimensions.iconMedium.value - 2).sp)
            }
            Text(
                text  = if (childCount == 1)
                    stringResource(Res.string.home_children_count_one)
                else
                    stringResource(Res.string.home_children_count_other, childCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(dimensions.cardCornerRadius - dimensions.spacingXSmall))
                .clickable(onClick = onAddChild)
                .padding(dimensions.spacingSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.iconXLarge - dimensions.spacingXSmall)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(dimensions.spacingSmall + dimensions.spacingXSmall)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.home_add_more_child),
                    tint     = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(dimensions.iconMedium)
                )
            }
            Spacer(Modifier.height(dimensions.spacingXSmall - dimensions.borderWidthThin))
            Text(
                text      = stringResource(Res.string.home_add_more_child),
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                maxLines  = 2,
                fontSize  = dimensions.homeSmallTextSize,                   // WAS: 9.sp
                lineHeight= dimensions.homeSmallLineHeight,                 // WAS: 11.sp
                modifier  = Modifier.widthIn(max = dimensions.avatarMedium + dimensions.spacingSmall)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NO BABIES STATE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoBabiesSection(onAddBaby: () -> Unit) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "👶",
            fontSize = (dimensions.iconXLarge.value + dimensions.spacingSmall.value).sp
        )
        Spacer(Modifier.height(dimensions.spacingMedium))
        Text(
            text       = stringResource(Res.string.home_no_babies_title),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onBackground,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(dimensions.spacingXSmall + dimensions.borderWidthMedium))
        Text(
            text      = stringResource(Res.string.home_no_babies_desc),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimensions.spacingMedium + dimensions.spacingSmall))
        Button(
            onClick = onAddBaby,
            shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(dimensions.iconMedium - dimensions.spacingXSmall)
            )
            Spacer(Modifier.width(dimensions.spacingXSmall + dimensions.borderWidthMedium))
            Text(
                text       = stringResource(Res.string.home_add_first_baby),
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHILD SELECTOR DROPDOWN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChildSelectorDropdown(
    babies        : List<BabyResponse>,
    selectedIndex : Int,
    expanded      : Boolean,
    onExpandToggle: () -> Unit,
    onSelect      : (Int) -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column {
        Text(
            text          = stringResource(Res.string.home_select_child),
            style         = MaterialTheme.typography.labelMedium,
            fontWeight    = FontWeight.SemiBold,
            color         = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing // WAS: 1.2.sp (raw literal)
        )

        Spacer(Modifier.height(dimensions.spacingXSmall + dimensions.borderWidthMedium))

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cardCornerRadius - dimensions.spacingXSmall))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                customColors.accentGradientStart.copy(0.18f),
                                customColors.accentGradientEnd.copy(0.10f)
                            )
                        )
                    )
                    .border(
                        dimensions.borderWidthThin,                         // WAS: 1.dp
                        customColors.accentGradientStart.copy(0.3f),
                        RoundedCornerShape(dimensions.cardCornerRadius - dimensions.spacingXSmall)
                    )
                    .clickable(onClick = onExpandToggle)
                    .padding(
                        horizontal = dimensions.screenPadding,
                        vertical   = dimensions.spacingSmall + dimensions.spacingXSmall
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = babies.getOrNull(selectedIndex)?.fullName
                        ?: stringResource(Res.string.home_select_child_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint               = customColors.accentGradientStart,
                    modifier           = Modifier.size(dimensions.iconMedium)
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = onExpandToggle) {
                babies.forEachIndexed { index, baby ->
                    val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
                            baby.gender.equals("GIRL", ignoreCase = true)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (isFemale) "👧" else "👦",
                                    fontSize = dimensions.iconMedium.value.sp
                                )
                                Spacer(Modifier.width(dimensions.spacingSmall))
                                Text(baby.fullName)
                            }
                        },
                        onClick = { onSelect(index) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BABY INFO CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyInfoCard(
    baby           : BabyResponse,
    nextVaccination: VaccinationResponse?,
    genderTheme    : GenderTheme
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val accentColor  = customColors.accentGradientStart

    val cardBg = when (genderTheme) {
        GenderTheme.GIRL    -> Brush.horizontalGradient(listOf(GirlLightColors.BabyPink,     GirlLightColors.Background))
        GenderTheme.BOY     -> Brush.horizontalGradient(listOf(BoyLightColors.BabyBlue,      BoyLightColors.Background))
        GenderTheme.NEUTRAL -> Brush.horizontalGradient(listOf(NeutralLightColors.BabyRose,  NeutralLightColors.Background))
    }

    val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
            baby.gender.equals("GIRL", ignoreCase = true)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(dimensions.cardElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(dimensions.spacingMedium + dimensions.spacingXSmall)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(dimensions.avatarMedium)
                            .background(accentColor.copy(0.12f), CircleShape)
                            .border(
                                dimensions.borderWidthThin,                 // WAS: 1.dp
                                accentColor.copy(0.25f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isFemale) "👧" else "👦",
                            fontSize = (dimensions.avatarMedium.value * 0.55f).sp
                        )
                    }

                    Spacer(Modifier.width(dimensions.spacingMedium))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = baby.fullName,
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text  = formatAge(baby.ageInMonths),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    baby.birthWeight?.let { w ->
                        BabyStatChip(
                            "⚖️",
                            stringResource(Res.string.baby_birth_weight),
                            stringResource(Res.string.profile_weight_value, w.toString()),
                            accentColor
                        )
                    }
                    baby.birthHeight?.let { h ->
                        BabyStatChip(
                            "📏",
                            stringResource(Res.string.baby_birth_height),
                            stringResource(Res.string.profile_height_value, h.toString()),
                            accentColor
                        )
                    }
                }

                if (nextVaccination != null) {
                    Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimensions.spacingSmall))
                            .background(accentColor.copy(0.08f))
                            .padding(
                                horizontal = dimensions.spacingSmall + dimensions.spacingXSmall,
                                vertical   = dimensions.spacingXSmall + 3.dp
                            ),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💉", fontSize = (dimensions.iconSmall.value - 2).sp)
                            Spacer(Modifier.width(dimensions.spacingXSmall + dimensions.borderWidthMedium))
                            Column {
                                Text(
                                    text  = stringResource(Res.string.home_next_vaccine_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                )
                                Text(
                                    text       = nextVaccination.vaccineName,
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = accentColor
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text  = stringResource(Res.string.home_scheduled_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                            Text(
                                text       = formatDate(nextVaccination.scheduledDate),
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface.copy(0.75f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))
                AgeProgressRibbon(
                    ageInDays   = baby.ageInDays,
                    ageInMonths = baby.ageInMonths,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun BabyStatChip(icon: String, label: String, value: String, accentColor: Color) {
    val dimensions = LocalDimensions.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensions.spacingSmall))
            .background(accentColor.copy(0.1f))
            .padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingXSmall + 1.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
    ) {
        Text(icon, fontSize = (dimensions.iconSmall.value - 4).sp)
        Column {
            Text(
                label,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                fontSize = dimensions.homeSmallTextSize                     // WAS: 9.sp
            )
            Text(
                value,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = accentColor
            )
        }
    }
}

@Composable
private fun AgeProgressRibbon(ageInDays: Long, ageInMonths: Int, accentColor: Color) {
    val dimensions    = LocalDimensions.current
    val monthProgress = ((ageInDays % 30).toInt().coerceIn(0, 30)) / 30f

    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = "📅 $ageInDays days old",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
            )
            Text(
                text       = "Month $ageInMonths progress",
                style      = MaterialTheme.typography.labelSmall,
                color      = accentColor.copy(0.8f),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(dimensions.spacingXSmall))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.spacingXSmall + 1.dp)
                .clip(RoundedCornerShape(dimensions.spacingXSmall / 2))
                .background(accentColor.copy(0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(monthProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(listOf(accentColor, accentColor.copy(0.6f)))
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOADING SKELETON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingSection() {
    val dimensions   = LocalDimensions.current
    val shimmerAlpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "shimmer_alpha"
    )
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + dimensions.spacingXSmall)) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.avatarLarge + dimensions.spacingMedium)
                    .clip(RoundedCornerShape(dimensions.cardCornerRadius))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = shimmerAlpha))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// USEFUL TOOLS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UsefulToolsSection(
    genderTheme   : GenderTheme,
    onSleepGuide  : () -> Unit,
    onFeedingGuide: () -> Unit,
    onMemory      : () -> Unit,
    onAddBaby     : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    val iconBg = when (genderTheme) {
        GenderTheme.GIRL    -> GirlLightColors.Primary.copy(0.15f)
        GenderTheme.BOY     -> BoyLightColors.Primary.copy(0.15f)
        GenderTheme.NEUTRAL -> NeutralLightColors.Primary.copy(0.10f)
    }

    val tools = listOf(
        Triple("💤", stringResource(Res.string.home_tool_sleep),    onSleepGuide),
        Triple("🍼", stringResource(Res.string.home_tool_feeding),  onFeedingGuide),
        Triple("📸", stringResource(Res.string.home_tool_memory),   onMemory),
        Triple("👶", stringResource(Res.string.home_tool_add_baby), onAddBaby)
    )

    Column {
        Text(
            text       = stringResource(Res.string.home_useful_tools),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

        Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
            listOf(0, 2).forEach { rowStart ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {
                    tools.subList(rowStart, rowStart + 2).forEach { (icon, label, action) ->
                        ToolCard(
                            icon        = icon,
                            label       = label,
                            iconBg      = iconBg,
                            accentColor = customColors.accentGradientStart,
                            onClick     = action,
                            modifier    = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    icon       : String,
    label      : String,
    iconBg     : Color,
    accentColor: Color,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cardCornerRadius))
            .clickable(onClick = onClick)
            .padding(vertical = dimensions.spacingXSmall + dimensions.borderWidthMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val toolIconSize = dimensions.avatarLarge + dimensions.spacingSmall
        val innerBoxSize = dimensions.avatarLarge - dimensions.spacingXSmall
        val toolCorner   = dimensions.cardCornerRadius + dimensions.spacingXSmall
        val innerCorner  = dimensions.cardCornerRadius - dimensions.spacingXSmall

        Box(
            modifier = Modifier
                .size(toolIconSize)
                .background(iconBg, RoundedCornerShape(toolCorner))
                .border(
                    dimensions.borderWidthThin,                             // WAS: 1.dp
                    accentColor.copy(0.18f),
                    RoundedCornerShape(toolCorner)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(innerBoxSize)
                    .background(accentColor.copy(0.07f), RoundedCornerShape(innerCorner))
            )
            Text(icon, fontSize = (dimensions.iconXLarge.value - 16).sp)
        }

        Spacer(Modifier.height(dimensions.spacingXSmall + 3.dp))

        Text(
            text      = label,
            style     = MaterialTheme.typography.labelMedium,
            fontWeight= FontWeight.Medium,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun formatAge(months: Int): String = when {
    months < 1  -> stringResource(Res.string.age_newborn)
    months < 12 -> if (months == 1)
        stringResource(Res.string.age_months, months)
    else
        stringResource(Res.string.age_months_plural, months)
    else -> {
        val y = months / 12
        val m = months % 12
        if (m == 0)
            stringResource(Res.string.age_years_only, y)
        else
            stringResource(Res.string.age_years_months, y, m)
    }
}

@Composable
private fun formatDate(dateStr: String): String {
    val parts = dateStr.split("-")
    if (parts.size != 3) return dateStr
    val monthIndex = parts[1].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "",
        stringResource(Res.string.month_jan), stringResource(Res.string.month_feb),
        stringResource(Res.string.month_mar), stringResource(Res.string.month_apr),
        stringResource(Res.string.month_may), stringResource(Res.string.month_jun),
        stringResource(Res.string.month_jul), stringResource(Res.string.month_aug),
        stringResource(Res.string.month_sep), stringResource(Res.string.month_oct),
        stringResource(Res.string.month_nov), stringResource(Res.string.month_dec)
    )
    return "${monthNames.getOrElse(monthIndex) { parts[1] }} ${parts[2]}"
}