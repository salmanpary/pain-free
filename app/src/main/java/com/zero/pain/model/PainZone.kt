package com.zero.pain.model

import androidx.annotation.Keep

@Keep
data class PainZone(
    val docId: String,
    val label: String,
    val fallbackRes: Int
)
