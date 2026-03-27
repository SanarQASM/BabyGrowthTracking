package org.example.project.babygrowthtrackingapplication.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.splash.CompleteSplashScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.HomeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddBabyScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddMeasurementScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.BabyProfileScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.onBoarding.OnboardingScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.EnterCodeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.EnterCodeViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.EnterNewPasswordScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.EnterNewPasswordViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.ForgotPasswordScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.ForgotPasswordViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.LoginScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.LoginViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.SignupScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.SignupViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.VerifyAccountScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.VerifyAccountViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.WelcomeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.AddBabyViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HealthRecordViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.SettingsViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.FamilyHistoryViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AllMeasurementsScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.FamilyHistoryScreen
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.example.project.babygrowthtrackingapplication.data.auth.SocialLoginHelper
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.ui.components.NavigationTab

// ─────────────────────────────────────────────────────────────────────────────
// Screen enum
// ─────────────────────────────────────────────────────────────────────────────

enum class Screen {
    Splash,
    Welcome,
    Login,
    Signup,
    ForgotPassword,
    EnterCode,
    EnterNewPassword,
    VerifyAccount,
    Onboarding,
    Home,
    AddBaby,
    BabyProfile,
    EditBaby,
    AddMeasurement,
    AllMeasurements,
    FamilyHistory
}

