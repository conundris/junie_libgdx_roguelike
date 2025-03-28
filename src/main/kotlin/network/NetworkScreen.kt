package org.example.network

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Input.Keys
import org.example.DifficultyLevel
import org.example.GameScreen
import org.example.MapType
import org.example.VampireSurvivorsGame
import org.example.WeaponType
import java.net.InetAddress

/**
 * Screen for network game setup.
 * Allows players to host a game, discover and join existing games,
 * and configure network settings.
 */
class NetworkScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer
) : Screen {
    companion object {
        fun create(game: VampireSurvivorsGame): NetworkScreen {
            return NetworkScreen(
                game = game,
                font = game.getFont(),
                layout = game.getLayout(),
                shapeRenderer = game.getShapeRenderer()
            )
        }
    }

    // Network components
    private val networkManager = NetworkManager()

    // UI state
    private enum class ScreenState {
        MAIN_MENU,
        HOST_SETUP,
        JOIN_GAME,
        LOBBY
    }

    private var currentState = ScreenState.MAIN_MENU
    private var playerName = "Player"

    // Game settings
    private var selectedWeaponType = WeaponType.SIMPLE
    private var selectedMapType = MapType.FOREST
    private var selectedDifficulty = DifficultyLevel.NORMAL

    init {
        // Network manager is initialized automatically
    }

    override fun render(delta: Float) {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Render UI
        game.getBatch().begin()

        // Render different UI based on current state
        when (currentState) {
            ScreenState.MAIN_MENU -> renderMainMenu()
            ScreenState.HOST_SETUP -> renderHostSetup()
            ScreenState.JOIN_GAME -> renderJoinGame()
            ScreenState.LOBBY -> renderLobby()
        }

        font.getData().setScale(1f)
        game.getBatch().end()

        // Handle input
        handleInput()
    }

    private fun renderMainMenu() {
        // Title
        font.getData().setScale(2f)
        val titleText = "Multiplayer Setup"
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.getBatch(), titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.8f)

        // Options
        font.getData().setScale(1.5f)

        val hostText = "1. Host a Game"
        layout.setText(font, hostText)
        font.draw(game.getBatch(), hostText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.6f)

        val joinText = "2. Join a Game"
        layout.setText(font, joinText)
        font.draw(game.getBatch(), joinText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.5f)

        // Player name
        font.getData().setScale(1.2f)
        val nameText = "Your Name: $playerName"
        layout.setText(font, nameText)
        font.draw(game.getBatch(), nameText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.4f)

        val backText = "ESC. Back to Main Menu"
        layout.setText(font, backText)
        font.draw(game.getBatch(), backText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.3f)
    }

    private fun renderHostSetup() {
        // Title
        font.getData().setScale(2f)
        val titleText = "Host Game Setup"
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.getBatch(), titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.8f)

        // Instructions
        font.getData().setScale(1.5f)
        val instructionText = "Press ENTER to start hosting"
        layout.setText(font, instructionText)
        font.draw(game.getBatch(), instructionText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.6f)

        // Player name
        font.getData().setScale(1.2f)
        val nameText = "Your Name: $playerName"
        layout.setText(font, nameText)
        font.draw(game.getBatch(), nameText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.5f)

        // Game settings
        val mapText = "Map: $selectedMapType"
        layout.setText(font, mapText)
        font.draw(game.getBatch(), mapText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.4f)

        val difficultyText = "Difficulty: $selectedDifficulty"
        layout.setText(font, difficultyText)
        font.draw(game.getBatch(), difficultyText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.35f)

        val backText = "ESC. Back to Multiplayer Menu"
        layout.setText(font, backText)
        font.draw(game.getBatch(), backText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.25f)
    }

    private fun renderJoinGame() {
        // Title
        font.getData().setScale(2f)
        val titleText = "Join Game"
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.getBatch(), titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.8f)

        // Instructions
        font.getData().setScale(1.5f)
        val instructionText = "Press ENTER to search for games"
        layout.setText(font, instructionText)
        font.draw(game.getBatch(), instructionText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.6f)

        // Player name
        font.getData().setScale(1.2f)
        val nameText = "Your Name: $playerName"
        layout.setText(font, nameText)
        font.draw(game.getBatch(), nameText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.5f)

        val backText = "ESC. Back to Multiplayer Menu"
        layout.setText(font, backText)
        font.draw(game.getBatch(), backText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.3f)
    }

    private fun renderLobby() {
        // Title
        font.getData().setScale(2f)
        val titleText = if (networkManager.isServer()) "Game Lobby (Host)" else "Game Lobby"
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.getBatch(), titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.8f)

        // Connected players
        font.getData().setScale(1.5f)
        val playersText = "Connected Players:"
        layout.setText(font, playersText)
        font.draw(game.getBatch(), playersText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.7f)

        // List connected players
        font.getData().setScale(1.2f)
        var yPos = Gdx.graphics.height * 0.65f
        val connectedClients = networkManager.getConnectedClients()

        if (connectedClients.isEmpty()) {
            val noPlayersText = "No players connected"
            layout.setText(font, noPlayersText)
            font.draw(game.getBatch(), noPlayersText,
                (Gdx.graphics.width - layout.width) / 2,
                yPos)
        } else {
            for ((clientId, playerName) in connectedClients) {
                val playerText = "$clientId: $playerName ${if (clientId == 0) "(Host)" else ""}"
                layout.setText(font, playerText)
                font.draw(game.getBatch(), playerText,
                    (Gdx.graphics.width - layout.width) / 2,
                    yPos)
                yPos -= 30f
            }
        }

        // Instructions
        if (networkManager.isServer()) {
            val startText = "Press ENTER to start the game"
            layout.setText(font, startText)
            font.draw(game.getBatch(), startText,
                (Gdx.graphics.width - layout.width) / 2,
                Gdx.graphics.height * 0.4f)
        } else {
            val waitText = "Waiting for host to start the game..."
            layout.setText(font, waitText)
            font.draw(game.getBatch(), waitText,
                (Gdx.graphics.width - layout.width) / 2,
                Gdx.graphics.height * 0.4f)
        }

        val backText = "ESC. Leave Lobby"
        layout.setText(font, backText)
        font.draw(game.getBatch(), backText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.3f)
    }

    private fun handleInput() {
        when (currentState) {
            ScreenState.MAIN_MENU -> handleMainMenuInput()
            ScreenState.HOST_SETUP -> handleHostSetupInput()
            ScreenState.JOIN_GAME -> handleJoinGameInput()
            ScreenState.LOBBY -> handleLobbyInput()
        }
    }

    private fun handleMainMenuInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            // Return to main menu
            networkManager.disconnectFromServer()
            game.setScreen(org.example.MainMenuScreen.create(game))
            dispose()
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
            // Host a game
            currentState = ScreenState.HOST_SETUP
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
            // Join a game
            currentState = ScreenState.JOIN_GAME
        }
    }

    private fun handleHostSetupInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            // Return to network main menu
            currentState = ScreenState.MAIN_MENU
        } else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            // Start hosting
            if (networkManager.startServer(playerName)) {
                currentState = ScreenState.LOBBY
            }
        }
    }

    private fun handleJoinGameInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            // Return to network main menu
            currentState = ScreenState.MAIN_MENU
        } else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            // Discover and join servers
            val servers = networkManager.discoverServers()
            if (servers.isNotEmpty()) {
                // For simplicity, join the first server found
                if (networkManager.connectToServer(servers[0], playerName)) {
                    currentState = ScreenState.LOBBY
                }
            }
        }
    }

    private fun handleLobbyInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            // Return to network main menu
            networkManager.disconnectFromServer()
            currentState = ScreenState.MAIN_MENU
        } else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            // Start the game
            if (networkManager.isServer()) {
                // Only the host can start the game
                startNetworkedGame()
            }
        }
    }

    private fun startNetworkedGame() {
        // Create a networked game screen
        val gameScreen = GameScreen.create(
            game = game,
            weaponType = selectedWeaponType,
            difficulty = selectedDifficulty,
            mapType = selectedMapType
        )

        // Initialize as server or client
        if (networkManager.isServer()) {
            gameScreen.initAsServer(playerName)
        } else if (networkManager.isClient()) {
            // This would need the server address, which we don't have in this simplified implementation
            // In a real implementation, we would store the server address when connecting
            // For now, we'll just use localhost as a placeholder
            val serverAddress = java.net.InetAddress.getLocalHost()
            gameScreen.initAsClient(serverAddress, playerName)
        }

        game.setScreen(gameScreen)
        dispose()
    }

    override fun resize(width: Int, height: Int) {
        // Nothing to do
    }

    override fun pause() {
        // Nothing to do
    }

    override fun resume() {
        // Nothing to do
    }

    override fun show() {
        // Nothing to do
    }

    override fun hide() {
        // Nothing to do
    }

    override fun dispose() {
        // Clean up network resources
        networkManager.disconnectFromServer()
    }
}
