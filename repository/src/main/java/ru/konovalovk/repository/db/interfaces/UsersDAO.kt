package ru.konovalovk.repository.db.interfaces

import androidx.room.*
import ru.konovalovk.repository.db.entity.Library
import ru.konovalovk.repository.db.entity.Users

@Dao
interface UsersDAO {
    @Query("SELECT * FROM USERS")
    fun getAll(): List<Users>

    @Query("SELECT * FROM USERS where id=:userId")
    fun getUsersById(userId: Int): List<Users>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @PrimaryKey(autoGenerate = true)
    fun insert(users: List<Users>)

    @Insert
    @PrimaryKey(autoGenerate = true)
    fun insert(user: Users)

    @Delete
    fun delete(user: Users)

    @Delete
    fun delete(users: List<Users>)

    @Query("DELETE FROM USERS")
    fun delete()
}