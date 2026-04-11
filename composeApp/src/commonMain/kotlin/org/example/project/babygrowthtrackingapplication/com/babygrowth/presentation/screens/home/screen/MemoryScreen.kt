package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlinx.coroutines.delay
import org.example.project.babygrowthtrackingapplication.platform.ByteArrayImage
import org.example.project.babygrowthtrackingapplication.platform.ImagePickerButton

// ─────────────────────────────────────────────────────────────────────────────
// MemoryScreen — landscape-aware entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MemoryScreen(
    viewModel      : MemoryViewModel,
    babies         : List<BabyResponse>,
    selectedBabyId : String?           = null,
    language       : Language          = Language.ENGLISH,
    onBack         : () -> Unit        = {}
) {
    // Use a stable key: join of babyIds so list-reference churn doesn't retrigger
    val babiesKey = remember(babies) { babies.joinToString(",") { it.babyId } }

    LaunchedEffect(babiesKey, selectedBabyId) {
        viewModel.load(babies, selectedBabyId)
    }

    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current
    val isLandscape = LocalIsLandscape.current

    // Full-screen image viewer overlay
    state.viewingImage?.let { img ->
        ImageViewerOverlay(
            image        = img,
            memoryTitle  = state.viewingMemoryTitle,
            onClose      = { viewModel.closeImageViewer() }
        )
        return
    }

    // Add-memory sheet
    if (state.showAddForm) {
        AddMemorySheet(
            formState      = state.addForm,
            babies         = state.babies,
            selectedBabyId = state.selectedBabyId,
            onBabySelect   = { viewModel.selectBaby(it) },
            onTitleChange  = viewModel::onTitleChange,
            onDescChange   = viewModel::onDescriptionChange,
            onDateChange   = viewModel::onDateChange,
            onAddImages    = { viewModel.onImagesSelected(it) },
            onRemoveImage  = viewModel::removeImage,
            onCaptionChange = viewModel::onCaptionChange,
            onSave         = viewModel::saveMemory,
            onCancel       = viewModel::closeAddForm,
            dimensions     = dimensions
        )
        return
    }

    // Delete confirm dialog
    state.deletingMemoryId?.let { memId ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deleteMemory(memId) },
            onDismiss = { viewModel.cancelDelete() }
        )
    }

    // Main content
    Scaffold(
        topBar = {
            MemoryTopBar(onBack = onBack)
        },
        floatingActionButton = {
            AddMemoryFab(onClick = { viewModel.openAddForm() })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLandscape) {
                MemoryLandscapeLayout(
                    state      = state,
                    viewModel  = viewModel,
                    dimensions = dimensions
                )
            } else {
                MemoryPortraitLayout(
                    state      = state,
                    viewModel  = viewModel,
                    dimensions = dimensions
                )
            }

            // Snackbar
            state.actionMessage?.let { msg ->
                LaunchedEffect(msg) {
                    delay(2500)
                    viewModel.clearActionMessage()
                }
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(dimensions.screenPadding),
                    containerColor = MaterialTheme.customColors.accentGradientStart
                ) {
                    Text(msg, color = MaterialTheme.colorScheme.onPrimary)
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
private fun MemoryTopBar(onBack: () -> Unit) {
    val customColors = MaterialTheme.customColors
    TopAppBar(
        title = {
            Text(
                text       = stringResource(Res.string.memory_screen_title),
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.titleMedium,
                color      = customColors.accentGradientStart
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back),
                    tint = customColors.accentGradientStart
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = customColors.accentGradientStart.copy(alpha = 0.12f)
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// FAB
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AddMemoryFab(onClick: () -> Unit) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    ExtendedFloatingActionButton(
        onClick           = onClick,
        containerColor    = customColors.accentGradientStart,
        contentColor      = MaterialTheme.colorScheme.onPrimary,
        shape             = RoundedCornerShape(dimensions.buttonCornerRadius),
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = {
            Text(
                stringResource(Res.string.memory_add),
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Portrait Layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemoryPortraitLayout(
    state      : MemoryUiState,
    viewModel  : MemoryViewModel,
    dimensions : Dimensions
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BabySelectorHeader(
            babies         = state.babies,
            selectedBabyId = state.selectedBabyId,
            onSelect       = viewModel::selectBaby,
            dimensions     = dimensions
        )

        if (state.hasAnyMissingImages) {
            MissingImagesBanner(dimensions = dimensions)
        }

        when {
            state.isLoading -> LoadingMemories(dimensions = dimensions)
            state.filteredMemories.isEmpty() -> EmptyMemoriesSection(
                dimensions = dimensions,
                onAddFirst = viewModel::openAddForm
            )
            else -> MemoryList(
                memories    = state.filteredMemories,
                dimensions  = dimensions,
                onViewImage = { img, title -> viewModel.viewImage(img, title) },
                onDelete    = { id -> viewModel.confirmDeleteMemory(id) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Landscape Layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemoryLandscapeLayout(
    state      : MemoryUiState,
    viewModel  : MemoryViewModel,
    dimensions : Dimensions
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane — baby selector + stats
        Column(
            modifier = Modifier
                .width(dimensions.landscapeNarrowPaneWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(dimensions.spacingMedium)
        ) {
            Text(
                text       = stringResource(Res.string.memory_select_child),
                style      = MaterialTheme.typography.labelMedium,
                color      = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            BabyChip(
                name       = stringResource(Res.string.memory_all_children),
                selected   = state.selectedBabyId == null,
                onClick    = { viewModel.selectBaby(null) },
                dimensions = dimensions
            )
            state.babies.forEach { baby ->
                Spacer(Modifier.height(dimensions.spacingXSmall))
                val isFemale = baby.gender.equals("GIRL", ignoreCase = true) ||
                        baby.gender.equals("FEMALE", ignoreCase = true)
                BabyChip(
                    name       = "${if (isFemale) "👧" else "👦"} ${baby.fullName}",
                    selected   = state.selectedBabyId == baby.babyId,
                    onClick    = { viewModel.selectBaby(baby.babyId) },
                    dimensions = dimensions
                )
            }
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                text  = "${state.filteredMemories.size} ${stringResource(Res.string.memory_count)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Right pane — memory list
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.hasAnyMissingImages) {
                MissingImagesBanner(dimensions = dimensions)
            }
            when {
                state.isLoading -> LoadingMemories(dimensions = dimensions)
                state.filteredMemories.isEmpty() -> EmptyMemoriesSection(
                    dimensions = dimensions,
                    onAddFirst = viewModel::openAddForm
                )
                else -> MemoryList(
                    memories    = state.filteredMemories,
                    dimensions  = dimensions,
                    onViewImage = { img, title -> viewModel.viewImage(img, title) },
                    onDelete    = { id -> viewModel.confirmDeleteMemory(id) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Baby Selector Header (portrait)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabySelectorHeader(
    babies         : List<BabyResponse>,
    selectedBabyId : String?,
    onSelect       : (String?) -> Unit,
    dimensions     : Dimensions
) {
    val customColors  = MaterialTheme.customColors
    val allChildrenLabel = stringResource(Res.string.memory_all_children)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall)
    ) {
        Text(
            text       = stringResource(Res.string.memory_select_child),
            style      = MaterialTheme.typography.labelMedium,
            color      = MaterialTheme.colorScheme.onSurface.copy(0.6f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(dimensions.spacingXSmall))

        var expanded by remember { mutableStateOf(false) }
        val selectedName = when {
            selectedBabyId == null -> "📸 $allChildrenLabel"
            else -> {
                val b = babies.find { it.babyId == selectedBabyId }
                val isFemale = b?.gender?.equals("GIRL", ignoreCase = true) == true ||
                        b?.gender?.equals("FEMALE", ignoreCase = true) == true
                "${if (isFemale) "👧" else "👦"} ${b?.fullName ?: ""}"
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimensions.cardCornerRadius))
                .background(customColors.accentGradientStart.copy(0.1f))
                .clickable { expanded = !expanded }
                .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedName,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint     = customColors.accentGradientStart,
                    modifier = Modifier.size(dimensions.iconMedium)
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text    = { Text("📸 $allChildrenLabel") },
                    onClick = { onSelect(null); expanded = false }
                )
                babies.forEach { baby ->
                    val isFemale = baby.gender.equals("GIRL", ignoreCase = true) ||
                            baby.gender.equals("FEMALE", ignoreCase = true)
                    DropdownMenuItem(
                        text    = {
                            Text("${if (isFemale) "👧" else "👦"} ${baby.fullName}")
                        },
                        onClick = { onSelect(baby.babyId); expanded = false }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Missing Images Warning Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MissingImagesBanner(dimensions: Dimensions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(0.7f))
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Text("⚠️", fontSize = dimensions.iconSmall.value.sp)
        Text(
            text     = stringResource(Res.string.memory_images_not_available),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Memory List
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemoryList(
    memories    : List<MemoryUiItem>,
    dimensions  : Dimensions,
    onViewImage : (MemoryUiImage, String) -> Unit,
    onDelete    : (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensions.screenPadding,
            vertical   = dimensions.spacingMedium
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        items(memories, key = { it.memoryId }) { memory ->
            MemoryCard(
                memory      = memory,
                dimensions  = dimensions,
                onViewImage = onViewImage,
                onDelete    = onDelete
            )
        }
        item { Spacer(Modifier.height(dimensions.spacingXLarge + dimensions.buttonHeight)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Memory Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemoryCard(
    memory      : MemoryUiItem,
    dimensions  : Dimensions,
    onViewImage : (MemoryUiImage, String) -> Unit,
    onDelete    : (String) -> Unit
) {
    val customColors = MaterialTheme.customColors

    Card(
        shape     = RoundedCornerShape(dimensions.chartCardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation / 2),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {

            // ── Header row ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = memory.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(dimensions.spacingXSmall))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = formatMemoryDate(memory.memoryDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
                        )
                        if (memory.ageInMonths != null) {
                            Text(
                                "•",
                                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text  = formatAge(memory.ageInMonths),
                                style = MaterialTheme.typography.bodySmall,
                                color = customColors.accentGradientStart.copy(0.85f)
                            )
                        }
                    }
                }

                IconButton(
                    onClick  = { onDelete(memory.memoryId) },
                    modifier = Modifier.size(dimensions.addButtonSize)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.memory_delete),
                        tint     = MaterialTheme.colorScheme.error.copy(0.7f),
                        modifier = Modifier.size(dimensions.iconMedium - 4.dp)
                    )
                }
            }

            // ── Description ───────────────────────────────────────────────────
            if (!memory.description.isNullOrBlank()) {
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Text(
                    text      = "\"${memory.description}\"",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                    maxLines  = 3,
                    overflow  = TextOverflow.Ellipsis,
                    fontStyle = FontStyle.Italic
                )
            }

            // ── Missing images warning ────────────────────────────────────────
            if (memory.hasMissingImages) {
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.spacingSmall))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(0.4f))
                        .padding(
                            horizontal = dimensions.spacingSmall,
                            vertical   = dimensions.spacingXSmall
                        )
                ) {
                    Text("⚠️", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text  = stringResource(Res.string.memory_images_missing_card),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // ── Image gallery ─────────────────────────────────────────────────
            if (memory.images.isNotEmpty()) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(
                    thickness = dimensions.hairlineDividerThickness,
                    color     = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(dimensions.spacingSmall))
                MemoryImageGallery(
                    images      = memory.images,
                    memoryTitle = memory.title,
                    dimensions  = dimensions,
                    onViewImage = onViewImage
                )
            }

            // ── No images placeholder ─────────────────────────────────────────
            if (memory.images.isEmpty()) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.spacingSmall))
                        .background(customColors.glassBackground)
                        .padding(dimensions.spacingSmall)
                ) {
                    Text("📷", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Text(
                        stringResource(Res.string.memory_no_images),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Horizontal Image Gallery
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemoryImageGallery(
    images      : List<MemoryUiImage>,
    memoryTitle : String,
    dimensions  : Dimensions,
    onViewImage : (MemoryUiImage, String) -> Unit
) {
    val imageHeight = dimensions.avatarLarge * 2f

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
        contentPadding        = PaddingValues(horizontal = dimensions.spacingXSmall)
    ) {
        items(images, key = { it.localKey }) { img ->
            MemoryImageThumb(
                image       = img,
                height      = imageHeight,
                dimensions  = dimensions,
                onClick     = { onViewImage(img, memoryTitle) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single image thumbnail
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemoryImageThumb(
    image      : MemoryUiImage,
    height     : Dp,
    dimensions : Dimensions,
    onClick    : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val width = height * 0.85f

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(dimensions.cardCornerRadius - 4.dp))
            .background(customColors.glassBackground)
            .clickable(enabled = image.isAvailable) { onClick() }
    ) {
        if (image.isAvailable && image.bytes != null) {
            ByteArrayImage(
                bytes        = image.bytes,
                modifier     = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.3f))
                        )
                    )
            )

            Icon(
                Icons.Default.ZoomIn,
                contentDescription = null,
                tint     = Color.White.copy(0.7f),
                modifier = Modifier
                    .size(dimensions.iconSmall)
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
            )

            if (!image.caption.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.4f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text     = image.caption,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 9.sp
                    )
                }
            }
        } else {
            Column(
                modifier            = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🖼️", fontSize = dimensions.iconMedium.value.sp)
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Text(
                    text      = stringResource(Res.string.memory_image_unavailable),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                    textAlign = TextAlign.Center,
                    fontSize  = 9.sp,
                    modifier  = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Full-Screen Image Viewer with Pinch-Zoom
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ImageViewerOverlay(
    image       : MemoryUiImage,
    memoryTitle : String,
    onClose     : () -> Unit
) {
    val dimensions = LocalDimensions.current

    var scale    by remember { mutableStateOf(1f) }
    var offset   by remember { mutableStateOf(Offset.Zero) }
    val minScale = 1f
    val maxScale = 5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.95f))
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    scale  = if (scale > 1.5f) 1f else 2.5f
                    offset = Offset.Zero
                })
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale  = (scale * zoom).coerceIn(minScale, maxScale)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                }
        ) {
            if (image.bytes != null) {
                ByteArrayImage(
                    bytes        = image.bytes,
                    contentScale = ContentScale.Fit,
                    modifier     = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX       = scale,
                            scaleY       = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(0.5f))
                .padding(
                    horizontal = dimensions.spacingSmall,
                    vertical   = dimensions.spacingXSmall
                )
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(dimensions.spacingSmall))
            Text(
                text     = memoryTitle,
                style    = MaterialTheme.typography.titleSmall,
                color    = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text  = "✕2 tap to zoom",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.5f)
            )
        }

        // Caption at bottom
        if (!image.caption.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.6f))
                    .padding(dimensions.spacingMedium)
            ) {
                Text(
                    text      = image.caption,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = Color.White,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Memory Sheet (full-screen form)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AddMemorySheet(
    formState       : AddMemoryFormState,
    babies          : List<BabyResponse>,
    selectedBabyId  : String?,
    onBabySelect    : (String?) -> Unit,
    onTitleChange   : (String) -> Unit,
    onDescChange    : (String) -> Unit,
    onDateChange    : (String) -> Unit,
    onAddImages     : (List<ByteArray>) -> Unit,
    onRemoveImage   : (Int) -> Unit,
    onCaptionChange : (Int, String) -> Unit,
    onSave          : () -> Unit,
    onCancel        : () -> Unit,
    dimensions      : Dimensions
) {
    val customColors = MaterialTheme.customColors

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.memory_add_title),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = customColors.accentGradientStart
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = customColors.accentGradientStart
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(0.12f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            // Baby picker
            FormSection(title = stringResource(Res.string.memory_select_child)) {
                var expanded by remember { mutableStateOf(false) }
                val selectedName = babies.find { it.babyId == selectedBabyId }?.fullName
                    ?: stringResource(Res.string.home_select_child_hint)
                Box {
                    OutlinedButton(
                        onClick  = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
                    ) {
                        Text(selectedName, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded          = expanded,
                        onDismissRequest  = { expanded = false }
                    ) {
                        babies.forEach { baby ->
                            DropdownMenuItem(
                                text    = { Text(baby.fullName) },
                                onClick = { onBabySelect(baby.babyId); expanded = false }
                            )
                        }
                    }
                }
            }

            // Title field
            FormSection(title = stringResource(Res.string.memory_title_label)) {
                MemoryTextField(
                    value         = formState.title,
                    onValueChange = onTitleChange,
                    placeholder   = stringResource(Res.string.memory_title_placeholder),
                    isError       = formState.titleError != null,
                    errorMessage  = formState.titleError,
                    dimensions    = dimensions
                )
            }

            // Date field
            FormSection(title = stringResource(Res.string.memory_date_label)) {
                MemoryTextField(
                    value         = formState.memoryDate,
                    onValueChange = onDateChange,
                    placeholder   = "YYYY-MM-DD",
                    isError       = formState.dateError != null,
                    errorMessage  = formState.dateError,
                    dimensions    = dimensions,
                    trailingIcon  = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint     = customColors.accentGradientStart,
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                    }
                )
            }

            // Description field
            FormSection(title = stringResource(Res.string.memory_desc_label)) {
                MemoryTextField(
                    value         = formState.description,
                    onValueChange = onDescChange,
                    placeholder   = stringResource(Res.string.memory_desc_placeholder),
                    singleLine    = false,
                    minLines      = 3,
                    maxLines      = 6,
                    dimensions    = dimensions
                )
            }

            // Images section
            FormSection(
                title = "${stringResource(Res.string.memory_images_label)} (${formState.imageCount}/${AddMemoryFormState.MAX_IMAGES})"
            ) {
                LocalStorageWarning(dimensions = dimensions)
                Spacer(Modifier.height(dimensions.spacingSmall))

                if (formState.selectedBytes.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(formState.selectedBytes) { index, bytes ->
                            AddImageThumb(
                                bytes     = bytes,
                                index     = index,
                                caption   = formState.captions.getOrNull(index) ?: "",
                                onRemove  = { onRemoveImage(index) },
                                onCaption = { onCaptionChange(index, it) },
                                dimensions = dimensions
                            )
                        }
                    }
                    Spacer(Modifier.height(dimensions.spacingSmall))
                }

                if (formState.canAddMore) {
                    ImagePickerButton(
                        remaining  = AddMemoryFormState.MAX_IMAGES - formState.imageCount,
                        dimensions = dimensions,
                        onPicked   = onAddImages
                    )
                } else {
                    Text(
                        text  = stringResource(Res.string.memory_max_images_reached),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }

            // Error
            formState.errorMessage?.let { err ->
                Text(
                    text  = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Save button
            Spacer(Modifier.height(dimensions.spacingSmall))
            Button(
                onClick  = onSave,
                enabled  = !formState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),
                shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart
                )
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconMedium),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        null,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                    Spacer(Modifier.width(dimensions.spacingSmall))
                    Text(
                        stringResource(Res.string.memory_save),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Cancel
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(stringResource(Res.string.btn_cancel))
            }

            Spacer(Modifier.height(dimensions.spacingXLarge))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Image Thumbnail (in form)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AddImageThumb(
    bytes      : ByteArray,
    index      : Int,
    caption    : String,
    onRemove   : () -> Unit,
    onCaption  : (String) -> Unit,
    dimensions : Dimensions
) {
    val size = dimensions.avatarLarge + dimensions.spacingLarge
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(dimensions.cardCornerRadius - 4.dp))
        ) {
            ByteArrayImage(
                bytes        = bytes,
                contentScale = ContentScale.Crop,
                modifier     = Modifier.fillMaxSize()
            )
            IconButton(
                onClick  = onRemove,
                modifier = Modifier
                    .size(dimensions.iconMedium + dimensions.spacingXSmall)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(0.55f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint     = Color.White,
                    modifier = Modifier.size(dimensions.iconSmall)
                )
            }
        }
    }
}



// ─────────────────────────────────────────────────────────────────────────────
// Local Storage Warning
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LocalStorageWarning(dimensions: Dimensions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.spacingSmall))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(0.5f))
            .padding(dimensions.spacingSmall),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
        verticalAlignment     = Alignment.Top
    ) {
        Text("💾", style = MaterialTheme.typography.bodySmall)
        Text(
            text     = stringResource(Res.string.memory_local_storage_warning),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty Memories Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyMemoriesSection(
    dimensions : Dimensions,
    onAddFirst : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📸", fontSize = dimensions.noBabiesEmojiSize)
        Spacer(Modifier.height(dimensions.spacingMedium))
        Text(
            text       = stringResource(Res.string.memory_no_memories_title),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(dimensions.spacingSmall))
        Text(
            text      = stringResource(Res.string.memory_no_memories_desc),
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onBackground.copy(0.55f)
        )
        Spacer(Modifier.height(dimensions.spacingSmall))
        LocalStorageWarning(dimensions = dimensions)
        Spacer(Modifier.height(dimensions.spacingLarge))
        Button(
            onClick = onAddFirst,
            shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors  = ButtonDefaults.buttonColors(
                containerColor = customColors.accentGradientStart
            )
        ) {
            Text(
                stringResource(Res.string.memory_add_first),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingMemories(dimensions: Dimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.customColors.accentGradientStart)
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                "Loading memories…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form Section wrapper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val dimensions = LocalDimensions.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface.copy(0.7f),
            modifier   = Modifier.padding(bottom = dimensions.spacingXSmall)
        )
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Memory TextField
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemoryTextField(
    value         : String,
    onValueChange : (String) -> Unit,
    placeholder   : String,
    modifier      : Modifier = Modifier,
    isError       : Boolean  = false,
    errorMessage  : String?  = null,
    singleLine    : Boolean  = true,
    minLines      : Int      = 1,
    maxLines      : Int      = 1,
    trailingIcon  : (@Composable () -> Unit)? = null,
    dimensions    : Dimensions
) {
    val customColors = MaterialTheme.customColors
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            },
            singleLine    = singleLine,
            minLines      = minLines,
            maxLines      = maxLines,
            isError       = isError,
            trailingIcon  = trailingIcon,
            shape         = RoundedCornerShape(dimensions.textFieldCornerRadius),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = customColors.accentGradientStart.copy(0.7f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f),
                cursorColor          = customColors.accentGradientStart
            )
        )
        if (isError && errorMessage != null) {
            Text(
                errorMessage,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    start = dimensions.spacingXSmall,
                    top   = dimensions.spacingXSmall
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Baby Chip (landscape sidebar)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BabyChip(
    name       : String,
    selected   : Boolean,
    onClick    : () -> Unit,
    dimensions : Dimensions
) {
    val customColors = MaterialTheme.customColors
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(name, style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth(),
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = customColors.accentGradientStart.copy(0.85f),
            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
            containerColor         = MaterialTheme.colorScheme.surface,
            labelColor             = MaterialTheme.colorScheme.onSurface.copy(0.7f)
        ),
        shape = RoundedCornerShape(dimensions.buttonCornerRadius / 2)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Confirm Delete Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConfirmDeleteDialog(
    onConfirm : () -> Unit,
    onDismiss : () -> Unit
) {
    val dimensions = LocalDimensions.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Text("🗑️", fontSize = MaterialTheme.typography.displaySmall.fontSize) },
        title = {
            Text(
                stringResource(Res.string.memory_delete_confirm_title),
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
        },
        text = {
            Text(
                stringResource(Res.string.memory_delete_confirm_message),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(Res.string.memory_delete_confirm_action),
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onError
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.btn_cancel))
            }
        },
        shape = RoundedCornerShape(dimensions.cardCornerRadius)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Date formatter helper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun formatMemoryDate(dateStr: String): String {
    val parts = dateStr.split("-")
    if (parts.size != 3) return dateStr
    val monthIndex = parts[1].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "", stringResource(Res.string.month_jan), stringResource(Res.string.month_feb),
        stringResource(Res.string.month_mar), stringResource(Res.string.month_apr),
        stringResource(Res.string.month_may), stringResource(Res.string.month_jun),
        stringResource(Res.string.month_jul), stringResource(Res.string.month_aug),
        stringResource(Res.string.month_sep), stringResource(Res.string.month_oct),
        stringResource(Res.string.month_nov), stringResource(Res.string.month_dec)
    )
    val month = monthNames.getOrElse(monthIndex) { parts[1] }
    return "$month ${parts[2]}, ${parts[0]}"
}