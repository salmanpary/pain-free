package com.example.painfree.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.painfree.core.Constants
import com.example.painfree.ui.theme.*
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun NavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "navButton")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "navGradientOffset"
    )

    val glanceOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "navGlance"
    )

    val animatedBorder = Brush.linearGradient(
        colors = Constants.ACCENT_GRADIENT,
        start = Offset(gradientOffset, 0f),
        end = Offset(gradientOffset + 500f, 500f)
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "navScale",
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(
            2.dp,
            animatedBorder,
        ),
    ) {
        val backgroundGradient = Brush.linearGradient(
            colors = listOf(CoralPink.copy(alpha = 0.5f), GoldenYellow.copy(alpha = 0.3f))
        )

        val glanceBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.4f),
                Color.Transparent,
            ),
            start = Offset(glanceOffset, 0f),
            end = Offset(glanceOffset + 100f, 100f)
        )
        
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundGradient).background(glanceBrush),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EspressoBrown,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
