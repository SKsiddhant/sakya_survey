package com.geosurvey.pro.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geosurvey.pro.presentation.theme.SakyaRed

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(SakyaRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = SakyaRed, modifier = Modifier.size(64.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Field Agent", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("sakya@solutions.com", color = Color.Gray)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SakyaRed)
            ) {
                Text("LOGOUT", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
