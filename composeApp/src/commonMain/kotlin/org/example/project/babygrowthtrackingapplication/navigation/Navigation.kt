package org.example.project.babygrowthtrackingapplication.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.splash.CompleteSplashScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.HomeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddBabyScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.BabyProfileScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddMeasurementScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AllMeasurementsScreen
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.onBoarding.OnboardingScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.AddBabyViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.example.project.babygrowthtrackingapplication.data.auth.SocialLoginHelper
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
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
    AddMeasurement,
    AllMeasurements
}

// ─────────────────────────────────────────────────────────────────────────────
// AppNavigation
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    onLanguageChange: (Language) -> Unit = {}
) {
    val preferencesManager = rememberPreferencesManager()
    var currentScreen by remember { mutableStateOf(Screen.Splash) }

    // ── Password-reset multi-step state ───────────────────────────────────────
    var resetEmail by remember { mutableStateOf("") }
    var resetCode  by remember { mutableStateOf("") }

    // ── Selected baby for profile screen ─────────────────────────────────────
    // Nullable so we never show BabyProfileScreen with stale data.
    var selectedBaby by remember { mutableStateOf<BabyResponse?>(null) }

    // ── Measurement screen context ────────────────────────────────────────────
    var measurementBabyId   by remember { mutableStateOf("") }
    var measurementBabyName by remember { mutableStateOf("") }
    var measurementIsFemale by remember { mutableStateOf(false) }

    // ── Tab state hoisted here so back navigation can restore the correct tab ─
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    // Records which tab the user was on before going to AddBaby or BabyProfile
    var originTab   by remember { mutableStateOf(NavigationTab.HOME) }

    val apiService = remember {
        ApiService(getToken = { preferencesManager.getAuthToken() })
    }

    val repository        = remember { AccountRepository(apiService, preferencesManager) }
    val socialAuthManager = remember { SocialAuthManager() }
    val socialLoginHelper = remember { SocialLoginHelper(repository) }

    // ── ViewModels hoisted to AppNavigation scope ─────────────────────────────

    val homeViewModel = remember {
        HomeViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    val addBabyViewModel = remember {
        AddBabyViewModel(apiService = apiService, preferencesManager = preferencesManager)
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

    var currentLanguage by remember { mutableStateOf(preferencesManager.getCurrentLanguage()) }

    InitializeSocialAuth(socialAuthManager)
    DisposableEffect(Unit) {
        onDispose {
            apiService.close()
            homeViewModel.onDestroy()
            addBabyViewModel.onDestroy()
            cleanupSocialAuth(socialAuthManager)
        }
    }

    // ── Screen transitions ────────────────────────────────────────────────────

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
                            fadeIn(tween(600, easing = FastOutSlowInEasing)) togetherWith
                                    fadeOut(tween(400, easing = FastOutSlowInEasing))
                        else ->
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    }

                    Screen.ForgotPassword ->
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(400)) togetherWith
                                slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(400))

                    Screen.EnterCode,
                    Screen.EnterNewPassword ->
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(400)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(400))

                    Screen.Onboarding ->
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec  = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(500)) togetherWith
                                slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(500))

                    Screen.Home ->
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec  = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(500)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(500))

                    // AddBaby slides up from the bottom — modal feel
                    Screen.AddBaby ->
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(200))

                    // BabyProfile slides in from the right — standard push feel
                    Screen.BabyProfile ->
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(200))

                    Screen.AddMeasurement, Screen.AllMeasurements ->
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
                        viewModel        = homeViewModel,
                        currentLanguage  = currentLanguage,
                        onLanguageChange = { newLanguage ->
                            preferencesManager.setLanguage(newLanguage)
                            currentLanguage = newLanguage
                            onLanguageChange(newLanguage)
                        },
                        selectedTab = selectedTab,
                        onTabChange = { selectedTab = it },
                        onAddBaby = {
                            originTab = selectedTab
                            addBabyViewModel.resetForm()
                            currentScreen = Screen.AddBaby
                        },
                        onSeeProfile = { baby ->
                            originTab     = selectedTab
                            selectedBaby  = baby
                            currentScreen = Screen.BabyProfile
                        },
                        onAddMeasurement = { babyId ->
                            val baby = homeViewModel.uiState.babies.find { it.babyId == babyId }
                            measurementBabyId   = babyId
                            measurementBabyName = baby?.fullName ?: ""
                            measurementIsFemale = baby?.gender?.let {
                                it.equals("FEMALE", ignoreCase = true) ||
                                        it.equals("GIRL",   ignoreCase = true)
                            } ?: false
                            originTab     = selectedTab
                            currentScreen = Screen.AddMeasurement
                        },
                        onViewAllMeasurements = { babyId ->
                            val baby = homeViewModel.uiState.babies.find { it.babyId == babyId }
                            measurementBabyId   = babyId
                            measurementBabyName = baby?.fullName ?: ""
                            measurementIsFemale = baby?.gender?.let {
                                it.equals("FEMALE", ignoreCase = true) ||
                                        it.equals("GIRL",   ignoreCase = true)
                            } ?: false
                            originTab     = selectedTab
                            currentScreen = Screen.AllMeasurements
                        }
                    )
                }

                // ── Add Baby ──────────────────────────────────────────────────
                Screen.AddBaby -> {
                    AddBabyScreen(
                        viewModel = addBabyViewModel,
                        onBack    = {
                            selectedTab   = originTab        // restore the tab we came from
                            currentScreen = Screen.Home
                        },
                        onSaved   = {
                            homeViewModel.loadHomeData()
                            selectedTab   = originTab        // restore the tab we came from
                            currentScreen = Screen.Home
                        }
                    )
                }

                // ── Baby Profile ──────────────────────────────────────────────
                Screen.BabyProfile -> {
                    // Guard: if selectedBaby is somehow null, go back to Home
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
                                selectedTab   = originTab    // restore the tab we came from
                                currentScreen = Screen.Home
                            },
                            onEditDetails = {
                                // TODO: navigate to EditBabyScreen when available
                            },
                            onDeleteBaby = {
                                homeViewModel.loadHomeData()
                                selectedBaby  = null
                                selectedTab   = originTab    // restore the tab we came from
                                currentScreen = Screen.Home
                            },
                            onAddMeasurement  = { /* TODO */ },
                            onViewGrowthChart = { /* TODO */ }
                        )
                    }
                }

                // ── Add Measurement ───────────────────────────────────────────
                Screen.AddMeasurement -> {
                    val genderTheme = if (measurementIsFemale) GenderTheme.GIRL else GenderTheme.BOY
                    BabyGrowthTheme(genderTheme = genderTheme) {
                        AddMeasurementScreen(
                            babyId     = measurementBabyId,
                            babyName   = measurementBabyName,
                            isFemale   = measurementIsFemale,
                            viewModel  = homeViewModel,
                            apiService = apiService,
                            userId     = homeViewModel.uiState.userId,
                            onBack     = {
                                selectedTab   = originTab
                                currentScreen = Screen.Home
                            },
                            onSaved    = {
                                selectedTab   = originTab
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }

                // ── All Measurements ──────────────────────────────────────────
                Screen.AllMeasurements -> {
                    val genderTheme = if (measurementIsFemale) GenderTheme.GIRL else GenderTheme.BOY
                    BabyGrowthTheme(genderTheme = genderTheme) {
                        AllMeasurementsScreen(
                            babyId    = measurementBabyId,
                            babyName  = measurementBabyName,
                            isFemale  = measurementIsFemale,
                            viewModel = homeViewModel,
                            onBack    = {
                                selectedTab   = originTab
                                currentScreen = Screen.Home
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