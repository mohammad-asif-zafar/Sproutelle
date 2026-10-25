# Clean Architecture & MVVM Walkthrough (iOS & Android)

Successfully implemented Clean Architecture with MVVM using Compose Multiplatform supporting both Phone and Tablet layouts for Splash, Onboarding, and Home screens, along with custom app launcher icons and native launch screen branding.

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
│   │   └── HomeRepository.kt             # Garden data repository interface
│   └── usecase/
│       ├── CheckOnboardingStatusUseCase.kt
│       ├── CompleteOnboardingUseCase.kt
│       └── GetHomeDataUseCase.kt
├── data/                                  # Data Layer
│   └── repository/
│       ├── UserPreferencesRepositoryImpl.kt
│       └── HomeRepositoryImpl.kt
└── presentation/                          # Presentation (MVVM) Layer
    ├── splash/
    │   ├── SplashViewModel.kt
    │   └── SplashScreen.kt                # Responsive splash artwork with device-specific images & loading progress
    ├── onboarding/
    │   ├── OnboardingViewModel.kt
    │   └── OnboardingScreen.kt            # Onboarding with portrait (mobile) & landscape (tablet) artwork
    └── home/
        ├── HomeViewModel.kt
        └── HomeScreen.kt                  # Phone: Bottom Navigation; Tablet: Navigation Rail
```

## Key Updates & Features

1. **Onboarding Custom Artwork**:
   - Integrated custom artwork assets for onboarding (`sproutelle_onboarding_welcome`, `sproutelle_onboarding_learn`, `sproutelle_onboarding_safety`).
   - Automatically switches between **portrait** images for mobile devices and **landscape** images for tablets.

2. **App Launcher Icon & Native Splash**:
   - Updated Android mipmap icons and iOS app icons with Sproutelle's new branding icon.
   - Configured native launch screens on both platforms.

3. **Testing & Verification**:
   - Automated unit tests covering all UseCases passed 4/4.
   - Full Gradle build (`:androidApp:assembleDebug` and `:shared:testAndroidHostTest`) completed successfully with 0 errors.
