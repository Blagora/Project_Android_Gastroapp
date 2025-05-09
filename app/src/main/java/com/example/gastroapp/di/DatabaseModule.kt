package com.example.gastroapp.di // O donde tengas tus módulos de Hilt

import android.content.Context
import androidx.room.Room
import com.example.gastroapp.data.local.AppDatabase
import com.example.gastroapp.data.local.RestauranteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "gastro_app_database"
        ).build()
    }

    @Provides
    fun provideRestauranteDao(appDatabase: AppDatabase): RestauranteDao {
        return appDatabase.restauranteDao()
    }
}