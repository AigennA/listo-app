package com.listo.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.LisGreen
import com.listo.app.ui.theme.LisPurple
import com.listo.app.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "logoScale"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF0D0D1A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            // Logo card
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(colors = listOf(LisGreen, LisPurple))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = PoppinsFontFamily
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Listo",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = PoppinsFontFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your shopping, sorted.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = PoppinsFontFamily
            )
        }
    }
}
