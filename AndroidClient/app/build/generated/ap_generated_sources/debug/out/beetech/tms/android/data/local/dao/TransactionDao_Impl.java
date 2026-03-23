package beetech.tms.android.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import beetech.tms.android.data.models.PendingAuditSession;
import beetech.tms.android.data.models.PendingTransaction;
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
public final class TransactionDao_Impl implements TransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PendingTransaction> __insertionAdapterOfPendingTransaction;

  private final EntityInsertionAdapter<PendingAuditSession> __insertionAdapterOfPendingAuditSession;

  private final EntityDeletionOrUpdateAdapter<PendingTransaction> __deletionAdapterOfPendingTransaction;

  private final EntityDeletionOrUpdateAdapter<PendingAuditSession> __deletionAdapterOfPendingAuditSession;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPendingTransaction = new EntityInsertionAdapter<PendingTransaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pending_transactions` (`localId`,`type`,`fromLocationId`,`toLocationId`,`departmentId`,`epcsJson`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PendingTransaction entity) {
        statement.bindLong(1, entity.localId);
        if (entity.type == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.type);
        }
        if (entity.fromLocationId == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.fromLocationId);
        }
        if (entity.toLocationId == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.toLocationId);
        }
        if (entity.departmentId == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.departmentId);
        }
        if (entity.epcsJson == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.epcsJson);
        }
        statement.bindLong(7, entity.timestamp);
      }
    };
    this.__insertionAdapterOfPendingAuditSession = new EntityInsertionAdapter<PendingAuditSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pending_audit_sessions` (`localSessionId`,`locationId`,`status`,`performByName`,`timestamp`,`recordsJson`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PendingAuditSession entity) {
        statement.bindLong(1, entity.localSessionId);
        statement.bindLong(2, entity.locationId);
        if (entity.status == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.status);
        }
        if (entity.performByName == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.performByName);
        }
        statement.bindLong(5, entity.timestamp);
        if (entity.recordsJson == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.recordsJson);
        }
      }
    };
    this.__deletionAdapterOfPendingTransaction = new EntityDeletionOrUpdateAdapter<PendingTransaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `pending_transactions` WHERE `localId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PendingTransaction entity) {
        statement.bindLong(1, entity.localId);
      }
    };
    this.__deletionAdapterOfPendingAuditSession = new EntityDeletionOrUpdateAdapter<PendingAuditSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `pending_audit_sessions` WHERE `localSessionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PendingAuditSession entity) {
        statement.bindLong(1, entity.localSessionId);
      }
    };
  }

  @Override
  public void insertTransaction(final PendingTransaction transaction) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPendingTransaction.insert(transaction);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAuditSession(final PendingAuditSession session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPendingAuditSession.insert(session);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteTransaction(final PendingTransaction transaction) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPendingTransaction.handle(transaction);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAuditSession(final PendingAuditSession session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPendingAuditSession.handle(session);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<PendingTransaction> getPendingTransactions() {
    final String _sql = "SELECT * FROM pending_transactions ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfLocalId = CursorUtil.getColumnIndexOrThrow(_cursor, "localId");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfFromLocationId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromLocationId");
      final int _cursorIndexOfToLocationId = CursorUtil.getColumnIndexOrThrow(_cursor, "toLocationId");
      final int _cursorIndexOfDepartmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "departmentId");
      final int _cursorIndexOfEpcsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "epcsJson");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<PendingTransaction> _result = new ArrayList<PendingTransaction>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PendingTransaction _item;
        _item = new PendingTransaction();
        _item.localId = _cursor.getInt(_cursorIndexOfLocalId);
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfFromLocationId)) {
          _item.fromLocationId = null;
        } else {
          _item.fromLocationId = _cursor.getInt(_cursorIndexOfFromLocationId);
        }
        if (_cursor.isNull(_cursorIndexOfToLocationId)) {
          _item.toLocationId = null;
        } else {
          _item.toLocationId = _cursor.getInt(_cursorIndexOfToLocationId);
        }
        if (_cursor.isNull(_cursorIndexOfDepartmentId)) {
          _item.departmentId = null;
        } else {
          _item.departmentId = _cursor.getInt(_cursorIndexOfDepartmentId);
        }
        if (_cursor.isNull(_cursorIndexOfEpcsJson)) {
          _item.epcsJson = null;
        } else {
          _item.epcsJson = _cursor.getString(_cursorIndexOfEpcsJson);
        }
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<PendingAuditSession> getPendingAuditSessions() {
    final String _sql = "SELECT * FROM pending_audit_sessions ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfLocalSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "localSessionId");
      final int _cursorIndexOfLocationId = CursorUtil.getColumnIndexOrThrow(_cursor, "locationId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfPerformByName = CursorUtil.getColumnIndexOrThrow(_cursor, "performByName");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfRecordsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "recordsJson");
      final List<PendingAuditSession> _result = new ArrayList<PendingAuditSession>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PendingAuditSession _item;
        _item = new PendingAuditSession();
        _item.localSessionId = _cursor.getInt(_cursorIndexOfLocalSessionId);
        _item.locationId = _cursor.getInt(_cursorIndexOfLocationId);
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _item.status = null;
        } else {
          _item.status = _cursor.getString(_cursorIndexOfStatus);
        }
        if (_cursor.isNull(_cursorIndexOfPerformByName)) {
          _item.performByName = null;
        } else {
          _item.performByName = _cursor.getString(_cursorIndexOfPerformByName);
        }
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        if (_cursor.isNull(_cursorIndexOfRecordsJson)) {
          _item.recordsJson = null;
        } else {
          _item.recordsJson = _cursor.getString(_cursorIndexOfRecordsJson);
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
