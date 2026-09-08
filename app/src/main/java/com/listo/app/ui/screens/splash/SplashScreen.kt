package com.listo.app.ui.screens.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.ui.components.BrandColors
import com.listo.app.ui.components.OrganicBackground
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.DisplayFontFamily
import com.listo.app.ui.theme.PoppinsFontFamily
import com.listo.app.ui.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 2200L

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var started by remember { mutableStateOf(false) }
    val nextRoute by viewModel.nextRoute.collectAsStateWithLifecycle()

    val markScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "markScale"
    )
    val wordmarkAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 350),
        label = "wordmarkAlpha"
    )
    val wordmarkShift by animateFloatAsState(
        targetValue = if (started) 0f else 24f,
        animationSpec = tween(durationMillis = 600, delayMillis = 350),
        label = "wordmarkShift"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 650),
        label = "taglineAlpha"
    )

    LaunchedEffect(nextRoute) {
        started = true
        // Wait for the stored onboarding flag before deciding where to go.
        val route = nextRoute ?: return@LaunchedEffect
        delay(SPLASH_DURATION_MS)
        navController.navigate(route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    OrganicBackground {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .scale(markScale)
                    .clip(RoundedCornerShape(34.dp))
                    .background(
                        Brush.linearGradient(listOf(BrandColors.Sprout, BrandColors.Forest))
                    ),
                contentAlignment = Alignment.Center
            ) {
                SproutMark(modifier = Modifier.size(60.dp))
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                color = BrandColors.Forest,
                fontFamily = DisplayFontFamily,
                modifier = Modifier
                    .offset(y = wordmarkShift.dp)
                    .alpha(wordmarkAlpha)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = BrandColors.Leaf,
                fontFamily = PoppinsFontFamily,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}

/** A clean two-leaf sprout, drawn in white for the logo badge. */
@Composable
private fun SproutMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stemX = w / 2f
        val stroke = w * 0.09f

        // Stem
        val stem = Path().apply {
            moveTo(stemX, h * 0.95f)
            quadraticBezierTo(stemX - w * 0.02f, h * 0.55f, stemX, h * 0.30f)
        }
        drawPath(stem, color = Color.White, style = Stroke(width = stroke, cap = StrokeCap.Round))

        // Two leaves growing from the stem tip
        drawLeaf(center = Offset(stemX, h * 0.42f), leafSize = w * 0.42f, angleDeg = -38f)
        drawLeaf(center = Offset(stemX, h * 0.34f), leafSize = w * 0.46f, angleDeg = 32f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeaf(
    center: Offset,
    leafSize: Float,
    angleDeg: Float
) {
    rotate(degrees = angleDeg, pivot = center) {
        val leaf = Path().apply {
            moveTo(center.x, center.y)
            quadraticBezierTo(
                center.x + leafSize * 0.55f, center.y - leafSize * 0.55f,
                center.x, center.y - leafSize
            )
            quadraticBezierTo(
                center.x - leafSize * 0.55f, center.y - leafSize * 0.55f,
                center.x, center.y
            )
            close()
        }
        drawPath(leaf, color = Color.White)
    }
}
