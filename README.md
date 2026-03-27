# FitLog - Fitness Diary App

A comprehensive Android fitness diary application built with **Kotlin**, **XML layouts**, and the **MVP (Model-View-Presenter)** architecture pattern. FitLog helps users track their workouts, exercises, and weight progression over time.

## Features

- **Workout Management**: Create workout sessions with date, type (strength/cardio/flexibility), notes, and duration
- **Exercise Tracking**: Add exercises to workouts with custom names and notes
- **Set Logging**: Record sets with reps and weight for each exercise
- **Progress Charts**: Visualize weight progression per exercise over time using MPAndroidChart line charts
- **Workout History**: Browse all workouts with type-based filtering
- **Exercise Library**: Auto-built library of all exercises performed, with search and usage counts

## Architecture: MVP (Model-View-Presenter)

FitLog deliberately uses the **MVP** pattern instead of MVVM or MVI to demonstrate this classic Android architecture:

### Why MVP?

- **Clear separation of concerns**: Views are passive (display-only), Presenters contain business logic, Models handle data
- **Testability**: Presenters can be unit tested by mocking the View interface
- **No framework dependency in business logic**: Presenters don't depend on Android framework classes

### How it works in FitLog

```
View (Activity/Fragment)          Presenter                    Model (Repository)
  |                                  |                              |
  |-- user action ------------------>|                              |
  |                                  |-- fetch/save data ---------->|
  |                                  |<-- data/result --------------|
  |<-- update UI --------------------|                              |
```

**Contract interfaces** define the communication protocol between each View-Presenter pair:
- `WorkoutListContract` (View + Presenter interfaces)
- `AddWorkoutContract`
- `WorkoutDetailContract`
- `ProgressContract`
- `ExerciseLibraryContract`

The **View** (Activity/Fragment):
- Implements the Contract.View interface
- Forwards user actions to the Presenter
- Updates UI only when told to by the Presenter
- Contains ZERO business logic

The **Presenter**:
- Implements the Contract.Presenter interface
- Manages a CoroutineScope for async operations
- Holds a nullable View reference (null-safe after detach)
- Cancelled on detachView() to prevent memory leaks

## XML Layouts Approach

FitLog uses traditional **XML layouts with ViewBinding** instead of Jetpack Compose:

- **ViewBinding** generates type-safe binding classes for each layout XML
- **Material Design Components** provide themed widgets (MaterialCardView, TextInputLayout, FloatingActionButton, BottomNavigationView)
- **RecyclerView with DiffUtil** for efficient list rendering with animations
- **Nested RecyclerView** pattern for exercises containing sets

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | XML Layouts + ViewBinding |
| Architecture | MVP (Model-View-Presenter) |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| Charts | MPAndroidChart |
| Lists | RecyclerView + DiffUtil |
| Design | Material Design Components |
| DI | Manual (Application class) |

## Project Structure

```
com.fitlog.app/
  |-- FitLogApp.kt                    # Application class (dependency root)
  |-- data/
  |   |-- local/                      # Room database layer
  |   |   |-- FitLogDatabase.kt       # Room database definition
  |   |   |-- WorkoutDao.kt           # Workout data access object
  |   |   |-- ExerciseDao.kt          # Exercise/set data access object
  |   |   |-- *Entity.kt              # Room entities (tables)
  |   |-- repository/                 # Repository pattern
  |       |-- *Repository.kt          # Interfaces
  |       |-- *RepositoryImpl.kt      # Implementations
  |-- domain/model/                   # Domain models (decoupled from DB)
  |-- presenter/                      # MVP Presenters
  |   |-- BasePresenter.kt            # Base presenter interface
  |   |-- BaseView.kt                 # Base view interface
  |   |-- workoutlist/                # Workout list screen MVP
  |   |-- addworkout/                 # Add workout screen MVP
  |   |-- workoutdetail/              # Workout detail screen MVP
  |   |-- progress/                   # Progress chart screen MVP
  |   |-- exerciselibrary/            # Exercise library screen MVP
  |-- ui/
  |   |-- activity/                   # Activities (MVP Views)
  |   |-- fragment/                   # Fragments (MVP Views)
  |   |-- adapter/                    # RecyclerView adapters with DiffUtil
  |   |-- dialog/                     # DialogFragments
  |-- util/                           # Utilities and extensions
```

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Build and Run

```bash
# Clone the repository
git clone https://github.com/cfvbhgc/fit-log.git
cd fit-log

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Open in Android Studio
1. Open Android Studio
2. File > Open > Select the `fit-log` directory
3. Wait for Gradle sync to complete
4. Run on emulator or device (API 26+)

## Database Schema

```
workouts (id, date, type, notes, durationMinutes)
    |
    +-- exercises (id, workoutId, name, notes)
            |
            +-- exercise_sets (id, exerciseId, setNumber, reps, weight)
```

Foreign keys with CASCADE delete ensure data integrity when parent records are removed.

## License

This project is available for educational and portfolio purposes.
