package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationBenchUi
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions

/**
 * Fallback map for iOS / Desktop / Web.
 * Renders a list-style representation of bench markers since native map
 * SDKs require platform-specific setup. Replace each actual with the real
 * SDK when available (MapKit for iOS, Leaflet/web-view for desktop/web).
 */
@Composable
actual fun MapView(
    modifier: Modifier,
    centerLat: Double,
    centerLng: Double,
    markers: List<VaccinationBenchUi>,
    selectedBenchId: String?,
    onMarkerClick: (VaccinationBenchUi) -> Unit
) {
    val dimensions = LocalDimensions.current
    Box(
        modifier = modifier
            .background(Color(0xFFD4E8C2), RoundedCornerShape(dimensions.cardCornerRadius))
    ) {
        // Decorative grid to simulate a map
        Column(
            modifier = Modifier.fillMaxSize().padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Text(
                text = "🗺️ Map View",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4A7A3A),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(markers) { bench ->
                    val isSelected = bench.benchId == selectedBenchId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onMarkerClick(bench) }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏥", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bench.nameEn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = bench.district,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        bench.distanceKm?.let {
                            Text(
                                text = it.toInt().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}