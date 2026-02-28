package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.AppointmentData
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.AppointmentStatus
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

@Composable
fun BenchTabContent() {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val appointments = remember {
        listOf(
            AppointmentData(
                doctor    = "Dr. Sarah Johnson",
                specialty = "Pediatrics",
                hospital  = "City Children's Hospital",
                dateTime  = "Mon, Feb 24  ·  10:00 AM",
                status    = AppointmentStatus.CONFIRMED
            ),
            AppointmentData(
                doctor    = "Dr. Ahmed Al-Farsi",
                specialty = "Child Neurology",
                hospital  = "Al-Noor Medical Center",
                dateTime  = "Wed, Feb 26  ·  2:30 PM",
                status    = AppointmentStatus.PENDING
            ),
            AppointmentData(
                doctor    = "Dr. Emma Williams",
                specialty = "Vaccination",
                hospital  = "Health & Growth Clinic",
                dateTime  = "Fri, Feb 28  ·  9:00 AM",
                status    = AppointmentStatus.CONFIRMED
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start  = dimensions.screenPadding,
            end    = dimensions.screenPadding,
            top    = dimensions.screenPadding,
            bottom = dimensions.spacingXXLarge
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // ── Hero header ───────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {
                    Surface(
                        shape = RoundedCornerShape(dimensions.cardCornerRadius),
                        color = customColors.accentGradientStart.copy(alpha = 0.15f),
                        modifier = Modifier.size(dimensions.iconXLarge)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(dimensions.iconLarge)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Hospital Bench",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find clinics & manage appointments",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        // ── Quick actions ─────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                BenchActionButton(
                    icon    = Icons.Default.Search,
                    label   = "Find Clinic",
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ }
                )
                BenchActionButton(
                    icon    = Icons.Default.CalendarToday,
                    label   = "Book Appointment",
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ }
                )
            }
        }

        // ── Section title ─────────────────────────────────────────────────
        item {
            Text(
                text = "Upcoming Appointments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = dimensions.spacingSmall)
            )
        }

        // ── Appointment cards ─────────────────────────────────────────────
        items(appointments.size) { index ->
            BenchAppointmentCard(data = appointments[index])
        }
    }
}

@Composable
private fun BenchActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        onClick  = onClick,
        modifier = modifier.height(76.dp),
        shape    = RoundedCornerShape(dimensions.cardCornerRadius),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.spacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = customColors.accentGradientStart,
                modifier = Modifier.size(dimensions.iconMedium)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BenchAppointmentCard(data: AppointmentData) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val statusColor = when (data.status) {
        AppointmentStatus.CONFIRMED -> customColors.success
        AppointmentStatus.PENDING   -> customColors.warning
    }
    val statusLabel = when (data.status) {
        AppointmentStatus.CONFIRMED -> "Confirmed"
        AppointmentStatus.PENDING   -> "Pending"
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            // Icon circle
            Surface(
                shape  = RoundedCornerShape(12.dp),
                color  = customColors.accentGradientStart.copy(alpha = 0.12f),
                modifier = Modifier.size(dimensions.avatarMedium)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = data.doctor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = data.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = data.hospital,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text  = data.dateTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Status chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text  = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}