# Vampire Survivors Clone

## Project Overview
This is a LibGDX-based game project written in Kotlin, implementing a clone of the popular game Vampire Survivors. The game features a top-down survival gameplay where the player fights against waves of enemies while collecting experience and upgrading their abilities.

## Technologies Used
- **Kotlin**: Primary programming language
- **LibGDX**: Game development framework
- **Gradle**: Build system and dependency management

## Main Components

### Core Game Components
1. **VampireSurvivorsGame** (Main.kt)
   - Main game class extending LibGDX's Game class
   - Handles game initialization and screen management
   - Manages the SpriteBatch for rendering

2. **GameScreen** (GameScreen.kt)
   - Main game screen implementation
   - Manages game loop and rendering
   - Coordinates between different game components

3. **MainMenuScreen** (MainMenuScreen.kt)
   - Initial game screen
   - Provides game start options

4. **Player** (Player.kt)
   - Player entity implementation
   - Handles player movement and state
   - Manages player health and abilities

### Combat System
1. **Weapon** (Weapon.kt)
   - Weapon system implementation
   - Manages projectiles and attacks

### Debug and Testing
1. **DebugMetrics** (DebugMetrics.kt)
   - Performance monitoring and debugging tools
   - Helps track game metrics during development

2. **GameUI** (GameUI.kt)
   - Renders game interface elements
   - Handles UI components and interactions

## Project Structure
```
src/
├── main/kotlin/
│   ├── Main.kt              # Game initialization
│   ├── GameScreen.kt        # Main game screen
│   ├── MainMenuScreen.kt    # Menu screen
│   ├── Player.kt            # Player implementation
│   ├── Weapon.kt            # Weapon system
│   ├── GameUI.kt           # User interface
│   └── DebugMetrics.kt     # Debug utilities
└── test/kotlin/
    ├── GameUITest.kt       # UI component tests
    ├── MainMenuScreenTest.kt # Menu screen tests
    ├── GameScreenTest.kt    # Game screen tests
    ├── GameUIDebugTest.kt   # UI debug tests
    └── DebugMetricsTest.kt  # Metrics tests
```

## Build and Run
The project uses Gradle for build management. The main class is set to `org.example.MainKt`.

### Running the Game
1. Clone the repository
2. Run using Gradle:
   ```bash
   ./gradlew run
   ```
   Note: On macOS, the application must be run with -XstartOnFirstThread JVM argument (this is handled automatically in build.gradle.kts)

### Game Configuration
- Window Size: 800x600
- FPS: 60
- Title: "Vampire Survivors Clone"

## Testing
The project includes comprehensive unit tests for various components:
- UI components (GameUITest)
- Screen management (MainMenuScreenTest, GameScreenTest)
- Debug features (DebugMetricsTest, GameUIDebugTest)

Run tests using:
```bash
./gradlew test
```