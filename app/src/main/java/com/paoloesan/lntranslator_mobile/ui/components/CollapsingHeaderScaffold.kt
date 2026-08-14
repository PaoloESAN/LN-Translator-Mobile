package com.paoloesan.lntranslator_mobile.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.paoloesan.lntranslator_mobile.LocalStrings

@Composable
fun CollapsingHeaderScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val strings = LocalStrings.current

    val topBarHeight = 56.dp
    val headerExpandedExtraHeight = 64.dp
    val collapseRange = 64.dp
    val collapseRangePx = with(density) { collapseRange.toPx() }

    val scrollPx = scrollState.value.toFloat()
    val fraction = if (collapseRangePx > 0f) (scrollPx / collapseRangePx).coerceIn(0f, 1f) else 0f

    val expandedX = 24.dp
    val collapsedX = 56.dp
    val currentX = lerp(expandedX, collapsedX, fraction)

    val expandedY = topBarHeight + 4.dp
    val collapsedY = 14.dp
    val currentY = lerp(expandedY, collapsedY, fraction)

    val currentScale = 1f - (0.24f * fraction)

    val currentXPx = with(density) { currentX.toPx() }
    val currentYPx = with(density) { currentY.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(bottom = 64.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .statusBarsPadding()
                    .height(topBarHeight + headerExpandedExtraHeight)
            )

            content()
        }

        // 2. Solid TopBar background that covers status bar + top bar (fades in on scroll)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fraction)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(topBarHeight)
            ) {
                if (fraction > 0.05f) {
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f * fraction)
                    )
                }
            }
        }

        // 3. Header Controls & Title (statusBarsPadding)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(topBarHeight + headerExpandedExtraHeight)
        ) {
            // Back button at top-left
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 4.dp, top = 4.dp)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.navBack,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Actions slot at top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 4.dp)
                    .height(48.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                actions()
            }

            // Animated Sliding Title (smooth GPU translation & scale)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .graphicsLayer {
                        translationX = currentXPx
                        translationY = currentYPx
                        scaleX = currentScale
                        scaleY = currentScale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    }
            )
        }
    }
}
