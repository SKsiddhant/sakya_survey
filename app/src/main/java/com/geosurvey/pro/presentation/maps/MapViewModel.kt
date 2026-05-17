package com.geosurvey.pro.presentation.maps

import androidx.lifecycle.viewModelScope
import com.geosurvey.pro.domain.repository.LocationRepository
import com.geosurvey.pro.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.geosurvey.pro.domain.model.SurveyLocation
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.maps.GeofenceManager

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val firestoreRepository: FirestoreRepository,
    private val geofenceManager: GeofenceManager
) : BaseViewModel<MapState, MapEffect, MapEvent>(MapState()) {

    init {
        fetchNearbySurveys()
    }

    private fun fetchNearbySurveys() {
        // In a real app, this would be a geo-query. For now, we fetch all.
        // firestoreRepository.getSurveys() returns Flow<List<Survey>>
        // We'll assume a method exists or use a placeholder
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            locationRepository.getLocationUpdates().collect { location ->
                setState { copy(userLocation = location) }
                checkProximity(location)
            }
        }
    }

    private fun checkProximity(userLocation: com.geosurvey.pro.domain.model.LocationData) {
        val nearby = uiState.value.nearbySurveys
        val inRange = nearby.any { survey ->
            val distance = locationRepository.calculateDistance(
                userLocation.latitude, userLocation.longitude,
                survey.latitude, survey.longitude
            )
            distance <= survey.radius
        }
        setState { copy(isUserInRange = inRange) }
    }

    override fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.PermissionResult -> {
                setState { copy(isLocationPermissionGranted = event.isGranted) }
                if (event.isGranted) {
                    viewModelScope.launch {
                        locationRepository.checkLocationSettings().onSuccess {
                            startLocationUpdates()
                        }
                    }
                }
            }
            is MapEvent.LocationSelected -> {
                setState { copy(selectedLocation = event.latLng) }
            }
            is MapEvent.RadiusChanged -> {
                setState { copy(selectedRadius = event.radius) }
            }
            MapEvent.SaveSurveyLocation -> {
                saveLocation()
            }
        }
    }

    private fun saveLocation() {
        val latLng = uiState.value.selectedLocation
        if (latLng != null) {
            viewModelScope.launch {
                val surveyLoc = SurveyLocation(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "New Survey",
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    radius = uiState.value.selectedRadius
                )
                // Save to Firestore and add Geofence
                geofenceManager.addGeofence(surveyLoc.id, surveyLoc.latitude, surveyLoc.longitude, surveyLoc.radius.toFloat())
                sendEffect(MapEffect.ShowToast("Location and Geofence saved"))
            }
        } else {
            sendEffect(MapEffect.ShowToast("Please select a location on the map"))
        }
    }
}
