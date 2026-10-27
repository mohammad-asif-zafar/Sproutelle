# Clean Architecture & MVVM Walkthrough (iOS & Android)

Successfully implemented Clean Architecture with MVVM using Compose Multiplatform supporting both Phone and Tablet layouts for Splash, Onboarding, and Home screens, along with real Supabase networking integration and Supabase Storage image loading for all 12 Home Screen Explore categories.

## Architecture Overview

```
com.hathway.sproutelle
├── core/
│   ├── navigation/
│   │   └── Screen.kt                      # Sealed navigation routes (Splash, Onboarding, Home)
│   └── responsive/
│       └── WindowSize.kt                  # Compact (Phone) vs Medium/Expanded (Tablet) detection
├── domain/                                # Business Domain Layer
│   ├── model/
│   │   ├── OnboardingPage.kt              # Onboarding slide model
│   │   └── HomeData.kt                    # Plant, Category, Activity, and User dashboard models
│   ├── repository/
│   │   ├── UserPreferencesRepository.kt  # Onboarding status flow repository
│   │   └── ExplorerRepository.kt         # Supabase Explore categories repository interface
│   └── usecase/
│       ├── CheckOnboardingStatusUseCase.kt
│       ├── CompleteOnboardingUseCase.kt
│       └── GetExplorerItemsUseCase.kt
├── data/                                  # Data Layer
│   ├── datasource/
│   │   ├── SupabaseConfig.kt             # Supabase URL & Anon Key configuration
│   │   └── ExplorerRemoteDataSource.kt   # Ktor REST client with Kotlinx Serialization & Coil 3 image loading
│   ├── model/
│   │   ├── ExplorerItemEntity.kt         # Kotlinx Serializable entity for homescreen_explorer
│   │   └── MediaAssetEntity.kt           # Kotlinx Serializable entity for media_assets
│   └── repository/
│       └── ExplorerRepositoryImpl.kt     # Caching repository with inactivity filtering & failure propagation
└── presentation/                          # Presentation (MVVM) Layer
    ├── home/
    │   ├── HomeViewModel.kt
    │   └── HomeScreen.kt                  # Responsive phone (2-column) & tablet (4-column) chunked grid layouts with Coil AsyncImage
```

## Key Updates & Features

1. **Live Supabase & Storage Image Verification**:
   - **Live HTTP Status**: Verified via Logcat that Supabase responded with **HTTP 200 OK** (`application/json; charset=utf-8`).
   - **Successful Decoding**: Successfully decoded **12 explorer items with media assets** from Supabase.
   - **Storage URL Resolution**: Dynamically constructs public storage URLs (`{SUPABASE_URL}/storage/v1/object/public/{bucket_name}/{file_path}`) via embedded `media_assets` relation.
   - **Coil 3 Integration**: Asynchronously loads remote storage illustrations into `AsyncImage` with graceful fallback badge placeholders.

2. **Testing & Verification**:
   - Automated unit tests passed successfully (**8 passed, 0 failed**).
   - Full Gradle build (`:androidApp:assembleDebug`) completed successfully with 0 errors.
