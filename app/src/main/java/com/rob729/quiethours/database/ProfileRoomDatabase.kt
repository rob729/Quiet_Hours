package com.rob729.quiethours.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Profile::class], version = 7)
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
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private fun migrate(a: Int, b: Int, sql: String) = object : Migration(a, b) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(sql)
            }
        }

        val MIGRATION_1_2: Migration = migrate(
            1,
            2,
            "ALTER TABLE profile_table " + " ADD COLUMN colorIndex INTEGER NOT NULL DEFAULT 3"
        )
        val MIGRATION_2_3: Migration = migrate(
            2,
            3,
            "ALTER TABLE profile_table " + " ADD COLUMN vibSwitch INTEGER NOT NULL DEFAULT 1"
        )
        val MIGRATION_3_4: Migration = migrate(
            3,
            4,
            "ALTER TABLE profile_table " + " ADD COLUMN timeInstance TEXT NOT NULL DEFAULT ''"
        )
        val MIGRATION_4_5: Migration = migrate(
            4,
            5,
            "ALTER TABLE profile_table " + " ADD COLUMN repeatWeekly INTEGER NOT NULL DEFAULT 0"
        )
        val MIGRATION_5_6: Migration = migrate(
            5,
            6,
            "ALTER TABLE profile_table " + " ADD COLUMN pauseSwitch INTEGER NOT NULL DEFAULT 1"
        )
        val MIGRATION_6_7: Migration = migrate(
            6,
            7,
            "ALTER TABLE profile_table " + " ADD COLUMN notes TEXT NOT NULL DEFAULT ''"
        )
    }
}