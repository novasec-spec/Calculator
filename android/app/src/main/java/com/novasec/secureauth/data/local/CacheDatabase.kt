package com.novasec.secureauth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CachedUser::class, AuthAttempt::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun authAttemptDao(): AuthAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: CacheDatabase? = null

        fun getInstance(context: Context): CacheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CacheDatabase::class.java,
                    "secureauth_cache"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
