package ru.konovalovk.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "SERVICES",
    foreignKeys = [ForeignKey(entity = Users::class, parentColumns = arrayOf("id"), childColumns = arrayOf("idUser"), onDelete = CASCADE)])
data class Services (
    @PrimaryKey
    val id: Int,
    val idUser: Int,
    val name: String,
    val apiKey: String
)