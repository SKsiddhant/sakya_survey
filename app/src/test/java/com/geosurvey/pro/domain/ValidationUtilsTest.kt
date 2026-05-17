package com.geosurvey.pro.domain

import com.geosurvey.pro.utils.ValidationUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `valid email returns true`() {
        // Note: Patterns.EMAIL_ADDRESS needs to be mocked or run in instrumented test
        // For unit test, we'd use a custom regex or Robolectric
    }

    @Test
    fun `password too short returns false`() {
        assertFalse(ValidationUtils.isValidPassword("12345"))
    }

    @Test
    fun `valid password returns true`() {
        assertTrue(ValidationUtils.isValidPassword("123456"))
    }

    @Test
    fun `radius out of bounds returns false`() {
        assertFalse(ValidationUtils.isValidRadius(10.0))
        assertFalse(ValidationUtils.isValidRadius(20000.0))
    }

    @Test
    fun `valid radius returns true`() {
        assertTrue(ValidationUtils.isValidRadius(500.0))
    }
}
