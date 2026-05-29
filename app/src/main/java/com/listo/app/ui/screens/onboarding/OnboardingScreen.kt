package com.listo.app.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.listo.app.ui.components.PrimaryButton
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.*
import kotlinx.coroutines.launch

private data class PageData(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

private val pages = listOf(
    PageData(
        title = "Organize Your Life",
        subtitle = "Create lists, set priorities and keep everything in one clean place.",
        primaryColor = LisGreen,
        secondaryColor = LisGreenLight
    ),
    PageData(
        title = "Track Your Tasks",
        subtitle = "Monitor your progress with beautiful charts and daily summaries.",
        primaryColor = LisPurple,
        secondaryColor = LisPurpleLight
    ),
    PageData(
        title = "Stay Productive",
        subtitle = "Get reminders, set due dates and never miss an important task again.",
        primaryColor = Color(0xFF00B894),
        secondaryColor = Color(0xFFD5F5EE)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    fun navigateToHome() {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        // Skip button
        TextButton(
            onClick = { navigateToHome() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Text(
                text = "Skip",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dots indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { index ->
                    val width by animateDpAsState(
                        targetValue = if (pagerState.currentPage == index) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    pages[index].primaryColor
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Next",
                containerColor = pages[pagerState.currentPage].primaryColor,
                onClick = {
                    if (pagerState.currentPage == pages.lastIndex) {
                        navigateToHome()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: PageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        OnboardingIllustration(
            primaryColor = page.primaryColor,
            secondaryColor = page.secondaryColor,
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun OnboardingIllustration(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Background circle
        drawCircle(
            color = secondaryColor,
            radius = w * 0.42f,
            center = Offset(cx, cy)
        )

        // Stacked card shapes
        val cardW = w * 0.55f
        val cardH = h * 0.12f
        val startX = cx - cardW / 2f

        listOf(0.3f, 0.45f, 0.6f).forEachIndexed { i, yRatio ->
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.15f + i * 0.1f),
                topLeft = Offset(startX, cy * yRatio * 1.4f),
                size = Size(cardW * (1f - i * 0.1f), cardH),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
        }

        // Main white card
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(startX - 8.dp.toPx(), cy - cardH),
            size = Size(cardW + 16.dp.toPx(), cardH * 1.5f),
            cornerRadius = CornerRadius(16.dp.toPx())
        )

        // Check circle
        drawCircle(
            color = primaryColor,
            radius = 12.dp.toPx(),
            center = Offset(startX + 20.dp.toPx(), cy - cardH / 4f)
        )

        // Progress arc background
        drawArc(
            color = primaryColor.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(cx - 30.dp.toPx(), cy + 20.dp.toPx()),
            size = Size(60.dp.toPx(), 60.dp.toPx()),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )

        // Progress arc fill
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(cx - 30.dp.toPx(), cy + 20.dp.toPx()),
            size = Size(60.dp.toPx(), 60.dp.toPx()),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
