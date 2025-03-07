package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.Input.Keys

class GameScreen(private val game: VampireSurvivorsGame) : Screen {
    private val camera: OrthographicCamera = OrthographicCamera()
    private val shapeRenderer: ShapeRenderer = ShapeRenderer()
    private val ui: GameUI = GameUI(game.batch)
    private var player = Player()
    private val enemies = mutableListOf<Enemy>()
    private var spawnTimer = 0f
    private val spawnInterval = 2f  // Spawn enemy every 2 seconds

    private fun resetGame() {
        player = Player()
        enemies.clear()
        spawnTimer = 0f
    }

    init {
        camera.setToOrtho(false, 800f, 600f)
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
        enemies.add(Enemy(x, y))
    }

    private fun updateEnemies(delta: Float) {
        // Update spawn timer
        spawnTimer += delta
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f
            spawnEnemy()
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
            player.update(delta)
            updateEnemies(delta)
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
