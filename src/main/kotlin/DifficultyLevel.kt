package org.example

enum class DifficultyLevel(
    val displayName: String,
    val enemyHealthMultiplier: Float,
    val enemyDamageMultiplier: Float,
    val experienceMultiplier: Float,
    val enemySpawnRateMultiplier: Float
) {
    EASY("Easy", 0.7f, 0.7f, 1.3f, 0.8f),
    NORMAL("Normal", 1.0f, 1.0f, 1.0f, 1.0f),
    HARD("Hard", 1.5f, 1.3f, 0.8f, 1.2f);

    companion object {
        fun getDefault() = NORMAL
    }
}