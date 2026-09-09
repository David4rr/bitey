package com.bitey.app.core.database.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.bitey.app.core.database.model.SavedCollageEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class SavedCollageDao_Impl implements SavedCollageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SavedCollageEntity> __insertionAdapterOfSavedCollageEntity;

  private final EntityDeletionOrUpdateAdapter<SavedCollageEntity> __deletionAdapterOfSavedCollageEntity;

  public SavedCollageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSavedCollageEntity = new EntityInsertionAdapter<SavedCollageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `saved_collages` (`collageId`,`title`,`canvasDataJson`,`previewImagePath`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedCollageEntity entity) {
        statement.bindLong(1, entity.getCollageId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getCanvasDataJson());
        statement.bindString(4, entity.getPreviewImagePath());
        statement.bindLong(5, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfSavedCollageEntity = new EntityDeletionOrUpdateAdapter<SavedCollageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `saved_collages` WHERE `collageId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedCollageEntity entity) {
        statement.bindLong(1, entity.getCollageId());
      }
    };
  }

  @Override
  public Object insertCollage(final SavedCollageEntity collage,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSavedCollageEntity.insertAndReturnId(collage);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCollage(final SavedCollageEntity collage,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSavedCollageEntity.handle(collage);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SavedCollageEntity>> getAllCollages() {
    final String _sql = "SELECT * FROM saved_collages ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"saved_collages"}, new Callable<List<SavedCollageEntity>>() {
      @Override
      @NonNull
      public List<SavedCollageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCollageId = CursorUtil.getColumnIndexOrThrow(_cursor, "collageId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCanvasDataJson = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasDataJson");
          final int _cursorIndexOfPreviewImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "previewImagePath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<SavedCollageEntity> _result = new ArrayList<SavedCollageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SavedCollageEntity _item;
            final long _tmpCollageId;
            _tmpCollageId = _cursor.getLong(_cursorIndexOfCollageId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCanvasDataJson;
            _tmpCanvasDataJson = _cursor.getString(_cursorIndexOfCanvasDataJson);
            final String _tmpPreviewImagePath;
            _tmpPreviewImagePath = _cursor.getString(_cursorIndexOfPreviewImagePath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new SavedCollageEntity(_tmpCollageId,_tmpTitle,_tmpCanvasDataJson,_tmpPreviewImagePath,_tmpCreatedAt);
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
  public Flow<SavedCollageEntity> getCollageById(final long id) {
    final String _sql = "SELECT * FROM saved_collages WHERE collageId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"saved_collages"}, new Callable<SavedCollageEntity>() {
      @Override
      @Nullable
      public SavedCollageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCollageId = CursorUtil.getColumnIndexOrThrow(_cursor, "collageId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCanvasDataJson = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasDataJson");
          final int _cursorIndexOfPreviewImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "previewImagePath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final SavedCollageEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpCollageId;
            _tmpCollageId = _cursor.getLong(_cursorIndexOfCollageId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCanvasDataJson;
            _tmpCanvasDataJson = _cursor.getString(_cursorIndexOfCanvasDataJson);
            final String _tmpPreviewImagePath;
            _tmpPreviewImagePath = _cursor.getString(_cursorIndexOfPreviewImagePath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new SavedCollageEntity(_tmpCollageId,_tmpTitle,_tmpCanvasDataJson,_tmpPreviewImagePath,_tmpCreatedAt);
          } else {
            _result = null;
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
}
