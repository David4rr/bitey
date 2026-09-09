package com.bitey.app.core.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.bitey.app.core.database.dao.PlateEntryDao;
import com.bitey.app.core.database.dao.PlateEntryDao_Impl;
import com.bitey.app.core.database.dao.SavedCollageDao;
import com.bitey.app.core.database.dao.SavedCollageDao_Impl;
import com.bitey.app.core.database.dao.TagDao;
import com.bitey.app.core.database.dao.TagDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BiteyDatabase_Impl extends BiteyDatabase {
  private volatile PlateEntryDao _plateEntryDao;

  private volatile TagDao _tagDao;

  private volatile SavedCollageDao _savedCollageDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `plate_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `note` TEXT, `fullImagePath` TEXT NOT NULL, `stickerImagePath` TEXT, `thumbnailPath` TEXT NOT NULL, `isStickerMode` INTEGER NOT NULL, `rating` REAL NOT NULL, `price` REAL, `currency` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `latitude` REAL, `longitude` REAL, `locationName` TEXT, `mealType` TEXT NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_plate_entries_timestamp` ON `plate_entries` (`timestamp`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_plate_entries_isFavorite` ON `plate_entries` (`isFavorite`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`tagId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tagName` TEXT NOT NULL, `category` TEXT)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tags_tagName` ON `tags` (`tagName`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `entry_tag_cross_ref` (`entryId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`entryId`, `tagId`), FOREIGN KEY(`entryId`) REFERENCES `plate_entries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`tagId`) REFERENCES `tags`(`tagId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_entry_tag_cross_ref_tagId` ON `entry_tag_cross_ref` (`tagId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_entry_tag_cross_ref_entryId` ON `entry_tag_cross_ref` (`entryId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `saved_collages` (`collageId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `canvasDataJson` TEXT NOT NULL, `previewImagePath` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4aebde4a019fd26127d3d99501cfcd88')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `plate_entries`");
        db.execSQL("DROP TABLE IF EXISTS `tags`");
        db.execSQL("DROP TABLE IF EXISTS `entry_tag_cross_ref`");
        db.execSQL("DROP TABLE IF EXISTS `saved_collages`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsPlateEntries = new HashMap<String, TableInfo.Column>(16);
        _columnsPlateEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("fullImagePath", new TableInfo.Column("fullImagePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("stickerImagePath", new TableInfo.Column("stickerImagePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("thumbnailPath", new TableInfo.Column("thumbnailPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("isStickerMode", new TableInfo.Column("isStickerMode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("rating", new TableInfo.Column("rating", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("price", new TableInfo.Column("price", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("currency", new TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("locationName", new TableInfo.Column("locationName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlateEntries.put("mealType", new TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlateEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlateEntries = new HashSet<TableInfo.Index>(2);
        _indicesPlateEntries.add(new TableInfo.Index("index_plate_entries_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        _indicesPlateEntries.add(new TableInfo.Index("index_plate_entries_isFavorite", false, Arrays.asList("isFavorite"), Arrays.asList("ASC")));
        final TableInfo _infoPlateEntries = new TableInfo("plate_entries", _columnsPlateEntries, _foreignKeysPlateEntries, _indicesPlateEntries);
        final TableInfo _existingPlateEntries = TableInfo.read(db, "plate_entries");
        if (!_infoPlateEntries.equals(_existingPlateEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "plate_entries(com.bitey.app.core.database.model.PlateEntryEntity).\n"
                  + " Expected:\n" + _infoPlateEntries + "\n"
                  + " Found:\n" + _existingPlateEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsTags = new HashMap<String, TableInfo.Column>(3);
        _columnsTags.put("tagId", new TableInfo.Column("tagId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTags.put("tagName", new TableInfo.Column("tagName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTags.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTags = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTags = new HashSet<TableInfo.Index>(1);
        _indicesTags.add(new TableInfo.Index("index_tags_tagName", true, Arrays.asList("tagName"), Arrays.asList("ASC")));
        final TableInfo _infoTags = new TableInfo("tags", _columnsTags, _foreignKeysTags, _indicesTags);
        final TableInfo _existingTags = TableInfo.read(db, "tags");
        if (!_infoTags.equals(_existingTags)) {
          return new RoomOpenHelper.ValidationResult(false, "tags(com.bitey.app.core.database.model.TagEntity).\n"
                  + " Expected:\n" + _infoTags + "\n"
                  + " Found:\n" + _existingTags);
        }
        final HashMap<String, TableInfo.Column> _columnsEntryTagCrossRef = new HashMap<String, TableInfo.Column>(2);
        _columnsEntryTagCrossRef.put("entryId", new TableInfo.Column("entryId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntryTagCrossRef.put("tagId", new TableInfo.Column("tagId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEntryTagCrossRef = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysEntryTagCrossRef.add(new TableInfo.ForeignKey("plate_entries", "CASCADE", "NO ACTION", Arrays.asList("entryId"), Arrays.asList("id")));
        _foreignKeysEntryTagCrossRef.add(new TableInfo.ForeignKey("tags", "CASCADE", "NO ACTION", Arrays.asList("tagId"), Arrays.asList("tagId")));
        final HashSet<TableInfo.Index> _indicesEntryTagCrossRef = new HashSet<TableInfo.Index>(2);
        _indicesEntryTagCrossRef.add(new TableInfo.Index("index_entry_tag_cross_ref_tagId", false, Arrays.asList("tagId"), Arrays.asList("ASC")));
        _indicesEntryTagCrossRef.add(new TableInfo.Index("index_entry_tag_cross_ref_entryId", false, Arrays.asList("entryId"), Arrays.asList("ASC")));
        final TableInfo _infoEntryTagCrossRef = new TableInfo("entry_tag_cross_ref", _columnsEntryTagCrossRef, _foreignKeysEntryTagCrossRef, _indicesEntryTagCrossRef);
        final TableInfo _existingEntryTagCrossRef = TableInfo.read(db, "entry_tag_cross_ref");
        if (!_infoEntryTagCrossRef.equals(_existingEntryTagCrossRef)) {
          return new RoomOpenHelper.ValidationResult(false, "entry_tag_cross_ref(com.bitey.app.core.database.model.EntryTagCrossRef).\n"
                  + " Expected:\n" + _infoEntryTagCrossRef + "\n"
                  + " Found:\n" + _existingEntryTagCrossRef);
        }
        final HashMap<String, TableInfo.Column> _columnsSavedCollages = new HashMap<String, TableInfo.Column>(5);
        _columnsSavedCollages.put("collageId", new TableInfo.Column("collageId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedCollages.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedCollages.put("canvasDataJson", new TableInfo.Column("canvasDataJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedCollages.put("previewImagePath", new TableInfo.Column("previewImagePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedCollages.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSavedCollages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSavedCollages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavedCollages = new TableInfo("saved_collages", _columnsSavedCollages, _foreignKeysSavedCollages, _indicesSavedCollages);
        final TableInfo _existingSavedCollages = TableInfo.read(db, "saved_collages");
        if (!_infoSavedCollages.equals(_existingSavedCollages)) {
          return new RoomOpenHelper.ValidationResult(false, "saved_collages(com.bitey.app.core.database.model.SavedCollageEntity).\n"
                  + " Expected:\n" + _infoSavedCollages + "\n"
                  + " Found:\n" + _existingSavedCollages);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4aebde4a019fd26127d3d99501cfcd88", "83b8b5c9a29d79860b704b203defc238");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "plate_entries","tags","entry_tag_cross_ref","saved_collages");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `plate_entries`");
      _db.execSQL("DELETE FROM `tags`");
      _db.execSQL("DELETE FROM `entry_tag_cross_ref`");
      _db.execSQL("DELETE FROM `saved_collages`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PlateEntryDao.class, PlateEntryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TagDao.class, TagDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SavedCollageDao.class, SavedCollageDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PlateEntryDao plateEntryDao() {
    if (_plateEntryDao != null) {
      return _plateEntryDao;
    } else {
      synchronized(this) {
        if(_plateEntryDao == null) {
          _plateEntryDao = new PlateEntryDao_Impl(this);
        }
        return _plateEntryDao;
      }
    }
  }

  @Override
  public TagDao tagDao() {
    if (_tagDao != null) {
      return _tagDao;
    } else {
      synchronized(this) {
        if(_tagDao == null) {
          _tagDao = new TagDao_Impl(this);
        }
        return _tagDao;
      }
    }
  }

  @Override
  public SavedCollageDao savedCollageDao() {
    if (_savedCollageDao != null) {
      return _savedCollageDao;
    } else {
      synchronized(this) {
        if(_savedCollageDao == null) {
          _savedCollageDao = new SavedCollageDao_Impl(this);
        }
        return _savedCollageDao;
      }
    }
  }
}
