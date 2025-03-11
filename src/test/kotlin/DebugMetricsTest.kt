package org.example

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import com.badlogic.gdx.math.Vector2
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class DebugMetricsTest {
    private lateinit var app: Application
    private lateinit var debugMetrics: DebugMetrics
    private lateinit var player: Player
    private lateinit var enemies: List<Enemy>
    private lateinit var projectiles: List<Projectile>

    init {
        // Initialize LibGDX headless backend
        val config = HeadlessApplicationConfiguration()
        app = HeadlessApplication(object : com.badlogic.gdx.ApplicationAdapter() {
            override fun create() {
                // Initialize OpenGL context
                Gdx.gl = mock()
                Gdx.gl20 = mock()
                Gdx.graphics = mock()
                Gdx.app = mock()
            }
        }, config)
    }

    @BeforeEach
    fun setUp() {
        debugMetrics = DebugMetrics.getInstance()
        // Ensure metrics start invisible
        if (debugMetrics.isVisible()) {
            debugMetrics.toggleVisibility()
        }
        println("[DEBUG_LOG] Initial visibility set to: ${debugMetrics.isVisible()}")

        // Mock player
        player = mock()
        whenever(player.position).thenReturn(Vector2(100f, 100f))
        whenever(player.health).thenReturn(80)

        val experience = mock<Experience>()
        whenever(experience.level).thenReturn(5)
        whenever(experience.currentExp).thenReturn(150)
        whenever(experience.currentLevelProgress).thenReturn(0.75f)
        whenever(player.experience).thenReturn(experience)

        // Create test enemies and projectiles
        enemies = listOf(
            mock<Enemy>(),
            mock<Enemy>()
        )

        projectiles = listOf(
            mock<Projectile>(),
            mock<Projectile>(),
            mock<Projectile>()
        )

        // Mock Gdx metrics
        whenever(Gdx.graphics.framesPerSecond).thenReturn(60)
        whenever(Gdx.graphics.deltaTime).thenReturn(0.016f) // ~16ms per frame at 60 FPS
        whenever(Gdx.app.javaHeap).thenReturn(1024L * 1024L * 100L) // 100MB
        whenever(Gdx.app.nativeHeap).thenReturn(1024L * 1024L * 50L) // 50MB
    }

    @Test
    fun `test metrics collection`() {
        // Make metrics visible and verify state
        debugMetrics.toggleVisibility()
        assert(debugMetrics.isVisible()) { "Debug metrics should be visible for testing" }
        println("[DEBUG_LOG] Metrics visibility: ${debugMetrics.isVisible()}")

        // Update and get metrics
        debugMetrics.update(enemies, projectiles, player)
        val metricsText = debugMetrics.getMetricsText()
        println("[DEBUG_LOG] Metrics text: $metricsText")
        assert(metricsText.contains("FPS: 60")) { "Should contain FPS" }
        assert(metricsText.contains("Frame Time: 16.00 ms")) { "Should contain frame time" }
        assert(metricsText.contains("Memory - Java: 100MB")) { "Should contain Java heap" }
        assert(metricsText.contains("Native: 50MB")) { "Should contain Native heap" }
        assert(metricsText.contains("CPU Load: 4%")) { "Should contain CPU load" }
        assert(metricsText.contains("GPU Load: 9%")) { "Should contain GPU load" }
        assert(metricsText.contains("Enemies: 2 Projectiles: 3")) { "Should contain entity counts" }
        assert(metricsText.contains("Pos: (100, 100)")) { "Should contain player position" }
        assert(metricsText.contains("Health: 80")) { "Should contain player health" }
        assert(metricsText.contains("Level: 5")) { "Should contain player level" }
        assert(metricsText.contains("Progress: 75%")) { "Should contain experience progress" }
    }

    @Test
    fun `test visibility toggle`() {
        // Verify initial state (should be invisible from setUp)
        println("[DEBUG_LOG] Initial visibility in toggle test: ${debugMetrics.isVisible()}")
        assert(!debugMetrics.isVisible()) { "Debug metrics should be initially invisible" }
        assert(debugMetrics.getMetricsText().isEmpty()) { "Metrics text should be empty when invisible" }

        // Toggle visibility
        debugMetrics.toggleVisibility()
        assert(debugMetrics.isVisible()) { "Debug metrics should be visible after toggle" }
        assert(debugMetrics.getMetricsText().isNotEmpty()) { "Metrics text should not be empty when visible" }

        // Toggle back
        debugMetrics.toggleVisibility()
        assert(!debugMetrics.isVisible()) { "Debug metrics should be invisible after second toggle" }
        assert(debugMetrics.getMetricsText().isEmpty()) { "Metrics text should be empty when invisible again" }
    }
}
