package com.geosurvey.pro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.geosurvey.pro.domain.model.Survey

@Entity(tableName = "surveys")
data class SurveyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val active: Boolean,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val jsonContent: String // Simplified: store questions as JSON string
)

fun Survey.toEntity(): SurveyEntity = SurveyEntity(
    id = id,
    title = title,
    description = description,
    active = isActive,
    latitude = latitude,
    longitude = longitude,
    radius = radius,
    jsonContent = "" // TODO: Use Gson to serialize questions
)
