package com.geosurvey.pro.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Login : Screen("login")
    object AdminLogin : Screen("admin_login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Map : Screen("map")
    object AdminDashboard : Screen("admin_dashboard")
    object History : Screen("history")
    object Profile : Screen("profile")
}
