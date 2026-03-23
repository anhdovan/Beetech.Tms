package beetech.tms.android.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DocumentDao_Impl implements DocumentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DocumentEntity> __insertionAdapterOfDocumentEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public DocumentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDocumentEntity = new EntityInsertionAdapter<DocumentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `documents` (`id`,`documentCode`,`title`,`status`,`category`,`customer`,`expiryDate`,`rfidTag`,`barcode`,`storageLocation`,`tagStatus`,`lastSyncAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DocumentEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.documentCode == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.documentCode);
        }
        if (entity.title == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.title);
        }
        if (entity.status == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.status);
        }
        if (entity.category == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.category);
        }
        if (entity.customer == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.customer);
        }
        if (entity.expiryDate == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.expiryDate);
        }
        if (entity.rfidTag == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.rfidTag);
        }
        if (entity.barcode == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.barcode);
        }
        if (entity.storageLocation == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.storageLocation);
        }
        if (entity.tagStatus == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.tagStatus);
        }
        statement.bindLong(12, entity.lastSyncAt);
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM documents";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<DocumentEntity> documents) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfDocumentEntity.insert(documents);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public List<DocumentEntity> getAll() {
    final String _sql = "SELECT * FROM documents ORDER BY documentCode";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDocumentCode = CursorUtil.getColumnIndexOrThrow(_cursor, "documentCode");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfCustomer = CursorUtil.getColumnIndexOrThrow(_cursor, "customer");
      final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
      final int _cursorIndexOfRfidTag = CursorUtil.getColumnIndexOrThrow(_cursor, "rfidTag");
      final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
      final int _cursorIndexOfStorageLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "storageLocation");
      final int _cursorIndexOfTagStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "tagStatus");
      final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
      final List<DocumentEntity> _result = new ArrayList<DocumentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final DocumentEntity _item;
        _item = new DocumentEntity();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfDocumentCode)) {
          _item.documentCode = null;
        } else {
          _item.documentCode = _cursor.getString(_cursorIndexOfDocumentCode);
        }
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _item.status = null;
        } else {
          _item.status = _cursor.getString(_cursorIndexOfStatus);
        }
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _item.category = null;
        } else {
          _item.category = _cursor.getString(_cursorIndexOfCategory);
        }
        if (_cursor.isNull(_cursorIndexOfCustomer)) {
          _item.customer = null;
        } else {
          _item.customer = _cursor.getString(_cursorIndexOfCustomer);
        }
        if (_cursor.isNull(_cursorIndexOfExpiryDate)) {
          _item.expiryDate = null;
        } else {
          _item.expiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
        }
        if (_cursor.isNull(_cursorIndexOfRfidTag)) {
          _item.rfidTag = null;
        } else {
          _item.rfidTag = _cursor.getString(_cursorIndexOfRfidTag);
        }
        if (_cursor.isNull(_cursorIndexOfBarcode)) {
          _item.barcode = null;
        } else {
          _item.barcode = _cursor.getString(_cursorIndexOfBarcode);
        }
        if (_cursor.isNull(_cursorIndexOfStorageLocation)) {
          _item.storageLocation = null;
        } else {
          _item.storageLocation = _cursor.getString(_cursorIndexOfStorageLocation);
        }
        if (_cursor.isNull(_cursorIndexOfTagStatus)) {
          _item.tagStatus = null;
        } else {
          _item.tagStatus = _cursor.getString(_cursorIndexOfTagStatus);
        }
        _item.lastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public DocumentEntity findByTag(final String tag) {
    final String _sql = "SELECT * FROM documents WHERE rfidTag = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (tag == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, tag);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDocumentCode = CursorUtil.getColumnIndexOrThrow(_cursor, "documentCode");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfCustomer = CursorUtil.getColumnIndexOrThrow(_cursor, "customer");
      final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
      final int _cursorIndexOfRfidTag = CursorUtil.getColumnIndexOrThrow(_cursor, "rfidTag");
      final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
      final int _cursorIndexOfStorageLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "storageLocation");
      final int _cursorIndexOfTagStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "tagStatus");
      final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
      final DocumentEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new DocumentEntity();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfDocumentCode)) {
          _result.documentCode = null;
        } else {
          _result.documentCode = _cursor.getString(_cursorIndexOfDocumentCode);
        }
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _result.title = null;
        } else {
          _result.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _result.status = null;
        } else {
          _result.status = _cursor.getString(_cursorIndexOfStatus);
        }
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _result.category = null;
        } else {
          _result.category = _cursor.getString(_cursorIndexOfCategory);
        }
        if (_cursor.isNull(_cursorIndexOfCustomer)) {
          _result.customer = null;
        } else {
          _result.customer = _cursor.getString(_cursorIndexOfCustomer);
        }
        if (_cursor.isNull(_cursorIndexOfExpiryDate)) {
          _result.expiryDate = null;
        } else {
          _result.expiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
        }
        if (_cursor.isNull(_cursorIndexOfRfidTag)) {
          _result.rfidTag = null;
        } else {
          _result.rfidTag = _cursor.getString(_cursorIndexOfRfidTag);
        }
        if (_cursor.isNull(_cursorIndexOfBarcode)) {
          _result.barcode = null;
        } else {
          _result.barcode = _cursor.getString(_cursorIndexOfBarcode);
        }
        if (_cursor.isNull(_cursorIndexOfStorageLocation)) {
          _result.storageLocation = null;
        } else {
          _result.storageLocation = _cursor.getString(_cursorIndexOfStorageLocation);
        }
        if (_cursor.isNull(_cursorIndexOfTagStatus)) {
          _result.tagStatus = null;
        } else {
          _result.tagStatus = _cursor.getString(_cursorIndexOfTagStatus);
        }
        _result.lastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<DocumentEntity> search(final String search) {
    final String _sql = "SELECT * FROM documents WHERE title LIKE '%' || ? || '%' OR documentCode LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (search == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, search);
    }
    _argIndex = 2;
    if (search == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, search);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDocumentCode = CursorUtil.getColumnIndexOrThrow(_cursor, "documentCode");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfCustomer = CursorUtil.getColumnIndexOrThrow(_cursor, "customer");
      final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
      final int _cursorIndexOfRfidTag = CursorUtil.getColumnIndexOrThrow(_cursor, "rfidTag");
      final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
      final int _cursorIndexOfStorageLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "storageLocation");
      final int _cursorIndexOfTagStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "tagStatus");
      final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
      final List<DocumentEntity> _result = new ArrayList<DocumentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final DocumentEntity _item;
        _item = new DocumentEntity();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfDocumentCode)) {
          _item.documentCode = null;
        } else {
          _item.documentCode = _cursor.getString(_cursorIndexOfDocumentCode);
        }
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _item.status = null;
        } else {
          _item.status = _cursor.getString(_cursorIndexOfStatus);
        }
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _item.category = null;
        } else {
          _item.category = _cursor.getString(_cursorIndexOfCategory);
        }
        if (_cursor.isNull(_cursorIndexOfCustomer)) {
          _item.customer = null;
        } else {
          _item.customer = _cursor.getString(_cursorIndexOfCustomer);
        }
        if (_cursor.isNull(_cursorIndexOfExpiryDate)) {
          _item.expiryDate = null;
        } else {
          _item.expiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
        }
        if (_cursor.isNull(_cursorIndexOfRfidTag)) {
          _item.rfidTag = null;
        } else {
          _item.rfidTag = _cursor.getString(_cursorIndexOfRfidTag);
        }
        if (_cursor.isNull(_cursorIndexOfBarcode)) {
          _item.barcode = null;
        } else {
          _item.barcode = _cursor.getString(_cursorIndexOfBarcode);
        }
        if (_cursor.isNull(_cursorIndexOfStorageLocation)) {
          _item.storageLocation = null;
        } else {
          _item.storageLocation = _cursor.getString(_cursorIndexOfStorageLocation);
        }
        if (_cursor.isNull(_cursorIndexOfTagStatus)) {
          _item.tagStatus = null;
        } else {
          _item.tagStatus = _cursor.getString(_cursorIndexOfTagStatus);
        }
        _item.lastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int count() {
    final String _sql = "SELECT COUNT(*) FROM documents";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int countTagged() {
    final String _sql = "SELECT COUNT(*) FROM documents WHERE tagStatus = 'Tagged' OR tagStatus = 'Verified'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
