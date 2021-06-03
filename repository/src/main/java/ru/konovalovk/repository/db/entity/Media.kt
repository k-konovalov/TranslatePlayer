package ru.konovalovk.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MEDIA")
data class Media (
    @PrimaryKey
    val id: Int?,
    val name: String,
    val duration: Int?
)