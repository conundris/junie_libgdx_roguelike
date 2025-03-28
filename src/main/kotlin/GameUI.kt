package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType

class GameUI(private val batch: SpriteBatch) {
    private val font = BitmapFont()
    private val layout = GlyphLayout()
    private val barWidth = 200f
    private val barHeight = 20f
    private val padding = 10f
    private val expBarY = padding
    private val healthBarY = Gdx.graphics.height - barHeight - padding
    private val debugMetrics = DebugMetrics.getInstance()

    fun render(shapeRenderer: ShapeRenderer, player: Player, enemies: List<BaseEnemy>, projectiles: List<Projectile>, bossAnnounced: Boolean = false, bossSpawned: Boolean = false) {
        // Update debug metrics
        debugMetrics.update(enemies, projectiles, player)

        // Handle debug toggle (F3 or O key)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F3) || 
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.O)) {
            debugMetrics.toggleVisibility()
        }
        shapeRenderer.begin(ShapeType.Filled)

        // Render health bar
        val healthPercentage = player.health / 100f
        // Health bar background
        shapeRenderer.setColor(0.5f, 0f, 0f, 1f)
        shapeRenderer.rect(padding, healthBarY, barWidth, barHeight)
        // Health bar fill
        shapeRenderer.setColor(0f, 1f, 0f, 1f)
        shapeRenderer.rect(padding, healthBarY, barWidth * healthPercentage, barHeight)

        // Render experience bar
        val expPercentage = player.experience.currentLevelProgress
        // Experience bar background
        shapeRenderer.setColor(0.2f, 0.2f, 0.5f, 1f)
        shapeRenderer.rect(padding, expBarY, barWidth, barHeight)
        // Experience bar fill
        shapeRenderer.setColor(0.4f, 0.4f, 1f, 1f)
        shapeRenderer.rect(padding, expBarY, barWidth * expPercentage, barHeight)

        shapeRenderer.end()

        // Render text
        batch.begin()
        font.setColor(Color.WHITE)

        // Health text
        font.draw(batch, "Health: ${player.health}/100",
                 padding, healthBarY + barHeight + padding)

        // Level and experience text
        font.draw(batch, "Level: ${player.experience.level}  XP: ${player.experience.currentExp}/${player.experience.expToNextLevel}",
                 padding, expBarY + barHeight + padding)

        if (player.experience.isSelectingUpgrade) {
            // Draw upgrade options
            font.getData().setScale(1.5f)
            val titleText = "Level Up! Choose your upgrade:"
            layout.setText(font, titleText)
            font.draw(batch, titleText, 
                     (Gdx.graphics.width - layout.width) / 2,
                     Gdx.graphics.height * 0.8f)

            font.getData().setScale(1f)
            player.experience.availableUpgrades.forEachIndexed { index, upgrade ->
                val optionText = "${index + 1}. ${upgrade.name}: ${upgrade.description}"
                layout.setText(font, optionText)
                font.draw(batch, optionText,
                         (Gdx.graphics.width - layout.width) / 2,
                         Gdx.graphics.height * (0.6f - index * 0.1f))
            }

            val helpText = "Press 1-3 to select an upgrade"
            layout.setText(font, helpText)
            font.draw(batch, helpText,
                     (Gdx.graphics.width - layout.width) / 2,
                     Gdx.graphics.height * 0.2f)
        } else if (!player.isAlive()) {
            val gameOverText = "GAME OVER"
            val restartText = "Press SPACE to restart"

            font.getData().setScale(2f)
            layout.setText(font, gameOverText)
            val gameOverX = (Gdx.graphics.width - layout.width) / 2
            val gameOverY = Gdx.graphics.height / 2f + 50f
            font.draw(batch, gameOverText, gameOverX, gameOverY)

            font.getData().setScale(1f)
            layout.setText(font, restartText)
            val restartX = (Gdx.graphics.width - layout.width) / 2
            val restartY = Gdx.graphics.height / 2f - 50f
            font.draw(batch, restartText, restartX, restartY)
        }

        font.getData().setScale(1f)  // Reset font scale

        // Render boss announcement or boss health bar
        if (bossAnnounced || bossSpawned) {
            if (bossSpawned) {
                // Find the boss in the enemies list
                val boss = enemies.find { it is BossEnemy }
                if (boss != null) {
                    // Draw boss health bar at the top of the screen
                    val bossHealthPercentage = boss.health.toFloat() / 500f  // Assuming max health is 500

                    // Draw boss health bar background
                    shapeRenderer.begin(ShapeType.Filled)
                    shapeRenderer.setColor(0.5f, 0f, 0.5f, 1f)  // Purple background
                    shapeRenderer.rect(
                        Gdx.graphics.width / 2f - 200f, 
                        Gdx.graphics.height - 40f, 
                        400f, 
                        30f
                    )

                    // Draw boss health bar fill
                    shapeRenderer.setColor(0.8f, 0f, 0.8f, 1f)  // Brighter purple fill
                    shapeRenderer.rect(
                        Gdx.graphics.width / 2f - 200f, 
                        Gdx.graphics.height - 40f, 
                        400f * bossHealthPercentage, 
                        30f
                    )
                    shapeRenderer.end()

                    // Draw boss label
                    font.getData().setScale(1.5f)
                    font.setColor(1f, 0f, 1f, 1f)  // Bright purple text
                    layout.setText(font, "BOSS")
                    font.draw(
                        batch, 
                        "BOSS", 
                        Gdx.graphics.width / 2f - layout.width / 2f,
                        Gdx.graphics.height - 15f
                    )
                }
            } else if (bossAnnounced) {
                // Draw boss warning message
                font.getData().setScale(2f)
                font.setColor(1f, 0f, 0f, 1f)  // Red text

                val warningText = "WARNING: BOSS APPROACHING!"
                layout.setText(font, warningText)
                font.draw(
                    batch, 
                    warningText, 
                    Gdx.graphics.width / 2f - layout.width / 2f,
                    Gdx.graphics.height / 2f + 50f
                )

                font.getData().setScale(1.5f)
                val subText = "Prepare yourself..."
                layout.setText(font, subText)
                font.draw(
                    batch, 
                    subText, 
                    Gdx.graphics.width / 2f - layout.width / 2f,
                    Gdx.graphics.height / 2f - 20f
                )
            }

            font.getData().setScale(1f)  // Reset font scale
            font.setColor(Color.WHITE)  // Reset color
        }

        // Render debug metrics if visible
        if (debugMetrics.isVisible()) {
            val debugText = debugMetrics.getMetricsText()
            val debugY = Gdx.graphics.height - 150f
            debugText.split('\n').forEachIndexed { index, line ->
                font.draw(batch, line, Gdx.graphics.width - 300f, debugY - (index * 20f))
            }
        }

        batch.end()
    }

    fun dispose() {
        font.dispose()
    }
}
