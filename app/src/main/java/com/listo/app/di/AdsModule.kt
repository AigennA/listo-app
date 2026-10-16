package com.listo.app.di

import android.content.Context
import com.listo.app.ads.AdsSdk
import com.listo.app.ads.ConsentGateway
import com.listo.app.ads.GoogleMobileAdsSdk
import com.listo.app.ads.UmpConsentGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {

    @Provides
    @Singleton
    fun provideConsentGateway(@ApplicationContext context: Context): ConsentGateway =
        UmpConsentGateway(context)

    @Provides
    @Singleton
    fun provideAdsSdk(@ApplicationContext context: Context): AdsSdk =
        GoogleMobileAdsSdk(context)
}
