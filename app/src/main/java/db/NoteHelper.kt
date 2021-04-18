package db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import db.DatabaseContract.NoteColumns.Companion.TABLE_NAME
import db.DatabaseContract.NoteColumns.Companion._ID
import java.sql.SQLException
import kotlin.jvm.Throws

class NoteHelper(context: Context) {
    companion object
    {
        private const val DATABASE_TABLE = TABLE_NAME
        private lateinit var databaseHelper: DatabaseHelper
        private var INSTANCE : NoteHelper? = null

        private lateinit var database: SQLiteDatabase

        // using singleton pattern, which allows an object to have only one instance
        // so there is no duplicate instance
        fun getInstance(context: Context) : NoteHelper =
            INSTANCE ?: synchronized(this) {    // synchronized to avoid instance duplication
                INSTANCE ?: NoteHelper(context)      // in all thread
            }   // main function is to do data manipulating process in table like query to read data
    }

    init {
        databaseHelper = DatabaseHelper(context)
    }

    @Throws(SQLException::class)
    fun open()
    {
        database = databaseHelper.writableDatabase
    }

    fun close()
    {
        databaseHelper.close()

        if (database.isOpen)
        {
            database.close()
        }
    }

    fun queryAll() : Cursor
    {
        return database.query(DATABASE_TABLE, null, null, null,
            null, null, "$_ID ASC", null)
    }

    fun queryById(id : String) : Cursor
    {
        return database.query(DATABASE_TABLE, null, "$_ID = ?", arrayOf(id),
            null, null, null, null)
    }

    fun insert(values: ContentValues?) : Long   // object Note as input parameter
    {
        return database.insert(DATABASE_TABLE, null, values)
    }

    fun update(id : String, values : ContentValues?) : Int  // update using _ID as reference
    {
        return database.update(DATABASE_TABLE, values, "$_ID = ?", arrayOf(id))
    }

    fun deleteById(id : String) : Int   // delete data on database, id is based on Note item
    {                                   // that is chosen to delete data
        return database.delete(DATABASE_TABLE, "$_ID = '$id'", null)
    }
}