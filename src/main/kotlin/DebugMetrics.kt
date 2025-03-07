package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2

class DebugMetrics {
    private var fps: Int = 0
    private var frameTime: Float = 0f
    private var javaHeap: Long = 0
    private var nativeHeap: Long = 0
    private var cpuLoad: Float = 0f
    private var gpuLoad: Float = 0f
    private var enemyCount: Int = 0
    private var projectileCount: Int = 0
    private var playerPosition: Vector2 = Vector2()
    private var playerHealth: Int = 0
    private var playerLevel: Int = 1
    private var playerExp: Int = 0
    private var playerExpProgress: Float = 0f
    private var isVisible: Boolean = false

    fun update(enemies: List<Enemy>, projectiles: List<Projectile>, player: Player) {
        fps = Gdx.graphics.framesPerSecond
        frameTime = Gdx.graphics.deltaTime * 1000 // Convert to milliseconds
        javaHeap = Gdx.app.javaHeap
        nativeHeap = Gdx.app.nativeHeap
        // Note: These are rough estimates based on memory usage patterns
        cpuLoad = (Gdx.app.nativeHeap.toFloat() / (1024 * 1024 * 1024)) * 100 // Relative to 1GB
        gpuLoad = (javaHeap.toFloat() / (1024 * 1024 * 1024)) * 100 // Using Java heap as a rough GPU estimate
        enemyCount = enemies.size
        projectileCount = projectiles.size
        playerPosition.set(player.position)
        playerHealth = player.health
        playerLevel = player.experience.level
        playerExp = player.experience.currentExp
        playerExpProgress = player.experience.currentLevelProgress
    }

    fun toggleVisibility() {
        val oldState = isVisible
        isVisible = !isVisible
        println("[DEBUG_LOG] DebugMetrics visibility changed from $oldState to $isVisible")
    }

    fun isVisible(): Boolean = isVisible

    fun getMetricsText(): String {
        if (!isVisible) return ""

        return """
            FPS: $fps (Frame Time: ${frameTime.toBigDecimal().setScale(2, java.math.RoundingMode.HALF_UP)} ms)
            Memory - Java: ${javaHeap/1024/1024}MB Native: ${nativeHeap/1024/1024}MB
            CPU Load: ${cpuLoad.toInt()}% GPU Load: ${gpuLoad.toInt()}%
            Entities - Enemies: $enemyCount Projectiles: $projectileCount
            Player - Pos: (${playerPosition.x.toInt()}, ${playerPosition.y.toInt()})
            Health: $playerHealth
            Level: $playerLevel (XP: $playerExp, Progress: ${(playerExpProgress * 100).toInt()}%)
        """.trimIndent()
    }

    companion object {
        @Volatile
        private var instance: DebugMetrics? = null

        fun getInstance(): DebugMetrics {
            return instance ?: synchronized(this) {
                instance ?: DebugMetrics().also { instance = it }
            }
        }
    }
}
