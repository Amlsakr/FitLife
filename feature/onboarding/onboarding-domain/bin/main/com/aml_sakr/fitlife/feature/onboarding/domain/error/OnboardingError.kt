package com.aml_sakr.fitlife.feature.onboarding.domain.error

import com.aml_sakr.fitlife.core.domain.DomainError

sealed interface OnboardingError : DomainError {
    data object StorageReadFailure : OnboardingError {
        override val code: String = "onboarding_storage_read_failure"
        override val message: String = "Unable to load your selected fitness level."
    }

    data object StorageWriteFailure : OnboardingError {
        override val code: String = "onboarding_storage_write_failure"
        override val message: String = "Unable to save your selected fitness level."
    }

    data object InvalidStoredFitnessLevel : OnboardingError {
        override val code: String = "onboarding_invalid_stored_level"
        override val message: String = "Saved fitness level is invalid."
    }
}
