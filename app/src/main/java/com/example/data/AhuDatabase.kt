package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AhuEntry::class], version = 1, exportSchema = false)
abstract class AhuDatabase : RoomDatabase() {
    abstract fun ahuDao(): AhuDao

    companion object {
        @Volatile
        private var INSTANCE: AhuDatabase? = null

        fun getDatabase(context: Context): AhuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AhuDatabase::class.java,
                    "ahu_savings_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
