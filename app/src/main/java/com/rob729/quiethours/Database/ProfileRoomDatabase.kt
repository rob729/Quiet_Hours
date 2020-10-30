package com.rob729.quiethours.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Profile::class], version = 5)
abstract class ProfileRoomDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDAO

    companion object {
        @Volatile
        private var INSTANCE: ProfileRoomDatabase? = null

        fun getDatabase(context: Context): ProfileRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProfileRoomDatabase::class.java,
                    "Profile_Database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profile_table " +
                        " ADD COLUMN colorIndex INTEGER NOT NULL DEFAULT 3")
            }
        }
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profile_table " +
                        " ADD COLUMN vibSwitch INTEGER NOT NULL DEFAULT 1")
            }
        }
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profile_table " +
                        " ADD COLUMN timeInstance TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profile_table " +
                        " ADD COLUMN repeatWeekly INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}