package com.geosurvey.pro.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.pro.domain.model.ResponseStatus
import com.geosurvey.pro.presentation.survey.StatusBadge
import com.geosurvey.pro.presentation.theme.SakyaRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Submission History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (state.userResponses.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No past submissions found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.userResponses) { response ->
                    HistoryCard(response)
                }
            }
        }
    }
}

@Composable
fun HistoryCard(response: com.geosurvey.pro.domain.model.SurveyResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${response.id.take(8)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                com.geosurvey.pro.presentation.survey.StatusBadge(isActive = response.status == ResponseStatus.APPROVED)
                // Wait, StatusBadge in SurveyListScreen takes isActive: Boolean. 
                // Let's use the one in SurveyResponsesScreen or similar.
                // For now, I'll just use a simple one here.
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Submitted on ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(response.timestamp)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            
            if (response.status == ResponseStatus.REJECTED) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Flagged: ${response.adminFeedback}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SakyaRed
                )
            }
        }
    }
}
