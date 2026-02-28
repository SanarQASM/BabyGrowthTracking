package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.AddBabyViewModel
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun AddBabyScreen(
    viewModel : AddBabyViewModel,
    onBack    : () -> Unit,
    onSaved   : () -> Unit
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    var showDatePicker         by remember { mutableStateOf(false) }
    var showImageWarningDialog by remember { mutableStateOf(false) }

    BabyGrowthTheme {
        Scaffold(
            topBar         = { AddBabyTopBar(onBack = onBack) },
            containerColor = Color.Transparent
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                customColors.accentGradientStart.copy(alpha = 0.15f),
                                customColors.accentGradientEnd.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .padding(paddingValues)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    Spacer(Modifier.height(dimensions.spacingMedium))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        customColors.accentGradientStart.copy(alpha = 0.6f),
                                        customColors.accentGradientEnd.copy(alpha = 0.45f)
                                    )
                                ),
                                RoundedCornerShape(
                                    topStart = dimensions.cardCornerRadius,
                                    topEnd   = dimensions.cardCornerRadius
                                )
                            )
                            .padding(bottom = dimensions.spacingXLarge)
                    ) {
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = dimensions.spacingLarge,
                                    vertical   = dimensions.spacingXLarge
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            PhotoSelectorSection(onPickImage = { showImageWarningDialog = true })

                            Spacer(Modifier.height(dimensions.spacingXLarge))

                            FormSectionCard(
                                title = stringResource(Res.string.add_baby_section_basic_info)
                            ) {
                                FormTextField(
                                    value         = state.fullName,
                                    onValueChange = viewModel::onFullNameChange,
                                    placeholder   = stringResource(Res.string.add_baby_field_full_name),
                                    isError       = state.nameError != null,
                                    errorMessage  = state.nameError
                                )

                                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

                                FormTextField(
                                    value         = state.dateOfBirth,
                                    onValueChange = {},
                                    placeholder   = stringResource(Res.string.add_baby_field_dob),
                                    isError       = state.dobError != null,
                                    errorMessage  = state.dobError,
                                    readOnly      = true,
                                    onClick       = { showDatePicker = true },
                                    trailingIcon  = {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = stringResource(Res.string.add_baby_field_dob_pick),
                                            tint     = customColors.accentGradientEnd,
                                            modifier = Modifier.size(dimensions.iconMedium)
                                        )
                                    }
                                )

                                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

                                GenderSelector(
                                    selected = state.gender,
                                    onSelect = viewModel::onGenderChange
                                )
                            }

                            Spacer(Modifier.height(dimensions.spacingMedium))

                            FormSectionCard(
                                title = stringResource(Res.string.add_baby_section_measurements)
                            ) {
                                FormTextField(
                                    value         = state.birthWeight,
                                    onValueChange = viewModel::onBirthWeightChange,
                                    placeholder   = stringResource(Res.string.add_baby_field_weight),
                                    keyboardType  = KeyboardType.Decimal,
                                    trailingText  = stringResource(Res.string.add_baby_unit_kg)
                                )

                                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

                                FormTextField(
                                    value         = state.birthHeight,
                                    onValueChange = viewModel::onBirthHeightChange,
                                    placeholder   = stringResource(Res.string.add_baby_field_height),
                                    keyboardType  = KeyboardType.Decimal,
                                    trailingText  = stringResource(Res.string.add_baby_unit_cm)
                                )

                                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))

                                FormTextField(
                                    value         = state.headCircumference,
                                    onValueChange = viewModel::onHeadCircumferenceChange,
                                    placeholder   = stringResource(Res.string.add_baby_field_head),
                                    keyboardType  = KeyboardType.Decimal,
                                    trailingText  = stringResource(Res.string.add_baby_unit_cm)
                                )
                            }

                            Spacer(Modifier.height(dimensions.spacingXLarge))

                            SaveButton(
                                isLoading        = state.isLoading,
                                isUploadingImage = state.isUploadingImage,
                                onClick          = { viewModel.saveBaby() }
                            )

                            Spacer(Modifier.height(dimensions.spacingMedium))

                            ResetButton(onClick = { viewModel.resetForm() })
                        }
                    }
                }

                state.errorMessage?.let { msg ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(dimensions.screenPadding),
                        action = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text(
                                    stringResource(Res.string.add_baby_dismiss),
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(msg, color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }

    // ── Image coming-soon dialog ──────────────────────────────────────────────
    if (showImageWarningDialog) {
        AlertDialog(
            onDismissRequest = { showImageWarningDialog = false },
            icon  = {
                Text(
                    "📸",
                    fontSize = MaterialTheme.typography.displaySmall.fontSize
                )
            },
            title = {
                Text(
                    text       = stringResource(Res.string.add_baby_photo_dialog_title),
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
            },
            text = {
                Text(
                    text      = stringResource(Res.string.add_baby_photo_dialog_message),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick  = { showImageWarningDialog = false },
                    shape    = RoundedCornerShape(LocalDimensions.current.buttonCornerRadius),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.customColors.accentGradientStart
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.add_baby_photo_dialog_confirm),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape          = RoundedCornerShape(LocalDimensions.current.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ── Date picker dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        BabyDatePickerDialog(
            onDateSelected = { dateString ->
                viewModel.onDateOfBirthChange(dateString)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBabyTopBar(onBack: () -> Unit) {
    val customColors = MaterialTheme.customColors
    TopAppBar(
        title = {
            Text(
                text       = stringResource(Res.string.add_baby_title),
                fontWeight = FontWeight.SemiBold,
                style      = MaterialTheme.typography.titleMedium,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back),
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = customColors.accentGradientStart.copy(alpha = 0.15f)
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SAVE BUTTON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SaveButton(
    isLoading       : Boolean,
    isUploadingImage: Boolean,
    onClick         : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val isEnabled    = !isLoading && !isUploadingImage

    Button(
        onClick        = onClick,
        enabled        = isEnabled,
        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors         = ButtonDefaults.buttonColors(
            containerColor         = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        elevation      = ButtonDefaults.buttonElevation(
            defaultElevation  = 0.dp,
            pressedElevation  = 0.dp,
            disabledElevation = 0.dp
        ),
        modifier       = Modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isEnabled) {
                        Brush.verticalGradient(
                            listOf(
                                customColors.accentGradientStart.copy(alpha = 0.85f),
                                customColors.accentGradientEnd.copy(alpha = 0.75f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .border(
                    width = dimensions.spacingXSmall / 2,
                    color = customColors.accentGradientStart.copy(
                        alpha = if (isEnabled) 0.60f else 0.25f
                    ),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glassmorphic white overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        customColors.glassOverlay,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )
            if (isLoading || isUploadingImage) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + dimensions.spacingXSmall)
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconMedium),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.spacingXSmall / 2
                    )
                    Text(
                        text       = if (isUploadingImage)
                            stringResource(Res.string.add_baby_uploading_photo)
                        else
                            stringResource(Res.string.add_baby_saving),
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                    Text(
                        text       = stringResource(Res.string.add_baby_save_button),
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RESET BUTTON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResetButton(onClick: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Button(
        onClick        = onClick,
        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        elevation      = ButtonDefaults.buttonElevation(
            defaultElevation  = 0.dp,
            pressedElevation  = 0.dp,
            disabledElevation = 0.dp
        ),
        modifier       = Modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            customColors.warning.copy(alpha = 0.70f),
                            customColors.warning.copy(alpha = 0.55f)
                        )
                    ),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .background(
                    customColors.glassOverlay,
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .border(
                    width = dimensions.spacingXSmall / 4,
                    color = customColors.glassOverlay.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Text(
                    "🗑️",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
                Text(
                    text       = stringResource(Res.string.add_baby_clear_button),
                    color      = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PHOTO SELECTOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PhotoSelectorSection(onPickImage: () -> Unit) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text          = stringResource(Res.string.add_baby_photo_section),
            style         = MaterialTheme.typography.labelLarge,
            fontWeight    = FontWeight.Bold,
            color         = MaterialTheme.colorScheme.onPrimary,
            letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensions.spacingMedium)
        )

        Box(
            modifier = Modifier
                .size(dimensions.logoSize * 0.4f)
                .clip(CircleShape)
                .background(customColors.glassOverlay.copy(alpha = 0.15f))
                .border(
                    width = dimensions.spacingXSmall / 4,
                    color = customColors.glassOverlay.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "👶",
                    fontSize = MaterialTheme.typography.displaySmall.fontSize
                )
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Text(
                    text      = stringResource(Res.string.add_baby_photo_tap),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = customColors.glassOverlay.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(dimensions.spacingSmall))

        Text(
            text      = stringResource(Res.string.add_baby_photo_coming_soon),
            style     = MaterialTheme.typography.labelSmall,
            color     = customColors.glassOverlay.copy(alpha = 0.60f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(dimensions.spacingSmall))

        Text(
            text       = stringResource(Res.string.app_name),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORM SECTION CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormSectionCard(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text          = title,
            style         = MaterialTheme.typography.labelLarge,
            fontWeight    = FontWeight.Bold,
            color         = MaterialTheme.colorScheme.onPrimary,
            letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
            modifier      = Modifier.padding(bottom = dimensions.spacingSmall)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    customColors.glassOverlay.copy(alpha = 0.18f),
                    RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .border(
                    width = dimensions.spacingXSmall / 4,
                    color = customColors.glassOverlay.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .padding(
                    horizontal = dimensions.spacingMedium,
                    vertical   = dimensions.spacingMedium
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORM TEXT FIELD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormTextField(
    value        : String,
    onValueChange: (String) -> Unit,
    placeholder  : String,
    modifier     : Modifier = Modifier,
    keyboardType : KeyboardType = KeyboardType.Text,
    trailingText : String? = null,
    trailingIcon : @Composable (() -> Unit)? = null,
    isError      : Boolean = false,
    errorMessage : String? = null,
    readOnly     : Boolean = false,
    onClick      : (() -> Unit)? = null
) {
    val dimensions       = LocalDimensions.current
    val textColor        = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val trailingColor    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.buttonHeight)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                    RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                )
                .border(
                    width = dimensions.spacingXSmall / 4,
                    color = if (isError)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.customColors.accentGradientStart.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                )
        ) {
            TextField(
                value         = value,
                onValueChange = onValueChange,
                readOnly      = readOnly,
                singleLine    = true,
                placeholder   = {
                    Text(
                        text  = placeholder,
                        color = placeholderColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                trailingIcon = trailingIcon ?: trailingText?.let { label ->
                    {
                        Text(
                            text       = label,
                            color      = trailingColor,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.padding(end = dimensions.spacingXSmall + dimensions.spacingXSmall)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedTextColor        = textColor,
                    unfocusedTextColor      = textColor,
                    disabledTextColor       = textColor,
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor  = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                    errorIndicatorColor     = Color.Transparent,
                    cursorColor             = MaterialTheme.customColors.accentGradientStart
                ),
                modifier  = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor)
            )

            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { onClick() }
                )
            }
        }

        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text       = errorMessage ?: "",
                color      = MaterialTheme.colorScheme.error,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.padding(
                    start = dimensions.spacingXSmall,
                    top   = dimensions.spacingXSmall
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GENDER SELECTOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GenderSelector(selected: String, onSelect: (String) -> Unit) {
    val dimensions = LocalDimensions.current
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = stringResource(Res.string.add_baby_gender_label),
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier   = Modifier.width(dimensions.avatarMedium + dimensions.spacingMedium)
        )
        GenderRadioOption(
            label      = stringResource(Res.string.add_baby_gender_male),
            isSelected = selected.equals("BOY", ignoreCase = true),
            onClick    = { onSelect("BOY") }
        )
        Spacer(Modifier.width(dimensions.spacingLarge))
        GenderRadioOption(
            label      = stringResource(Res.string.add_baby_gender_female),
            isSelected = selected.equals("GIRL", ignoreCase = true),
            onClick    = { onSelect("GIRL") }
        )
    }
}

@Composable
private fun GenderRadioOption(
    label     : String,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.clickable { onClick() }
    ) {
        RadioButton(
            selected = isSelected,
            onClick  = onClick,
            colors   = RadioButtonDefaults.colors(
                selectedColor   = customColors.accentGradientStart,
                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            color      = if (isSelected) customColors.accentGradientStart
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DATE PICKER DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun BabyDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss     : () -> Unit
) {
    val dimensions      = LocalDimensions.current
    val customColors    = MaterialTheme.customColors
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        selectableDates    = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton    = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant   = Instant.fromEpochMilliseconds(millis)
                        val date      = instant.toLocalDateTime(TimeZone.UTC).date
                        val formatted = "${date.year}-" +
                                "${date.month.number.toString().padStart(2, '0')}-" +
                                "${date.day.toString().padStart(2, '0')}"
                        onDateSelected(formatted)
                    } ?: onDismiss()
                },
                shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart
                )
            ) {
                Text(
                    stringResource(Res.string.add_baby_date_ok),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(stringResource(Res.string.add_baby_date_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState, showModeToggle = true)
    }
}