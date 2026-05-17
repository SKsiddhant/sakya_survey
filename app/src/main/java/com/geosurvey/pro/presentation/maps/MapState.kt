package com.geosurvey.pro.presentation.maps

import com.geosurvey.pro.domain.model.LocationData
import com.google.android.gms.maps.model.LatLng

import com.geosurvey.pro.domain.model.SurveyLocation

data class MapState(
    val userLocation: LocationData? = null,
    val selectedLocation: LatLng? = null,
    val selectedRadius: Double = 500.0,
    val nearbySurveys: List<SurveyLocation> = emptyList(),
    val isLocationPermissionGranted: Boolean = false,
    val isUserInRange: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MapEffect {
    data class ShowToast(val message: String) : MapEffect()
}

sealed class MapEvent {
    data class PermissionResult(val isGranted: Boolean) : MapEvent()
    data class LocationSelected(val latLng: LatLng) : MapEvent()
    data class RadiusChanged(val radius: Double) : MapEvent()
    object SaveSurveyLocation : MapEvent()
}
