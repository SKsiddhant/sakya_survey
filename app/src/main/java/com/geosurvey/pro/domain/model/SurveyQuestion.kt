package com.geosurvey.pro.domain.model

enum class QuestionType {
    TEXT, MULTIPLE_CHOICE, CHECKBOX, IMAGE, VOICE
}

data class SurveyQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val type: QuestionType = QuestionType.TEXT,
    val options: List<String> = emptyList(),
    val isRequired: Boolean = true
)
