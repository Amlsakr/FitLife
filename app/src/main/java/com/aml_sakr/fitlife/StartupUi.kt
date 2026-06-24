package com.aml_sakr.fitlife

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun LoadingBranchDestination(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
internal fun ProtectedDestination(
    title: String,
    onSignOut: suspend () -> Unit,
    onDeleteAccount: suspend () -> Result<Unit, AuthError>,
    sessionStartContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSigningOut by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (isSigningOut) return@Button
                        isSigningOut = true
                        coroutineScope.launch {
                            try {
                                onSignOut()
                            } catch (cancellation: CancellationException) {
                                throw cancellation
                            } catch (_: Throwable) {
                                snackbarHostState.showSnackbar(
                                    "Unable to sign out. Please try again."
                                )
                            } finally {
                                isSigningOut = false
                            }
                        }
                    },
                    enabled = !isSigningOut
                ) {
                    if (isSigningOut) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text("Sign out")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showDeleteAccountDialog = true },
                    enabled = !isSigningOut && !isDeletingAccount
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text("Delete account")
                    }
                }
                if (sessionStartContent != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    sessionStartContent()
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            title = { Text("Delete your account?") },
            text = { Text("This will remove your FitLife account and the data tied to it from the app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isDeletingAccount) return@TextButton
                        showDeleteAccountDialog = false
                        isDeletingAccount = true
                        coroutineScope.launch {
                            try {
                                when (val result = onDeleteAccount()) {
                                    is Result.Success -> {
                                        showDeleteAccountDialog = false
                                    }

                                    is Result.Failure -> {
                                        snackbarHostState.showSnackbar(
                                            when (result.error) {
                                                AuthError.ReauthenticationRequired ->
                                                    "Please sign in again before deleting your account."
                                                else ->
                                                    "Unable to delete account. Please try again."
                                            }
                                        )
                                    }
                                }
                            } catch (cancellation: CancellationException) {
                                throw cancellation
                            } catch (_: Throwable) {
                                snackbarHostState.showSnackbar(
                                    "Unable to delete account. Please try again."
                                )
                            } finally {
                                isDeletingAccount = false
                            }
                        }
                    },
                    enabled = !isDeletingAccount
                ) {
                    Text("Delete permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FitLifeAppPreview() {
    FitnessAppTheme {
        ProtectedDestination(
            title = "Splash preview handled in auth-ui",
            onSignOut = {},
            onDeleteAccount = { Result.Success(Unit) }
        )
    }
}
