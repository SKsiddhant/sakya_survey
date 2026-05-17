package com.geosurvey.pro.presentation.survey.form

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
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
import com.geosurvey.pro.domain.model.SurveyQuestion
import com.geosurvey.pro.presentation.components.GeoSurveyButton
import com.geosurvey.pro.presentation.theme.SakyaRed

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyFormScreen(
    surveyId: String,
    viewModel: SurveyFormViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(surveyId) {
        viewModel.onEvent(SurveyFormEvent.LoadSurvey(surveyId))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isSuccess) {
            SuccessScreen(onDone = onNavigateBack)
        } else if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SakyaRed)
            }
        } else {
            val questions = state.survey?.questions ?: emptyList()
            val pagerState = rememberPagerState(pageCount = { questions.size })
            
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Goted Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    questions.forEachIndexed { index, _ ->
                        val color = if (index <= pagerState.currentPage) SakyaRed else SakyaRed.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = false // Forced progression is more goted for surveys
                ) { page ->
                    val question = questions[page]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Question ${page + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = SakyaRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        QuestionWidget(
                            question = question,
                            answer = state.answers[question.id],
                            onAnswerChanged = { viewModel.onEvent(SurveyFormEvent.AnswerChanged(question.id, it)) }
                        )
                    }
                }
                
                // Goted Bottom Control Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (pagerState.currentPage > 0) {
                            OutlinedButton(
                                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                                modifier = Modifier.height(56.dp).weight(0.4f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back")
                            }
                        }

                        val isLastPage = pagerState.currentPage == (questions.size - 1)
                        GeoSurveyButton(
                            text = if (isLastPage) "Submit Survey" else "Next Question",
                            onClick = {
                                if (isLastPage) {
                                    viewModel.onEvent(SurveyFormEvent.Submit)
                                } else {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isSubmitting
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionWidget(
    question: SurveyQuestion,
    answer: Any?,
    onAnswerChanged: (Any) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (question.isRequired) {
                Text(
                    " *",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                when (question.type) {
                    QuestionType.TEXT -> {
                        OutlinedTextField(
                            value = (answer as? String) ?: "",
                            onValueChange = { onAnswerChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Type your response here...") },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                    QuestionType.MULTIPLE_CHOICE -> {
                        question.options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = answer == option,
                                    onClick = { onAnswerChanged(option) }
                                )
                                Text(
                                    option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    QuestionType.CHECKBOX -> {
                        val selectedOptions = (answer as? List<*>)?.map { it.toString() } ?: emptyList()
                        question.options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedOptions.contains(option),
                                    onCheckedChange = { checked ->
                                        val newList = if (checked) selectedOptions + option else selectedOptions - option
                                        onAnswerChanged(newList)
                                    }
                                )
                                Text(
                                    option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    QuestionType.IMAGE -> {
                        ImagePickerWidget(
                            currentImage = answer,
                            onImageSelected = { onAnswerChanged(it) }
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${question.type.name} implementation coming soon",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePickerWidget(
    currentImage: Any?,
    onImageSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentImage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = currentImage,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Change Image", tint = Color.White)
                }
            }
        } else {
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Image")
            }
        }
    }
}
