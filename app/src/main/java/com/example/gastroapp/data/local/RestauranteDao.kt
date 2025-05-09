package com.example.gastroapp.data.local // O donde tengas tu DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RestauranteDao {

    @Query("SELECT * FROM restaurantes_table")
    fun getAllRestaurantes(): Flow<List<RestauranteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restaurantes: List<RestauranteEntity>)

    @Query("DELETE FROM restaurantes_table")
    suspend fun deleteAll()

    @Transaction
    suspend fun refreshRestaurantes(restaurantes: List<RestauranteEntity>) {
        deleteAll()
        insertAll(restaurantes)
    }
}