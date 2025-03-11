package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Input

class WeaponSelectionScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer,
    private val selectedMap: MapType
) : Screen {
    private val titleText = "Choose Your Weapon"
    private val navigationText = "Use LEFT/RIGHT to select weapon"
    private val controlsText = "SPACE to Continue, ESC to return"
    private var selectedWeapon = WeaponType.SIMPLE

    companion object {
        fun create(
            game: VampireSurvivorsGame,
            selectedMap: MapType
        ): WeaponSelectionScreen {
            return WeaponSelectionScreen(
                game = game,
                font = BitmapFont(),
                layout = GlyphLayout(),
                shapeRenderer = ShapeRenderer(),
                selectedMap = selectedMap
            )
        }
    }

    private fun handleInput(key: Int) {
        when (key) {
            Input.Keys.SPACE -> {
                game.setScreen(DifficultySelectionScreen.create(
                    game = game,
                    selectedMap = selectedMap,
                    selectedWeapon = selectedWeapon
                ))
                dispose()
            }
            Input.Keys.ESCAPE -> {
                game.setScreen(MapSelectionScreen.create(
                    game = game,
                    weaponType = selectedWeapon,
                    difficultyLevel = DifficultyLevel.getDefault()
                ))
                dispose()
            }
            Input.Keys.LEFT -> {
                selectedWeapon = when (selectedWeapon) {
                    WeaponType.SIMPLE -> WeaponType.MELEE
                    WeaponType.SPREAD -> WeaponType.SIMPLE
                    WeaponType.BEAM -> WeaponType.SPREAD
                    WeaponType.BURST -> WeaponType.BEAM
                    WeaponType.MELEE -> WeaponType.BURST
                }
            }
            Input.Keys.RIGHT -> {
                selectedWeapon = when (selectedWeapon) {
                    WeaponType.SIMPLE -> WeaponType.SPREAD
                    WeaponType.SPREAD -> WeaponType.BEAM
                    WeaponType.BEAM -> WeaponType.BURST
                    WeaponType.BURST -> WeaponType.MELEE
                    WeaponType.MELEE -> WeaponType.SIMPLE
                }
            }
        }
    }

    private fun getWeaponDescription(): String {
        return when (selectedWeapon) {
            WeaponType.SIMPLE -> "Simple - Single straight shot"
            WeaponType.SPREAD -> "Spread - Multiple spread shots"
            WeaponType.BEAM -> "Beam - Focused beam attack"
            WeaponType.BURST -> "Burst - Rapid-fire burst"
            WeaponType.MELEE -> "Melee - Close-range powerful attack"
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            handleInput(Input.Keys.LEFT)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            handleInput(Input.Keys.RIGHT)
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

        // Draw weapon description
        font.getData().setScale(1.2f)
        layout.setText(font, getWeaponDescription())
        font.draw(game.getBatch(), getWeaponDescription(),
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
