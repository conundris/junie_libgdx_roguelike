package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Input

class DifficultySelectionScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer,
    private val selectedMap: MapType,
    private val selectedWeapon: WeaponType
) : Screen {
    private val titleText = "Select Difficulty"
    private val navigationText = "Use UP/DOWN to select difficulty"
    private val controlsText = "SPACE to Start, ESC to return"
    private var selectedDifficulty = DifficultyLevel.getDefault()

    companion object {
        fun create(
            game: VampireSurvivorsGame,
            selectedMap: MapType,
            selectedWeapon: WeaponType
        ): DifficultySelectionScreen {
            return DifficultySelectionScreen(
                game = game,
                font = BitmapFont(),
                layout = GlyphLayout(),
                shapeRenderer = ShapeRenderer(),
                selectedMap = selectedMap,
                selectedWeapon = selectedWeapon
            )
        }
    }

    private fun handleInput(key: Int) {
        when (key) {
            Input.Keys.SPACE -> {
                game.setScreen(GameScreen.create(
                    game = game,
                    weaponType = selectedWeapon,
                    difficulty = selectedDifficulty,
                    mapType = selectedMap
                ))
                dispose()
            }
            Input.Keys.ESCAPE -> {
                game.setScreen(WeaponSelectionScreen.create(
                    game = game,
                    selectedMap = selectedMap
                ))
                dispose()
            }
            Input.Keys.UP -> {
                selectedDifficulty = when (selectedDifficulty) {
                    DifficultyLevel.EASY -> DifficultyLevel.NORMAL
                    DifficultyLevel.NORMAL -> DifficultyLevel.HARD
                    DifficultyLevel.HARD -> DifficultyLevel.EASY
                }
            }
            Input.Keys.DOWN -> {
                selectedDifficulty = when (selectedDifficulty) {
                    DifficultyLevel.EASY -> DifficultyLevel.HARD
                    DifficultyLevel.NORMAL -> DifficultyLevel.EASY
                    DifficultyLevel.HARD -> DifficultyLevel.NORMAL
                }
            }
        }
    }

    private fun getDifficultyDescription(): String {
        return when (selectedDifficulty) {
            DifficultyLevel.EASY -> "Easy - For a relaxed experience"
            DifficultyLevel.NORMAL -> "Normal - Balanced challenge"
            DifficultyLevel.HARD -> "Hard - For experienced players"
        }
    }

    override fun render(delta: Float) {
        // Clear screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            handleInput(Input.Keys.SPACE)
            return
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            handleInput(Input.Keys.ESCAPE)
            return
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            handleInput(Input.Keys.UP)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            handleInput(Input.Keys.DOWN)
        }

        game.getBatch().begin()

        // Draw title
        font.getData().setScale(2f)
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.getBatch(), titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.8f)

        // Draw navigation instructions
        font.getData().setScale(1.5f)
        layout.setText(font, navigationText)
        font.draw(game.getBatch(), navigationText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.6f)

        // Draw difficulty description
        font.getData().setScale(1.2f)
        layout.setText(font, getDifficultyDescription())
        font.draw(game.getBatch(), getDifficultyDescription(),
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.4f)

        // Draw controls
        font.getData().setScale(1.5f)
        layout.setText(font, controlsText)
        font.draw(game.getBatch(), controlsText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.2f)

        font.getData().setScale(1f)  // Reset font scale
        game.getBatch().end()
    }

    override fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
    }

    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
}
