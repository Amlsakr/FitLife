package com.aml_sakr.fitlife.feature.shell.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.home.ui.navigation.HomeDestination
import com.aml_sakr.fitlife.feature.home.ui.navigation.HomeTabHost
import com.aml_sakr.fitlife.feature.profile.ui.navigation.ProfileDestination
import com.aml_sakr.fitlife.feature.profile.ui.navigation.ProfileTabHost
import com.aml_sakr.fitlife.feature.progress.ui.navigation.ProgressDestination
import com.aml_sakr.fitlife.feature.progress.ui.navigation.ProgressTabHost
import com.aml_sakr.fitlife.feature.workout.ui.navigation.WorkoutDestination
import com.aml_sakr.fitlife.feature.workout.ui.navigation.WorkoutTabHost
import kotlinx.serialization.Serializable

@Composable
fun AppShell(
    onSignOut: suspend () -> Unit,
    onDeleteAccount: suspend () -> Result<Unit, AuthError>,
    modifier: Modifier = Modifier
) {
    val selectedTabHolder = rememberSaveable { mutableStateOf(AppShellTab.Home) }
    var selectedTab by selectedTabHolder

    val homeBackStack = rememberTypedNavBackStack<HomeDestination>(HomeDestination.Root)
    val workoutBackStack = rememberTypedNavBackStack<WorkoutDestination>(WorkoutDestination.Root)
    val progressBackStack = rememberTypedNavBackStack<ProgressDestination>(ProgressDestination.Root)
    val profileBackStack = rememberTypedNavBackStack<ProfileDestination>(ProfileDestination.Root)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                AppShellTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = "${tab.label} tab"
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clipToBounds()
        ) {
            HomeTabHost(
                isVisible = selectedTab == AppShellTab.Home,
                backStack = homeBackStack,
                modifier = Modifier.fillMaxSize()
            )
            WorkoutTabHost(
                isVisible = selectedTab == AppShellTab.Workout,
                backStack = workoutBackStack,
                modifier = Modifier.fillMaxSize()
            )
            ProgressTabHost(
                isVisible = selectedTab == AppShellTab.Progress,
                backStack = progressBackStack,
                modifier = Modifier.fillMaxSize()
            )
            ProfileTabHost(
                isVisible = selectedTab == AppShellTab.Profile,
                backStack = profileBackStack,
                onSignOut = onSignOut,
                onDeleteAccount = onDeleteAccount,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@Suppress("UNCHECKED_CAST")
private fun <T : NavKey> rememberTypedNavBackStack(vararg elements: NavKey): NavBackStack<T> {
    return rememberNavBackStack(*elements) as NavBackStack<T>
}

@Serializable
internal enum class AppShellTab {
    Home,
    Workout,
    Progress,
    Profile;

    val label: String
        get() = when (this) {
            Home -> "Home"
            Workout -> "Workout"
            Progress -> "Progress"
            Profile -> "Profile"
        }

    val icon: ImageVector
        get() = when (this) {
            Home -> Icons.Outlined.Home
            Workout -> Icons.Outlined.FitnessCenter
            Progress -> Icons.Outlined.QueryStats
            Profile -> Icons.Outlined.Person
    }
}
