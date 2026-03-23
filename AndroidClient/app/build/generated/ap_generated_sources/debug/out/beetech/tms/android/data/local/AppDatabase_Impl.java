package beetech.tms.android.data.local;

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
import beetech.tms.android.data.local.dao.MasterDataDao;
import beetech.tms.android.data.local.dao.MasterDataDao_Impl;
import beetech.tms.android.data.local.dao.TransactionDao;
import beetech.tms.android.data.local.dao.TransactionDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MasterDataDao _masterDataDao;

  private volatile TransactionDao _transactionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `locations` (`id` INTEGER NOT NULL, `name` TEXT, `code` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `items` (`id` INTEGER NOT NULL, `code` TEXT, `status` TEXT, `category` TEXT, `location` TEXT, `department` TEXT, `washCount` INTEGER NOT NULL, `epc` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_transactions` (`localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT, `fromLocationId` INTEGER, `toLocationId` INTEGER, `departmentId` INTEGER, `epcsJson` TEXT, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_audit_sessions` (`localSessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `locationId` INTEGER NOT NULL, `status` TEXT, `performByName` TEXT, `timestamp` INTEGER NOT NULL, `recordsJson` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ac997e14cb886c75afd2cdc52293b5fb')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `locations`");
        db.execSQL("DROP TABLE IF EXISTS `items`");
        db.execSQL("DROP TABLE IF EXISTS `pending_transactions`");
        db.execSQL("DROP TABLE IF EXISTS `pending_audit_sessions`");
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
        final HashMap<String, TableInfo.Column> _columnsLocations = new HashMap<String, TableInfo.Column>(3);
        _columnsLocations.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocations.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocations.put("code", new TableInfo.Column("code", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLocations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLocations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLocations = new TableInfo("locations", _columnsLocations, _foreignKeysLocations, _indicesLocations);
        final TableInfo _existingLocations = TableInfo.read(db, "locations");
        if (!_infoLocations.equals(_existingLocations)) {
          return new RoomOpenHelper.ValidationResult(false, "locations(beetech.tms.android.data.models.StorageLocation).\n"
                  + " Expected:\n" + _infoLocations + "\n"
                  + " Found:\n" + _existingLocations);
        }
        final HashMap<String, TableInfo.Column> _columnsItems = new HashMap<String, TableInfo.Column>(8);
        _columnsItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("code", new TableInfo.Column("code", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("location", new TableInfo.Column("location", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("department", new TableInfo.Column("department", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("washCount", new TableInfo.Column("washCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("epc", new TableInfo.Column("epc", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoItems = new TableInfo("items", _columnsItems, _foreignKeysItems, _indicesItems);
        final TableInfo _existingItems = TableInfo.read(db, "items");
        if (!_infoItems.equals(_existingItems)) {
          return new RoomOpenHelper.ValidationResult(false, "items(beetech.tms.android.data.models.TextileItem).\n"
                  + " Expected:\n" + _infoItems + "\n"
                  + " Found:\n" + _existingItems);
        }
        final HashMap<String, TableInfo.Column> _columnsPendingTransactions = new HashMap<String, TableInfo.Column>(7);
        _columnsPendingTransactions.put("localId", new TableInfo.Column("localId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingTransactions.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingTransactions.put("fromLocationId", new TableInfo.Column("fromLocationId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingTransactions.put("toLocationId", new TableInfo.Column("toLocationId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingTransactions.put("departmentId", new TableInfo.Column("departmentId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingTransactions.put("epcsJson", new TableInfo.Column("epcsJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingTransactions.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingTransactions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingTransactions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPendingTransactions = new TableInfo("pending_transactions", _columnsPendingTransactions, _foreignKeysPendingTransactions, _indicesPendingTransactions);
        final TableInfo _existingPendingTransactions = TableInfo.read(db, "pending_transactions");
        if (!_infoPendingTransactions.equals(_existingPendingTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_transactions(beetech.tms.android.data.models.PendingTransaction).\n"
                  + " Expected:\n" + _infoPendingTransactions + "\n"
                  + " Found:\n" + _existingPendingTransactions);
        }
        final HashMap<String, TableInfo.Column> _columnsPendingAuditSessions = new HashMap<String, TableInfo.Column>(6);
        _columnsPendingAuditSessions.put("localSessionId", new TableInfo.Column("localSessionId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingAuditSessions.put("locationId", new TableInfo.Column("locationId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingAuditSessions.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingAuditSessions.put("performByName", new TableInfo.Column("performByName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingAuditSessions.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingAuditSessions.put("recordsJson", new TableInfo.Column("recordsJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingAuditSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingAuditSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPendingAuditSessions = new TableInfo("pending_audit_sessions", _columnsPendingAuditSessions, _foreignKeysPendingAuditSessions, _indicesPendingAuditSessions);
        final TableInfo _existingPendingAuditSessions = TableInfo.read(db, "pending_audit_sessions");
        if (!_infoPendingAuditSessions.equals(_existingPendingAuditSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_audit_sessions(beetech.tms.android.data.models.PendingAuditSession).\n"
                  + " Expected:\n" + _infoPendingAuditSessions + "\n"
                  + " Found:\n" + _existingPendingAuditSessions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ac997e14cb886c75afd2cdc52293b5fb", "5947f205466a2a379cc846b410e41087");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "locations","items","pending_transactions","pending_audit_sessions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `locations`");
      _db.execSQL("DELETE FROM `items`");
      _db.execSQL("DELETE FROM `pending_transactions`");
      _db.execSQL("DELETE FROM `pending_audit_sessions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
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
    _typeConvertersMap.put(MasterDataDao.class, MasterDataDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
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
  public MasterDataDao masterDataDao() {
    if (_masterDataDao != null) {
      return _masterDataDao;
    } else {
      synchronized(this) {
        if(_masterDataDao == null) {
          _masterDataDao = new MasterDataDao_Impl(this);
        }
        return _masterDataDao;
      }
    }
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }
}
