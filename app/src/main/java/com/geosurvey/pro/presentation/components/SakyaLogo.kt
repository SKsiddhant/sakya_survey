package com.geosurvey.pro.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.pro.presentation.theme.SakyaRed
import com.geosurvey.pro.presentation.theme.SakyaTeal

@Composable
fun SakyaLogo(
    modifier: Modifier = Modifier,
    showBackground: Boolean = false
) {
    Box(
        modifier = modifier
            .then(if (showBackground) Modifier.background(Color.Black).padding(16.dp) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sakya",
                color = SakyaRed,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            
            HorizontalDivider(
                modifier = Modifier.width(220.dp),
                thickness = 2.dp,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "SALES AND SOLUTIONS PVT. LTD.",
                color = SakyaTeal,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
