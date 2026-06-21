package com.aml_sakr.fitlife.feature.auth.auth_ui.forgotpassword

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.action.AuthAction
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState
import com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel.AuthViewModel

private val ForgetPasswordBackground = Color(0xFFF0F4F8)
private val ForgetPasswordBackgroundGlow = Color(0xFF96CCFF)
private val ForgetPasswordCard = Color.White
private val ForgetPasswordPrimary = Color(0xFF0B6FAE)
private val ForgetPasswordPrimaryText = Color(0xFF1A1A2E)
private val ForgetPasswordSecondaryText = Color(0xFF546E7A)
private val ForgetPasswordOutline = Color(0xFFCFD8DC)
private val ForgetPasswordSurfaceBorder = Color(0xFFECEFF1)
private val ForgetPasswordPlaceholder = Color(0xFFB7C1CF)

@Composable
fun ForgotPasswordRoute(
    viewModel: AuthViewModel,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessages = remember { mutableStateListOf<SnackbarMessage>() }
    var nextSnackbarMessageId by remember { mutableLongStateOf(0L) }
    val nextSnackbarMessage = snackbarMessages.firstOrNull()
    val snackbarMessage = nextSnackbarMessage?.let { stringResource(it.messageResId) }

    LaunchedEffect(viewModel) {
        viewModel.actions.collect { action ->
            when (action) {
                is AuthAction.ShowMessage ->
                    snackbarMessages.add(
                        SnackbarMessage(
                            id = nextSnackbarMessageId++,
                            messageResId = action.messageResId
                        )
                    )
                AuthAction.NavigateToSignIn -> onNavigateToSignIn()
                is AuthAction.NavigateToAuthenticatedUser -> Unit
                AuthAction.NavigateToOnboarding -> Unit
                AuthAction.LaunchGoogleSignIn -> Unit
            }
        }
    }

    LaunchedEffect(nextSnackbarMessage?.id) {
        val message = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        snackbarMessages.removeAt(0)
    }

    ForgetPasswordScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                AuthEvent.ShowSignIn -> onNavigateToSignIn()
                else -> viewModel.onEvent(event)
            }
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

private data class SnackbarMessage(
    val id: Long,
    val messageResId: Int
)

@Composable
fun ForgetPasswordScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(ForgetPasswordBackground)
        ) {
            ForgetPasswordBackdrop()

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                ForgetPasswordBrandMark()
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.auth_brand_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = ForgetPasswordPrimary
                )

                Spacer(modifier = Modifier.height(48.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = ForgetPasswordCard,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, ForgetPasswordSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_reset_password_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ForgetPasswordPrimaryText
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.auth_reset_password_description),
                            style = MaterialTheme.typography.bodyLarge,
                            color = ForgetPasswordSecondaryText
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = stringResource(R.string.auth_email_address_label),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ForgetPasswordPrimaryText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forget_password_email"),
                            enabled = !state.isLoading,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.auth_email_placeholder),
                                    color = ForgetPasswordPlaceholder
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = ForgetPasswordSecondaryText
                                )
                            },
                            singleLine = true,
                            isError = state.emailErrorResId != null,
                            supportingText = {
                                state.emailErrorResId?.let { errorResId ->
                                    Text(
                                        text = stringResource(errorResId),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForgetPasswordPrimary,
                                unfocusedBorderColor = ForgetPasswordOutline,
                                disabledBorderColor = ForgetPasswordOutline,
                                focusedLeadingIconColor = ForgetPasswordPrimary,
                                unfocusedLeadingIconColor = ForgetPasswordSecondaryText,
                                focusedTextColor = ForgetPasswordPrimaryText,
                                unfocusedTextColor = ForgetPasswordPrimaryText,
                                disabledTextColor = ForgetPasswordPrimaryText,
                                cursorColor = ForgetPasswordPrimary,
                                focusedLabelColor = ForgetPasswordPrimary,
                                unfocusedLabelColor = ForgetPasswordSecondaryText,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorLeadingIconColor = MaterialTheme.colorScheme.error,
                                errorTextColor = MaterialTheme.colorScheme.error,
                                errorCursorColor = MaterialTheme.colorScheme.error
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { onEvent(AuthEvent.ResetPasswordRequested) }
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onEvent(AuthEvent.ResetPasswordRequested) },
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("forget_password_submit"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForgetPasswordPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = ForgetPasswordPrimary.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Text(text = stringResource(R.string.auth_send_reset_link_loading))
                            } else {
                                Text(text = stringResource(R.string.auth_send_reset_link_button))
                                Spacer(modifier = Modifier.size(10.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                TextButton(
                    onClick = { onEvent(AuthEvent.ShowSignIn) },
                    enabled = true,
                    modifier = Modifier.testTag("forget_password_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = ForgetPasswordPrimary
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.auth_back_to_login),
                        color = ForgetPasswordPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Deprecated("Use ForgetPasswordScreen")
@Composable
fun ForgotPasswordScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    ForgetPasswordScreen(
        state = state,
        onEvent = onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
private fun ForgetPasswordBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 14.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ForgetPasswordBackgroundGlow.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 28.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ForgetPasswordPrimary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun ForgetPasswordBrandMark() {
    Surface(
        color = ForgetPasswordPrimary,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ForgetPasswordScreenPreview() {
    FitnessAppTheme {
        ForgetPasswordScreen(
            state = AuthState(),
            onEvent = {}
        )
    }
}
