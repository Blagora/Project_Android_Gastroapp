package com.example.gastroapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RestauranteDao {
    @Query("SELECT * FROM restaurantes")
    fun getAllRestaurantes(): Flow<List<RestauranteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurantes(restaurantes: List<RestauranteEntity>)

    @Query("DELETE FROM restaurantes")
    suspend fun deleteAllRestaurantes()

    @Transaction
    suspend fun refreshRestaurantes(restaurantes: List<RestauranteEntity>) {
        deleteAllRestaurantes()
        insertRestaurantes(restaurantes)
    }
}