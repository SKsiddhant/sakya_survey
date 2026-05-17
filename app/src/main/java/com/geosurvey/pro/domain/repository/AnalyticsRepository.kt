package com.geosurvey.pro.domain.repository

import com.geosurvey.pro.domain.model.AnalyticsData
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getAdminAnalytics(adminId: String): Flow<AnalyticsData>
}
