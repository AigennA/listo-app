package com.listo.app.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.listo.app.R
import com.listo.app.ui.components.BrandColors
import com.listo.app.ui.components.OrganicBackground
import com.listo.app.ui.navigation.Screen
import com.listo.app.ui.theme.PoppinsFontFamily
import com.listo.app.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

private data class PageData(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val accent: Color
)

private val pages = listOf(
    PageData(R.string.onb1_title, R.string.onb1_sub, BrandColors.Sprout),
    PageData(R.string.onb2_title, R.string.onb2_sub, BrandColors.Amber),
    PageData(R.string.onb3_title, R.string.onb3_sub, BrandColors.Coral)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    // Remember that the intro was seen before leaving, so it is shown only once.
    fun goHome() {
        scope.launch {
            viewModel.markOnboardingDone()
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    }

    val accent by animateColorAsState(
        targetValue = pages[pagerState.currentPage].accent,
        animationSpec = tween(400),
        label = "accent"
    )

    OrganicBackground {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            OnboardingPage(page = pages[index])
        }

        TextButton(
            onClick = { goHome() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .statusBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.onb_skip),
                color = BrandColors.Leaf,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp)
                .padding(bottom = 44.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (selected) 28.dp else 9.dp,
                        animationSpec = tween(300),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .height(9.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (selected) accent else BrandColors.Forest.copy(alpha = 0.18f))
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    if (pagerState.currentPage == pages.lastIndex) {
                        goHome()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.lastIndex) stringResource(R.string.onb_start)
                           else stringResource(R.string.onb_next),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: PageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(72.dp))

        BasketIllustration(accent = page.accent, modifier = Modifier.size(248.dp))

        Spacer(Modifier.height(52.dp))

        Text(
            text = stringResource(page.titleRes),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            textAlign = TextAlign.Center,
            color = BrandColors.Forest
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(page.subtitleRes),
            fontFamily = PoppinsFontFamily,
            fontSize = 15.sp,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center,
            color = BrandColors.Ink.copy(alpha = 0.7f)
        )

        Spacer(Modifier.weight(1f))
    }
}

/** An organic basket motif: a soft bowl, a leaf, and a check — tinted per page. */
@Composable
private fun BasketIllustration(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Soft halo
        drawCircle(color = accent.copy(alpha = 0.12f), radius = w * 0.46f, center = Offset(cx, cy))
        drawCircle(color = accent.copy(alpha = 0.10f), radius = w * 0.34f, center = Offset(cx, cy * 0.86f))

        // Bowl / basket
        val bowlTop = cy + h * 0.02f
        val bowlWidth = w * 0.58f
        val bowl = Path().apply {
            moveTo(cx - bowlWidth / 2f, bowlTop)
            quadraticBezierTo(cx, bowlTop + h * 0.30f, cx + bowlWidth / 2f, bowlTop)
            close()
        }
        drawPath(bowl, color = accent)
        // Bowl rim
        drawLine(
            color = BrandColors.Forest,
            start = Offset(cx - bowlWidth / 2f - w * 0.03f, bowlTop),
            end = Offset(cx + bowlWidth / 2f + w * 0.03f, bowlTop),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )

        // A round fruit peeking out
        drawCircle(color = BrandColors.Coral, radius = w * 0.085f, center = Offset(cx - w * 0.12f, bowlTop - h * 0.05f))
        drawCircle(color = BrandColors.Amber, radius = w * 0.07f, center = Offset(cx + w * 0.10f, bowlTop - h * 0.04f))

        // Leaf rising above
        val leafBase = Offset(cx + w * 0.02f, bowlTop - h * 0.06f)
        val leafSize = w * 0.26f
        val leaf = Path().apply {
            moveTo(leafBase.x, leafBase.y)
            quadraticBezierTo(leafBase.x + leafSize * 0.7f, leafBase.y - leafSize, leafBase.x, leafBase.y - leafSize * 1.4f)
            quadraticBezierTo(leafBase.x - leafSize * 0.7f, leafBase.y - leafSize, leafBase.x, leafBase.y)
            close()
        }
        drawPath(leaf, color = BrandColors.Sprout)
        // Leaf vein
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = leafBase,
            end = Offset(leafBase.x, leafBase.y - leafSize * 1.3f),
            strokeWidth = w * 0.012f,
            cap = StrokeCap.Round
        )

        // Check badge
        val badgeCenter = Offset(cx + w * 0.30f, cy + h * 0.06f)
        val badgeRadius = w * 0.11f
        drawCircle(color = BrandColors.Forest, radius = badgeRadius, center = badgeCenter)
        val check = Path().apply {
            moveTo(badgeCenter.x - badgeRadius * 0.45f, badgeCenter.y)
            lineTo(badgeCenter.x - badgeRadius * 0.05f, badgeCenter.y + badgeRadius * 0.4f)
            lineTo(badgeCenter.x + badgeRadius * 0.5f, badgeCenter.y - badgeRadius * 0.4f)
        }
        drawPath(check, color = Color.White, style = Stroke(width = w * 0.03f, cap = StrokeCap.Round))
    }
}
