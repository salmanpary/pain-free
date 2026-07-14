package com.zero.pain.core

import com.zero.pain.R
import com.zero.pain.model.PainZone

import com.zero.pain.ui.theme.*

object Constants {
    // Background Colors
    val BACKGROUND_GRADIENT = listOf(PeachBg, PeachBg)
    val ACCENT_GRADIENT = listOf(CoralPink, GoldenYellow)
    val WHATSAPP_GRADIENT = listOf(CoralPink, GoldenYellow)
    val LOADER_COLOR = CoralPink

    // Contact Info
    const val WHATSAPP_URL = "https://wa.me/919605922507?text=Hi,%20I'm%20interested%20in%20learning%20more%20about%20your%20personal%20training%20services."
    const val WHATSAPP_BUTTON_TEXT = "Contact for Personal Training"

    // UI Text
    const val SELECTION_TITLE = "Where's the pain?"
    const val SELECTION_SUBTITLE = "Select a targeted zone to start your\npersonalized recovery path"
    const val LOADING_TITLE = "Personalizing your recovery..."
    const val LOADING_SUBTITLE = "Fetching targeted stretches"
    const val INSTRUCTIONS_HEADER = "Instructions"

    // Firestore Collections
    const val COLLECTION_PAINS = "pains"

    // Mapping of UI ID to Firestore/Resource data
    val PAIN_ZONES = mapOf(
        "piriformis" to PainZone("piriformis_pain", "Piriformis Pain", R.drawable.piriformis),
        "rhomboid" to PainZone("rhomboid_pain", "Rhomboid Pain", R.drawable.rhomboid_pain),
    )

    // Fallback titles used in repository when Firestore data is missing
    val PAIN_FALLBACK_TITLES = mapOf(
        "piriformis_pain" to "Piriformis Pain",
        "rhomboid_pain" to "Rhomboid Pain"
    )

    val DEFAULT_INSTRUCTIONS = mapOf(
        "piriformis" to PIRIFORMIS_DEFAULT_INSTRUCTIONS,
        "rhomboid" to RHOMBOID_DEFAULT_INSTRUCTIONS
    )

    const val PIRIFORMIS_DEFAULT_INSTRUCTIONS = 
        "1. Lie on your back with both knees bent.\n" +
        "2. Cross your affected leg's ankle over the opposite knee.\n" +
        "3. Pull the thigh of the uncrossed leg toward your chest.\n" +
        "4. Hold for 30 seconds and repeat."

    const val RHOMBOID_DEFAULT_INSTRUCTIONS = 
        "1. Stand or sit tall.\n" +
        "2. Interlock your fingers and push your hands away from your chest.\n" +
        "3. Feel the stretch between your shoulder blades.\n" +
        "4. Hold for 30 seconds and repeat."
}
