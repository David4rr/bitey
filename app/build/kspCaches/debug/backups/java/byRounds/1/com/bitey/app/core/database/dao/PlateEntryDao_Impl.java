package com.bitey.app.core.database.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.bitey.app.core.database.converter.BiteyTypeConverters;
import com.bitey.app.core.database.model.EntryTagCrossRef;
import com.bitey.app.core.database.model.MealType;
import com.bitey.app.core.database.model.PlateEntryEntity;
import com.bitey.app.core.database.model.PlateEntryWithTags;
import com.bitey.app.core.database.model.TagEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlateEntryDao_Impl implements PlateEntryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlateEntryEntity> __insertionAdapterOfPlateEntryEntity;

  private final BiteyTypeConverters __biteyTypeConverters = new BiteyTypeConverters();

  private final EntityInsertionAdapter<EntryTagCrossRef> __insertionAdapterOfEntryTagCrossRef;

  private final EntityDeletionOrUpdateAdapter<PlateEntryEntity> __deletionAdapterOfPlateEntryEntity;

  private final EntityDeletionOrUpdateAdapter<PlateEntryEntity> __updateAdapterOfPlateEntryEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateFavoriteStatus;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCrossRefsForEntry;

  public PlateEntryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlateEntryEntity = new EntityInsertionAdapter<PlateEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `plate_entries` (`id`,`title`,`note`,`fullImagePath`,`stickerImagePath`,`thumbnailPath`,`isStickerMode`,`rating`,`price`,`currency`,`isFavorite`,`timestamp`,`latitude`,`longitude`,`locationName`,`mealType`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlateEntryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getNote() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getNote());
        }
        statement.bindString(4, entity.getFullImagePath());
        if (entity.getStickerImagePath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getStickerImagePath());
        }
        statement.bindString(6, entity.getThumbnailPath());
        final int _tmp = entity.isStickerMode() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindDouble(8, entity.getRating());
        if (entity.getPrice() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getPrice());
        }
        statement.bindString(10, entity.getCurrency());
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getTimestamp());
        if (entity.getLatitude() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(14);
        } else {
          statement.bindDouble(14, entity.getLongitude());
        }
        if (entity.getLocationName() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getLocationName());
        }
        final String _tmp_2 = __biteyTypeConverters.fromMealType(entity.getMealType());
        statement.bindString(16, _tmp_2);
      }
    };
    this.__insertionAdapterOfEntryTagCrossRef = new EntityInsertionAdapter<EntryTagCrossRef>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `entry_tag_cross_ref` (`entryId`,`tagId`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EntryTagCrossRef entity) {
        statement.bindLong(1, entity.getEntryId());
        statement.bindLong(2, entity.getTagId());
      }
    };
    this.__deletionAdapterOfPlateEntryEntity = new EntityDeletionOrUpdateAdapter<PlateEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `plate_entries` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlateEntryEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPlateEntryEntity = new EntityDeletionOrUpdateAdapter<PlateEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `plate_entries` SET `id` = ?,`title` = ?,`note` = ?,`fullImagePath` = ?,`stickerImagePath` = ?,`thumbnailPath` = ?,`isStickerMode` = ?,`rating` = ?,`price` = ?,`currency` = ?,`isFavorite` = ?,`timestamp` = ?,`latitude` = ?,`longitude` = ?,`locationName` = ?,`mealType` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlateEntryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getNote() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getNote());
        }
        statement.bindString(4, entity.getFullImagePath());
        if (entity.getStickerImagePath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getStickerImagePath());
        }
        statement.bindString(6, entity.getThumbnailPath());
        final int _tmp = entity.isStickerMode() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindDouble(8, entity.getRating());
        if (entity.getPrice() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getPrice());
        }
        statement.bindString(10, entity.getCurrency());
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getTimestamp());
        if (entity.getLatitude() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(14);
        } else {
          statement.bindDouble(14, entity.getLongitude());
        }
        if (entity.getLocationName() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getLocationName());
        }
        final String _tmp_2 = __biteyTypeConverters.fromMealType(entity.getMealType());
        statement.bindString(16, _tmp_2);
        statement.bindLong(17, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateFavoriteStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE plate_entries SET isFavorite = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteCrossRefsForEntry = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM entry_tag_cross_ref WHERE entryId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertEntry(final PlateEntryEntity entry,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPlateEntryEntity.insertAndReturnId(entry);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCrossRefs(final List<EntryTagCrossRef> crossRefs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEntryTagCrossRef.insert(crossRefs);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEntry(final PlateEntryEntity entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPlateEntryEntity.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateEntry(final PlateEntryEntity entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPlateEntryEntity.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertEntryWithTags(final PlateEntryEntity entry, final List<TagEntity> tags,
      final TagDao tagDao, final Continuation<? super Long> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> PlateEntryDao.DefaultImpls.insertEntryWithTags(PlateEntryDao_Impl.this, entry, tags, tagDao, __cont), $completion);
  }

  @Override
  public Object updateFavoriteStatus(final long id, final boolean isFavorite,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateFavoriteStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isFavorite ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateFavoriteStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCrossRefsForEntry(final long entryId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCrossRefsForEntry.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, entryId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCrossRefsForEntry.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlateEntryWithTags>> getAllEntriesWithTags() {
    final String _sql = "SELECT * FROM plate_entries ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"entry_tag_cross_ref", "tags",
        "plate_entries"}, new Callable<List<PlateEntryWithTags>>() {
      @Override
      @NonNull
      public List<PlateEntryWithTags> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
            final int _cursorIndexOfFullImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "fullImagePath");
            final int _cursorIndexOfStickerImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "stickerImagePath");
            final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
            final int _cursorIndexOfIsStickerMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isStickerMode");
            final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
            final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
            final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
            final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
            final LongSparseArray<ArrayList<TagEntity>> _collectionTags = new LongSparseArray<ArrayList<TagEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionTags.containsKey(_tmpKey)) {
                _collectionTags.put(_tmpKey, new ArrayList<TagEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshiptagsAscomBiteyAppCoreDatabaseModelTagEntity(_collectionTags);
            final List<PlateEntryWithTags> _result = new ArrayList<PlateEntryWithTags>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final PlateEntryWithTags _item;
              final PlateEntryEntity _tmpEntry;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpNote;
              if (_cursor.isNull(_cursorIndexOfNote)) {
                _tmpNote = null;
              } else {
                _tmpNote = _cursor.getString(_cursorIndexOfNote);
              }
              final String _tmpFullImagePath;
              _tmpFullImagePath = _cursor.getString(_cursorIndexOfFullImagePath);
              final String _tmpStickerImagePath;
              if (_cursor.isNull(_cursorIndexOfStickerImagePath)) {
                _tmpStickerImagePath = null;
              } else {
                _tmpStickerImagePath = _cursor.getString(_cursorIndexOfStickerImagePath);
              }
              final String _tmpThumbnailPath;
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
              final boolean _tmpIsStickerMode;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsStickerMode);
              _tmpIsStickerMode = _tmp != 0;
              final float _tmpRating;
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
              final Double _tmpPrice;
              if (_cursor.isNull(_cursorIndexOfPrice)) {
                _tmpPrice = null;
              } else {
                _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
              }
              final String _tmpCurrency;
              _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
              final boolean _tmpIsFavorite;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
              _tmpIsFavorite = _tmp_1 != 0;
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final Double _tmpLatitude;
              if (_cursor.isNull(_cursorIndexOfLatitude)) {
                _tmpLatitude = null;
              } else {
                _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
              }
              final Double _tmpLongitude;
              if (_cursor.isNull(_cursorIndexOfLongitude)) {
                _tmpLongitude = null;
              } else {
                _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
              }
              final String _tmpLocationName;
              if (_cursor.isNull(_cursorIndexOfLocationName)) {
                _tmpLocationName = null;
              } else {
                _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
              }
              final MealType _tmpMealType;
              final String _tmp_2;
              _tmp_2 = _cursor.getString(_cursorIndexOfMealType);
              _tmpMealType = __biteyTypeConverters.toMealType(_tmp_2);
              _tmpEntry = new PlateEntryEntity(_tmpId,_tmpTitle,_tmpNote,_tmpFullImagePath,_tmpStickerImagePath,_tmpThumbnailPath,_tmpIsStickerMode,_tmpRating,_tmpPrice,_tmpCurrency,_tmpIsFavorite,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationName,_tmpMealType);
              final ArrayList<TagEntity> _tmpTagsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpTagsCollection = _collectionTags.get(_tmpKey_1);
              _item = new PlateEntryWithTags(_tmpEntry,_tmpTagsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PlateEntryWithTags>> getFavoriteEntriesWithTags() {
    final String _sql = "SELECT * FROM plate_entries WHERE isFavorite = 1 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"entry_tag_cross_ref", "tags",
        "plate_entries"}, new Callable<List<PlateEntryWithTags>>() {
      @Override
      @NonNull
      public List<PlateEntryWithTags> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
            final int _cursorIndexOfFullImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "fullImagePath");
            final int _cursorIndexOfStickerImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "stickerImagePath");
            final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
            final int _cursorIndexOfIsStickerMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isStickerMode");
            final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
            final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
            final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
            final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
            final LongSparseArray<ArrayList<TagEntity>> _collectionTags = new LongSparseArray<ArrayList<TagEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionTags.containsKey(_tmpKey)) {
                _collectionTags.put(_tmpKey, new ArrayList<TagEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshiptagsAscomBiteyAppCoreDatabaseModelTagEntity(_collectionTags);
            final List<PlateEntryWithTags> _result = new ArrayList<PlateEntryWithTags>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final PlateEntryWithTags _item;
              final PlateEntryEntity _tmpEntry;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpNote;
              if (_cursor.isNull(_cursorIndexOfNote)) {
                _tmpNote = null;
              } else {
                _tmpNote = _cursor.getString(_cursorIndexOfNote);
              }
              final String _tmpFullImagePath;
              _tmpFullImagePath = _cursor.getString(_cursorIndexOfFullImagePath);
              final String _tmpStickerImagePath;
              if (_cursor.isNull(_cursorIndexOfStickerImagePath)) {
                _tmpStickerImagePath = null;
              } else {
                _tmpStickerImagePath = _cursor.getString(_cursorIndexOfStickerImagePath);
              }
              final String _tmpThumbnailPath;
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
              final boolean _tmpIsStickerMode;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsStickerMode);
              _tmpIsStickerMode = _tmp != 0;
              final float _tmpRating;
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
              final Double _tmpPrice;
              if (_cursor.isNull(_cursorIndexOfPrice)) {
                _tmpPrice = null;
              } else {
                _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
              }
              final String _tmpCurrency;
              _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
              final boolean _tmpIsFavorite;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
              _tmpIsFavorite = _tmp_1 != 0;
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final Double _tmpLatitude;
              if (_cursor.isNull(_cursorIndexOfLatitude)) {
                _tmpLatitude = null;
              } else {
                _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
              }
              final Double _tmpLongitude;
              if (_cursor.isNull(_cursorIndexOfLongitude)) {
                _tmpLongitude = null;
              } else {
                _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
              }
              final String _tmpLocationName;
              if (_cursor.isNull(_cursorIndexOfLocationName)) {
                _tmpLocationName = null;
              } else {
                _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
              }
              final MealType _tmpMealType;
              final String _tmp_2;
              _tmp_2 = _cursor.getString(_cursorIndexOfMealType);
              _tmpMealType = __biteyTypeConverters.toMealType(_tmp_2);
              _tmpEntry = new PlateEntryEntity(_tmpId,_tmpTitle,_tmpNote,_tmpFullImagePath,_tmpStickerImagePath,_tmpThumbnailPath,_tmpIsStickerMode,_tmpRating,_tmpPrice,_tmpCurrency,_tmpIsFavorite,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationName,_tmpMealType);
              final ArrayList<TagEntity> _tmpTagsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpTagsCollection = _collectionTags.get(_tmpKey_1);
              _item = new PlateEntryWithTags(_tmpEntry,_tmpTagsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PlateEntryWithTags>> getEntriesByMealType(final MealType mealType) {
    final String _sql = "SELECT * FROM plate_entries WHERE mealType = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __biteyTypeConverters.fromMealType(mealType);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"entry_tag_cross_ref", "tags",
        "plate_entries"}, new Callable<List<PlateEntryWithTags>>() {
      @Override
      @NonNull
      public List<PlateEntryWithTags> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
            final int _cursorIndexOfFullImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "fullImagePath");
            final int _cursorIndexOfStickerImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "stickerImagePath");
            final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
            final int _cursorIndexOfIsStickerMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isStickerMode");
            final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
            final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
            final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
            final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
            final LongSparseArray<ArrayList<TagEntity>> _collectionTags = new LongSparseArray<ArrayList<TagEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionTags.containsKey(_tmpKey)) {
                _collectionTags.put(_tmpKey, new ArrayList<TagEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshiptagsAscomBiteyAppCoreDatabaseModelTagEntity(_collectionTags);
            final List<PlateEntryWithTags> _result = new ArrayList<PlateEntryWithTags>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final PlateEntryWithTags _item;
              final PlateEntryEntity _tmpEntry;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpNote;
              if (_cursor.isNull(_cursorIndexOfNote)) {
                _tmpNote = null;
              } else {
                _tmpNote = _cursor.getString(_cursorIndexOfNote);
              }
              final String _tmpFullImagePath;
              _tmpFullImagePath = _cursor.getString(_cursorIndexOfFullImagePath);
              final String _tmpStickerImagePath;
              if (_cursor.isNull(_cursorIndexOfStickerImagePath)) {
                _tmpStickerImagePath = null;
              } else {
                _tmpStickerImagePath = _cursor.getString(_cursorIndexOfStickerImagePath);
              }
              final String _tmpThumbnailPath;
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
              final boolean _tmpIsStickerMode;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsStickerMode);
              _tmpIsStickerMode = _tmp_1 != 0;
              final float _tmpRating;
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
              final Double _tmpPrice;
              if (_cursor.isNull(_cursorIndexOfPrice)) {
                _tmpPrice = null;
              } else {
                _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
              }
              final String _tmpCurrency;
              _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
              final boolean _tmpIsFavorite;
              final int _tmp_2;
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsFavorite);
              _tmpIsFavorite = _tmp_2 != 0;
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final Double _tmpLatitude;
              if (_cursor.isNull(_cursorIndexOfLatitude)) {
                _tmpLatitude = null;
              } else {
                _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
              }
              final Double _tmpLongitude;
              if (_cursor.isNull(_cursorIndexOfLongitude)) {
                _tmpLongitude = null;
              } else {
                _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
              }
              final String _tmpLocationName;
              if (_cursor.isNull(_cursorIndexOfLocationName)) {
                _tmpLocationName = null;
              } else {
                _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
              }
              final MealType _tmpMealType;
              final String _tmp_3;
              _tmp_3 = _cursor.getString(_cursorIndexOfMealType);
              _tmpMealType = __biteyTypeConverters.toMealType(_tmp_3);
              _tmpEntry = new PlateEntryEntity(_tmpId,_tmpTitle,_tmpNote,_tmpFullImagePath,_tmpStickerImagePath,_tmpThumbnailPath,_tmpIsStickerMode,_tmpRating,_tmpPrice,_tmpCurrency,_tmpIsFavorite,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationName,_tmpMealType);
              final ArrayList<TagEntity> _tmpTagsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpTagsCollection = _collectionTags.get(_tmpKey_1);
              _item = new PlateEntryWithTags(_tmpEntry,_tmpTagsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<PlateEntryWithTags> getEntryById(final long id) {
    final String _sql = "SELECT * FROM plate_entries WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"entry_tag_cross_ref", "tags",
        "plate_entries"}, new Callable<PlateEntryWithTags>() {
      @Override
      @Nullable
      public PlateEntryWithTags call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
            final int _cursorIndexOfFullImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "fullImagePath");
            final int _cursorIndexOfStickerImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "stickerImagePath");
            final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
            final int _cursorIndexOfIsStickerMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isStickerMode");
            final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
            final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
            final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
            final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
            final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
            final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
            final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
            final LongSparseArray<ArrayList<TagEntity>> _collectionTags = new LongSparseArray<ArrayList<TagEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionTags.containsKey(_tmpKey)) {
                _collectionTags.put(_tmpKey, new ArrayList<TagEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshiptagsAscomBiteyAppCoreDatabaseModelTagEntity(_collectionTags);
            final PlateEntryWithTags _result;
            if (_cursor.moveToFirst()) {
              final PlateEntryEntity _tmpEntry;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpNote;
              if (_cursor.isNull(_cursorIndexOfNote)) {
                _tmpNote = null;
              } else {
                _tmpNote = _cursor.getString(_cursorIndexOfNote);
              }
              final String _tmpFullImagePath;
              _tmpFullImagePath = _cursor.getString(_cursorIndexOfFullImagePath);
              final String _tmpStickerImagePath;
              if (_cursor.isNull(_cursorIndexOfStickerImagePath)) {
                _tmpStickerImagePath = null;
              } else {
                _tmpStickerImagePath = _cursor.getString(_cursorIndexOfStickerImagePath);
              }
              final String _tmpThumbnailPath;
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
              final boolean _tmpIsStickerMode;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsStickerMode);
              _tmpIsStickerMode = _tmp != 0;
              final float _tmpRating;
              _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
              final Double _tmpPrice;
              if (_cursor.isNull(_cursorIndexOfPrice)) {
                _tmpPrice = null;
              } else {
                _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
              }
              final String _tmpCurrency;
              _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
              final boolean _tmpIsFavorite;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
              _tmpIsFavorite = _tmp_1 != 0;
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              final Double _tmpLatitude;
              if (_cursor.isNull(_cursorIndexOfLatitude)) {
                _tmpLatitude = null;
              } else {
                _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
              }
              final Double _tmpLongitude;
              if (_cursor.isNull(_cursorIndexOfLongitude)) {
                _tmpLongitude = null;
              } else {
                _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
              }
              final String _tmpLocationName;
              if (_cursor.isNull(_cursorIndexOfLocationName)) {
                _tmpLocationName = null;
              } else {
                _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
              }
              final MealType _tmpMealType;
              final String _tmp_2;
              _tmp_2 = _cursor.getString(_cursorIndexOfMealType);
              _tmpMealType = __biteyTypeConverters.toMealType(_tmp_2);
              _tmpEntry = new PlateEntryEntity(_tmpId,_tmpTitle,_tmpNote,_tmpFullImagePath,_tmpStickerImagePath,_tmpThumbnailPath,_tmpIsStickerMode,_tmpRating,_tmpPrice,_tmpCurrency,_tmpIsFavorite,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationName,_tmpMealType);
              final ArrayList<TagEntity> _tmpTagsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpTagsCollection = _collectionTags.get(_tmpKey_1);
              _result = new PlateEntryWithTags(_tmpEntry,_tmpTagsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PlateEntryEntity>> getEntriesWithLocation() {
    final String _sql = "SELECT * FROM plate_entries WHERE latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"plate_entries"}, new Callable<List<PlateEntryEntity>>() {
      @Override
      @NonNull
      public List<PlateEntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfFullImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "fullImagePath");
          final int _cursorIndexOfStickerImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "stickerImagePath");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfIsStickerMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isStickerMode");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final List<PlateEntryEntity> _result = new ArrayList<PlateEntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlateEntryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpFullImagePath;
            _tmpFullImagePath = _cursor.getString(_cursorIndexOfFullImagePath);
            final String _tmpStickerImagePath;
            if (_cursor.isNull(_cursorIndexOfStickerImagePath)) {
              _tmpStickerImagePath = null;
            } else {
              _tmpStickerImagePath = _cursor.getString(_cursorIndexOfStickerImagePath);
            }
            final String _tmpThumbnailPath;
            _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            final boolean _tmpIsStickerMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsStickerMode);
            _tmpIsStickerMode = _tmp != 0;
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final Double _tmpPrice;
            if (_cursor.isNull(_cursorIndexOfPrice)) {
              _tmpPrice = null;
            } else {
              _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
            }
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final String _tmpLocationName;
            if (_cursor.isNull(_cursorIndexOfLocationName)) {
              _tmpLocationName = null;
            } else {
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
            }
            final MealType _tmpMealType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __biteyTypeConverters.toMealType(_tmp_2);
            _item = new PlateEntryEntity(_tmpId,_tmpTitle,_tmpNote,_tmpFullImagePath,_tmpStickerImagePath,_tmpThumbnailPath,_tmpIsStickerMode,_tmpRating,_tmpPrice,_tmpCurrency,_tmpIsFavorite,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationName,_tmpMealType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PlateEntryEntity>> getRandomEntries(final int limit) {
    final String _sql = "SELECT * FROM plate_entries ORDER BY RANDOM() LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"plate_entries"}, new Callable<List<PlateEntryEntity>>() {
      @Override
      @NonNull
      public List<PlateEntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfFullImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "fullImagePath");
          final int _cursorIndexOfStickerImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "stickerImagePath");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfIsStickerMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isStickerMode");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final List<PlateEntryEntity> _result = new ArrayList<PlateEntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlateEntryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpFullImagePath;
            _tmpFullImagePath = _cursor.getString(_cursorIndexOfFullImagePath);
            final String _tmpStickerImagePath;
            if (_cursor.isNull(_cursorIndexOfStickerImagePath)) {
              _tmpStickerImagePath = null;
            } else {
              _tmpStickerImagePath = _cursor.getString(_cursorIndexOfStickerImagePath);
            }
            final String _tmpThumbnailPath;
            _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            final boolean _tmpIsStickerMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsStickerMode);
            _tmpIsStickerMode = _tmp != 0;
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final Double _tmpPrice;
            if (_cursor.isNull(_cursorIndexOfPrice)) {
              _tmpPrice = null;
            } else {
              _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
            }
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final String _tmpLocationName;
            if (_cursor.isNull(_cursorIndexOfLocationName)) {
              _tmpLocationName = null;
            } else {
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
            }
            final MealType _tmpMealType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __biteyTypeConverters.toMealType(_tmp_2);
            _item = new PlateEntryEntity(_tmpId,_tmpTitle,_tmpNote,_tmpFullImagePath,_tmpStickerImagePath,_tmpThumbnailPath,_tmpIsStickerMode,_tmpRating,_tmpPrice,_tmpCurrency,_tmpIsFavorite,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationName,_tmpMealType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PlateEntryEntity>> getRandomFavoriteEntries(final int limit) {
    final String _sql = "SELECT * FROM plate_entries WHERE isFavorite = 1 ORDER BY RANDOM() LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"plate_entries"}, new Callable<List<PlateEntryEntity>>() {
      @Override
      @NonNull
      public List<PlateEntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfFullImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "fullImagePath");
          final int _cursorIndexOfStickerImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "stickerImagePath");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfIsStickerMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isStickerMode");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final List<PlateEntryEntity> _result = new ArrayList<PlateEntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlateEntryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpFullImagePath;
            _tmpFullImagePath = _cursor.getString(_cursorIndexOfFullImagePath);
            final String _tmpStickerImagePath;
            if (_cursor.isNull(_cursorIndexOfStickerImagePath)) {
              _tmpStickerImagePath = null;
            } else {
              _tmpStickerImagePath = _cursor.getString(_cursorIndexOfStickerImagePath);
            }
            final String _tmpThumbnailPath;
            _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            final boolean _tmpIsStickerMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsStickerMode);
            _tmpIsStickerMode = _tmp != 0;
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final Double _tmpPrice;
            if (_cursor.isNull(_cursorIndexOfPrice)) {
              _tmpPrice = null;
            } else {
              _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
            }
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final String _tmpLocationName;
            if (_cursor.isNull(_cursorIndexOfLocationName)) {
              _tmpLocationName = null;
            } else {
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
            }
            final MealType _tmpMealType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __biteyTypeConverters.toMealType(_tmp_2);
            _item = new PlateEntryEntity(_tmpId,_tmpTitle,_tmpNote,_tmpFullImagePath,_tmpStickerImagePath,_tmpThumbnailPath,_tmpIsStickerMode,_tmpRating,_tmpPrice,_tmpCurrency,_tmpIsFavorite,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpLocationName,_tmpMealType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshiptagsAscomBiteyAppCoreDatabaseModelTagEntity(
      @NonNull final LongSparseArray<ArrayList<TagEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshiptagsAscomBiteyAppCoreDatabaseModelTagEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `tags`.`tagId` AS `tagId`,`tags`.`tagName` AS `tagName`,`tags`.`category` AS `category`,_junction.`entryId` FROM `entry_tag_cross_ref` AS _junction INNER JOIN `tags` ON (_junction.`tagId` = `tags`.`tagId`) WHERE _junction.`entryId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      // _junction.entryId;
      final int _itemKeyIndex = 3;
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfTagId = 0;
      final int _cursorIndexOfTagName = 1;
      final int _cursorIndexOfCategory = 2;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<TagEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final TagEntity _item_1;
          final long _tmpTagId;
          _tmpTagId = _cursor.getLong(_cursorIndexOfTagId);
          final String _tmpTagName;
          _tmpTagName = _cursor.getString(_cursorIndexOfTagName);
          final String _tmpCategory;
          if (_cursor.isNull(_cursorIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
          }
          _item_1 = new TagEntity(_tmpTagId,_tmpTagName,_tmpCategory);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
