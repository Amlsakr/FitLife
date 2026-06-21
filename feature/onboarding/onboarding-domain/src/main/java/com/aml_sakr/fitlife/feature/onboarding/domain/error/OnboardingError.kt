package com.aml_sakr.fitlife.feature.onboarding.domain.error

import com.aml_sakr.fitlife.core.domain.DomainError

sealed interface OnboardingError : DomainError {
    data object StorageReadFailure : OnboardingError {
        override val code: String = "onboarding_storage_read_failure"
        override val message: String = "Unable to load your onboarding data."
    }

    data object StorageWriteFailure : OnboardingError {
        override val code: String = "onboarding_storage_write_failure"
        override val message: String = "Unable to save your onboarding data."
    }

    data object InvalidStoredFitnessLevel : OnboardingError {
        override val code: String = "onboarding_invalid_stored_level"
        override val message: String = "Saved fitness level is invalid."
    }

    data object InvalidStoredBeginnerDraft : OnboardingError {
        override val code: String = "onboarding_invalid_stored_beginner_draft"
        override val message: String = "Saved beginner onboarding data is invalid."
    }

    data object InvalidStoredIntermediateDraft : OnboardingError {
        override val code: String = "onboarding_invalid_stored_intermediate_draft"
        override val message: String = "Saved intermediate onboarding data is invalid."
    }

    data object RemoteSyncFailure : OnboardingError {
        override val code: String = "onboarding_remote_sync_failure"
        override val message: String = "Unable to save your profile online."
    }

    data object MissingUserId : OnboardingError {
        override val code: String = "onboarding_missing_user_id"
        override val message: String = "Unable to save your beginner profile right now."
    }
}
