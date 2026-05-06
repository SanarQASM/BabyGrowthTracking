package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationBenchUi
/**
 * Platform-agnostic map composable.
 * Renders an interactive map centered at [centerLat]/[centerLng].
 * [markers] are shown as pins; [selectedBenchId] highlights the active marker.
 * Tapping a pin calls [onMarkerClick] with the bench.
 */
@Composable
expect fun MapView(
    modifier: Modifier,
    centerLat: Double,
    centerLng: Double,
    markers: List<VaccinationBenchUi>,
    selectedBenchId: String?,
    onMarkerClick: (VaccinationBenchUi) -> Unit
)
