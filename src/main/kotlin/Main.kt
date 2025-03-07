package org.example

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class VampireSurvivorsGame : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()
        setScreen(MainMenuScreen.create(this))
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render()
    }

    override fun dispose() {
        batch.dispose()
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
