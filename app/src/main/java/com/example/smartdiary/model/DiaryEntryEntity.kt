package com.smartdiary.model

data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val lightLevel: Float = 0f,         // Lux detectado pelo sensor de luz
    val stepsAtTime: Int = 0,           // Passos/movimento detectados no momento
    val mood: String = "😐",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", 0f, 0, "😐", 0L)

    fun toMap(): Map<String, Any> = mapOf(
        "id"          to id,
        "userId"      to userId,
        "title"       to title,
        "description" to description,
        "imageUrl"    to imageUrl,
        "lightLevel"  to lightLevel,
        "stepsAtTime" to stepsAtTime,
        "mood"        to mood,
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

    // Retorna uma descrição textual charmosa do ambiente para o Pitch
    fun getAmbientContextDescription(): String {
        val lightDesc = when {
            lightLevel < 10f -> "Ambiente Escuro (Noite/Quarto)"
            lightLevel < 100f -> "Luz Suave (Interior)"
            else -> "Ambiente Iluminado (Dia/Exterior)"
        }
        val motionDesc = if (stepsAtTime > 0) "Em Movimento 🚶" else "Relaxado/Estático 🧘"
        return "$lightDesc • $motionDesc"
    }
}