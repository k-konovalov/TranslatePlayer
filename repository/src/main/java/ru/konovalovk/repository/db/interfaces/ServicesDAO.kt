package ru.konovalovk.repository.db.interfaces

import androidx.room.*
import ru.konovalovk.repository.db.entity.Media
import ru.konovalovk.repository.db.entity.Services

@Dao
interface ServicesDAO {
    @Query("SELECT * FROM SERVICES")
    fun getAll(): List<Media>

    @Query("SELECT * FROM SERVICES where id=:serviceId")
    fun getServicesById(serviceId: Int): List<Services>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @PrimaryKey(autoGenerate = true)
    fun insert(services: List<Services>)

    @Insert
    @PrimaryKey(autoGenerate = true)
    fun insert(services: Services)

    @Delete
    fun delete(services: Services)

    @Delete
    fun delete(services: List<Services>)

    @Query("DELETE FROM Services")
    fun delete()
}