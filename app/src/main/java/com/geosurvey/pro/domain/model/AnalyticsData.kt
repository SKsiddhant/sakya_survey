package com.geosurvey.pro.domain.model

data class AnalyticsData(
    val totalSurveys: Int = 0,
    val totalResponses: Int = 0,
    val activeUsers: Int = 0,
    val completionRate: Float = 0f,
    val responsesByDate: Map<Long, Int> = emptyMap(),
    val responsesBySurvey: Map<String, Int> = emptyMap()
)
