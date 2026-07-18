package com.zero.pain.model

import androidx.annotation.Keep

sealed class Screen {
    object Selection : Screen()
    
    @Keep
    data class StretchDetailState(
        val painId: String,
        val mainTitle: String,
        val gifUrls: List<String>,
        val instructions: List<String>,
        val pageTitles: List<String>,
        val aspectRatios: List<Float?>,
        val fallbackRes: Int
    ) : Screen()
}
