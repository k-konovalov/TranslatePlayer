package ru.konovalovk.repository.db.interfaces

import androidx.room.*
import ru.konovalovk.repository.db.entity.Media

@Dao
interface MediaDAO {
    @Query("SELECT * FROM MEDIA")
    fun getAll(): List<Media>

    @Query("SELECT * FROM MEDIA where id=:mediaId")
    fun getMediaById(mediaId: Int): List<Media>

    @Query("SELECT * FROM MEDIA where name=:name")
    fun getMediaByName(name: String): Media?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @PrimaryKey(autoGenerate = true)
    fun insert(media: List<Media>)

    @Update
    fun update(media: Media)

    @Insert
    @PrimaryKey(autoGenerate = true)
    fun insert(media: Media)

    @Delete
    fun delete(media: Media)

    @Delete
    fun delete(media: List<Media>)

    @Query("DELETE FROM MEDIA")
    fun delete()
}