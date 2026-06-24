package com.smartdiary.model

data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val lightLevel: Float = 0f,
    val mood: String = "😐",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", 0f, "😐", 0.0, 0.0, 0L)

    fun toMap(): Map<String, Any> = mapOf(
        "id"          to id,
        "userId"      to userId,
        "title"       to title,
        "description" to description,
        "imageUrl"    to imageUrl,
        "lightLevel"  to lightLevel,
        "mood"        to mood,
        "latitude"    to latitude,
        "longitude"   to longitude,
        "createdAt"   to createdAt
    )

    fun moodLabel(): String = when (mood) {
        "😄" -> "Feliz"
        "😔" -> "Triste"
        "😤" -> "Estressado"
        "😴" -> "Cansado"
        "😐" -> "Neutro"
        else -> "Neutro"
    }

    fun hasLocation(): Boolean = latitude != 0.0 && longitude != 0.0
}