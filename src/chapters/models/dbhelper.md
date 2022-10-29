# DBHelper

The `DBHelper` class is responsible for [saving data in an SQLite database](https://developer.android.com/training/data-storage/sqlite).
The data is stored in an SQLite database so that it is easily accessible and scalable given the amount of data that is used by the application.
The `DBHelper` class contains methods responsible for setting up the SQLite database, the different tables within it, and how to add and remove data to those tables.

## Snippet

```kt
class DBHelper(context: Context) :
  SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
  private val TAG = "DB"

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL("""
      CREATE TABLE $SESSION_TABLE (
        $SESSION_ID     INTEGER      PRIMARY KEY AUTOINCREMENT,
        $SESSION_NAME   VARCHAR(100) NOT NULL,
        $SESSION_ROWS   INTEGER      NOT NULL,
        $SESSION_COLS   INTEGER      NOT NULL
      );
    """.trimIndent())

    db.execSQL("""
      CREATE TABLE $ROUTER_TABLE (
        $ROUTER_BSSID   INTEGER     PRIMARY KEY,
        $ROUTER_NAME  VARCHAR(100)  NOT NULL
      );
    """.trimIndent())

    db.execSQL("""
      CREATE TABLE $SESSION_ROUTER_TABLE (
        $SESSION_ROUTER_ID          INTEGER PRIMARY KEY AUTOINCREMENT,
        $SESSION_ROUTER_SESSION_ID  INTEGER NOT NULL,
        $SESSION_ROUTER_ROUTER_ID   INTEGER NOT NULL,
        $SESSION_ROUTER_ROW         INTEGER NOT NULL,
        $SESSION_ROUTER_COL         INTEGER NOT NULL,
        FOREIGN KEY($SESSION_ROUTER_SESSION_ID) REFERENCES $SESSION_TABLE($SESSION_ID) ON DELETE CASCADE,
        FOREIGN KEY($SESSION_ROUTER_ROUTER_ID) REFERENCES $ROUTER_TABLE($ROUTER_BSSID),
        UNIQUE($SESSION_ROUTER_SESSION_ID, $SESSION_ROUTER_ROUTER_ID)
      );
    """.trimIndent())

    db.execSQL("""
      CREATE TABLE $RECORD_TABLE (
        $RECORD_ID          INTEGER   PRIMARY KEY AUTOINCREMENT,
        $RECORD_SESSION_ID  INTEGER   NOT NULL,
        $RECORD_LABEL       INTEGER   NOT NULL,
        $RECORD_TIMESTAMP   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY($RECORD_SESSION_ID) REFERENCES $SESSION_TABLE($SESSION_ID) ON DELETE CASCADE
      );
    """.trimIndent())

    db.execSQL("""
      CREATE TABLE $DATA_TABLE (
        $DATA_ID          INTEGER PRIMARY KEY AUTOINCREMENT,
        $DATA_RECORD_ID   INTEGER NOT NULL,
        $DATA_ROUTER_ID   INTEGER NOT NULL,
        $DATA_VALUE       INTEGER NOT NULL,
        FOREIGN KEY($DATA_RECORD_ID) REFERENCES $RECORD_ID($RECORD_ID) ON DELETE CASCADE,
        FOREIGN KEY($DATA_ROUTER_ID) REFERENCES $ROUTER_TABLE($ROUTER_BSSID)
      )
    """.trimIndent())
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    if (db == null) return;
    db.execSQL("DROP TABLE IF EXISTS $DATA_TABLE")
    db.execSQL("DROP TABLE IF EXISTS $RECORD_TABLE")
    db.execSQL("DROP TABLE IF EXISTS $SESSION_ROUTER_TABLE")
    db.execSQL("DROP TABLE IF EXISTS $ROUTER_TABLE")
    db.execSQL("DROP TABLE IF EXISTS $SESSION_TABLE")
    onCreate(db)
  }

  // Session
  fun addSession(name: String, rows: Int, cols: Int): Long {
    val values = ContentValues()
    values.put(SESSION_NAME, name)
    values.put(SESSION_ROWS, rows)
    values.put(SESSION_COLS, cols)

    val db = this.writableDatabase
    return try {
      db.insertOrThrow(SESSION_TABLE, null, values)
    } catch (e: Exception) {
      Log.e(TAG, Log.getStackTraceString(e))
      -1L
    }
  }

  fun updateSession(session: Session) {
    val values = ContentValues()
    values.put(SESSION_NAME, session.name)
    values.put(SESSION_ROWS, session.rows)
    values.put(SESSION_COLS, session.cols)

    val db = this.writableDatabase
    db.update(SESSION_TABLE, values,
      "$SESSION_ID=?", arrayOf(session.id.toString()))
  }

  fun getSessionById(id: Long): Session? {
    val db = this.readableDatabase
    val query = """
      SELECT  *
      FROM    $SESSION_TABLE
      WHERE   $SESSION_ID=?
      LIMIT   1;
    """.trimIndent()
    val cursor = db.rawQuery(query, arrayOf(id.toString()))
    if (!cursor.moveToFirst()) {
      return null
    }

    return Session(
      cursor.getLong(cursor.getColumnIndex(SESSION_ID)),
      cursor.getString(cursor.getColumnIndex(SESSION_NAME)),
      cursor.getInt(cursor.getColumnIndex(SESSION_ROWS)),
      cursor.getInt(cursor.getColumnIndex(SESSION_COLS))
    )
  }
  
  @SuppressLint("Recycle")
  fun getAllSessions(): ArrayList<Session> {
    val db = this.readableDatabase
    val query = """
      SELECT  *
      FROM    $SESSION_TABLE;
    """.trimIndent()
    val cursor = db.rawQuery(query, null)
    if (!cursor.moveToFirst()) {
      return arrayListOf()
    }

    val idIndex = cursor.getColumnIndex("id")
    val nameIndex = cursor.getColumnIndex("name")
    val rowsIndex = cursor.getColumnIndex("rows")
    val colsIndex = cursor.getColumnIndex("cols")
    val sessions = arrayListOf<Session>()
    do {
      sessions.add(Session(
        cursor.getLong(idIndex),
        cursor.getString(nameIndex),
        cursor.getInt(rowsIndex),
        cursor.getInt(colsIndex)
      ))
    } while (cursor.moveToNext())

    return sessions
  }

  fun deleteSession(id: Long) {
    val db = this.writableDatabase
    db.delete(SESSION_TABLE, "id=?", arrayOf(id.toString()))
  }

  // Router Db CRUD
  fun addRouter(bssid: Long, name: String) {
    val values = ContentValues()
    values.put(ROUTER_BSSID, bssid)
    values.put(ROUTER_NAME, name)

    val db = this.writableDatabase
    db.insertWithOnConflict(
      ROUTER_TABLE, null,
      values, SQLiteDatabase.CONFLICT_IGNORE
    )
  }

  /**
   * Gets all the routers added
   *
   * @return Map of (bssid: router name)
   */
  @SuppressLint("Recycle")
  fun getRouters(): ArrayList<Router> {
    val db = this.readableDatabase
    val query = """
      SELECT  *
      FROM  $ROUTER_TABLE;
    """.trimIndent()
    val cursor = db.rawQuery(query, null)
    if (!cursor!!.moveToFirst()) {
      return arrayListOf()
    }

    val routers = arrayListOf<Router>()
    do {
      routers.add(Router(
        cursor.getLong(cursor.getColumnIndex(ROUTER_BSSID)),
        cursor.getString(cursor.getColumnIndex(ROUTER_NAME))
      ))
    } while (cursor.moveToNext())

    return routers
  }

  // Session Router Db CRUD
  /**
   * Adds a new session router row
   *
   * @param sessionId Session Id
   * @param bssid Router Id
   * @param row Row of the router in the matrix
   * @param col Col of the router in the matrix
   */
  fun addSessionRouter(sessionId: Long, bssid: Long, row: Int, col: Int) {
    val values = ContentValues()
    values.put(SESSION_ROUTER_SESSION_ID, sessionId)
    values.put(SESSION_ROUTER_ROUTER_ID, bssid)
    values.put(SESSION_ROUTER_ROW, row)
    values.put(SESSION_ROUTER_COL, col)

    val db = this.writableDatabase
    db.insert(SESSION_ROUTER_TABLE, null, values)
  }

  fun deleteSessionRouters(sessionId: Long) {
    val db = this.writableDatabase
    db.delete(SESSION_ROUTER_TABLE, "$SESSION_ROUTER_SESSION_ID=?", arrayOf(sessionId.toString()))
  }

  /**
   *
   */
  @SuppressLint("Recycle")
  fun getSessionRouters(sessionId: Long): ArrayList<Router> {
    val db = this.readableDatabase
    val query = """
      SELECT
        r.$ROUTER_BSSID AS id,
        r.$ROUTER_NAME AS name,
        sr.$SESSION_ROUTER_ROW AS `row`,
        sr.$SESSION_ROUTER_COL AS col
      FROM (
        SELECT    *
        FROM      $SESSION_ROUTER_TABLE
        WHERE     $SESSION_ROUTER_SESSION_ID=?
      ) AS sr
      JOIN        $ROUTER_TABLE r
        ON        r.$ROUTER_BSSID = sr.$SESSION_ROUTER_ROUTER_ID
      ORDER BY    sr.$SESSION_ROUTER_ROW, sr.$SESSION_ROUTER_COL;
    """.trimIndent()
    val cursor = db.rawQuery(query, arrayOf(sessionId.toString()))
    if (!cursor!!.moveToFirst()) {
      return arrayListOf()
    }

    val bssidIndex = cursor.getColumnIndex("id")
    val nameIndex = cursor.getColumnIndex("name")
    val rowIndex = cursor.getColumnIndex("row")
    val colIndex = cursor.getColumnIndex("col")

    val routers = arrayListOf<Router>()
    do {
      val router = Router(
        cursor.getLong(bssidIndex),
        cursor.getString(nameIndex)
      )
      router.row = cursor.getInt(rowIndex)
      router.col = cursor.getInt(colIndex)
      routers.add(router)
    } while (cursor.moveToNext())

    return routers
  }

  // Record and Data
  /**
   * Adds a row of data to a session
   *
   * @param label
   * @param values Key Value pair of router bssid and value
   */
  fun addRow(sessionId: Long, label: Int, values: HashMap<Long, Float>) {
    val db = this.writableDatabase
    db.transaction {
      // Create a record
      val contentValues = ContentValues()
      contentValues.put(RECORD_SESSION_ID, sessionId)
      contentValues.put(RECORD_LABEL, label)

      val id: Long = this.insert(RECORD_TABLE, null, contentValues)

      for ((bssid, value) in values) {
        Log.d(TAG, "INSERTING: $bssid: $value")
        contentValues.clear()
        contentValues.put(DATA_RECORD_ID, id)
        contentValues.put(DATA_ROUTER_ID, bssid)
        contentValues.put(DATA_VALUE, value)
        this.insert(DATA_TABLE, null, contentValues)
      }
    }
  }

  /**
   * Updates a row of data
   *
   * @param sessionId To get the current routers
   * @param recordId Record id
   * @param values Array of new dbm values
   */
  fun updateRow(sessionId: Long, recordId: Long, values: ArrayList<Float>): Boolean {
    // Get the ordered session routers
    val routers = getSessionRouters(sessionId)

    if (routers.size != values.size) {
      Log.e(TAG, "Size not matching ${routers.size} ${values.size}")
      return false
    }

    val db = this.writableDatabase
    try {
      db.transaction {
        val contentValues = ContentValues()
        for (i in 0 until routers.size) {
          contentValues.clear()
          contentValues.put(DATA_VALUE, values[i])

          val wheres = arrayOf(recordId.toString(), routers[i].getBSSID().toString())
          this.update(DATA_TABLE, contentValues,
            "$DATA_RECORD_ID=? AND $DATA_ROUTER_ID=?",
            wheres
          )
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, Log.getStackTraceString(e))
      return false
    }

    return true
  }

  /**
   * Deletes a row of data
   *
   * @param recordId The record table
   */
  fun deleteRow(recordId: Long) {
    val db = this.writableDatabase
    db.delete(RECORD_TABLE, "$RECORD_ID=?", arrayOf(recordId.toString()))
  }

  /**
   * Deletes all rows from a session
   *
   * @param sessionId
   */
  fun deleteSessionRows(sessionId: Long) {
    val db = this.writableDatabase
    db.delete(RECORD_TABLE, "$RECORD_SESSION_ID=?", arrayOf(sessionId.toString()))
  }

  /**
   * Returns the whole data set of labels and values
   *
   * @param sessionId Session
   * @return
   */
  @SuppressLint("Recycle")
  fun getData(sessionId: Long): ArrayList<RowData> {
    val db = this.readableDatabase

    val session = getSessionById(sessionId) ?: return arrayListOf()
    val size = session.rows * session.cols

    // Get all rows
    val rowCursor = db.rawQuery("""
      SELECT    $RECORD_ID, $RECORD_LABEL
      FROM      $RECORD_TABLE
      WHERE     $RECORD_SESSION_ID=?
      ORDER BY  $RECORD_TIMESTAMP;
    """.trimIndent(), arrayOf(sessionId.toString()))

    if (!rowCursor.moveToFirst()) {
      Log.e(TAG, "No rows found")
      rowCursor.close()
      return arrayListOf()
    }

    // Query per row
    val idIndex = rowCursor.getColumnIndex(RECORD_ID)
    val labelIndex = rowCursor.getColumnIndex(RECORD_LABEL)
    val data = arrayListOf<RowData>()
    do {
      val rowData = RowData(rowCursor.getLong(idIndex), rowCursor.getInt(labelIndex))

      val colCursor = db.rawQuery("""
        SELECT  sr.$SESSION_ROUTER_ROUTER_ID, d.$DATA_VALUE
        FROM (
          SELECT  *
          FROM    $DATA_TABLE
          WHERE   $DATA_RECORD_ID=?
          LIMIT   ?
        ) AS d
        LEFT JOIN ( -- TODO: Can be placed in a temporary table
          SELECT  *
          FROM    $SESSION_ROUTER_TABLE
          WHERE   $SESSION_ROUTER_SESSION_ID=?
          LIMIT   ?
        ) AS sr
          ON        sr.$SESSION_ROUTER_ROUTER_ID = d.$DATA_ROUTER_ID
        ORDER BY    sr.$SESSION_ROUTER_ROW, sr.$SESSION_ROUTER_COL;
      """.trimIndent(), arrayOf(
        rowCursor.getString(idIndex), size.toString(),
        sessionId.toString(), size.toString()
      ))

      if (!colCursor.moveToFirst()) {
        Log.e(TAG, "No cols were found")
        colCursor.close()
        rowCursor.close()
        return arrayListOf() // ERROR
      }

      val valIndex = colCursor.getColumnIndex(DATA_VALUE)
      do {
        rowData.addVal(colCursor.getFloat(valIndex))
      } while (colCursor.moveToNext())
      colCursor.close()

      if (rowData.values.size != size) {
        Log.e(TAG, "Size does not match, ${rowData.values.size} != $size")
        rowCursor.close()
        return arrayListOf() // ERROR
      }

      data.add(rowData)
    } while (rowCursor.moveToNext())
    rowCursor.close()

    return data
  }

  companion object {
    // Singleton
    private var sInstance: DBHelper? = null
    @Synchronized
    fun getInstance(ctx: Context): DBHelper? {
      if (sInstance == null) {
        sInstance = DBHelper(ctx.applicationContext)
      }
      return sInstance
    }

    private const val DATABASE_NAME = "parking_rssi"
    private const val DATABASE_VERSION = 13

    // SESSIONS
    const val SESSION_TABLE = "sessions"
    const val SESSION_ID = "id"
    const val SESSION_NAME = "name"
    const val SESSION_ROWS = "rows"
    const val SESSION_COLS = "cols"

    // ROUTERS
    const val ROUTER_TABLE = "routers"
    const val ROUTER_BSSID = "bssid"
    const val ROUTER_NAME = "name"

    // SESSION_ROUTERS
    const val SESSION_ROUTER_TABLE = "session_routers"
    const val SESSION_ROUTER_ID = "id"
    const val SESSION_ROUTER_SESSION_ID = "session_id"
    const val SESSION_ROUTER_ROUTER_ID = "router_id"
    const val SESSION_ROUTER_ROW = "r"
    const val SESSION_ROUTER_COL = "c"

    // RECORDS
    const val RECORD_TABLE = "records"
    const val RECORD_ID = "id"
    const val RECORD_SESSION_ID = "session_id"
    const val RECORD_LABEL = "label"
    const val RECORD_TIMESTAMP = "timestamp"

    // DATA
    const val DATA_TABLE = "data"
    const val DATA_ID = "id"
    const val DATA_RECORD_ID = "record_id"
    const val DATA_ROUTER_ID = "router_id"
    const val DATA_VALUE = "value"
  }
}

```

## Tables

> **NOTE:** The values for each table provided is only for visualization.

### Session Table

The session table records the name, rows, and columns of a specific session.

| id  | name      | rows | cols |
| --- | --------- | ---- | ---- |
| 0   | Session 1 | 3    | 2    |

### Router Table

The router table records the BSSID or the MAC address of a router as well as its name.

| bssid           | name                                       |
| --------------- | ------------------------------------------ |
| 208914460048774 | 66852627208993-xiaomi-repeater-v3_miapdb21 |

### Session Router Table

The session router table records a router in a given session, along with its positioning in the router matrix.

| id  | session_id | router_id       | r   | c   |
| --- | ---------- | --------------- | --- | --- |
| 0   | 0          | 208914460048774 | 0   | 0   |

### Record Table

The record table records the total amount of cars (label) for a specific session as well as a timestamp to when it has been recorded.

| id  | session_id | label | timestamp  |
| --- | ---------- | ----- | ---------- |
| 0   | 0          | 5     | 1667021128 |

### Data Table

The data table links the router, and the RSSI value associated with it to a record.

| id  | record_id | router_id       | value                     |
| --- | --------- | --------------- | ------------------------- |
| 0   | 0         | 208914460048774 | [(208914460048774,-27.0)] |