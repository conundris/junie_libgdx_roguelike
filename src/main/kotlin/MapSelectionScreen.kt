package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Input

class MapSelectionScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer,
    private val weaponType: WeaponType,
    private val difficultyLevel: DifficultyLevel
) : Screen {
    private val titleText = "Select Your Battlefield"
    private val navigationText = "Use LEFT/RIGHT to select map"
    private val controlsText = "SPACE to Start, ESC to return"
    private var selectedMap = MapType.FOREST

    // For testing
    internal fun handleInput(key: Int) {
        when (key) {
            Input.Keys.SPACE -> {
                game.setScreen(WeaponSelectionScreen.create(
                    game = game,
                    selectedMap = selectedMap
                ))
                dispose()
            }
            Input.Keys.ESCAPE -> {
                game.setScreen(mainMenuScreenFactory(game))
                dispose()
            }
            Input.Keys.LEFT -> {
                selectedMap = when (selectedMap) {
                    MapType.FOREST -> MapType.GRAVEYARD
                    MapType.DESERT -> MapType.FOREST
                    MapType.DUNGEON -> MapType.DESERT
                    MapType.CASTLE -> MapType.DUNGEON
                    MapType.GRAVEYARD -> MapType.CASTLE
                }
            }
            Input.Keys.RIGHT -> {
                selectedMap = when (selectedMap) {
                    MapType.FOREST -> MapType.DESERT
                    MapType.DESERT -> MapType.DUNGEON
                    MapType.DUNGEON -> MapType.CASTLE
                    MapType.CASTLE -> MapType.GRAVEYARD
                    MapType.GRAVEYARD -> MapType.FOREST
                }
            }
        }
    }

    private fun getMapDescription(): String {
        return when (selectedMap) {
            MapType.FOREST -> "Forest - Dense vegetation provides cover"
            MapType.DESERT -> "Desert - Wide open spaces, nowhere to hide"
            MapType.DUNGEON -> "Dungeon - Dark corridors and tight spaces"
            MapType.CASTLE -> "Castle - Multiple levels and passages"
            MapType.GRAVEYARD -> "Graveyard - Where evil truly lurks"
        }
    }

    internal fun getSelectedMap() = selectedMap

    internal var mainMenuScreenFactory: (VampireSurvivorsGame) -> MainMenuScreen = { game ->
        MainMenuScreen.create(game)
    }

    companion object {
        fun create(
            game: VampireSurvivorsGame,
            weaponType: WeaponType,
            difficultyLevel: DifficultyLevel
        ): MapSelectionScreen {
            return MapSelectionScreen(
                game = game,
                font = game.getFont(),
                layout = game.getLayout(),
                shapeRenderer = game.getShapeRenderer(),
                weaponType = weaponType,
                difficultyLevel = difficultyLevel
            )
        }

        // Factory method for testing
        internal fun createForTesting(
            game: VampireSurvivorsGame,
            font: BitmapFont,
            layout: GlyphLayout,
            shapeRenderer: ShapeRenderer,
            weaponType: WeaponType,
            difficultyLevel: DifficultyLevel
        ): MapSelectionScreen {
            return MapSelectionScreen(
                game = game,
                font = font,
                layout = layout,
                shapeRenderer = shapeRenderer,
                weaponType = weaponType,
                difficultyLevel = difficultyLevel
            )
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

        // Draw map description
        font.getData().setScale(1.2f)
        layout.setText(font, getMapDescription())
        font.draw(game.getBatch(), getMapDescription(),
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
        // Resources are managed by the game class
    }

    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
}
