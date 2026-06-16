package com.aml_sakr.fitlife.feature.auth.auth_ui.signup

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.shared.AuthCredentialForm
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState

private val SignUpBackground = Color(0xFFF2F6FB)
private val SignUpAccent = Color(0xFF0B6FAE)
private val SignUpPrimaryText = Color(0xFF16243A)
private val SignUpSecondaryText = Color(0xFF587188)
private val SignUpCardBorder = Color(0xFFD6E0EB)
private val SignUpTrustText = Color(0xFF6B7786)

@Composable
internal fun SignUpScreenContent(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    googleSignInEnabled: Boolean,
    isGoogleSignInInProgress: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignUpBackground)
    ) {
        SignUpBackdrop()

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignUpHero()

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, SignUpCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.auth_create_your_account),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SignUpPrimaryText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.auth_sign_up_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SignUpSecondaryText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AuthCredentialForm(
                        state = state,
                        onEvent = onEvent,
                        isSignUp = true,
                        primaryButtonTextResId = R.string.auth_create_account_button,
                        socialDividerTextResId = R.string.auth_register_with,
                        googleButtonTextResId = R.string.auth_continue_with_google,
                        promptTextResId = R.string.auth_already_have_an_account,
                        actionTextResId = R.string.auth_login_button,
                        googleSignInEnabled = googleSignInEnabled,
                        isGoogleSignInInProgress = isGoogleSignInInProgress
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.auth_terms_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = SignUpTrustText,
                modifier = Modifier.widthIn(max = 360.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.widthIn(max = 360.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrustChip(
                    modifier = Modifier.width(160.dp),
                    label = R.string.auth_secure_data
                )
                TrustChip(
                    modifier = Modifier.width(160.dp),
                    label = R.string.auth_ai_powered
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SignUpBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-56).dp, y = 24.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(SignUpAccent.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 56.dp, y = 36.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.56f))
                .border(1.dp, SignUpCardBorder, CircleShape)
        )
    }
}

@Composable
private fun SignUpHero() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = SignUpAccent,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 12.dp
        ) {
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFE7F5FF)
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = (-10).dp, y = (-14).dp)
                        .clip(CircleShape)
                        .background(SignUpAccent.copy(alpha = 0.84f))
                )
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 7.dp)
                        .offset(y = 2.dp)
                        .rotate(22f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(SignUpAccent)
                )
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 5.dp)
                        .offset(x = 14.dp, y = (-10).dp)
                        .rotate(42f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF75D1FF))
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.auth_brand_name),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = SignUpAccent
        )
    }
}

@Composable
private fun TrustChip(
    modifier: Modifier = Modifier,
    label: Int
) {
    Surface(
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SignUpCardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.labelLarge,
                color = SignUpTrustText
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun SignUpScreenPreview() {
    FitnessAppTheme {
        SignUpScreenContent(
            state = AuthState(mode = com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthMode.SignUp),
            onEvent = {},
            googleSignInEnabled = true,
            isGoogleSignInInProgress = false
        )
    }
}
