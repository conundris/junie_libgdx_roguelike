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
    private val ui: GameUI
) : Screen {
    internal lateinit var player: Player
    private val enemies = mutableListOf<Enemy>()  // Keep Enemy type for compatibility
    private val powerUps = mutableListOf<PowerUp>()
    private var spawnTimer = 0f
    private var spawnInterval = 2f  // Initial spawn interval
    private var powerUpTimer = 0f
    private var powerUpInterval = 10f  // Spawn power-up every 10 seconds

    companion object {
        fun create(game: VampireSurvivorsGame, weaponType: WeaponType = WeaponType.SIMPLE): GameScreen {
            val camera = OrthographicCamera().apply {
                setToOrtho(false, 800f, 600f)
            }
            return createWithCamera(game, weaponType, camera)
        }

        private fun createWithCamera(
            game: VampireSurvivorsGame,
            weaponType: WeaponType,
            camera: OrthographicCamera
        ): GameScreen {
            val screen = GameScreen(
                game = game,
                camera = camera,
                shapeRenderer = ShapeRenderer(),
                ui = GameUI(game.batch)
            )
            screen.player = Player(weaponType)
            return screen
        }

        // Factory method for testing
        internal fun createForTesting(
            game: VampireSurvivorsGame,
            camera: OrthographicCamera,
            shapeRenderer: ShapeRenderer,
            ui: GameUI
        ): GameScreen {
            val screen = GameScreen(game, camera, shapeRenderer, ui)
            screen.player = Player(WeaponType.SIMPLE)
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
        spawnInterval = 2f
        powerUpInterval = 10f
    }

    private var gameTime = 0f
    private var difficultyLevel = 1

    private fun updateDifficulty(delta: Float) {
        gameTime += delta
        difficultyLevel = (gameTime / 60f).toInt() + 1  // Increase difficulty every minute

        // Adjust spawn interval based on difficulty
        spawnInterval = (2f / difficultyLevel).coerceAtLeast(0.5f)
    }

    private fun spawnEnemy() {
        // Spawn enemies from outside the screen
        val side = (0..3).random() // 0: top, 1: right, 2: bottom, 3: left
        val (x, y) = when (side) {
            0 -> Pair((0..800).random().toFloat(), -30f) // top
            1 -> Pair(830f, (0..600).random().toFloat()) // right
            2 -> Pair((0..800).random().toFloat(), 630f) // bottom
            else -> Pair(-30f, (0..600).random().toFloat()) // left
        }

        // Create basic enemy for compatibility
        enemies.add(Enemy(x, y))
    }

    private fun spawnPowerUp() {
        // Spawn power-up at random position within screen bounds
        val x = (50..750).random().toFloat()
        val y = (50..550).random().toFloat()

        // Random power-up type
        val type = PowerUpType.values().random()
        powerUps.add(PowerUp(Vector2(x, y), type))
    }

    private fun updateEnemies(delta: Float) {
        // Update difficulty and spawn timers
        updateDifficulty(delta)
        spawnTimer += delta
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f
            spawnEnemy()
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
            player.gainExperience(enemy.expValue)
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
        }

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        game.batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        // Render game objects
        enemies.forEach { it.render(shapeRenderer) }  // Render enemies first
        powerUps.forEach { it.render(shapeRenderer) }  // Render power-ups
        player.render(shapeRenderer)  // Render player and weapon last

        // Reset projection matrix for UI rendering
        game.batch.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.projectionMatrix = game.batch.projectionMatrix

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
        ui.render(shapeRenderer, player)
    }

    override fun resize(width: Int, height: Int) {
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
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
