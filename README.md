# Album List App

Displays a list of albums with images and titles from a remote JSON and caches them for offline access.

## Tech Stack & Libraries

* Language: Kotlin
* Architecture: MVVM, Clean Architecture
* Dependency Injection: Koin
* Networking: Retrofit + Moshi
* Persistence: Room (with Paging)
* Image Loading: Coil (with User-Agent header)
* UI: Jetpack Compose + Material 3
* Navigation: Jetpack Navigation Compose
* Tests: JUnit, Turbine, MockK, Koin Test

## Architecture

This project follows a modular Clean Architecture approach, split into three layers:

* Data: Handles all data-related operations, including:

  * Local data access (Room)
  * Remote data fetching (Retrofit)
  * Repository interface and its implementation

* Domain: Contains business logic via use cases:

  * `GetAlbumListUseCase`
  * `ObserveNetworkStateUseCase`
  * `RefreshAlbumsUseCase`

* Presentation: Responsible for the user interface, including:

  * Screens and UI logic
  * ViewModels
  * Composables (UI components)

## Offline Support Strategy

* The JSON file (\~5000 entries) is fetched when the user opens the `MainScreen`.
* The repository first checks if local albums exist in Room. If so, it emits them immediately.
* It then fetches fresh data from the network and upserts it into the local DB.
* If there’s no network and no local data, a network error is emitted.

This allows the app to work completely offline using cached data.

## Image Loading Logic

Images require a custom `User-Agent` header. This is configured globally in the `MyApplication` class using Coil:

```kotlin
ImageLoader.Builder(context)
  .okHttpClient {
    OkHttpClient.Builder()
      .addInterceptor { chain ->
        chain.proceed(
          chain.request().newBuilder()
            .header("User-Agent", "Android")
            .build()
        )
      }
      .build()
  }
  .build()
```

## Test Coverage

Unit tests cover the following layers:

* ViewModels
* UseCases
* Repositories (with mocked data sources)

Tools used:

* JUnit
* Turbine (for Flow testing)
* MockK (Android compatible mocking)
* Koin Test (for verifying DI modules)

## Configuration Change Handling

Jetpack `ViewModel` is used to survive configuration changes such as screen rotation. This ensures that UI state is preserved without reloading or recomputation.

## Known Limitations & Future Improvements

* There is no detail screen implemented, as the provided JSON does not contain enough information to support a meaningful detail view, so I just added a simple zoom-in effect.

## Justification for Technical Choices

* Koin: Chosen for DI because it integrates smoothly with Compose, is easy to use, and doesn’t rely on annotation processing.
* Coil: Used for image loading as it’s built for Compose and supports custom headers (needed for User-Agent).
* Room + Paging: Efficient for storing and displaying large datasets offline, with minimal boilerplate.
* Jetpack Compose: Chosen for the UI due to its declarative approach, modern tooling, and Android-first support.
* Jetpack Navigation Compose: Used for composable-friendly screen navigation and clean backstack management.
* MVVM + Clean Architecture: Ensures separation of concerns, testability, and scalability for future app features.

---

Feel free to clone the repository, run the app, and review the default branch for the full implementation.
