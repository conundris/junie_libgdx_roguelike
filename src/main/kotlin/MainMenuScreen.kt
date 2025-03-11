package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType

class MainMenuScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer
) : Screen {
    private val titleText = "Vampire Survivors Clone"
    private val playText = "Press SPACE to Play"
    private val weaponText = "Select Weapon (LEFT/RIGHT)"
    private val difficultyText = "Select Difficulty (UP/DOWN)"
    private var selectedWeaponType = WeaponType.SIMPLE
    private var selectedDifficulty = DifficultyLevel.getDefault()

    fun getSelectedWeaponType() = selectedWeaponType
    fun getSelectedDifficulty() = selectedDifficulty

    // For testing
    internal var hubWorldScreenFactory: (VampireSurvivorsGame, WeaponType, DifficultyLevel) -> HubWorldScreen = { game, weaponType, difficulty ->
        HubWorldScreen.create(game, weaponType, difficulty)
    }

    fun handleInput(key: Int) {
        when (key) {
            com.badlogic.gdx.Input.Keys.SPACE -> {
                game.setScreen(hubWorldScreenFactory(game, selectedWeaponType, selectedDifficulty))
                dispose()
            }
            com.badlogic.gdx.Input.Keys.LEFT -> {
                selectedWeaponType = when (selectedWeaponType) {
                    WeaponType.SIMPLE -> WeaponType.MELEE
                    WeaponType.SPREAD -> WeaponType.SIMPLE
                    WeaponType.BEAM -> WeaponType.SPREAD
                    WeaponType.BURST -> WeaponType.BEAM
                    WeaponType.MELEE -> WeaponType.BURST
                }
            }
            com.badlogic.gdx.Input.Keys.RIGHT -> {
                selectedWeaponType = when (selectedWeaponType) {
                    WeaponType.SIMPLE -> WeaponType.SPREAD
                    WeaponType.SPREAD -> WeaponType.BEAM
                    WeaponType.BEAM -> WeaponType.BURST
                    WeaponType.BURST -> WeaponType.MELEE
                    WeaponType.MELEE -> WeaponType.SIMPLE
                }
            }
            com.badlogic.gdx.Input.Keys.UP -> {
                selectedDifficulty = when (selectedDifficulty) {
                    DifficultyLevel.EASY -> DifficultyLevel.NORMAL
                    DifficultyLevel.NORMAL -> DifficultyLevel.HARD
                    DifficultyLevel.HARD -> DifficultyLevel.EASY
                }
            }
            com.badlogic.gdx.Input.Keys.DOWN -> {
                selectedDifficulty = when (selectedDifficulty) {
                    DifficultyLevel.EASY -> DifficultyLevel.HARD
                    DifficultyLevel.NORMAL -> DifficultyLevel.EASY
                    DifficultyLevel.HARD -> DifficultyLevel.NORMAL
                }
            }
        }
    }

    private fun getWeaponDescription(): String {
        return when (selectedWeaponType) {
            WeaponType.SIMPLE -> "Simple - Single straight shot"
            WeaponType.SPREAD -> "Spread - Multiple spread shots"
            WeaponType.BEAM -> "Beam - Focused beam attack"
            WeaponType.BURST -> "Burst - Rapid-fire burst"
            WeaponType.MELEE -> "Melee - Close-range powerful attack"
        }
    }

    private fun getDifficultyDescription(): String {
        return when (selectedDifficulty) {
            DifficultyLevel.EASY -> "Easy - For a relaxed experience"
            DifficultyLevel.NORMAL -> "Normal - Balanced challenge"
            DifficultyLevel.HARD -> "Hard - For experienced players"
        }
    }

    companion object {
        fun create(game: VampireSurvivorsGame): MainMenuScreen {
            return MainMenuScreen(
                game = game,
                font = BitmapFont(),
                layout = GlyphLayout(),
                shapeRenderer = ShapeRenderer()
            )
        }

        // Factory method for testing
        internal fun createForTesting(
            game: VampireSurvivorsGame,
            font: BitmapFont,
            layout: GlyphLayout,
            shapeRenderer: ShapeRenderer
        ): MainMenuScreen {
            return MainMenuScreen(game, font, layout, shapeRenderer)
        }
    }

    override fun render(delta: Float) {
        // Clear screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Handle input
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            handleInput(com.badlogic.gdx.Input.Keys.SPACE)
            return
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            handleInput(com.badlogic.gdx.Input.Keys.LEFT)
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            handleInput(com.badlogic.gdx.Input.Keys.RIGHT)
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
            handleInput(com.badlogic.gdx.Input.Keys.UP)
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            handleInput(com.badlogic.gdx.Input.Keys.DOWN)
        }

        // Draw title and play button
        game.batch.begin()

        // Draw title
        font.getData().setScale(2f)
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.batch, titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.7f)

        // Draw play button
        // Draw weapon selection
        font.getData().setScale(1.5f)
        layout.setText(font, weaponText)
        font.draw(game.batch, weaponText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.6f)

        // Draw weapon description
        font.getData().setScale(1.2f)
        layout.setText(font, getWeaponDescription())
        font.draw(game.batch, getWeaponDescription(),
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.5f)

        // Draw difficulty selection
        font.getData().setScale(1.5f)
        layout.setText(font, difficultyText)
        font.draw(game.batch, difficultyText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.4f)

        // Draw difficulty description
        font.getData().setScale(1.2f)
        layout.setText(font, getDifficultyDescription())
        font.draw(game.batch, getDifficultyDescription(),
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.3f)

        // Draw play text
        font.getData().setScale(1.5f)
        layout.setText(font, playText)
        font.draw(game.batch, playText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.2f)

        font.getData().setScale(1f)  // Reset font scale
        game.batch.end()
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
