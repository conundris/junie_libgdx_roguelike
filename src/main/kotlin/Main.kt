package org.example

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class VampireSurvivorsGame : Game() {
    private lateinit var _batch: SpriteBatch
    private lateinit var _font: BitmapFont
    private lateinit var _shapeRenderer: ShapeRenderer
    private lateinit var _layout: GlyphLayout

    fun getBatch(): SpriteBatch = _batch
    fun getFont(): BitmapFont = _font
    fun getShapeRenderer(): ShapeRenderer = _shapeRenderer
    fun getLayout(): GlyphLayout = _layout

    override fun create() {
        _batch = SpriteBatch()
        _font = BitmapFont()
        _shapeRenderer = ShapeRenderer()
        _layout = GlyphLayout()
        setScreen(HubWorldScreen.create(this))
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render()
    }

    override fun dispose() {
        _batch.dispose()
        _font.dispose()
        _shapeRenderer.dispose()
        screen?.dispose()
    }
}

/**
 * Main entry point for the application.
 * Note: On macOS, the application must be run with -XstartOnFirstThread JVM argument.
 * This is handled automatically in build.gradle.kts based on the detected operating system.
 */
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Vampire Survivors Clone")
        setWindowedMode(800, 600)
        setForegroundFPS(60)
    }
    Lwjgl3Application(VampireSurvivorsGame(), config)
}
