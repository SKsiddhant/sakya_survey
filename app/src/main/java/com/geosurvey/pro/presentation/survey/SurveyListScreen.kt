package com.geosurvey.pro.presentation.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.pro.domain.model.Survey
import com.geosurvey.pro.presentation.components.SurveyCardShimmer
import com.geosurvey.pro.presentation.theme.SakyaRed
import com.geosurvey.pro.presentation.theme.SakyaTeal

import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyListScreen(
    viewModel: SurveyViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToResponses: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Sakya Admin",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        ) 
                    },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = SakyaRed)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Surveys", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Insights", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Survey") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (selectedTab == 0) {
            SurveyTabContent(
                state = state,
                padding = padding,
                onNavigateToCreate = onNavigateToCreate,
                onNavigateToEdit = onNavigateToEdit,
                onViewResponses = onNavigateToResponses,
                onToggle = { id, active -> viewModel.onEvent(SurveyEvent.ToggleStatus(id, active)) },
                onRetry = { /* viewModel.loadSurveys() is private, but onEvent can handle a trigger */ }
            )
        } else {
            AnalyticsTabContent(state = state, padding = padding)
        }
    }
}

@Composable
fun SurveyTabContent(
    state: SurveyState,
    padding: PaddingValues,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onViewResponses: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onRetry: () -> Unit
) {
    if (state.isLoading) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) {
                SurveyCardShimmer()
            }
        }
    } else if (state.error != null) {
        DatabaseErrorState(error = state.error, onRetry = onRetry)
    } else if (state.surveys.isEmpty()) {
        EmptySurveysState(
            modifier = Modifier.padding(padding),
            onAction = onNavigateToCreate
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.surveys) { survey ->
                SurveyCard(
                    survey = survey,
                    onEdit = { onNavigateToEdit(survey.id) },
                    onViewResponses = { onViewResponses(survey.id) },
                    onToggle = { onToggle(survey.id, !survey.isActive) }
                )
            }
        }
    }
}

@Composable
fun DatabaseErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SakyaRed, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Database Connection Error", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Root Cause: $error\n\nPlease ensure your Cloud Firestore database is created in the Firebase Console.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry Connection")
        }
    }
}

@Composable
fun AnalyticsTabContent(state: SurveyState, padding: PaddingValues) {
    val totalSurveys = state.surveys.size
    val totalResponses = state.allResponses.size
    val flaggedCount = state.allResponses.count { it.status == com.geosurvey.pro.domain.model.ResponseStatus.REJECTED }
    
    // Users who uploaded photos
    val photoUsers = state.allResponses.filter { response ->
        response.answers.values.any { it.toString().contains("firebasestorage") }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Sakya Business Intelligence",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Live performance metrics for your projects",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(label = "Surveys", value = totalSurveys.toString(), icon = Icons.AutoMirrored.Filled.Assignment, modifier = Modifier.weight(1f))
                MetricCard(label = "Responses", value = totalResponses.toString(), icon = Icons.AutoMirrored.Filled.TrendingUp, modifier = Modifier.weight(1f))
            }
        }

        item {
            MetricCard(
                label = "Flagged for Re-submission", 
                value = flaggedCount.toString(), 
                icon = Icons.Default.Flag, 
                modifier = Modifier.fillMaxWidth(),
                color = if (flaggedCount > 0) SakyaRed else Color.Gray
            )
        }

        item {
            SectionLabel(title = "PHOTO SUBMISSIONS (${photoUsers.size})", color = SakyaTeal)
        }

        if (photoUsers.isEmpty()) {
            item {
                Text("No photo submissions found yet.", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            items(photoUsers) { response ->
                val surveyName = state.surveys.find { it.id == response.surveyId }?.title ?: "Unknown Survey"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(SakyaTeal.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = SakyaTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "User ID: ${response.respondentId.take(8)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                "Submitted to: $surveyName",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun MetricCard(
    label: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    modifier: Modifier = Modifier,
    color: Color = com.geosurvey.pro.presentation.theme.SakyaRed
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun SectionLabel(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Box(modifier = Modifier.size(width = 4.dp, height = 16.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = Color.Gray
        )
    }
}

@Composable
fun SurveyCard(
    survey: Survey,
    onEdit: () -> Unit,
    onViewResponses: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onEdit
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = survey.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Geofence: ${survey.radius.toInt()}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Switch(
                    checked = survey.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = survey.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(isActive = survey.isActive)
                
                TextButton(onClick = onViewResponses) {
                    Text(
                        text = "VIEW RESPONSES (${survey.questions.size} Qs)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(isActive: Boolean) {
    val containerColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFEEBEE)
    val contentColor = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val text = if (isActive) "Active" else "Inactive"

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

@Composable
fun EmptySurveysState(modifier: Modifier = Modifier, onAction: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Assignment,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No surveys found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Start by creating your first geo-survey.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAction) {
            Text("Create Survey")
        }
    }
}
