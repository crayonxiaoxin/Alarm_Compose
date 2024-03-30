package com.github.crayonxiaoxin.alarmclock_compose.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.crayonxiaoxin.alarmclock_compose.data.db.dao.AlarmDao
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import java.util.concurrent.Executors

/**
 * Room 数据库
 */
@Database(
    entities = [Alarm::class],
    version = 1,
    exportSchema = true,
    autoMigrations = [],
)
@TypeConverters(Converters::class)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "hera_db"
                )
                    .setQueryCallback({ sqlQuery, bindArgs ->
//                        Log.e("SQL", sqlQuery)
//                        Log.e("SQL Args", bindArgs.toString())
                    }, Executors.newSingleThreadExecutor())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}