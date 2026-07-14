package com.zero.pain.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zero.pain.R
import com.zero.pain.core.Constants
import com.zero.pain.ui.theme.*

@Composable
fun PainSelectionScreen(onPainClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = Constants.SELECTION_TITLE,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-2).sp,
                    lineHeight = 48.sp,
                    brush = Brush.linearGradient(
                        colors = Constants.ACCENT_GRADIENT,
                    ),
                    shadow = Shadow(
                        color = EspressoBrown.copy(alpha = 0.2f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f,
                    ),
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = Constants.SELECTION_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = EspressoBrown.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Constants.PAIN_ZONES.forEach { (id, zone) ->
                key(id) {
                    PainButton(
                        imageRes = zone.fallbackRes,
                        label = zone.label,
                    ) { onPainClick(id) }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
fun PainButton(imageRes: Int, label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "painButton")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    val animatedBorder = Brush.linearGradient(
        colors = Constants.ACCENT_GRADIENT,
        start = Offset(gradientOffset, 0f),
        end = Offset(gradientOffset + 500f, 500f)
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(width = 280.dp, height = 320.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(32.dp),
        color = LightGrey,
        border = BorderStroke(2.dp, animatedBorder),
        shadowElevation = 24.dp, // Prominent Drop Shadow
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.sp
                    ),
                    color = EspressoBrown
                )
            }
        }
    }
}
