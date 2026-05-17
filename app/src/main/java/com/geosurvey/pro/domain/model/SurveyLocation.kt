package com.geosurvey.pro.domain.model

data class SurveyLocation(
    val id: String = "",
    val title: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 500.0
)
