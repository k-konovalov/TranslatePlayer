package ru.konovalovk.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LANGUAGES")
data class Language (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val language: String
)