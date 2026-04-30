package com.compose.yifeng2fa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS password_accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                websiteName TEXT NOT NULL,
                websiteUrl TEXT NOT NULL DEFAULT '',
                account TEXT NOT NULL DEFAULT '',
                password TEXT NOT NULL,
                note TEXT NOT NULL DEFAULT '',
                category TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL DEFAULT 0,
                updatedAt INTEGER NOT NULL DEFAULT 0
            )"""
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS strong_password_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                password TEXT NOT NULL,
                keyword1 TEXT NOT NULL DEFAULT '',
                keyword2 TEXT NOT NULL DEFAULT '',
                length INTEGER NOT NULL DEFAULT 16,
                useUppercase INTEGER NOT NULL DEFAULT 1,
                useLowercase INTEGER NOT NULL DEFAULT 1,
                useNumbers INTEGER NOT NULL DEFAULT 1,
                useSymbols INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT 0
            )"""
        )
    }
}

@Database(entities = [TotpEntity::class, PasswordEntity::class, StrongPasswordHistoryEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun totpDao(): TotpDao
    abstract fun passwordDao(): PasswordDao
    abstract fun strongPasswordHistoryDao(): StrongPasswordHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yifeng_2fa_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
