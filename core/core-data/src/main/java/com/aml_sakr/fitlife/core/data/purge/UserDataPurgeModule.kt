package com.aml_sakr.fitlife.core.data.purge

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UserDataPurgeModule {
    @Multibinds
    abstract fun userDataPurgeContributors(): Set<UserDataPurgeContributor>
}
