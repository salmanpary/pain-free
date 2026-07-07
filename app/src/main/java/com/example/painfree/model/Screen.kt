package com.example.painfree.model

sealed class Screen {
    object Selection : Screen()
    
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
