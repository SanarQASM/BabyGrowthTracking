package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Demo screen showcasing all iOS-style glassmorphic components
 * Inspired by iPhone Control Center design
 */
@Composable
fun GlassmorphicComponentsDemo() {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val scrollState = rememberScrollState()

    // State variables for demo
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedChip by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingLarge)
        ) {
            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // Header
            Text(
                text = "iOS-Style Components",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Glassmorphic design inspired by iPhone Control Center",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // BUTTONS SECTION
            SectionHeader("Buttons")

            // Primary Button
            PrimaryButton(
                text = "Primary Button",
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Primary Button with Loading
            PrimaryButton(
                text = "Loading State",
                onClick = { },
                loading = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Secondary Button
            SecondaryButton(
                text = "Secondary Button",
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Danger Button
            DangerButton(
                text = "Delete Account",
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Text Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppTextButton(
                    text = "Skip",
                    onClick = { }
                )
                AppTextButton(
                    text = "Learn More",
                    onClick = { }
                )
            }

            // Icon Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GradientIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add",
                    onClick = { }
                )
                GradientIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit",
                    onClick = { }
                )
                GradientIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // CHIP BUTTONS SECTION
            SectionHeader("Chip Buttons")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipButton(
                    text = "0-3 months",
                    selected = selectedChip == 0,
                    onClick = { selectedChip = 0 }
                )
                ChipButton(
                    text = "3-6 months",
                    selected = selectedChip == 1,
                    onClick = { selectedChip = 1 }
                )
                ChipButton(
                    text = "6-12 months",
                    selected = selectedChip == 2,
                    onClick = { selectedChip = 2 }
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // TEXT INPUTS SECTION
            SectionHeader("Text Inputs")

            // Standard Glassmorphic TextField
            GlassmorphicTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Enter your email",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )

            // Password TextField
            GlassmorphicTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            // Outlined TextField
            OutlinedGlassmorphicTextField(
                value = email,
                onValueChange = { email = it },
                label = "Alternative Style",
                placeholder = "Outlined glassmorphic field",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )

            // Search TextField
            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else null
            )

            // Multi-line TextField
            MultiLineGlassmorphicTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                placeholder = "Enter your notes here...",
                minLines = 4
            )

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            // Error State Example
            SectionHeader("Error State")

            GlassmorphicTextField(
                value = "invalid@",
                onValueChange = { },
                label = "Email with Error",
                placeholder = "Enter valid email",
                isError = true,
                errorMessage = "Please enter a valid email address",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )

            Spacer(modifier = Modifier.height(dimensions.spacingXLarge))
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { },
                shape = CircleShape,
                containerColor = MaterialTheme.customColors.accentGradientStart,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

// ========== PREVIEWS ==========

//(name = "Phone - Light Mode", widthDp = 360, heightDp = 800)
@Preview
@Composable
fun GlassmorphicComponentsDemoLightPreview() {
    BabyGrowthTheme(darkTheme = false) {
        GlassmorphicComponentsDemo()
    }
}

//(name = "Phone - Dark Mode", widthDp = 360, heightDp = 800)
@Preview
@Composable
fun GlassmorphicComponentsDemoDarkPreview() {
    BabyGrowthTheme(darkTheme = true) {
        GlassmorphicComponentsDemo()
    }
}

//(name = "Tablet - Light", widthDp = 800, heightDp = 1280)
@Preview
@Composable
fun GlassmorphicComponentsTabletPreview() {
    BabyGrowthTheme(darkTheme = false) {
        GlassmorphicComponentsDemo()
    }
}

//(name = "Buttons Only - Light", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun ButtonsPreviewLight() {
    BabyGrowthTheme(darkTheme = false) {
        ButtonsShowcase()
    }
}

//(name = "Buttons Only - Dark", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun ButtonsPreviewDark() {
    BabyGrowthTheme(darkTheme = true) {
        ButtonsShowcase()
    }
}

//(name = "Text Fields Only - Light", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun TextFieldsPreviewLight() {
    BabyGrowthTheme(darkTheme = false) {
        TextFieldsShowcase()
    }
}

//(name = "Text Fields Only - Dark", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun TextFieldsPreviewDark() {
    BabyGrowthTheme(darkTheme = true) {
        TextFieldsShowcase()
    }
}

/**
 * Showcase of all button types
 */
@Composable
fun ButtonsShowcase() {
    val dimensions = LocalDimensions.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingLarge)
        ) {
            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Text(
                text = "iOS-Style Buttons",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Primary Buttons
            Text(
                text = "Primary Buttons",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            PrimaryButton(
                text = "Continue",
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = "Loading...",
                onClick = { },
                loading = true,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = "Disabled",
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Secondary Button
            Text(
                text = "Secondary Buttons",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            SecondaryButton(
                text = "Cancel",
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = "Disabled",
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Danger Button
            Text(
                text = "Danger Buttons",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            DangerButton(
                text = "Delete Account",
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            // Text Buttons
            Text(
                text = "Text Buttons",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppTextButton(text = "Skip", onClick = { })
                AppTextButton(text = "Learn More", onClick = { })
                AppTextButton(text = "Help", onClick = { })
            }

            // Icon Buttons
            Text(
                text = "Icon Buttons",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GradientIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add",
                    onClick = { }
                )
                GradientIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit",
                    onClick = { }
                )
                GradientIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    onClick = { }
                )
                GradientIconButton(
                    icon = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    onClick = { }
                )
            }

            // Chip Buttons
            Text(
                text = "Chip Buttons",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            var selectedChip by remember { mutableStateOf(0) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipButton(
                    text = "0-3 mo",
                    selected = selectedChip == 0,
                    onClick = { selectedChip = 0 }
                )
                ChipButton(
                    text = "3-6 mo",
                    selected = selectedChip == 1,
                    onClick = { selectedChip = 1 }
                )
                ChipButton(
                    text = "6-12 mo",
                    selected = selectedChip == 2,
                    onClick = { selectedChip = 2 }
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }

        // FAB
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { },
                shape = CircleShape,
                containerColor = MaterialTheme.customColors.accentGradientStart,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

/**
 * Showcase of all text field types
 */
@Composable
fun TextFieldsShowcase() {
    val dimensions = LocalDimensions.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingLarge)
        ) {
            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Text(
                text = "iOS-Style Text Fields",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Standard TextField
            Text(
                text = "Standard Glassmorphic",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            GlassmorphicTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Enter your email",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )

            // Password Field
            GlassmorphicTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide" else "Show",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            // Outlined Style
            Text(
                text = "Outlined Glassmorphic",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            OutlinedGlassmorphicTextField(
                value = email,
                onValueChange = { email = it },
                label = "Alternative Style",
                placeholder = "With subtle border",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )

            // Search Field
            Text(
                text = "Search Field",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else null
            )

            // Multi-line Field
            Text(
                text = "Multi-line Field",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            MultiLineGlassmorphicTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                placeholder = "Enter your notes here...",
                minLines = 4
            )

            // Error State
            Text(
                text = "Error State",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingMedium)
            )

            GlassmorphicTextField(
                value = "invalid@",
                onValueChange = { },
                label = "Email with Error",
                placeholder = "Enter valid email",
                isError = true,
                errorMessage = "Please enter a valid email address",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))
        }
    }
}