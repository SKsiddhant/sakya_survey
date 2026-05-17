package com.geosurvey.pro.presentation.survey.form

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.geosurvey.pro.presentation.components.GeoSurveyButton
import com.geosurvey.pro.utils.rememberSuccessHaptic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun SuccessScreen(
    onDone: () -> Unit
) {
    val haptic = rememberSuccessHaptic()
    val checkProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        haptic()
        checkProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            val color = com.geosurvey.pro.presentation.theme.SakyaRed
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = color.copy(alpha = 0.1f),
                    radius = size.minDimension / 2
                )
                
                // Animated Checkmark path
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.5f)
                    lineTo(size.width * 0.45f, size.height * 0.7f)
                    lineTo(size.width * 0.75f, size.height * 0.3f)
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                    alpha = checkProgress.value
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Submission Successful!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your data has been securely uploaded to Sakya Survey systems.",
            style = MaterialTheme.typography.bodyLarge,
            color = androidx.compose.ui.graphics.Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        GeoSurveyButton(
            text = "Continue",
            onClick = onDone
        )
    }
}
