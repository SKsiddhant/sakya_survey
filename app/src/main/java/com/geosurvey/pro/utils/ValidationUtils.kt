package com.geosurvey.pro.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidSurveyTitle(title: String): Boolean {
        return title.isNotBlank() && title.length in 3..100
    }

    fun isValidRadius(radius: Double): Boolean {
        return radius in 50.0..10000.0 // 50m to 10km
    }
}
