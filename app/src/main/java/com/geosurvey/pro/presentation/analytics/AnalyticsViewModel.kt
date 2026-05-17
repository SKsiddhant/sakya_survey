package com.geosurvey.pro.presentation.analytics

import androidx.lifecycle.viewModelScope
import com.geosurvey.pro.domain.model.AnalyticsData
import com.geosurvey.pro.domain.repository.AnalyticsRepository
import com.geosurvey.pro.domain.repository.AuthRepository
import com.geosurvey.pro.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsState(
    val data: AnalyticsData = AnalyticsData(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : BaseViewModel<AnalyticsState, Unit, Unit>(AnalyticsState()) {

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val user = authRepository.currentUser.first()
            user?.let {
                analyticsRepository.getAdminAnalytics(it.id).collect { data ->
                    setState { copy(data = data, isLoading = false) }
                }
            }
        }
    }

    override fun onEvent(event: Unit) {}
}
