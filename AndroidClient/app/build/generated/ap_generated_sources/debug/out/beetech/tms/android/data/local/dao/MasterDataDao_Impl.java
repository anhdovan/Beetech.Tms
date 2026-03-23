package beetech.tms.android.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.data.models.TextileItem;
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
public final class MasterDataDao_Impl implements MasterDataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StorageLocation> __insertionAdapterOfStorageLocation;

  private final EntityInsertionAdapter<TextileItem> __insertionAdapterOfTextileItem;

  private final SharedSQLiteStatement __preparedStmtOfClearLocations;

  private final SharedSQLiteStatement __preparedStmtOfClearItems;

  public MasterDataDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStorageLocation = new EntityInsertionAdapter<StorageLocation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `locations` (`id`,`name`,`code`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final StorageLocation entity) {
        statement.bindLong(1, entity.id);
        if (entity.name == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.name);
        }
        if (entity.code == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.code);
        }
      }
    };
    this.__insertionAdapterOfTextileItem = new EntityInsertionAdapter<TextileItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `items` (`id`,`code`,`status`,`category`,`location`,`department`,`washCount`,`epc`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final TextileItem entity) {
        statement.bindLong(1, entity.id);
        if (entity.code == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.code);
        }
        if (entity.status == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.status);
        }
        if (entity.category == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.category);
        }
        if (entity.location == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.location);
        }
        if (entity.department == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.department);
        }
        statement.bindLong(7, entity.washCount);
        if (entity.epc == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.epc);
        }
      }
    };
    this.__preparedStmtOfClearLocations = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM locations";
        return _query;
      }
    };
    this.__preparedStmtOfClearItems = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM items";
        return _query;
      }
    };
  }

  @Override
  public void insertLocations(final List<StorageLocation> locations) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStorageLocation.insert(locations);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertItems(final List<TextileItem> items) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfTextileItem.insert(items);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearLocations() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearLocations.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearLocations.release(_stmt);
    }
  }

  @Override
  public void clearItems() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearItems.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearItems.release(_stmt);
    }
  }

  @Override
  public List<StorageLocation> getAllLocations() {
    final String _sql = "SELECT * FROM locations";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final List<StorageLocation> _result = new ArrayList<StorageLocation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final StorageLocation _item;
        _item = new StorageLocation();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _item.code = null;
        } else {
          _item.code = _cursor.getString(_cursorIndexOfCode);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public TextileItem getItemByEpc(final String epc) {
    final String _sql = "SELECT * FROM items WHERE epc = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (epc == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, epc);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
      final int _cursorIndexOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "department");
      final int _cursorIndexOfWashCount = CursorUtil.getColumnIndexOrThrow(_cursor, "washCount");
      final int _cursorIndexOfEpc = CursorUtil.getColumnIndexOrThrow(_cursor, "epc");
      final TextileItem _result;
      if (_cursor.moveToFirst()) {
        _result = new TextileItem();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _result.code = null;
        } else {
          _result.code = _cursor.getString(_cursorIndexOfCode);
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
        if (_cursor.isNull(_cursorIndexOfLocation)) {
          _result.location = null;
        } else {
          _result.location = _cursor.getString(_cursorIndexOfLocation);
        }
        if (_cursor.isNull(_cursorIndexOfDepartment)) {
          _result.department = null;
        } else {
          _result.department = _cursor.getString(_cursorIndexOfDepartment);
        }
        _result.washCount = _cursor.getInt(_cursorIndexOfWashCount);
        if (_cursor.isNull(_cursorIndexOfEpc)) {
          _result.epc = null;
        } else {
          _result.epc = _cursor.getString(_cursorIndexOfEpc);
        }
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
  public List<TextileItem> getAllItems() {
    final String _sql = "SELECT * FROM items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
      final int _cursorIndexOfDepartment = CursorUtil.getColumnIndexOrThrow(_cursor, "department");
      final int _cursorIndexOfWashCount = CursorUtil.getColumnIndexOrThrow(_cursor, "washCount");
      final int _cursorIndexOfEpc = CursorUtil.getColumnIndexOrThrow(_cursor, "epc");
      final List<TextileItem> _result = new ArrayList<TextileItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final TextileItem _item;
        _item = new TextileItem();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _item.code = null;
        } else {
          _item.code = _cursor.getString(_cursorIndexOfCode);
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
        if (_cursor.isNull(_cursorIndexOfLocation)) {
          _item.location = null;
        } else {
          _item.location = _cursor.getString(_cursorIndexOfLocation);
        }
        if (_cursor.isNull(_cursorIndexOfDepartment)) {
          _item.department = null;
        } else {
          _item.department = _cursor.getString(_cursorIndexOfDepartment);
        }
        _item.washCount = _cursor.getInt(_cursorIndexOfWashCount);
        if (_cursor.isNull(_cursorIndexOfEpc)) {
          _item.epc = null;
        } else {
          _item.epc = _cursor.getString(_cursorIndexOfEpc);
        }
        _result.add(_item);
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
