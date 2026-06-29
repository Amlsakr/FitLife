package com.aml_sakr.fitlife.feature.session.data.pose.di

import com.aml_sakr.fitlife.feature.session.data.pose.AndroidLightSensorProvider
import com.aml_sakr.fitlife.feature.session.data.pose.MlKitPoseDetector
import com.aml_sakr.fitlife.feature.session.domain.pose.ILightSensorProvider
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PoseModule {
    @Binds
    @Singleton
    abstract fun bindPoseDetector(impl: MlKitPoseDetector): PoseDetector

    @Binds
    @Singleton
    abstract fun bindLightSensorProvider(impl: AndroidLightSensorProvider): ILightSensorProvider
}
