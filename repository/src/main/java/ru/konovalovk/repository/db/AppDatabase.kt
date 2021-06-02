package ru.konovalovk.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.konovalovk.repository.db.entity.*
import ru.konovalovk.repository.db.interfaces.*

@Database(
    entities = [Language::class, Library::class, Media::class, Services::class, Users::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val libraryDAO: LibraryDAO
    abstract val languageDAO: LanguageDAO
    abstract val mediaDAO: MediaDAO
    abstract val servicesDAO: ServicesDAO
    abstract val usersDAO: UsersDAO

    companion object {
        @Volatile
        var instance: AppDatabase? = null
            private set

        fun getDb(applicationContext: Context): AppDatabase {
            synchronized(this) {
                return instance ?: Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "AppDatabase.db"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                    .apply { instance = this }
            }
        }
    }
}
