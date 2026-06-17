package com.aml_sakr.fitlife.feature.auth.auth_ui.signin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.shared.AuthCredentialForm
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState

private val SignInBackground = Color(0xFFF2F6FB)
private val SignInAccent = Color(0xFF0B6FAE)
private val SignInPrimaryText = Color(0xFF16243A)
private val SignInSecondaryText = Color(0xFF587188)
private val SignInCardBorder = Color(0xFFD6E0EB)
private val SignInTileDeep = Color(0xFF92A1B3)
private val SignInTileLight = Color(0xFFD8E4EF)
private val SignInTileCyan = Color(0xFF9ADCEB)

@Composable
internal fun SignInScreenContent(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    googleSignInEnabled: Boolean,
    isGoogleSignInInProgress: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignInBackground)
    ) {
        SignInBackdrop()

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignInHero()

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, SignInCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.auth_welcome_back),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SignInPrimaryText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.auth_sign_in_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SignInSecondaryText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AuthCredentialForm(
                        state = state,
                        onEvent = onEvent,
                        isSignUp = false,
                        primaryButtonTextResId = R.string.auth_login_button,
                        googleButtonTextResId = R.string.auth_continue_with_google,
                        promptTextResId = R.string.auth_new_to_fitlife,
                        actionTextResId = R.string.auth_register_button,
                        showForgotPasswordAction = true,
                        googleSignInEnabled = googleSignInEnabled,
                        isGoogleSignInInProgress = isGoogleSignInInProgress
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SignInFeatureTile(
                    modifier = Modifier.weight(1f),
                    title = "Training floor",
                    subtitle = "Light-filled studio",
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            SignInTileDeep,
                            SignInTileLight
                        )
                    )
                )
                SignInFeatureTile(
                    modifier = Modifier.weight(1f),
                    title = "Smart metrics",
                    subtitle = "Live watch data",
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            SignInTileCyan,
                            SignInAccent.copy(alpha = 0.72f)
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SignInBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-24).dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(SignInAccent.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-72).dp, y = 40.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.62f))
                .border(1.dp, SignInCardBorder, CircleShape)
        )
    }
}

@Composable
private fun SignInHero() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = SignInAccent,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 12.dp
        ) {
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 7.dp)
                        .rotate(45f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .align(Alignment.Center)
                )
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 7.dp)
                        .rotate((-45).toFloat())
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .align(Alignment.Center)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopStart)
                        .offset(x = 22.dp, y = 22.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-22).dp, y = (-22).dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.auth_brand_name),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = SignInAccent
        )
    }
}

@Composable
private fun SignInFeatureTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    gradient: Brush
) {
    Surface(
        modifier = modifier.height(172.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = 16.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-18).dp, y = 20.dp)
                    .size(96.dp, 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.16f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.84f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun SignInScreenPreview() {
    FitnessAppTheme {
        SignInScreenContent(
            state = AuthState(),
            onEvent = {},
            googleSignInEnabled = true,
            isGoogleSignInInProgress = false
        )
    }
}
