package com.example.painfree.model

sealed class Screen {
    object Selection : Screen()
    
    data class StretchDetail(
        val painId: String,
        val mainTitle: String,
        val gifUrls: List<String>,
        val instructions: List<String>,
        val pageTitles: List<String>,
        val fallbackRes: Int
    ) : Screen()
}
