package com.example.painfree.ui.components

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.painfree.R
import com.example.painfree.core.Constants
import com.example.painfree.ui.theme.*

@Composable
fun SimpleWhatsAppButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "glance")
    val glanceOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glanceAnim"
    )

    val buttonBackground = Brush.linearGradient(
        colors = Constants.ACCENT_GRADIENT
    )

    val glanceBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.3f),
            Color.Transparent,
        ),
        start = Offset(glanceOffset, 0f),
        end = Offset(glanceOffset + 200f, 200f)
    )

    Surface(
        onClick = {
            val url = Constants.WHATSAPP_URL
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        },
        interactionSource = interactionSource,
        modifier = modifier
            .widthIn(max = 280.dp)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier
                .background(buttonBackground)
                .background(glanceBrush)
                .border(
                    BorderStroke(
                        1.dp, 
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                    RoundedCornerShape(28.dp),
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.whatsapp),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = CreamyWhite,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    Constants.WHATSAPP_BUTTON_TEXT,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = CreamyWhite,
                )
            }
        }
    }
}
