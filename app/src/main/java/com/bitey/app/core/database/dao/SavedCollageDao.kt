package com.bitey.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bitey.app.core.database.model.SavedCollageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCollageDao {

    @Query("SELECT * FROM saved_collages ORDER BY createdAt DESC")
    fun getAllCollages(): Flow<List<SavedCollageEntity>>

    @Query("SELECT * FROM saved_collages WHERE collageId = :id LIMIT 1")
    fun getCollageById(id: Long): Flow<SavedCollageEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollage(collage: SavedCollageEntity): Long

    @Delete
    suspend fun deleteCollage(collage: SavedCollageEntity)
}
