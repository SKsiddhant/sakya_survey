package com.geosurvey.pro.maps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

import com.geosurvey.pro.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        val event = GeofencingEvent.fromIntent(intent!!) ?: return
        if (event.hasError()) {
            Log.e("GeofenceReceiver", "Geofence event error: ${event.errorCode}")
            return
        }

        val transition = event.geofenceTransition
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = event.triggeringGeofences
            triggeringGeofences?.forEach { geofence ->
                notificationHelper.showNotification(
                    title = "New Survey Nearby!",
                    message = "You've entered a survey zone. Tap to participate.",
                    surveyId = geofence.requestId
                )
            }
        }
    }
}
