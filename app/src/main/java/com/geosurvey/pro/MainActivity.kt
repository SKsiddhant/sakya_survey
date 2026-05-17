package com.geosurvey.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.geosurvey.pro.navigation.NavGraph
import com.geosurvey.pro.presentation.theme.GeoSurveyProTheme
import com.geosurvey.pro.utils.DemoAccountSeeder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.utils.NotificationHelper
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var demoSeeder: DemoAccountSeeder

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Seed demo accounts and start notification listener
        lifecycleScope.launch {
            demoSeeder.createDemoAccounts()
            
            // Notification Observer
            val startTime = System.currentTimeMillis()
            firestoreRepository.getBroadcasts().collect { broadcasts ->
                broadcasts.forEach { data ->
                    val timestamp = data["timestamp"] as? Long ?: 0
                    if (timestamp > startTime) {
                        notificationHelper.showNotification(
                            title = data["title"] as? String ?: "Sakya Survey",
                            message = data["body"] as? String ?: "New update available"
                        )
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            GeoSurveyProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}