// ─────────────────────────────────────────────────────────────────────────────
// AppNavigation
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    // FIX: receive language from App.kt — eliminates the duplicate state bug
    currentLanguage     : Language              = Language.ENGLISH,
    onLanguageChange    : (Language) -> Unit    = {},
    // FIX: new callbacks so App.kt BabyGrowthTheme actually re-composes
    onDarkModeChange    : (Boolean) -> Unit     = {},
    onGenderThemeChange : (GenderTheme) -> Unit = {},
) {
    val preferencesManager = rememberPreferencesManager()
    var currentScreen by remember { mutableStateOf(Screen.Splash) }

    var resetEmail by remember { mutableStateOf("") }
    var resetCode  by remember { mutableStateOf("") }

    var selectedBaby        by remember { mutableStateOf<BabyResponse?>(null) }
    var measurementBaby     by remember { mutableStateOf<BabyResponse?>(null) }
    var allMeasurementsBaby by remember { mutableStateOf<BabyResponse?>(null) }
    var familyHistoryBaby   by remember { mutableStateOf<BabyResponse?>(null) }

    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    var originTab   by remember { mutableStateOf(NavigationTab.HOME) }

    val apiService = remember {
        ApiService(getToken = { preferencesManager.getAuthToken() })
    }

    val repository        = remember { AccountRepository(apiService, preferencesManager) }
    val socialAuthManager = remember { SocialAuthManager() }
    val socialLoginHelper = remember { SocialLoginHelper(repository) }

    val homeViewModel = remember {
        HomeViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    val addBabyViewModel = remember {
        AddBabyViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    val healthRecordViewModel = remember {
        HealthRecordViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    val settingsViewModel = remember {
        SettingsViewModel(
            apiService         = apiService,
            preferencesManager = preferencesManager,
            accountRepository  = repository,
        )
    }

    val familyHistoryViewModel = remember {
        FamilyHistoryViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    val signupViewModel = remember {
        SignupViewModel(
            repository        = repository,
            socialAuthManager = socialAuthManager,
            socialLoginHelper = socialLoginHelper
        )
    }

    val verifyAccountViewModel = remember {
        VerifyAccountViewModel(
            repository = repository,
            userEmail  = signupViewModel.uiState.email,
            userPhone  = signupViewModel.uiState.phone
        )
    }

    val enterNewPasswordViewModel = remember {
        EnterNewPasswordViewModel(authRepository = repository)
    }

    // FIX: removed duplicate `var currentLanguage` — now received as a parameter from App.kt

    InitializeSocialAuth(socialAuthManager)
    DisposableEffect(Unit) {
        onDispose {
            apiService.close()
            homeViewModel.onDestroy()
            addBabyViewModel.onDestroy()
            healthRecordViewModel.onDestroy()
            settingsViewModel.onDestroy()
            familyHistoryViewModel.onDestroy()
            cleanupSocialAuth(socialAuthManager)
        }
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when (targetState) {
                    Screen.Login,
                    Screen.Signup,
                    Screen.VerifyAccount ->
                        fadeIn(tween(600, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(tween(400, easing = FastOutSlowInEasing))

                    Screen.Welcome -> when (initialState) {
                        Screen.Login, Screen.Signup, Screen.VerifyAccount ->
                            fadeIn(tween(500)) togetherWith fadeOut(tween(300))
                        else ->
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec  = tween(400, easing = FastOutSlowInEasing)
                            ) + fadeIn(tween(300)) togetherWith
                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(tween(200))
                    }

                    Screen.Home ->
                        fadeIn(tween(400, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(tween(300, easing = FastOutSlowInEasing))

                    Screen.AddBaby,
                    Screen.EditBaby ->
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(200))

                    Screen.BabyProfile,
                    Screen.AddMeasurement,
                    Screen.AllMeasurements,
                    Screen.FamilyHistory ->
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(200))

                    else -> fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                }
            },
            label = "screen_transition"
        ) { screen ->

            when (screen) {

                // ── Splash ────────────────────────────────────────────────────
                Screen.Splash -> {
                    CompleteSplashScreen(
                        onSplashComplete = {
                            currentScreen = when {
                                repository.isLoggedIn() -> {
                                    homeViewModel.loadHomeData()
                                    Screen.Home
                                }
                                !preferencesManager.isOnboardingComplete() -> Screen.Onboarding
                                else -> Screen.Welcome
                            }
                        }
                    )
                }

                // ── Welcome ───────────────────────────────────────────────────
                Screen.Welcome -> {
                    WelcomeScreen(
                        onLoginClick          = { currentScreen = Screen.Login },
                        onSignUpClick         = { currentScreen = Screen.Signup },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope  = this@AnimatedContent
                    )
                }

                // ── Login ─────────────────────────────────────────────────────
                Screen.Login -> {
                    val viewModel = remember {
                        LoginViewModel(
                            authRepository    = repository,
                            socialAuthManager = socialAuthManager,
                            socialLoginHelper = socialLoginHelper
                        )
                    }
                    LoginScreen(
                        viewModel             = viewModel,
                        onBackClick           = { currentScreen = Screen.Welcome },
                        onLoginSuccess        = {
                            homeViewModel.loadHomeData()
                            currentScreen = Screen.Home
                        },
                        onForgotPasswordClick = { currentScreen = Screen.ForgotPassword },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope  = this@AnimatedContent
                    )
                }

                // ── Signup ────────────────────────────────────────────────────
                Screen.Signup -> {
                    SignupScreen(
                        viewModel             = signupViewModel,
                        onBackClick           = { currentScreen = Screen.Welcome },
                        onSignupSuccess       = {
                            verifyAccountViewModel.refreshContactInfo(
                                email = signupViewModel.uiState.email,
                                phone = signupViewModel.uiState.phone
                            )
                            currentScreen = Screen.VerifyAccount
                        },
                        onSocialSignupSuccess = {
                            homeViewModel.loadHomeData()
                            currentScreen = Screen.Home
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope  = this@AnimatedContent
                    )
                }

                // ── Forgot Password ───────────────────────────────────────────
                Screen.ForgotPassword -> {
                    val viewModel = remember { ForgotPasswordViewModel(repository) }
                    ForgotPasswordScreen(
                        viewModel      = viewModel,
                        onBackClick    = { currentScreen = Screen.Login },
                        onResetSuccess = { email ->
                            resetEmail    = email
                            currentScreen = Screen.EnterCode
                        }
                    )
                }

                // ── Enter Code ────────────────────────────────────────────────
                Screen.EnterCode -> {
                    val viewModel = remember { EnterCodeViewModel(repository, resetEmail) }
                    EnterCodeScreen(
                        viewModel      = viewModel,
                        emailOrPhone   = resetEmail,
                        onBackClick    = { currentScreen = Screen.ForgotPassword },
                        onCodeVerified = { email, code ->
                            resetEmail    = email
                            resetCode     = code
                            currentScreen = Screen.EnterNewPassword
                        }
                    )
                }

                // ── Enter New Password ────────────────────────────────────────
                Screen.EnterNewPassword -> {
                    EnterNewPasswordScreen(
                        viewModel              = enterNewPasswordViewModel,
                        emailOrPhone           = resetEmail,
                        verificationCode       = resetCode,
                        onBackClick            = { currentScreen = Screen.EnterCode },
                        onPasswordResetSuccess = {
                            resetEmail    = ""
                            resetCode     = ""
                            currentScreen = Screen.Login
                        }
                    )
                }

                // ── Verify Account ────────────────────────────────────────────
                Screen.VerifyAccount -> {
                    VerifyAccountScreen(
                        viewModel             = verifyAccountViewModel,
                        onBackClick           = { currentScreen = Screen.Signup },
                        onVerificationSuccess = {
                            preferencesManager.setUserLoggedIn(true)
                            homeViewModel.loadHomeData()
                            currentScreen = Screen.Home
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope  = this@AnimatedContent
                    )
                }

                // ── Onboarding ────────────────────────────────────────────────
                Screen.Onboarding -> {
                    OnboardingScreen(
                        onFinish = {
                            preferencesManager.setOnboardingComplete(true)
                            currentScreen = Screen.Welcome
                        }
                    )
                }

                // ── Home ──────────────────────────────────────────────────────
                Screen.Home -> {
                    HomeScreen(
                        viewModel             = homeViewModel,
                        healthRecordViewModel = healthRecordViewModel,
                        settingsViewModel     = settingsViewModel,
                        familyHistoryViewModel = familyHistoryViewModel,
                        currentLanguage       = currentLanguage,
                        onLanguageChange      = { newLanguage ->
                            preferencesManager.setLanguage(newLanguage)
                            onLanguageChange(newLanguage)   // FIX: bubble to App.kt, no duplicate state
                        },
                        onDarkModeChange    = onDarkModeChange,     // FIX: wired through
                        onGenderThemeChange = onGenderThemeChange,  // FIX: wired through
                        selectedTab  = selectedTab,
                        onTabChange  = { selectedTab = it },
                        onAddBaby    = {
                            originTab = selectedTab
                            addBabyViewModel.resetForm()
                            currentScreen = Screen.AddBaby
                        },
                        onSeeProfile = { baby ->
                            originTab     = selectedTab
                            selectedBaby  = baby
                            currentScreen = Screen.BabyProfile
                        },
                        onEditDetails = { baby ->
                            originTab = selectedTab
                            addBabyViewModel.prefillFromBaby(baby)
                            currentScreen = Screen.EditBaby
                        },
                        onAddMeasurement = { baby ->
                            originTab       = selectedTab
                            measurementBaby = baby
                            currentScreen   = Screen.AddMeasurement
                        },
                        onViewGrowthChart = { _ ->
                            selectedTab = NavigationTab.CHARTS
                        },
                        onAddMeasurementById = { babyId ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                originTab       = selectedTab
                                measurementBaby = baby
                                currentScreen   = Screen.AddMeasurement
                            }
                        },
                        onViewAllMeasurementsById = { babyId ->
                            originTab = selectedTab
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                allMeasurementsBaby = baby
                                currentScreen       = Screen.AllMeasurements
                            }
                        },
                        onNavigateToWelcome = {
                            currentScreen = Screen.Welcome
                        },
                        onNavigateToFamilyHistory = { babyId, babyName ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                familyHistoryBaby = baby
                                familyHistoryViewModel.loadFamilyHistory(babyId)
                                currentScreen = Screen.FamilyHistory
                            }
                        },
                    )
                }

                // ── Add Baby (new) ────────────────────────────────────────────
                Screen.AddBaby -> {
                    AddBabyScreen(
                        viewModel = addBabyViewModel,
                        onBack    = {
                            selectedTab   = originTab
                            currentScreen = Screen.Home
                        },
                        onSaved   = {
                            homeViewModel.loadHomeData()
                            selectedTab   = originTab
                            currentScreen = Screen.Home
                        }
                    )
                }

                // ── Edit Baby (pre-filled) ────────────────────────────────────
                Screen.EditBaby -> {
                    AddBabyScreen(
                        viewModel = addBabyViewModel,
                        onBack    = {
                            if (selectedBaby != null) {
                                currentScreen = Screen.BabyProfile
                            } else {
                                selectedTab   = originTab
                                currentScreen = Screen.Home
                            }
                        },
                        onSaved   = {
                            homeViewModel.loadHomeData()
                            selectedBaby = homeViewModel.uiState.babies
                                .find { it.babyId == selectedBaby?.babyId }
                                ?: selectedBaby
                            if (selectedBaby != null && originTab == NavigationTab.BABY) {
                                currentScreen = Screen.BabyProfile
                            } else {
                                selectedTab   = originTab
                                currentScreen = Screen.Home
                            }
                        }
                    )
                }

                // ── Baby Profile ──────────────────────────────────────────────
                Screen.BabyProfile -> {
                    val baby = selectedBaby
                    if (baby == null) {
                        currentScreen = Screen.Home
                    } else {
                        BabyProfileScreen(
                            baby         = baby,
                            vaccinations = homeViewModel.uiState
                                .upcomingVaccinations[baby.babyId] ?: emptyList(),
                            latestGrowth = homeViewModel.uiState
                                .latestGrowthRecords[baby.babyId],
                            onBack = {
                                selectedBaby  = null
                                selectedTab   = originTab
                                currentScreen = Screen.Home
                            },
                            onEditDetails = {
                                addBabyViewModel.prefillFromBaby(baby)
                                currentScreen = Screen.EditBaby
                            },
                            onDeleteBaby = {
                                homeViewModel.loadHomeData()
                                selectedBaby  = null
                                selectedTab   = originTab
                                currentScreen = Screen.Home
                            },
                            onAddMeasurement = {
                                measurementBaby = baby
                                currentScreen   = Screen.AddMeasurement
                            },
                            onViewGrowthChart = {
                                selectedBaby  = null
                                selectedTab   = NavigationTab.CHARTS
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }

                // ── Add Measurement ───────────────────────────────────────────
                Screen.AddMeasurement -> {
                    val baby = measurementBaby
                    if (baby == null) {
                        currentScreen = Screen.Home
                    } else {
                        AddMeasurementScreen(
                            babyId             = baby.babyId,
                            babyName           = baby.fullName,
                            apiService         = apiService,
                            preferencesManager = preferencesManager,
                            onBack = {
                                measurementBaby = null
                                if (selectedBaby != null) {
                                    currentScreen = Screen.BabyProfile
                                } else {
                                    selectedTab   = originTab
                                    currentScreen = Screen.Home
                                }
                            },
                            onSaved = {
                                homeViewModel.loadHomeData()
                                measurementBaby = null
                                if (selectedBaby != null) {
                                    currentScreen = Screen.BabyProfile
                                } else {
                                    selectedTab   = originTab
                                    currentScreen = Screen.Home
                                }
                            }
                        )
                    }
                }

                // ── All Measurements ──────────────────────────────────────────
                Screen.AllMeasurements -> {
                    val baby = allMeasurementsBaby
                    if (baby == null) {
                        currentScreen = Screen.Home
                    } else {
                        val isFemale = baby.gender.equals("FEMALE", ignoreCase = true) ||
                                baby.gender.equals("GIRL", ignoreCase = true)
                        AllMeasurementsScreen(
                            babyId    = baby.babyId,
                            babyName  = baby.fullName,
                            isFemale  = isFemale,
                            viewModel = homeViewModel,
                            onBack    = {
                                allMeasurementsBaby = null
                                if (selectedBaby != null) {
                                    currentScreen = Screen.BabyProfile
                                } else {
                                    selectedTab   = originTab
                                    currentScreen = Screen.Home
                                }
                            }
                        )
                    }
                }

                // ── Family History ────────────────────────────────────────────
                Screen.FamilyHistory -> {
                    val baby = familyHistoryBaby
                    if (baby == null) {
                        currentScreen = Screen.Home
                    } else {
                        FamilyHistoryScreen(
                            babyId    = baby.babyId,
                            babyName  = baby.fullName,
                            viewModel = familyHistoryViewModel,
                            onBack    = {
                                familyHistoryBaby = null
                                currentScreen     = Screen.Home
                            }
                        )
                    }
                }

            } // end when(screen)
        } // end AnimatedContent
    } // end SharedTransitionLayout
}

// ─────────────────────────────────────────────────────────────────────────────
// Platform expectations
// ─────────────────────────────────────────────────────────────────────────────

@Composable
expect fun InitializeSocialAuth(socialAuthManager: SocialAuthManager)

expect fun cleanupSocialAuth(socialAuthManager: SocialAuthManager)

@Composable
expect fun rememberPreferencesManager(): PreferencesManager