package helper

import android.database.Cursor
import db.DatabaseContract
import entity.Note

object MappingHelper {
    fun mapCursorToArrayList(notesCursor : Cursor?) : ArrayList<Note>
    {
        val notesList = ArrayList<Note>()

        notesCursor?.apply {
            while (moveToNext())    // get data one by one, then put it in ArrayList
            {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID))
                val title = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.TITLE))
                val description = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION))
                val date = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DATE))
                notesList.add(Note(id, title, description, date))
            }
        }

        return notesList
    }
}

// db query results is cursor, so we need to convert it to arraylist in order to show it to list

// MoveToFirst : move cursor to first row
// MoveToNext : move cursor to next row

//Fungsi apply digunakan untuk menyederhanakan kode yang berulang. Misalnya notesCursor.geInt cukup
// ditulis getInt dan notesCursor.getColumnIndexOrThrow cukup ditulis getColumnIndexOrThrow.