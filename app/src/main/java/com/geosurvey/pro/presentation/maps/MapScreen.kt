package com.geosurvey.pro.presentation.maps

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geosurvey.pro.presentation.components.GeoSurveyButton
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    isAdmin: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.all { it }
        viewModel.onEvent(MapEvent.PermissionResult(isGranted))
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }

    // Update camera when user location is found for the first time
    LaunchedEffect(state.userLocation) {
        state.userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude), 15f
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { if (isAdmin) viewModel.onEvent(MapEvent.LocationSelected(it)) },
            properties = MapProperties(isMyLocationEnabled = state.isLocationPermissionGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
        ) {
            if (isAdmin && state.selectedLocation != null) {
                Marker(
                    state = MarkerState(position = state.selectedLocation!!),
                    title = "Survey Center"
                )
                Circle(
                    center = state.selectedLocation!!,
                    radius = state.selectedRadius,
                    fillColor = Color.Blue.copy(alpha = 0.2f),
                    strokeColor = Color.Blue,
                    strokeWidth = 2f
                )
            }
        }

        if (isAdmin) {
            AdminControls(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(16.dp),
                onSave = { viewModel.onEvent(MapEvent.SaveSurveyLocation) },
                onRadiusChange = { viewModel.onEvent(MapEvent.RadiusChanged(it)) },
                currentRadius = state.selectedRadius
            )
        }
    }
}

@Composable
fun AdminControls(
    modifier: Modifier = Modifier,
    onSave: () -> Unit,
    onRadiusChange: (Double) -> Unit,
    currentRadius: Double
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Radius: ${currentRadius.toInt()}m", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = currentRadius.toFloat(),
                onValueChange = { onRadiusChange(it.toDouble()) },
                valueRange = 100f..5000f,
                steps = 49
            )
            GeoSurveyButton(
                text = "Save Survey Location",
                onClick = onSave
            )
        }
    }
}
