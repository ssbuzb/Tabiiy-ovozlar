package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CustomPresetEntity::class,
        FavoriteEntity::class,
        UserSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NatureCalmDatabase : RoomDatabase() {

    abstract fun soundscapeDao(): SoundscapeDao

    companion object {
        @Volatile
        private var INSTANCE: NatureCalmDatabase? = null

        fun getInstance(context: Context): NatureCalmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NatureCalmDatabase::class.java,
                    "nature_calm.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
