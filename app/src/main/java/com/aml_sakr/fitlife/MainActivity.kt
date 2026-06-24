package com.aml_sakr.fitlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var authSessionReader: AuthSessionReader

    @Inject
    lateinit var onboardingCompletionReader: OnboardingCompletionReader

    @Inject
    lateinit var onboardingRepository: OnboardingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                FitLifeApp(
                    authRepository = authRepository,
                    authSessionReader = authSessionReader,
                    onboardingCompletionReader = onboardingCompletionReader,
                    onboardingRepository = onboardingRepository,
                    googleClientId = resolveGoogleClientId()
                )
            }
        }
    }

    private fun resolveGoogleClientId(): String {
        val resourceId = resources.getIdentifier(
            "default_web_client_id",
            "string",
            packageName
        )
        return if (resourceId == 0) "" else getString(resourceId)
    }
}
