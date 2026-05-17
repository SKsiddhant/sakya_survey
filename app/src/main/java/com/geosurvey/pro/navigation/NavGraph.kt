package com.geosurvey.pro.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geosurvey.pro.presentation.auth.AuthEffect
import com.geosurvey.pro.presentation.auth.AuthViewModel
import com.geosurvey.pro.presentation.auth.LoginScreen
import com.geosurvey.pro.presentation.auth.RegisterScreen
import com.geosurvey.pro.presentation.splash.SplashScreen
import com.geosurvey.pro.presentation.dashboard.DashboardScreen
import com.geosurvey.pro.presentation.dashboard.DashboardViewModel
import com.geosurvey.pro.presentation.dashboard.HistoryScreen
import com.geosurvey.pro.presentation.dashboard.ProfileScreen
import com.geosurvey.pro.presentation.survey.CreateSurveyScreen
import com.geosurvey.pro.presentation.survey.SurveyListScreen
import com.geosurvey.pro.presentation.survey.SurveyResponsesScreen
import com.geosurvey.pro.presentation.survey.SurveyResponsesViewModel
import com.geosurvey.pro.presentation.survey.SurveyViewModel
import com.geosurvey.pro.presentation.survey.form.SurveyFormScreen
import com.geosurvey.pro.presentation.survey.form.SurveyFormViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(animationSpec = tween(400)) { it } },
        exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(animationSpec = tween(400)) { -it } },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(animationSpec = tween(400)) { -it } },
        popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(animationSpec = tween(400)) { it } }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        
        composable(Screen.Login.route) {
            LaunchedEffect(Unit) {
                authViewModel.effect.collect { effect ->
                    when (effect) {
                        AuthEffect.NavigateToUserDashboard -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        AuthEffect.NavigateToAdminDashboard -> navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        else -> {}
                    }
                }
            }
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPass = { navController.navigate(Screen.ForgotPassword.route) },
                onSuccess = { /* Handled via effects */ }
            )
        }
        
        composable(Screen.AdminLogin.route) {
            LaunchedEffect(Unit) {
                authViewModel.effect.collect { effect ->
                    when (effect) {
                        AuthEffect.NavigateToAdminDashboard -> navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.AdminLogin.route) { inclusive = true }
                        }
                        AuthEffect.NavigateToUserDashboard -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.AdminLogin.route) { inclusive = true }
                        }
                        else -> {}
                    }
                }
            }
            LoginScreen(
                isAdmin = true,
                viewModel = authViewModel,
                onNavigateToRegister = {},
                onNavigateToForgotPass = { navController.navigate(Screen.ForgotPassword.route) },
                onSuccess = { /* Handled via effects */ }
            )
        }
        
        composable(Screen.Register.route) {
            LaunchedEffect(Unit) {
                authViewModel.effect.collect { effect ->
                    if (effect is AuthEffect.NavigateToUserDashboard) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            }
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Home.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToSurvey = { surveyId ->
                    navController.navigate("survey_form/$surveyId")
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onLogout = {
                authViewModel.onEvent(com.geosurvey.pro.presentation.auth.AuthEvent.Logout)
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        
        composable(Screen.AdminDashboard.route) {
            val surveyViewModel: SurveyViewModel = hiltViewModel()
            SurveyListScreen(
                viewModel = surveyViewModel,
                onNavigateToCreate = { navController.navigate("create_survey") },
                onNavigateToEdit = { surveyId -> navController.navigate("create_survey?id=$surveyId") },
                onNavigateToResponses = { surveyId -> navController.navigate("survey_responses/$surveyId") },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(
            route = "survey_responses/{surveyId}",
            arguments = listOf(navArgument("surveyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId") ?: ""
            val responsesViewModel: SurveyResponsesViewModel = hiltViewModel()
            SurveyResponsesScreen(
                surveyId = surveyId,
                viewModel = responsesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "survey_form/{surveyId}",
            arguments = listOf(navArgument("surveyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId") ?: ""
            val formViewModel: SurveyFormViewModel = hiltViewModel()
            SurveyFormScreen(
                surveyId = surveyId,
                viewModel = formViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "create_survey?id={id}",
            arguments = listOf(navArgument("id") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val surveyViewModel: SurveyViewModel = hiltViewModel()
            CreateSurveyScreen(
                viewModel = surveyViewModel,
                surveyId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("create_survey") {
            val surveyViewModel: SurveyViewModel = hiltViewModel()
            CreateSurveyScreen(
                viewModel = surveyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
