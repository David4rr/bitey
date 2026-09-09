package com.bitey.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bitey.app.core.database.converter.BiteyTypeConverters
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.SavedCollageDao
import com.bitey.app.core.database.dao.TagDao
import com.bitey.app.core.database.model.EntryTagCrossRef
import com.bitey.app.core.database.model.PlateEntryEntity
import com.bitey.app.core.database.model.SavedCollageEntity
import com.bitey.app.core.database.model.TagEntity

@Database(
    entities = [
        PlateEntryEntity::class,
        TagEntity::class,
        EntryTagCrossRef::class,
        SavedCollageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(BiteyTypeConverters::class)
abstract class BiteyDatabase : RoomDatabase() {
    abstract fun plateEntryDao(): PlateEntryDao
    abstract fun tagDao(): TagDao
    abstract fun savedCollageDao(): SavedCollageDao
}
