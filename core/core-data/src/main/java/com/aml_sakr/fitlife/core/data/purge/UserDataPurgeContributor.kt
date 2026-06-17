package com.aml_sakr.fitlife.core.data.purge

interface UserDataPurgeContributor {
    suspend fun purgeUserData(userId: String)
}
