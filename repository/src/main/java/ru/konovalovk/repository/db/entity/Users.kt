package ru.konovalovk.repository.db.entity;

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "USERS")
data class Users (
    @PrimaryKey
    val id: Int,
    val uuid: Int
)