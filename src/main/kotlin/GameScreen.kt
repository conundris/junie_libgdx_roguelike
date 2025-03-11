package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.Input.Keys

class GameScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val camera: OrthographicCamera,
    private val shapeRenderer: ShapeRenderer,
    private val ui: GameUI,
    private val difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
    private val mapType: MapType = MapType.FOREST
) : Screen {
    internal lateinit var player: Player
    internal val enemies = mutableListOf<Enemy>()  // Keep Enemy type for compatibility
    internal val powerUps = mutableListOf<PowerUp>()
    internal var spawnTimer = 0f
    internal var spawnInterval = 2f  // Initial spawn interval
    internal var powerUpTimer = 0f
    internal var powerUpInterval = 10f  // Spawn power-up every 10 seconds

    companion object {
        const val WORLD_WIDTH = 2400f  // 3x original width
        const val WORLD_HEIGHT = 1800f // 3x original height
        const val VIEWPORT_WIDTH = 800f
        const val VIEWPORT_HEIGHT = 600f
        const val SPAWN_MARGIN = 50f   // Margin for enemy spawning outside viewport

        fun create(
            game: VampireSurvivorsGame,
            weaponType: WeaponType = WeaponType.SIMPLE,
            difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
            mapType: MapType = MapType.FOREST
        ): GameScreen {
            val camera = OrthographicCamera().apply {
                setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
                position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0f)
            }
            return createWithCamera(game, weaponType, difficulty, mapType, camera)
        }

        private fun createWithCamera(
            game: VampireSurvivorsGame,
            weaponType: WeaponType,
            difficulty: DifficultyLevel,
            mapType: MapType,
            camera: OrthographicCamera
        ): GameScreen {
            val screen = GameScreen(
                game = game,
                camera = camera,
                shapeRenderer = ShapeRenderer(),
                ui = GameUI(game.getBatch()),
                difficulty = difficulty,
                mapType = mapType
            )
            screen.player = Player(weaponType).apply {
                // Start player at the center of the world
                position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2)
            }
            return screen
        }

        // Factory method for testing
        internal fun createForTesting(
            game: VampireSurvivorsGame,
            camera: OrthographicCamera,
            shapeRenderer: ShapeRenderer,
            ui: GameUI,
            difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
            mapType: MapType = MapType.FOREST
        ): GameScreen {
            val screen = GameScreen(game, camera, shapeRenderer, ui, difficulty, mapType)
            screen.player = Player(WeaponType.SIMPLE).apply {
                position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2)
            }
            return screen
        }
    }

    internal fun resetGame() {
        player = Player(player.weapon.getCurrentWeaponType())
        enemies.clear()
        powerUps.clear()
        spawnTimer = 0f
        powerUpTimer = 0f
        gameTime = 0f
        difficultyLevel = 1
        spawnInterval = 2f / difficulty.enemySpawnRateMultiplier
        powerUpInterval = 10f
    }

    internal var gameTime = 0f
    internal var difficultyLevel = 1

    internal fun updateDifficulty(delta: Float) {
        gameTime += delta
        difficultyLevel = (gameTime / 60f).toInt() + 1  // Increase difficulty every minute

        // Adjust spawn interval based on difficulty and difficulty level multiplier
        val baseInterval = 1.5f / difficultyLevel  // Reduced base interval
        spawnInterval = (baseInterval / difficulty.enemySpawnRateMultiplier).coerceAtLeast(0.3f)  // Lower minimum interval
    }

    private fun spawnEnemy() {
        // Get camera viewport bounds
        val viewportLeft = (camera.position.x - VIEWPORT_WIDTH / 2).coerceIn(0f, WORLD_WIDTH - VIEWPORT_WIDTH)
        val viewportRight = viewportLeft + VIEWPORT_WIDTH
        val viewportBottom = (camera.position.y - VIEWPORT_HEIGHT / 2).coerceIn(0f, WORLD_HEIGHT - VIEWPORT_HEIGHT)
        val viewportTop = viewportBottom + VIEWPORT_HEIGHT

        // Adjust spawn behavior based on map type
        val spawnMargin = when (mapType) {
            MapType.DESERT -> SPAWN_MARGIN * 1.5f  // Enemies visible from further away in desert
            MapType.DUNGEON -> SPAWN_MARGIN * 0.7f // Enemies spawn closer in dungeon
            else -> SPAWN_MARGIN
        }

        // Spawn enemies just outside the visible area
        val side = (0..3).random() // 0: top, 1: right, 2: bottom, 3: left
        val (x, y) = when (side) {
            0 -> Pair(((viewportLeft - spawnMargin).toInt()..(viewportRight + spawnMargin).toInt()).random().toFloat(), 
                     viewportTop + spawnMargin) // top
            1 -> Pair(viewportRight + spawnMargin,
                     ((viewportBottom - spawnMargin).toInt()..(viewportTop + spawnMargin).toInt()).random().toFloat()) // right
            2 -> Pair(((viewportLeft - spawnMargin).toInt()..(viewportRight + spawnMargin).toInt()).random().toFloat(),
                     viewportBottom - spawnMargin) // bottom
            else -> Pair(viewportLeft - spawnMargin,
                     ((viewportBottom - spawnMargin).toInt()..(viewportTop + spawnMargin).toInt()).random().toFloat()) // left
        }

        // Create enemy with difficulty-adjusted stats and map-specific modifications
        val enemy = Enemy(x, y).apply {
            // Base difficulty adjustments
            health = (health * difficulty.enemyHealthMultiplier).toInt()
            damage = (damage * difficulty.enemyDamageMultiplier).toInt()

            // Map-specific modifications
            when (mapType) {
                MapType.FOREST -> {
                    // Balanced stats
                }
                MapType.DESERT -> {
                    speed *= 1.2f  // Faster enemies in the desert
                    health = (health * 0.8f).toInt()  // But lower health
                }
                MapType.DUNGEON -> {
                    speed *= 0.8f  // Slower in tight spaces
                    damage = (damage * 1.2f).toInt()  // But hit harder
                }
                MapType.CASTLE -> {
                    health = (health * 1.2f).toInt()  // Tougher enemies
                    speed *= 0.9f  // Slightly slower
                }
                MapType.GRAVEYARD -> {
                    damage = (damage * 1.3f).toInt()  // More dangerous
                    health = (health * 0.7f).toInt()  // But easier to kill
                }
            }
        }
        enemies.add(enemy)
    }

    private fun spawnPowerUp() {
        // Get camera viewport bounds with margin
        val margin = 50f
        val viewportLeft = (camera.position.x - VIEWPORT_WIDTH / 2).coerceIn(0f, WORLD_WIDTH - VIEWPORT_WIDTH)
        val viewportRight = viewportLeft + VIEWPORT_WIDTH
        val viewportBottom = (camera.position.y - VIEWPORT_HEIGHT / 2).coerceIn(0f, WORLD_HEIGHT - VIEWPORT_HEIGHT)
        val viewportTop = viewportBottom + VIEWPORT_HEIGHT

        // Spawn power-up within visible area (with margin)
        val x = ((viewportLeft + margin).toInt()..(viewportRight - margin).toInt()).random().toFloat()
        val y = ((viewportBottom + margin).toInt()..(viewportTop - margin).toInt()).random().toFloat()

        // Adjust power-up spawn based on map type
        val type = when (mapType) {
            MapType.FOREST -> {
                // Balanced power-up distribution
                PowerUpType.values().random()
            }
            MapType.DESERT -> {
                // More speed and damage boosts in the desert
                when {
                    Math.random() < 0.3 -> PowerUpType.SPEED
                    Math.random() < 0.5 -> PowerUpType.DAMAGE
                    else -> PowerUpType.values().random()
                }
            }
            MapType.DUNGEON -> {
                // More health in the dangerous dungeon
                if (Math.random() < 0.4) PowerUpType.HEALTH else PowerUpType.values().random()
            }
            MapType.CASTLE -> {
                // More damage boosts in the castle
                if (Math.random() < 0.4) PowerUpType.DAMAGE else PowerUpType.values().random()
            }
            MapType.GRAVEYARD -> {
                // More experience in the graveyard
                if (Math.random() < 0.4) PowerUpType.EXPERIENCE else PowerUpType.values().random()
            }
        }
        powerUps.add(PowerUp(Vector2(x, y), type))
    }

    internal fun updateEnemies(delta: Float) {
        // Update difficulty and spawn timers
        updateDifficulty(delta)
        spawnTimer += delta
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f
            // Spawn multiple enemies based on difficulty level
            val spawnCount = (2 + (difficultyLevel / 2)).coerceAtMost(8)  // Start with 2, add 1 per 2 levels, max 8
            repeat(spawnCount) {
                spawnEnemy()
            }
        }

        // Update power-up spawn timer
        powerUpTimer += delta
        if (powerUpTimer >= powerUpInterval) {
            powerUpTimer = 0f
            spawnPowerUp()
        }

        // Update existing enemies
        enemies.forEach { enemy ->
            enemy.update(delta, player.position)

            // Check collision with player
            if (enemy.overlaps(player)) {
                player.takeDamage(enemy.damage)
                // Simple knockback effect
                enemy.position.x += (enemy.position.x - player.position.x) * 0.5f
                enemy.position.y += (enemy.position.y - player.position.y) * 0.5f
            }
        }

        // Update and check power-ups
        powerUps.removeAll { !it.update(delta) }  // Remove expired power-ups

        // Check power-up collection
        val collectedPowerUps = powerUps.filter { it.overlaps(player) }
        collectedPowerUps.forEach { it.applyEffect(player) }
        powerUps.removeAll { it.overlaps(player) }

        // Handle dead enemies and experience gain
        val deadEnemies = enemies.filter { !it.isAlive() }
        deadEnemies.forEach { enemy ->
            val adjustedExp = (enemy.expValue * difficulty.experienceMultiplier).toInt()
            player.gainExperience(adjustedExp)
        }
        enemies.removeAll { !it.isAlive() }
    }

    override fun render(delta: Float) {
        // Handle game restart
        if (!player.isAlive() && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            resetGame()
            return
        }

        // Handle upgrade selection input
        if (player.experience.isSelectingUpgrade) {
            if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
                player.experience.selectUpgrade(0)
            } else if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
                player.experience.selectUpgrade(1)
            } else if (Gdx.input.isKeyJustPressed(Keys.NUM_3)) {
                player.experience.selectUpgrade(2)
            }
        }

        // Update game state if player is alive and not selecting upgrade
        if (player.isAlive() && !player.experience.isSelectingUpgrade) {
            updateEnemies(delta)
            player.update(delta, enemies)
            player.weapon.checkCollisions(enemies)

            // Update camera to follow player
            camera.position.x = player.position.x.coerceIn(VIEWPORT_WIDTH / 2, WORLD_WIDTH - VIEWPORT_WIDTH / 2)
            camera.position.y = player.position.y.coerceIn(VIEWPORT_HEIGHT / 2, WORLD_HEIGHT - VIEWPORT_HEIGHT / 2)
        }

        // Set background color based on map type
        val (r, g, b) = when (mapType) {
            MapType.FOREST -> Triple(0.1f, 0.3f, 0.1f)    // Dark green for forest
            MapType.DESERT -> Triple(0.3f, 0.3f, 0.1f)    // Sandy color for desert
            MapType.DUNGEON -> Triple(0.1f, 0.1f, 0.15f)  // Dark blue-grey for dungeon
            MapType.CASTLE -> Triple(0.2f, 0.2f, 0.25f)   // Grey for castle
            MapType.GRAVEYARD -> Triple(0.15f, 0.1f, 0.15f) // Dark purple for graveyard
        }
        Gdx.gl.glClearColor(r, g, b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        game.getBatch().projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        // Draw world boundary
        shapeRenderer.begin(ShapeType.Line)
        shapeRenderer.color = com.badlogic.gdx.graphics.Color(0.4f, 0.4f, 0.4f, 1f) // Slightly brighter for better visibility
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        shapeRenderer.end()

        // Render game objects
        enemies.forEach { it.render(shapeRenderer) }  // Render enemies first
        powerUps.forEach { it.render(shapeRenderer) }  // Render power-ups
        player.render(shapeRenderer)  // Render player and weapon last

        // Reset projection matrix for UI rendering
        game.getBatch().projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.projectionMatrix = game.getBatch().projectionMatrix

        // Draw minimap in the corner
        val minimapSize = 100f
        val margin = 10f
        val minimapX = Gdx.graphics.width - minimapSize - margin
        val minimapY = margin
        val minimapScale = minimapSize / WORLD_WIDTH

        // Draw minimap
        val minimapHeight = minimapSize * (WORLD_HEIGHT / WORLD_WIDTH)

        shapeRenderer.begin(ShapeType.Filled)
        // Draw minimap background
        shapeRenderer.color = com.badlogic.gdx.graphics.Color(0.1f, 0.1f, 0.1f, 0.8f)
        shapeRenderer.rect(minimapX, minimapY, minimapSize, minimapHeight)

        // Draw enemy positions on minimap
        shapeRenderer.color = com.badlogic.gdx.graphics.Color(0.8f, 0.2f, 0.2f, 0.6f)
        enemies.forEach { enemy ->
            val enemyMinimapX = minimapX + (enemy.position.x * minimapScale)
            val enemyMinimapY = minimapY + (enemy.position.y * minimapScale)
            shapeRenderer.circle(enemyMinimapX, enemyMinimapY, 1f)
        }

        // Draw player position on minimap
        shapeRenderer.color = com.badlogic.gdx.graphics.Color.GREEN
        val playerMinimapX = minimapX + (player.position.x * minimapScale)
        val playerMinimapY = minimapY + (player.position.y * minimapScale)
        shapeRenderer.circle(playerMinimapX, playerMinimapY, 2f)

        // Draw viewport area on minimap
        shapeRenderer.color = com.badlogic.gdx.graphics.Color(1f, 1f, 1f, 0.3f)
        val viewportMinimapWidth = camera.viewportWidth * minimapScale
        val viewportMinimapHeight = camera.viewportHeight * minimapScale
        shapeRenderer.rect(
            minimapX + (camera.position.x - camera.viewportWidth/2) * minimapScale,
            minimapY + (camera.position.y - camera.viewportHeight/2) * minimapScale,
            viewportMinimapWidth,
            viewportMinimapHeight
        )
        shapeRenderer.end()

        // Draw minimap border
        shapeRenderer.begin(ShapeType.Line)
        shapeRenderer.color = com.badlogic.gdx.graphics.Color(0.5f, 0.5f, 0.5f, 1f)
        shapeRenderer.rect(minimapX, minimapY, minimapSize, minimapHeight)
        shapeRenderer.end()

        // If selecting upgrade, render the darkened background
        if (player.experience.isSelectingUpgrade) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.begin(ShapeType.Filled)
            shapeRenderer.setColor(0f, 0f, 0f, 0.7f)
            shapeRenderer.rect(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            shapeRenderer.end()
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }

        // Render UI
        ui.render(shapeRenderer, player, enemies, player.weapon.getProjectiles())
    }

    override fun resize(width: Int, height: Int) {
        // Calculate viewport size maintaining aspect ratio
        val aspectRatio = width.toFloat() / height.toFloat()
        val viewportHeight = VIEWPORT_HEIGHT
        val viewportWidth = viewportHeight * aspectRatio

        // Update camera viewport
        camera.viewportWidth = viewportWidth
        camera.viewportHeight = viewportHeight

        // Ensure camera stays within world bounds
        camera.position.x = camera.position.x.coerceIn(
            viewportWidth / 2,
            WORLD_WIDTH - viewportWidth / 2
        )
        camera.position.y = camera.position.y.coerceIn(
            viewportHeight / 2,
            WORLD_HEIGHT - viewportHeight / 2
        )
        camera.update()
    }

    override fun show() {
        // Called when this screen becomes the current screen
    }

    override fun hide() {
        // Called when this screen is no longer the current screen
    }

    override fun pause() {
        // Called when game is paused
    }

    override fun resume() {
        // Called when game is resumed
    }

    override fun dispose() {
        shapeRenderer.dispose()
        ui.dispose()
    }
}
