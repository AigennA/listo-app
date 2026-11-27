package com.listo.app.di

import android.content.Context
import com.listo.app.domain.spending.PeriodCalendar
import com.listo.app.ui.viewmodel.Clock
import com.listo.app.utils.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)

    /** The device's time zone and week start, read fresh each time a screen asks. */
    @Provides
    fun providePeriodCalendar(): PeriodCalendar = PeriodCalendar()

    @Provides
    fun provideClock(): Clock = Clock { System.currentTimeMillis() }
}
