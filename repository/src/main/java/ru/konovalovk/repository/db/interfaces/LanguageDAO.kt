package ru.konovalovk.repository.db.interfaces

import androidx.room.*
import ru.konovalovk.repository.db.entity.Language

@Dao
interface LanguageDAO {
    @Query("SELECT * FROM LANGUAGES")
    fun getAll(): List<Language>

    @Query("SELECT * FROM LANGUAGES where id=:languageId")
    fun getLanguageById(languageId: Int): List<Language>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(languages: List<Language>)

    @Insert
    @PrimaryKey(autoGenerate = true)
    fun insert(language: Language)

    @Delete
    fun delete(language: Language)

    @Delete
    fun delete(languages: List<Language>)

    @Query("DELETE FROM LANGUAGES")
    fun delete()
}