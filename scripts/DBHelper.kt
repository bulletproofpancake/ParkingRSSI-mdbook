package com.silentrald.parkingrssi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $ROUTER_TABLE (
                $ROUTER_BSSID   INTEGER PRIMARY KEY,
                $ROUTER_NAME    VARCHAR(32) NOT NULL,
                $ROUTER_ACTIVE  BOOLEAN NOT NULL DEFAULT 1,
                $ROUTER_ROW     INTEGER NOT NULL,
                $ROUTER_COL     INTEGER NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $DATA_TABLE (
                $DATA_ID       INTEGER PRIMARY KEY,
                $DATA_INPUT    INTEGER NOT NULL,
                $DATA_LEVELS   TEXT NOT NULL,
                $DATA_LABELS   TEXT NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            INSERT INTO $DATA_TABLE (
                $DATA_ID,
                $DATA_INPUT,
                $DATA_LEVELS,
                $DATA_LABELS
            )
            VALUES (0, 0, '', '');
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $DATA_TABLE")
        db.execSQL("""DROP TABLE IF EXISTS $ROUTER_TABLE""")
        onCreate(db)
    }

    fun addRouter(bssidStr: String, name: String, row: Int, col: Int, active: Boolean = true) {
        val bssid = Router.convertBSSIDToLong(bssidStr)
        addRouter(bssid, name, row, col, active)
    }

    fun addRouter(bssid: Long, name: String, row: Int, col: Int, active: Boolean = true) {
        val values = ContentValues()
        values.put(ROUTER_BSSID, bssid)
        values.put(ROUTER_NAME, name)
        values.put(ROUTER_ACTIVE, active)
        values.put(ROUTER_ROW, row)
        values.put(ROUTER_COL, col)

        val db = this.writableDatabase
        db.insert(ROUTER_TABLE, null, values)
        db.close()
    }

    fun getRouters(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("""
            SELECT  *
            FROM    $ROUTER_TABLE;
        """.trimIndent(), null)
    }

//    fun removeRouter(bssidStr: String) {
//        val bssid = Router.convertBSSIDToLong(bssidStr)
//        removeRouter(bssid)
//    }

//    fun removeRouter(bssid: Long) {
//        val db = this.writableDatabase
//        db.delete(ROUTER_TABLE, "$ROUTER_BSSID=?", arrayOf(bssid.toString()))
//        db.close()
//    }

    fun removeAllRouters() {
        val db = this.writableDatabase
        db.delete(ROUTER_TABLE, "", null)
        db.close()
    }

    fun getData(): Data {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT  *
            FROM    $DATA_TABLE
            WHERE   $DATA_ID = 0
            LIMIT   1;
        """.trimIndent(), null
        )

        cursor.moveToFirst()
        val matrixString = cursor.getString(cursor.getColumnIndex(DATA_LEVELS))
        val labelsString = cursor.getString(cursor.getColumnIndex(DATA_LABELS))
        val input = cursor.getInt(cursor.getColumnIndex(DATA_INPUT))
        cursor.close()

        val matrix = arrayListOf<ArrayList<Float>>()
        if (!matrixString.equals("")) {
            val array = matrixString.split("\n")
            for (str in array) {
                val floatString = str.split(",")
                val floats = arrayListOf<Float>()
                for (fs in floatString) {
                    floats.add(fs.toFloat())
                }
                matrix.add(floats)
            }
        }

        val outputs = arrayListOf<Int>()
        if (!labelsString.equals("")) {
            val labels = labelsString.split(",")
            for (l in labels) {
                outputs.add(l.toInt())
            }
        }

        return Data(matrix, outputs, input)
    }

    fun setData(matrix: ArrayList<ArrayList<Float>>, labels: ArrayList<Int>, input: Int) {
        val array = arrayListOf<String>()
        for (vector in matrix) {
            array.add(vector.joinToString(","))
        }
        val matrixString = array.joinToString("\n")
        val labelsString = labels.joinToString(",")

        val values = ContentValues()
        values.put(DATA_INPUT, input)
        values.put(DATA_LEVELS, matrixString)
        values.put(DATA_LABELS, labelsString)

        val db = this.writableDatabase
        db.update(DATA_TABLE, values, "$DATA_ID=0", null)
        db.close()
    }

//    fun clearData() {
//        val values = ContentValues()
//        values.put(DATA_LEVELS, "")
//        values.put(DATA_LABELS, "")
//
//        val db = this.writableDatabase
//        db.update(DATA_TABLE, values, "$DATA_ID=0", null)
//        db.close()
//    }

    companion object{
        private const val DATABASE_NAME = "parking_rssi"
        private const val DATABASE_VERSION = 8

        const val ROUTER_TABLE = "routers"
        const val ROUTER_BSSID = "bssid"
        const val ROUTER_NAME = "name"
        const val ROUTER_ACTIVE = "active"
        const val ROUTER_ROW = "r"
        const val ROUTER_COL = "c"

        const val DATA_TABLE = "data"
        const val DATA_ID = "id"
        const val DATA_INPUT = "input"
        const val DATA_LEVELS = "levels"
        const val DATA_LABELS = "label"
    }

    class Data (
        val matrix: ArrayList<ArrayList<Float>>,
        val labels: ArrayList<Int>,
        val input: Int
        ) {}
}