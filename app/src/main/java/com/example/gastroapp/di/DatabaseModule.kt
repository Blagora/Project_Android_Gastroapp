package com.example.gastroapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gastroapp-db"
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Eliminar configuraciones PRAGMA no recomendadas
                // db.execSQL("PRAGMA journal_mode = WAL")
                // db.execSQL("PRAGMA synchronous = NORMAL")
            }
        })
        .fallbackToDestructiveMigration()
        .allowMainThreadQueries() // Solo para desarrollo, remover en producción
        .build()
    }

    @Provides
    fun provideRestauranteDao(database: AppDatabase): RestauranteDao {
        return database.restauranteDao()
    }
}