package org.example.project.babygrowthtrackingapplication.platform

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationBenchUi

@Composable
actual fun MapView(
    modifier: Modifier,
    centerLat: Double,
    centerLng: Double,
    markers: List<VaccinationBenchUi>,
    selectedBenchId: String?,
    onMarkerClick: (VaccinationBenchUi) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLng), 12f)
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
        markers.forEach { bench ->
            Marker(
                state = MarkerState(LatLng(bench.latitude, bench.longitude)),
                title = bench.nameEn,
                snippet = bench.district,
                onClick = { onMarkerClick(bench); true }
            )
        }
    }
}