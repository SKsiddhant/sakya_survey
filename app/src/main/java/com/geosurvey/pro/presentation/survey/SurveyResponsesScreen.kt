package com.geosurvey.pro.presentation.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.geosurvey.pro.domain.model.QuestionType
import com.geosurvey.pro.domain.model.ResponseStatus
import com.geosurvey.pro.domain.model.SurveyResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyResponsesScreen(
    surveyId: String,
    viewModel: SurveyResponsesViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(surveyId) {
        viewModel.onEvent(SurveyResponsesEvent.LoadResponses(surveyId))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        state.survey?.title?.let { "$it Responses" } ?: "Responses",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.responses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No responses yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.responses) { response ->
                    ResponseItem(
                        response = response,
                        survey = state.survey,
                        onApprove = { viewModel.onEvent(SurveyResponsesEvent.ApproveResponse(response.id)) },
                        onReject = { feedback -> 
                            viewModel.onEvent(SurveyResponsesEvent.RejectResponse(response.id, feedback)) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResponseItem(
    response: SurveyResponse,
    survey: com.geosurvey.pro.domain.model.Survey?,
    onApprove: () -> Unit,
    onReject: (String) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Respondent: ${response.respondentId.take(8)}...",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                StatusBadge(status = response.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Display Answers
            survey?.questions?.forEach { question ->
                val answer = response.answers[question.id]
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (question.type == QuestionType.IMAGE && answer is String) {
                        AsyncImage(
                            model = answer,
                            contentDescription = "Response Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = answer?.toString() ?: "No answer",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (response.status == ResponseStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                    Button(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Flag")
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Flag Response") },
            text = {
                Column {
                    Text("Provide feedback to the user on why this was flagged.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Feedback") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onReject(feedback)
                    showRejectDialog = false
                }) {
                    Text("Flag")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusBadge(status: ResponseStatus) {
    val (containerColor, contentColor, text) = when (status) {
        ResponseStatus.PENDING -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Pending")
        ResponseStatus.APPROVED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Approved")
        ResponseStatus.REJECTED -> Triple(Color(0xFFFEEBEE), Color(0xFFC62828), "Flagged")
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}
