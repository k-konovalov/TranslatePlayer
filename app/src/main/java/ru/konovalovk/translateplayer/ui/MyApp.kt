package ru.konovalovk.translateplayer.ui

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.konovalovk.repository.db.AppDatabase
import ru.konovalovk.repository.db.entity.Language
import ru.konovalovk.repository.db.entity.Services
import ru.konovalovk.repository.db.entity.Users
import java.util.*

class MyApp: Application() {
    val ioScope = CoroutineScope(Dispatchers.IO)
    companion object {
        val db: AppDatabase = AppDatabase.instance
    }

    override fun onCreate() {
        AppDatabase.initDb(this)
        //fillDb()
        super.onCreate()
    }

    fun fillDb(){
        ioScope.launch {
            db.usersDAO.delete()
            db.languageDAO.delete()
            db.servicesDAO.delete()
            db.usersDAO.insert(Users(uuid = UUID.randomUUID().toString()))
            db.languageDAO.insert(listOf(Language(language = "en"),Language(language = "ru")))
            db.servicesDAO.insert(Services(0, apiKey = "", name = "test", idUser = db.usersDAO.getAll()[0].id))
        }
    }
}