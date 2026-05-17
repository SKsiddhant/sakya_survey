package com.geosurvey.pro.presentation.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.pro.domain.model.QuestionType
import com.geosurvey.pro.domain.model.SurveyQuestion
import com.geosurvey.pro.presentation.theme.SakyaRed
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

import androidx.compose.ui.platform.LocalContext
import android.location.Geocoder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyScreen(
    viewModel: SurveyViewModel,
    surveyId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(state.currentSurvey.latitude, state.currentSurvey.longitude),
            15f
        )
    }

    val searchLocation = { query: String ->
        if (query.length > 3) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                // Use the older API for compatibility if needed, or the new one
                val addresses = geocoder.getFromLocationName(query, 1)
                if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]
                    val cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: query
                    viewModel.onEvent(
                        SurveyEvent.UpdateLocation(
                            lat = address.latitude,
                            lng = address.longitude,
                            radius = state.currentSurvey.radius,
                            locationName = cityName
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is SurveyEffect.NavigateBack) {
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(state.currentSurvey.questions.size) {
        if (state.currentSurvey.questions.isNotEmpty()) {
            listState.animateScrollToItem(state.currentSurvey.questions.size + 4)
        }
    }

    LaunchedEffect(state.currentSurvey.latitude, state.currentSurvey.longitude) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(state.currentSurvey.latitude, state.currentSurvey.longitude),
            15f
        )
    }

    LaunchedEffect(surveyId) {
        if (surveyId != null) {
            viewModel.onEvent(SurveyEvent.LoadSurvey(surveyId))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (surveyId == null) "Create Survey" else "Edit Survey",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        onClick = { viewModel.onEvent(SurveyEvent.SaveSurvey) },
                        color = SakyaRed,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            "SAVE", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    viewModel.onEvent(SurveyEvent.AddQuestion)
                },
                containerColor = SakyaRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Question", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SectionHeader(title = "GENERAL INFO", icon = Icons.Default.Info)
            }
            
            item {
                GotedInputCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GotedTextField(
                            value = state.currentSurvey.title,
                            onValueChange = { viewModel.onEvent(SurveyEvent.UpdateTitle(it)) },
                            label = "Survey Title",
                            placeholder = "Enter project name..."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GotedTextField(
                            value = state.currentSurvey.description,
                            onValueChange = { viewModel.onEvent(SurveyEvent.UpdateDescription(it)) },
                            label = "Description",
                            placeholder = "Project details...",
                            singleLine = false,
                            minLines = 3
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "GEOFENCING", icon = Icons.Default.LocationOn)
            }

            item {
                GotedInputCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GotedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    searchLocation(it)
                                },
                                label = "Search Location (e.g. City Name)",
                                placeholder = "Search Jabalpur, Delhi...",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(SurveyEvent.UseCurrentLocation)
                                },
                                modifier = Modifier.padding(top = 24.dp).background(SakyaRed.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Current Location", tint = SakyaRed)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp))) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(isMyLocationEnabled = true),
                                uiSettings = MapUiSettings(myLocationButtonEnabled = true),
                                onMapClick = { latLng ->
                                    viewModel.onEvent(SurveyEvent.UpdateLocation(latLng.latitude, latLng.longitude, state.currentSurvey.radius))
                                }
                            ) {
                                Marker(
                                    state = rememberMarkerState(position = LatLng(state.currentSurvey.latitude, state.currentSurvey.longitude)),
                                    title = "Target Area"
                                )
                                Circle(
                                    center = LatLng(state.currentSurvey.latitude, state.currentSurvey.longitude),
                                    radius = state.currentSurvey.radius,
                                    fillColor = SakyaRed.copy(alpha = 0.15f),
                                    strokeColor = SakyaRed,
                                    strokeWidth = 2f
                                )
                            }
                            
                            // Map Key Overlay Warning
                            if (state.currentSurvey.latitude == 0.0) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                                    Text("Map loading... Ensure API Key is set.", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GotedTextField(
                                value = state.currentSurvey.latitude.toString(),
                                onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.onEvent(SurveyEvent.UpdateLocation(v, state.currentSurvey.longitude, state.currentSurvey.radius)) } },
                                label = "Lat",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            GotedTextField(
                                value = state.currentSurvey.longitude.toString(),
                                onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.onEvent(SurveyEvent.UpdateLocation(state.currentSurvey.latitude, v, state.currentSurvey.radius)) } },
                                label = "Lng",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        GotedTextField(
                            value = state.currentSurvey.radius.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.onEvent(SurveyEvent.UpdateLocation(state.currentSurvey.latitude, state.currentSurvey.longitude, v)) } },
                            label = "Radius (Meters)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
            
            item {
                SectionHeader(title = "QUESTIONNAIRE", icon = Icons.Default.QuestionAnswer)
            }
            
            if (state.currentSurvey.questions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No questions yet.", color = Color.Gray)
                            TextButton(onClick = { viewModel.onEvent(SurveyEvent.AddQuestion) }) {
                                Text("Click to add your first question", color = SakyaRed)
                            }
                        }
                    }
                }
            }

            items(
                items = state.currentSurvey.questions,
                key = { it.id }
            ) { question ->
                QuestionItem(
                    question = question,
                    onUpdate = { viewModel.onEvent(SurveyEvent.UpdateQuestion(it)) },
                    onRemove = { viewModel.onEvent(SurveyEvent.RemoveQuestion(question.id)) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun GotedInputCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF121212),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        content = content
    )
}

@Composable
fun GotedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.DarkGray) },
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = SakyaRed,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            )
        )
    }
}

@Composable
fun QuestionItem(
    question: SurveyQuestion,
    onUpdate: (SurveyQuestion) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF181818),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                GotedTextField(
                    value = question.text,
                    onValueChange = { onUpdate(question.copy(text = it)) },
                    label = "Question Text",
                    placeholder = "What would you like to ask?",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.padding(top = 24.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = SakyaRed, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("RESPONSE TYPE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    QuestionTypeDropdown(
                        selectedType = question.type,
                        onTypeSelected = { onUpdate(question.copy(type = it)) }
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("REQUIRED", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = question.isRequired,
                        onCheckedChange = { onUpdate(question.copy(isRequired = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = SakyaRed)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTypeDropdown(
    selectedType: QuestionType,
    onTypeSelected: (QuestionType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            onClick = { expanded = true },
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedType.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1E1E1E))
        ) {
            QuestionType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name, color = Color.White) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = SakyaRed
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = SakyaRed
        )
    }
}
