package com.bitey.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bitey.app.core.database.model.EntryTagCrossRef
import com.bitey.app.core.database.model.MealType
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.PlateEntryWithTags
import com.bitey.app.core.database.model.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlateEntryDao {

    @Transaction
    @Query("SELECT * FROM plate_entries ORDER BY timestamp DESC")
    fun getAllEntriesWithTags(): Flow<List<PlateEntryWithTags>>

    @Transaction
    @Query("SELECT * FROM plate_entries WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteEntriesWithTags(): Flow<List<PlateEntryWithTags>>

    @Transaction
    @Query("SELECT * FROM plate_entries WHERE mealType = :mealType ORDER BY timestamp DESC")
    fun getEntriesByMealType(mealType: MealType): Flow<List<PlateEntryWithTags>>

    @Transaction
    @Query("SELECT * FROM plate_entries WHERE id = :id LIMIT 1")
    fun getEntryById(id: Long): Flow<PlateEntryWithTags?>

    @Query("SELECT * FROM plate_entries WHERE latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY timestamp DESC")
    fun getEntriesWithLocation(): Flow<List<PlateEntryEntity>>

    @Query("SELECT * FROM plate_entries ORDER BY RANDOM() LIMIT :limit")
    fun getRandomEntries(limit: Int): Flow<List<PlateEntryEntity>>

    @Query("SELECT * FROM plate_entries WHERE isFavorite = 1 ORDER BY RANDOM() LIMIT :limit")
    fun getRandomFavoriteEntries(limit: Int): Flow<List<PlateEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PlateEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: PlateEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: PlateEntryEntity)

    @Query("UPDATE plate_entries SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<EntryTagCrossRef>)

    @Query("DELETE FROM entry_tag_cross_ref WHERE entryId = :entryId")
    suspend fun deleteCrossRefsForEntry(entryId: Long)

    @Transaction
    suspend fun insertEntryWithTags(
        entry: PlateEntryEntity,
        tags: List<TagEntity>,
        tagDao: TagDao
    ): Long {
        val entryId = insertEntry(entry)
        val tagIds = tags.map { tag ->
            val existing = tagDao.getTagByName(tag.tagName)
            existing?.tagId ?: tagDao.insertTag(tag)
        }
        val crossRefs = tagIds.map { tagId -> EntryTagCrossRef(entryId = entryId, tagId = tagId) }
        insertCrossRefs(crossRefs)
        return entryId
    }
}
