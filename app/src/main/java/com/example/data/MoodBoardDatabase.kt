package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MoodBoard::class, SavedPin::class, MarketplaceAsset::class],
    version = 1,
    exportSchema = false
)
abstract class MoodBoardDatabase : RoomDatabase() {
    abstract fun moodBoardDao(): MoodBoardDao

    companion object {
        @Volatile
        private var INSTANCE: MoodBoardDatabase? = null

        fun getDatabase(context: Context): MoodBoardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodBoardDatabase::class.java,
                    "mood_board_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
