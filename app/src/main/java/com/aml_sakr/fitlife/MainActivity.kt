package com.aml_sakr.fitlife

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSession
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.DetermineStartupDestinationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.auth.ui.splash.SplashRoute
import com.aml_sakr.fitlife.feature.auth.ui.splash.SplashViewModel
import com.aml_sakr.fitlife.feature.auth.ui.splash.StartupRouteErrorLogger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                FitLifeApp()
            }
        }
    }
}

@Composable
fun FitLifeApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val startupDestinationUseCase = remember {
        DetermineStartupDestinationUseCase(
            authSessionReader = DefaultAuthSessionReader,
            onboardingCompletionReader = DefaultOnboardingCompletionReader
        )
    }
    val splashViewModel = remember {
        SplashViewModel(
            determineStartupDestination = startupDestinationUseCase::invoke,
            startupRouteErrorLogger = AndroidStartupRouteErrorLogger
        )
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(AppRoute.Splash.route) {
            SplashRoute(
                viewModel = splashViewModel,
                onNavigateToAuth = { navController.navigateFromSplash(AppRoute.Auth) },
                onNavigateToOnboarding = { navController.navigateFromSplash(AppRoute.Onboarding) },
                onNavigateToHome = { navController.navigateFromSplash(AppRoute.Home) }
            )
        }
        composable(AppRoute.Auth.route) {
            PlaceholderDestination("Sign in")
        }
        composable(AppRoute.Onboarding.route) {
            PlaceholderDestination("Onboarding")
        }
        composable(AppRoute.Home.route) {
            PlaceholderDestination("Home")
        }
    }
}

private fun NavHostController.navigateFromSplash(route: AppRoute) {
    navigate(route.route) {
        popUpTo(AppRoute.Splash.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

@Composable
private fun PlaceholderDestination(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

private sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Auth : AppRoute("auth")
    data object Onboarding : AppRoute("onboarding")
    data object Home : AppRoute("home")
}

private object DefaultAuthSessionReader : AuthSessionReader {
    override suspend fun currentSession(): AuthSession? = null
}

private object DefaultOnboardingCompletionReader : OnboardingCompletionReader {
    override suspend fun isOnboardingComplete(userId: String): Boolean = false
}

private object AndroidStartupRouteErrorLogger : StartupRouteErrorLogger {
    override fun logStartupRouteFailure(throwable: Throwable) {
        Log.e("FitLifeStartup", "Unable to determine startup route", throwable)
    }
}

@Preview(showBackground = true)
@Composable
fun FitLifeAppPreview() {
    FitnessAppTheme {
        PlaceholderDestination("Splash preview handled in auth-ui")
    }
}
