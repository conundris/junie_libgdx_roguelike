# Guidelines for Junie

**Important:** strictly follow the instructions below:

- First, before starting any work, create a new git branch. (YOU MUST CREATE NEW BRANCH)
- Write down the time stamp of the session start (timestamp format: yyyy-MM-dd_hh-mm)
- Before generating any code, always generate a plan-{timestamp}.md file.
- Use the plan-{timestamp}.md file as an input to generate the detailed enumerated task list.
- Store the task list to tasks-{timestemp}.md file
- Do the work that was submitted with user prompt.
- After each task is done, mark it as completed in the tasks-{timestamp}.md file
- All the work should be committed to the branch upon completion.

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

2. **GameScreen** (GameScreen.kt)
   - Main game screen implementation
   - Manages game loop and rendering
   - Coordinates between different game components

3. **Player** (Player.kt)
   - Player entity implementation
   - Handles player movement and state
   - Manages player health and abilities

4. **Enemy** (Enemy.kt)
   - Enemy entity implementation
   - Handles enemy behavior and movement

### Combat System
1. **Weapon** (Weapon.kt)
   - Weapon system implementation
   - Manages projectiles and attacks
   - Handles weapon upgrades

2. **WeaponUpgrade** (WeaponUpgrade.kt)
   - Upgrade system for weapons
   - Defines different types of upgrades
   - Manages upgrade selection and application

### Progression System
1. **Experience** (Experience.kt)
   - Player progression system
   - Handles leveling and experience points
   - Manages upgrade availability and selection

### User Interface
1. **GameUI** (GameUI.kt)
   - Renders game interface elements
   - Displays health and experience bars
   - Shows upgrade options and game over screen

## Project Structure
```
src/
├── main/kotlin/
│   ├── Main.kt              # Game initialization
│   ├── GameScreen.kt        # Main game screen
│   ├── Player.kt            # Player implementation
│   ├── Enemy.kt             # Enemy implementation
│   ├── Weapon.kt            # Weapon system
│   ├── WeaponUpgrade.kt     # Upgrade system
│   ├── Experience.kt        # Level progression
│   └── GameUI.kt            # User interface
└── test/kotlin/
    └── GameUITest.kt        # UI component tests
```

## Build and Run
The project uses Gradle for build management. Main class is set to `org.example.MainKt`.

## Testing
Unit tests are implemented for components like GameUI, ensuring proper functionality of game systems.