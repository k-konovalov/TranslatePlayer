package ru.konovalovk.repository.db.interfaces

import androidx.room.*
import ru.konovalovk.repository.db.entity.Library

@Dao
interface LibraryDAO {
    @Query("SELECT * FROM LIBRARY")
    fun getAll(): List<Library>

    @Query("SELECT * FROM LIBRARY where id=:libraryId")
    fun getWordsById(libraryId: String): List<Library>

    @Query("SELECT * FROM LIBRARY where originalWord=:originalWord")
    fun getWordByOriginal(originalWord: String): Library

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @PrimaryKey(autoGenerate = true)
    fun insert(actors: List<Library>)

    @Update
    fun update(library: Library)

    @Insert
    @PrimaryKey(autoGenerate = true)
    fun insert(actor: Library)

    @Delete
    fun delete(actor: Library)

    @Delete
    fun delete(actors: List<Library>)

    @Query("DELETE FROM LIBRARY")
    fun delete()

}