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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlateEntryEntity::class,
        TagEntity::class,
        EntryTagCrossRef::class,
        SavedCollageEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(BiteyTypeConverters::class)
abstract class BiteyDatabase : RoomDatabase() {
    abstract fun plateEntryDao(): PlateEntryDao
    abstract fun tagDao(): TagDao
    abstract fun savedCollageDao(): SavedCollageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plate_entries ADD COLUMN extraStickers TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plate_entries ADD COLUMN plateSessionId TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_plate_entries_plateSessionId ON plate_entries(plateSessionId)")
            }
        }
    }
}
