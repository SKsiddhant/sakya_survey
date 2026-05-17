package com.geosurvey.pro.domain.repository

import com.geosurvey.pro.domain.model.LocationData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationUpdates(): Flow<LocationData>
    suspend fun getCurrentLocation(): LocationData?
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float
    suspend fun checkLocationSettings(): Result<Unit>
}
