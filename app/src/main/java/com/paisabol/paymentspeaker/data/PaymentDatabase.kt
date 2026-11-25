package com.paisabol.paymentspeaker.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.paisabol.paymentspeaker.model.PaymentRecord
import com.paisabol.paymentspeaker.model.PaymentSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DB_NAME,
    null,
    DB_VERSION
) {

    private val _payments = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val payments: StateFlow<List<PaymentRecord>> = _payments.asStateFlow()

    init {
        refresh()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PAYMENTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_AMOUNT REAL NOT NULL,
                $COL_SOURCE TEXT NOT NULL,
                $COL_SOURCE_LABEL TEXT NOT NULL,
                $COL_SENDER TEXT,
                $COL_PACKAGE TEXT NOT NULL,
                $COL_RAW_TITLE TEXT NOT NULL,
                $COL_RAW_TEXT TEXT NOT NULL,
                $COL_SPOKEN_TEXT TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_payments_ts ON $TABLE_PAYMENTS($COL_TIMESTAMP DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAYMENTS")
        onCreate(db)
    }

    @Synchronized
    fun insert(record: PaymentRecord): Long {
        val values = ContentValues().apply {
            put(COL_AMOUNT, record.amount)
            put(COL_SOURCE, record.source.name)
            put(COL_SOURCE_LABEL, record.sourceLabel)
            put(COL_SENDER, record.senderName)
            put(COL_PACKAGE, record.packageName)
            put(COL_RAW_TITLE, record.rawTitle)
            put(COL_RAW_TEXT, record.rawText)
            put(COL_SPOKEN_TEXT, record.spokenText)
            put(COL_TIMESTAMP, record.timestampMillis)
        }
        val id = writableDatabase.insert(TABLE_PAYMENTS, null, values)
        refresh()
        return id
    }

    @Synchronized
    fun deleteById(id: Long) {
        writableDatabase.delete(TABLE_PAYMENTS, "$COL_ID = ?", arrayOf(id.toString()))
        refresh()
    }

    @Synchronized
    fun clearAll() {
        writableDatabase.delete(TABLE_PAYMENTS, null, null)
        refresh()
    }

    @Synchronized
    fun refresh() {
        val list = mutableListOf<PaymentRecord>()
        readableDatabase.query(
            TABLE_PAYMENTS,
            null,
            null,
            null,
            null,
            null,
            "$COL_TIMESTAMP DESC",
            "500"
        ).use { cursor ->
            val idxId = cursor.getColumnIndexOrThrow(COL_ID)
            val idxAmount = cursor.getColumnIndexOrThrow(COL_AMOUNT)
            val idxSource = cursor.getColumnIndexOrThrow(COL_SOURCE)
            val idxSourceLabel = cursor.getColumnIndexOrThrow(COL_SOURCE_LABEL)
            val idxSender = cursor.getColumnIndexOrThrow(COL_SENDER)
            val idxPkg = cursor.getColumnIndexOrThrow(COL_PACKAGE)
            val idxTitle = cursor.getColumnIndexOrThrow(COL_RAW_TITLE)
            val idxText = cursor.getColumnIndexOrThrow(COL_RAW_TEXT)
            val idxSpoken = cursor.getColumnIndexOrThrow(COL_SPOKEN_TEXT)
            val idxTs = cursor.getColumnIndexOrThrow(COL_TIMESTAMP)

            while (cursor.moveToNext()) {
                list.add(
                    PaymentRecord(
                        id = cursor.getLong(idxId),
                        amount = cursor.getDouble(idxAmount),
                        source = PaymentSource.fromName(cursor.getString(idxSource)),
                        sourceLabel = cursor.getString(idxSourceLabel),
                        senderName = cursor.getString(idxSender),
                        packageName = cursor.getString(idxPkg),
                        rawTitle = cursor.getString(idxTitle),
                        rawText = cursor.getString(idxText),
                        spokenText = cursor.getString(idxSpoken),
                        timestampMillis = cursor.getLong(idxTs)
                    )
                )
            }
        }
        _payments.value = list
    }

    companion object {
        private const val DB_NAME = "paisabol_payments.db"
        private const val DB_VERSION = 1
        private const val TABLE_PAYMENTS = "payments"

        private const val COL_ID = "id"
        private const val COL_AMOUNT = "amount"
        private const val COL_SOURCE = "source"
        private const val COL_SOURCE_LABEL = "source_label"
        private const val COL_SENDER = "sender_name"
        private const val COL_PACKAGE = "package_name"
        private const val COL_RAW_TITLE = "raw_title"
        private const val COL_RAW_TEXT = "raw_text"
        private const val COL_SPOKEN_TEXT = "spoken_text"
        private const val COL_TIMESTAMP = "timestamp_millis"
    }
}
