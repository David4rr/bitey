package com.bitey.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.bitey.app.core.database.BiteyDatabase
import com.bitey.app.core.database.dao.PlateEntryDao
import com.bitey.app.core.database.dao.SavedCollageDao
import com.bitey.app.core.database.dao.TagDao
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
    fun provideBiteyDatabase(
        @ApplicationContext context: Context
    ): BiteyDatabase {
        return Room.databaseBuilder(
            context,
            BiteyDatabase::class.java,
            "bitey_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePlateEntryDao(database: BiteyDatabase): PlateEntryDao = database.plateEntryDao()

    @Provides
    fun provideTagDao(database: BiteyDatabase): TagDao = database.tagDao()

    @Provides
    fun provideSavedCollageDao(database: BiteyDatabase): SavedCollageDao = database.savedCollageDao()
}
