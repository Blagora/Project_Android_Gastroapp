package com.example.gastroapp.data.local // O donde esté tu base de datos

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RestauranteEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Asegúrate de registrar tus TypeConverters aquí también
abstract class AppDatabase : RoomDatabase() {
    abstract fun restauranteDao(): RestauranteDao
}