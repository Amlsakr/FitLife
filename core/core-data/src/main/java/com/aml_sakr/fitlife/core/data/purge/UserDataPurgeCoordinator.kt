package com.aml_sakr.fitlife.core.data.purge

import javax.inject.Inject

class UserDataPurgeCoordinator @Inject constructor(
    private val contributors: Set<@JvmSuppressWildcards UserDataPurgeContributor>
) {
    suspend fun purgeUserData(userId: String) {
        contributors.forEach { contributor ->
            contributor.purgeUserData(userId)
        }
    }
}
