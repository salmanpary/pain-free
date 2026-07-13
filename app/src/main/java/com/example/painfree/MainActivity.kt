package com.example.painfree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.painfree.core.Constants
import com.example.painfree.data.PainRepository
import com.example.painfree.model.Screen
import com.example.painfree.ui.components.ModernLoadingOverlay
import com.example.painfree.ui.components.SimpleWhatsAppButton
import com.example.painfree.ui.screens.PainSelectionScreen
import com.example.painfree.ui.screens.StretchDetailScreen
import com.example.painfree.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository = PainRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PainFreeTheme {
                val backgroundGradient = remember {
                    Brush.linearGradient(
                        colors = Constants.BACKGROUND_GRADIENT,
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f),
                    )
                }
                
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Selection) }
                val scope = rememberCoroutineScope()
                var isLoading by remember { mutableStateOf(value = false) }

                fun navigateToPain(painId: String, fallbackRes: Int) {
                    isLoading = true
                    scope.launch {
                        try {
                            val data = repository.getPainDetails(painId)
                            currentScreen = Screen.StretchDetailState(
                                painId = painId,
                                mainTitle = data.mainTitle,
                                gifUrls = data.gifUrls,
                                instructions = data.instructions,
                                pageTitles = data.pageTitles,
                                aspectRatios = data.aspectRatios,
                                fallbackRes = fallbackRes,
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            currentScreen = Screen.StretchDetailState(painId, "Recovery", emptyList(), emptyList(), emptyList(), emptyList(), fallbackRes)
                        } finally {
                            isLoading = false
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PeachBg,
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
                        when (val screen = currentScreen) {
                            is Screen.Selection -> PainSelectionScreen { painId ->
                                Constants.PAIN_ZONES[painId]?.let { zone ->
                                    navigateToPain(zone.docId, zone.fallbackRes)
                                }
                            }
                            is Screen.StretchDetailState -> {
                                val defaultInstructions = Constants.DEFAULT_INSTRUCTIONS[screen.painId] 
                                    ?: Constants.RHOMBOID_DEFAULT_INSTRUCTIONS
                                
                                StretchDetailScreen(
                                    mainTitle = screen.mainTitle,
                                    imageUrls = screen.gifUrls,
                                    fallbackRes = screen.fallbackRes,
                                    instructionsList = screen.instructions,
                                    pageTitles = screen.pageTitles,
                                    aspectRatios = screen.aspectRatios,
                                    defaultInstructions = defaultInstructions,
                                ) {
                                    currentScreen = Screen.Selection
                                }
                            }
                        }
                        
                        ModernLoadingOverlay(isLoading = isLoading)

                        SimpleWhatsAppButton(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
                        )
                    }
                }
            }
        }
    }
}
