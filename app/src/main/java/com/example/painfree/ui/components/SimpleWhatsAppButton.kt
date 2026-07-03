package com.example.painfree.ui.components

import android.content.Intent
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

@Composable
fun SimpleWhatsAppButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val glossyGradient = Brush.verticalGradient(
        colors = if (isPressed) 
            Constants.WHATSAPP_GRADIENT.reversed()
        else 
            Constants.WHATSAPP_GRADIENT,
    )

    Surface(
        onClick = {
            val url = Constants.WHATSAPP_URL
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        },
        interactionSource = interactionSource,
        modifier = modifier
            .widthIn(max = 260.dp)
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = Color.Transparent,
        shadowElevation = if (isPressed) 2.dp else 6.dp,
    ) {
        Box(
            modifier = Modifier
                .background(glossyGradient)
                .border(
                    BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)),
                    RoundedCornerShape(25.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                        ),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    ),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.whatsapp),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    Constants.WHATSAPP_BUTTON_TEXT,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        shadow = Shadow(Color.Black.copy(alpha = 0.3f), Offset(1f, 1f), 3f),
                    ),
                    color = Color.White,
                )
            }
        }
    }
}
