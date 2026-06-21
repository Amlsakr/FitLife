package com.aml_sakr.fitlife.feature.auth.auth_ui.forgotpassword

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ForgotPasswordScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersTheApprovedResetPasswordLayout() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FitnessAppTheme {
                ForgetPasswordScreen(
                    state = AuthState(),
                    onEvent = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.auth_reset_password_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.auth_reset_password_description)).assertIsDisplayed()
        composeRule.onNodeWithTag("forget_password_email").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.auth_send_reset_link_button)).assertIsDisplayed()
        composeRule.onNodeWithTag("forget_password_back").assertIsDisplayed()
    }

    @Test
    fun loadingStateDisablesPrimaryActionAndKeepsBackLinkEnabled() {
        composeRule.setContent {
            FitnessAppTheme {
                ForgetPasswordScreen(
                    state = AuthState(isLoading = true),
                    onEvent = {}
                )
            }
        }

        composeRule.onNodeWithTag("forget_password_submit").assertIsNotEnabled()
        composeRule.onNodeWithTag("forget_password_back").assertIsEnabled()
    }

    @Test
    fun clickingBackLinkEmitsSignInEvent() {
        val events = mutableListOf<AuthEvent>()

        composeRule.setContent {
            FitnessAppTheme {
                ForgetPasswordScreen(
                    state = AuthState(),
                    onEvent = events::add
                )
            }
        }

        composeRule.onNodeWithTag("forget_password_back").performClick()

        assertEquals(listOf(AuthEvent.ShowSignIn), events)
    }
}
