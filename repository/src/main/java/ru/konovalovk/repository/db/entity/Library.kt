package ru.konovalovk.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "LIBRARY",
foreignKeys = [
    ForeignKey(entity = Users::class, parentColumns = arrayOf("id"), childColumns = arrayOf("userId")),
    ForeignKey(entity = Language::class, parentColumns = arrayOf("id"), childColumns = arrayOf("languageOriginalId")),
    ForeignKey(entity = Language::class, parentColumns = arrayOf("id"), childColumns = arrayOf("languageTranslationId")),
    ForeignKey(entity = Services::class, parentColumns = arrayOf("id"), childColumns = arrayOf("serviceId")),
    ForeignKey(entity = Media::class, parentColumns = arrayOf("id"), childColumns = arrayOf("mediaId"))]
)
data class Library (
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val languageOriginalId: Int,
    val languageTranslationId: Int,
    val serviceId: Int,
    val mediaId: Int,
    val originalWord: String,
    val translatedWord: String,
    val translationFrequency: Int,
    val transcription: String?,
    val variants: String)
