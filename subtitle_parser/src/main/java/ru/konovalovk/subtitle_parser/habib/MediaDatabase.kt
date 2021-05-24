package ru.konovalovk.subtitle_parser.habib

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import java.util.*

object MediaDatabase {
    private const val ANKI_WORDS_TABLE_NAME = "anki_words_table"
    private const val ANKI_WORD = "anki_word"
    private const val ANKI_NOTE_ID = "anki_note_id"

    private val mDb: SQLiteDatabase? = null

    @Synchronized
    fun saveAnkiWord(word: String?, noteId: Long) {
        if (TextUtils.isEmpty(word)) return
        val values = ContentValues()
        values.put(ANKI_WORD, word)
        values.put(ANKI_NOTE_ID, noteId)
        mDb?.replace(ANKI_WORDS_TABLE_NAME, null, values)
    }

    @Synchronized
    fun getAnkiNoteId(word: String?): ArrayList<Long>? {
        val noteIdList = ArrayList<Long>()
        if (TextUtils.isEmpty(word)) return noteIdList
        val cursor = mDb?.query(ANKI_WORDS_TABLE_NAME, arrayOf(ANKI_NOTE_ID), ANKI_WORD + "=?", arrayOf(word), null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val noteID = cursor.getLong(0)
                noteIdList.add(noteID)
            }
        }
        cursor?.close()
        return noteIdList
    }
}