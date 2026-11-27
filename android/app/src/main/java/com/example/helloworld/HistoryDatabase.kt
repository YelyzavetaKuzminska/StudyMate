package com.example.helloworld

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class HistoryRecord(
    val id: Long,
    val task: String,
    val category: String,
    val recommendation: String,
    val createdAt: Long,
    val status: String
)

class HistoryDatabase(context: Context) :
    SQLiteOpenHelper(context, "studymate_history.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                task TEXT NOT NULL,
                category TEXT NOT NULL,
                recommendation TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                status TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Початкова версія бази даних.
    }

    fun addRecord(
        task: String,
        category: String,
        recommendation: String
    ): Long {
        val values = ContentValues().apply {
            put("task", task)
            put("category", category)
            put("recommendation", recommendation)
            put("created_at", System.currentTimeMillis())
            put("status", "Готово")
        }

        return writableDatabase.insertOrThrow(
            "history", null, values
        )
    }

    fun getAllRecords(): List<HistoryRecord> {
        val records = mutableListOf<HistoryRecord>()

        readableDatabase.rawQuery(
            "SELECT * FROM history ORDER BY created_at DESC, id DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                records.add(readRecord(cursor))
            }
        }

        return records
    }

    fun getRecord(id: Long): HistoryRecord? {
        readableDatabase.rawQuery(
            "SELECT * FROM history WHERE id = ?",
            arrayOf(id.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                readRecord(cursor)
            } else {
                null
            }
        }
    }

    fun deleteRecord(id: Long): Int {
        return writableDatabase.delete(
            "history",
            "id = ?",
            arrayOf(id.toString())
        )
    }

    private fun readRecord(cursor: Cursor): HistoryRecord {
        return HistoryRecord(
            id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id")
            ),
            task = cursor.getString(
                cursor.getColumnIndexOrThrow("task")
            ),
            category = cursor.getString(
                cursor.getColumnIndexOrThrow("category")
            ),
            recommendation = cursor.getString(
                cursor.getColumnIndexOrThrow("recommendation")
            ),
            createdAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")
            ),
            status = cursor.getString(
                cursor.getColumnIndexOrThrow("status")
            )
        )
    }
}
