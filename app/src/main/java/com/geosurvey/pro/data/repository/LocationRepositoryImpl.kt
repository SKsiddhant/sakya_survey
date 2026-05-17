package com.geosurvey.pro.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.geosurvey.pro.domain.model.LocationData
import com.geosurvey.pro.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    override suspend fun checkLocationSettings(): Result<Unit> {
        return try {
            val request = LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build())
                .build()
            LocationServices.getSettingsClient(context).checkLocationSettings(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<LocationData> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(LocationData(it.latitude, it.longitude, it.accuracy))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, callback, context.mainLooper)
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationData? {
        return try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            location?.let { LocationData(it.latitude, it.longitude, it.accuracy) }
        } catch (e: Exception) {
            null
        }
    }

    override fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}
