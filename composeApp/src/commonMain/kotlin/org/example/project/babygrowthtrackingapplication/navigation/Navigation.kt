package org.example.project.babygrowthtrackingapplication.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.splash.CompleteSplashScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.HomeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddBabyScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddMeasurementScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.BabyProfileScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.MemoryScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.onBoarding.OnboardingScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens.EnterCodeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.EnterCodeViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens.EnterNewPasswordScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.EnterNewPasswordViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens.ForgotPasswordScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.ForgotPasswordViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens.LoginScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.LoginViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens.SignupScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.SignupViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens.VerifyAccountScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.VerifyAccountViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens.WelcomeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.AddBabyViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HomeViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HealthRecordViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.SettingsViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.FamilyHistoryViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.ChildIllnessesViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VisionMotorViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HearingSpeechViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.GuideViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.MemoryViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AllMeasurementsScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.ChildIllnessesScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.FamilyHistoryScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.VisionMotorScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.HearingSpeechScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.SleepGuideScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.FeedingGuideScreen
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.example.project.babygrowthtrackingapplication.data.auth.SocialLoginHelper
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
import org.example.project.babygrowthtrackingapplication.data.repository.GuideRepository
import org.example.project.babygrowthtrackingapplication.notifications.DeepLinkRoutes
import org.example.project.babygrowthtrackingapplication.notifications.FcmTokenService
import org.example.project.babygrowthtrackingapplication.notifications.NotificationRepository
import org.example.project.babygrowthtrackingapplication.notifications.NotificationScreen
import org.example.project.babygrowthtrackingapplication.notifications.NotificationViewModel
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.ui.components.NavigationTab

// ─────────────────────────────────────────────────────────────────────────────
// Screens that are safe to restore after the app resumes.
// ─────────────────────────────────────────────────────────────────────────────
private val RESTORABLE_SCREENS = setOf(
    Screen.Home,
    Screen.Memory,
    Screen.SleepGuide,
    Screen.FeedingGuide,
    Screen.FamilyHistory,
    Screen.ChildIllnesses,
    Screen.ChildDevVisionMotor,
    Screen.ChildDevHearingSpeech,
    Screen.AllMeasurements,
)

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
    FamilyHistory,
    ChildIllnesses,
    ChildDevVisionMotor,
    ChildDevHearingSpeech,
    SleepGuide,
    FeedingGuide,
    Memory,
    // ── NEW ──────────────────────────────────────────────────────────────────
    Notifications,
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun resolveScreen(name: String?): Screen? {
    if (name == null) return null
    return try {
        val candidate = Screen.valueOf(name)
        if (candidate in RESTORABLE_SCREENS) candidate else null
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun resolveTab(name: String): NavigationTab {
    return try {
        NavigationTab.valueOf(name)
    } catch (_: IllegalArgumentException) {
        NavigationTab.HOME
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Deep-link route → Screen mapping
// Translates the String route stored inside AppNotification.deepLinkRoute
// into a concrete Screen enum value so AppNavigation can navigate to it.
// ─────────────────────────────────────────────────────────────────────────────
private fun deepLinkRouteToScreen(route: String): Screen? = when (route) {
    DeepLinkRoutes.HOME            -> Screen.Home
    DeepLinkRoutes.GROWTH_CHART    -> Screen.Home   // we switch the tab below
    DeepLinkRoutes.FAMILY_HISTORY  -> Screen.FamilyHistory
    DeepLinkRoutes.CHILD_ILLNESSES -> Screen.ChildIllnesses
    DeepLinkRoutes.VISION_MOTOR    -> Screen.ChildDevVisionMotor
    DeepLinkRoutes.HEARING_SPEECH  -> Screen.ChildDevHearingSpeech
    DeepLinkRoutes.MEMORIES        -> Screen.Memory
    DeepLinkRoutes.SETTINGS        -> Screen.Home   // we switch to settings tab
    DeepLinkRoutes.ADD_MEASUREMENT -> Screen.AddMeasurement
    else                           -> null
}

// ─────────────────────────────────────────────────────────────────────────────
// AppNavigation
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    currentLanguage     : Language              = Language.ENGLISH,
    startRoute          : String?               = null,
    onLanguageChange    : (Language) -> Unit    = {},
    onDarkModeChange    : (Boolean) -> Unit     = {},
    onGenderThemeChange : (GenderTheme) -> Unit = {},
) {
    val preferencesManager = rememberPreferencesManager()
    var currentScreen by remember { mutableStateOf(Screen.Splash) }

    var resetEmail by remember { mutableStateOf("") }
    var resetCode  by remember { mutableStateOf("") }

    // ── Baby-specific navigation state ────────────────────────────────────────
    var selectedBaby        by remember { mutableStateOf<BabyResponse?>(null) }
    var measurementBaby     by remember { mutableStateOf<BabyResponse?>(null) }
    var allMeasurementsBaby by remember { mutableStateOf<BabyResponse?>(null) }
    var familyHistoryBaby   by remember { mutableStateOf<BabyResponse?>(null) }
    var childIllnessesBaby  by remember { mutableStateOf<BabyResponse?>(null) }
    var childDevBaby        by remember { mutableStateOf<BabyResponse?>(null) }

    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    var originTab   by remember { mutableStateOf(NavigationTab.HOME) }

    // ── Services & repositories ───────────────────────────────────────────────
    val apiService = remember {
        ApiService(getToken = { preferencesManager.getAuthToken() })
    }

    val repository        = remember { AccountRepository(apiService, preferencesManager) }
    val socialAuthManager = remember { SocialAuthManager() }
    val socialLoginHelper = remember { SocialLoginHelper(repository) }

    // ── ViewModels ────────────────────────────────────────────────────────────
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
    val childIllnessesViewModel = remember {
        ChildIllnessesViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }
    val visionMotorViewModel = remember {
        VisionMotorViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }
    val hearingSpeechViewModel = remember {
        HearingSpeechViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }
    val guideRepository = remember { GuideRepository(apiService) }
    val guideViewModel  = remember { GuideViewModel(repository = guideRepository) }
    val memoryViewModel = remember {
        MemoryViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    // ── NEW: Notification infrastructure ─────────────────────────────────────
    //
    // NotificationRepository needs an HttpClient.  We re-use apiService's
    // internal client via a thin wrapper that borrows the same base URL and
    // auth-token supplier.  Adjust the baseUrl constant to match your backend.
    val notificationRepository = remember {
        NotificationRepository(
            client   = apiService.httpClient,          // expose httpClient from ApiService (see note below)
            baseUrl  = apiService.baseUrl,             // expose baseUrl from ApiService
            getToken = { preferencesManager.getAuthToken() }
        )
    }
    val fcmTokenService = remember { FcmTokenService() }
    val notificationViewModel = remember {
        NotificationViewModel(
            repository      = notificationRepository,
            getUserId       = { preferencesManager.getUserId() },
            fcmTokenService = fcmTokenService
        )
    }
    // ─────────────────────────────────────────────────────────────────────────

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

    LaunchedEffect(startRoute, repository.isLoggedIn()) {
        if (startRoute != null && repository.isLoggedIn()) {
            notificationViewModel.onDeepLinkReceived(startRoute)
        }
    }

    // ── Persist navigation state on every screen change ───────────────────────
    LaunchedEffect(currentScreen, selectedTab) {
        if (currentScreen in RESTORABLE_SCREENS && repository.isLoggedIn()) {
            preferencesManager.saveLastScreen(currentScreen.name, selectedTab.name)
        }
    }

    // ── Start / stop notification polling based on login state ────────────────
    // When the user logs out we stop polling; on login we (re)start it.
    LaunchedEffect(repository.isLoggedIn()) {
        if (repository.isLoggedIn()) {
            notificationViewModel.startUnreadPolling()
        } else {
            notificationViewModel.stopPolling()
        }
    }


    // ── Handle deep-link navigation emitted by NotificationViewModel ──────────
    // When the user taps a notification that carries a deepLinkRoute, the
    // NotificationViewModel sets pendingNavigateTo.  We observe it here so that
    // we can perform the actual navigation inside AppNavigation, which owns all
    // navigation state.
    val notifState = notificationViewModel.uiState
    LaunchedEffect(notifState.pendingNavigateTo) {
        val route = notifState.pendingNavigateTo ?: return@LaunchedEffect
        val targetScreen = deepLinkRouteToScreen(route)

        when (route) {
            DeepLinkRoutes.GROWTH_CHART -> {
                selectedTab   = NavigationTab.CHARTS
                currentScreen = Screen.Home
            }
            DeepLinkRoutes.SETTINGS -> {
                selectedTab   = NavigationTab.SETTINGS
                currentScreen = Screen.Home
            }
            DeepLinkRoutes.ADD_MEASUREMENT -> {
                // Navigate to AddMeasurement for the currently-selected baby
                val baby = homeViewModel.uiState.selectedBaby
                if (baby != null) {
                    originTab       = selectedTab
                    measurementBaby = baby
                    currentScreen   = Screen.AddMeasurement
                } else {
                    currentScreen = Screen.Home
                }
            }
            DeepLinkRoutes.FAMILY_HISTORY -> {
                val baby = homeViewModel.uiState.selectedBaby
                if (baby != null) {
                    familyHistoryBaby = baby
                    familyHistoryViewModel.loadFamilyHistory(baby.babyId)
                    currentScreen = Screen.FamilyHistory
                } else currentScreen = Screen.Home
            }
            DeepLinkRoutes.CHILD_ILLNESSES -> {
                val baby = homeViewModel.uiState.selectedBaby
                if (baby != null) {
                    childIllnessesBaby = baby
                    childIllnessesViewModel.loadIllnesses(baby.babyId)
                    currentScreen = Screen.ChildIllnesses
                } else currentScreen = Screen.Home
            }
            DeepLinkRoutes.VISION_MOTOR -> {
                val baby = homeViewModel.uiState.selectedBaby
                if (baby != null) {
                    childDevBaby  = baby
                    visionMotorViewModel.load(baby.babyId, baby.ageInMonths)
                    currentScreen = Screen.ChildDevVisionMotor
                } else currentScreen = Screen.Home
            }
            DeepLinkRoutes.HEARING_SPEECH -> {
                val baby = homeViewModel.uiState.selectedBaby
                if (baby != null) {
                    childDevBaby  = baby
                    hearingSpeechViewModel.load(baby.babyId, baby.ageInMonths)
                    currentScreen = Screen.ChildDevHearingSpeech
                } else currentScreen = Screen.Home
            }
            DeepLinkRoutes.MEMORIES -> {
                originTab     = selectedTab
                currentScreen = Screen.Memory
            }
            else -> {
                if (targetScreen != null) currentScreen = targetScreen
                else currentScreen = Screen.Home
            }
        }

        notificationViewModel.onNavigationHandled()
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    InitializeSocialAuth(socialAuthManager)
    DisposableEffect(Unit) {
        onDispose {
            apiService.close()
            homeViewModel.onDestroy()
            addBabyViewModel.onDestroy()
            healthRecordViewModel.onDestroy()
            settingsViewModel.onDestroy()
            familyHistoryViewModel.onDestroy()
            childIllnessesViewModel.onDestroy()
            visionMotorViewModel.onDestroy()
            hearingSpeechViewModel.onDestroy()
            guideViewModel.onDestroy()
            memoryViewModel.onDestroy()
            notificationViewModel.onDestroy()   // ← NEW
            cleanupSocialAuth(socialAuthManager)
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────
    SharedTransitionLayout {
        AnimatedContent(
            targetState  = currentScreen,
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
                    Screen.FamilyHistory,
                    Screen.ChildIllnesses,
                    Screen.ChildDevVisionMotor,
                    Screen.ChildDevHearingSpeech,
                    Screen.SleepGuide,
                    Screen.FeedingGuide,
                    Screen.Memory,
                    Screen.Notifications ->          // ← NEW: same slide-in transition
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
                            if (!repository.isLoggedIn()) {
                                currentScreen = if (!preferencesManager.isOnboardingComplete())
                                    Screen.Onboarding else Screen.Welcome
                            } else {
                                val restoredScreen = resolveScreen(preferencesManager.getLastScreen())
                                val restoredTab    = resolveTab(preferencesManager.getLastTab())

                                if (restoredScreen != null) {
                                    selectedTab   = restoredTab
                                    originTab     = restoredTab
                                    homeViewModel.loadHomeData()
                                    settingsViewModel.refreshProfile()
                                    when (restoredScreen) {
                                        Screen.Memory -> { /* loads lazily */ }
                                        Screen.SleepGuide,
                                        Screen.FeedingGuide -> { /* loads on demand */ }
                                        else -> { }
                                    }
                                    currentScreen = restoredScreen
                                } else {
                                    homeViewModel.loadHomeData()
                                    settingsViewModel.refreshProfile()
                                    currentScreen = Screen.Home
                                }
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
                            settingsViewModel.refreshProfile()
                            notificationViewModel.startUnreadPolling()  // ← NEW
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
                            settingsViewModel.refreshProfile()
                            notificationViewModel.startUnreadPolling()  // ← NEW
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
                            settingsViewModel.refreshProfile()
                            notificationViewModel.startUnreadPolling()  // ← NEW
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
                        viewModel                  = homeViewModel,
                        healthRecordViewModel      = healthRecordViewModel,
                        settingsViewModel          = settingsViewModel,
                        familyHistoryViewModel     = familyHistoryViewModel,
                        childIllnessesViewModel    = childIllnessesViewModel,
                        visionMotorViewModel       = visionMotorViewModel,
                        hearingSpeechViewModel     = hearingSpeechViewModel,
                        guideViewModel             = guideViewModel,
                        notificationViewModel      = notificationViewModel,  // ← NEW
                        currentLanguage            = currentLanguage,
                        onLanguageChange           = { newLanguage ->
                            preferencesManager.setLanguage(newLanguage)
                            onLanguageChange(newLanguage)
                        },
                        onDarkModeChange           = onDarkModeChange,
                        onGenderThemeChange        = onGenderThemeChange,
                        selectedTab                = selectedTab,
                        onTabChange                = { selectedTab = it },
                        onAddBaby                  = {
                            originTab = selectedTab
                            addBabyViewModel.resetForm()
                            currentScreen = Screen.AddBaby
                        },
                        onSeeProfile               = { baby ->
                            originTab     = selectedTab
                            selectedBaby  = baby
                            currentScreen = Screen.BabyProfile
                        },
                        onEditDetails              = { baby ->
                            originTab = selectedTab
                            addBabyViewModel.prefillFromBaby(baby)
                            currentScreen = Screen.EditBaby
                        },
                        onAddMeasurement           = { baby ->
                            originTab       = selectedTab
                            measurementBaby = baby
                            currentScreen   = Screen.AddMeasurement
                        },
                        onViewGrowthChart          = { _ ->
                            selectedTab = NavigationTab.CHARTS
                        },
                        onAddMeasurementById       = { babyId ->
                            val baby = homeViewModel.uiState.babies
                                .firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                originTab       = selectedTab
                                measurementBaby = baby
                                currentScreen   = Screen.AddMeasurement
                            }
                        },
                        onViewAllMeasurementsById  = { babyId ->
                            originTab = selectedTab
                            val baby = homeViewModel.uiState.babies
                                .firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                allMeasurementsBaby = baby
                                currentScreen       = Screen.AllMeasurements
                            }
                        },
                        onNavigateToWelcome        = {
                            preferencesManager.clearLastScreen()
                            notificationViewModel.stopPolling()             // ← NEW
                            currentScreen = Screen.Welcome
                        },
                        onNavigateToFamilyHistory  = { babyId, babyName ->
                            val baby = homeViewModel.uiState.babies
                                .firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                familyHistoryBaby = baby
                                familyHistoryViewModel.loadFamilyHistory(babyId)
                                currentScreen = Screen.FamilyHistory
                            }
                        },
                        onNavigateToChildIllnesses = { babyId, babyName ->
                            val baby = homeViewModel.uiState.babies
                                .firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                childIllnessesBaby = baby
                                childIllnessesViewModel.loadIllnesses(babyId)
                                currentScreen = Screen.ChildIllnesses
                            }
                        },
                        onNavigateToVisionMotor    = { babyId, babyName ->
                            val baby = homeViewModel.uiState.babies
                                .firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                childDevBaby  = baby
                                visionMotorViewModel.load(baby.babyId, baby.ageInMonths)
                                currentScreen = Screen.ChildDevVisionMotor
                            }
                        },
                        onNavigateToHearingSpeech  = { babyId, babyName ->
                            val baby = homeViewModel.uiState.babies
                                .firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                childDevBaby  = baby
                                hearingSpeechViewModel.load(baby.babyId, baby.ageInMonths)
                                currentScreen = Screen.ChildDevHearingSpeech
                            }
                        },
                        onNavigateToSleepGuide     = {
                            originTab     = selectedTab
                            currentScreen = Screen.SleepGuide
                        },
                        onNavigateToFeedingGuide   = {
                            originTab     = selectedTab
                            currentScreen = Screen.FeedingGuide
                        },
                        onNavigateToMemory         = {
                            originTab     = selectedTab
                            currentScreen = Screen.Memory
                        },
                        onNavigateToNotifications  = {                       // ← NEW
                            originTab     = selectedTab
                            notificationViewModel.loadNotifications(refresh = true)
                            currentScreen = Screen.Notifications
                        },
                    )
                }

                // ── Add Baby ──────────────────────────────────────────────────
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

                // ── Edit Baby ─────────────────────────────────────────────────
                Screen.EditBaby -> {
                    AddBabyScreen(
                        viewModel = addBabyViewModel,
                        onBack    = {
                            if (selectedBaby != null) currentScreen = Screen.BabyProfile
                            else { selectedTab = originTab; currentScreen = Screen.Home }
                        },
                        onSaved   = {
                            homeViewModel.loadHomeData()
                            selectedBaby = homeViewModel.uiState.babies
                                .find { it.babyId == selectedBaby?.babyId }
                                ?: selectedBaby
                            if (selectedBaby != null && originTab == NavigationTab.BABY)
                                currentScreen = Screen.BabyProfile
                            else { selectedTab = originTab; currentScreen = Screen.Home }
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
                            onBack           = {
                                selectedBaby  = null
                                selectedTab   = originTab
                                currentScreen = Screen.Home
                            },
                            onEditDetails    = {
                                addBabyViewModel.prefillFromBaby(baby)
                                currentScreen = Screen.EditBaby
                            },
                            onDeleteBaby     = {
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
                            onBack             = {
                                measurementBaby = null
                                if (selectedBaby != null) currentScreen = Screen.BabyProfile
                                else { selectedTab = originTab; currentScreen = Screen.Home }
                            },
                            onSaved            = {
                                homeViewModel.loadHomeData()
                                measurementBaby = null
                                if (selectedBaby != null) currentScreen = Screen.BabyProfile
                                else { selectedTab = originTab; currentScreen = Screen.Home }
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
                                if (selectedBaby != null) currentScreen = Screen.BabyProfile
                                else { selectedTab = originTab; currentScreen = Screen.Home }
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

                // ── Child Illnesses ───────────────────────────────────────────
                Screen.ChildIllnesses -> {
                    val baby = childIllnessesBaby
                    if (baby == null) {
                        currentScreen = Screen.Home
                    } else {
                        ChildIllnessesScreen(
                            babyId    = baby.babyId,
                            babyName  = baby.fullName,
                            viewModel = childIllnessesViewModel,
                            onBack    = {
                                childIllnessesBaby = null
                                currentScreen      = Screen.Home
                            }
                        )
                    }
                }

                // ── Child Dev: Vision + Motor ─────────────────────────────────
                Screen.ChildDevVisionMotor -> {
                    val baby = childDevBaby
                    if (baby == null) {
                        currentScreen = Screen.Home
                    } else {
                        VisionMotorScreen(
                            babyId        = baby.babyId,
                            babyName      = baby.fullName,
                            babyAgeMonths = baby.ageInMonths,
                            viewModel     = visionMotorViewModel,
                            onBack        = {
                                childDevBaby  = null
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }

                // ── Child Dev: Hearing + Speech ───────────────────────────────
                Screen.ChildDevHearingSpeech -> {
                    val baby = childDevBaby
                    if (baby == null) {
                        currentScreen = Screen.Home
                    } else {
                        HearingSpeechScreen(
                            babyId        = baby.babyId,
                            babyName      = baby.fullName,
                            babyAgeMonths = baby.ageInMonths,
                            viewModel     = hearingSpeechViewModel,
                            onBack        = {
                                childDevBaby  = null
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }

                // ── Sleep Guide ───────────────────────────────────────────────
                Screen.SleepGuide -> {
                    SleepGuideScreen(
                        babies    = homeViewModel.uiState.babies,
                        viewModel = guideViewModel,
                        language  = currentLanguage.code,
                        onBack    = {
                            selectedTab   = originTab
                            currentScreen = Screen.Home
                        }
                    )
                }

                // ── Feeding Guide ─────────────────────────────────────────────
                Screen.FeedingGuide -> {
                    FeedingGuideScreen(
                        babies    = homeViewModel.uiState.babies,
                        viewModel = guideViewModel,
                        language  = currentLanguage.code,
                        onBack    = {
                            selectedTab   = originTab
                            currentScreen = Screen.Home
                        }
                    )
                }

                // ── Memory ────────────────────────────────────────────────────
                Screen.Memory -> {
                    MemoryScreen(
                        viewModel      = memoryViewModel,
                        babies         = homeViewModel.uiState.babies,
                        selectedBabyId = homeViewModel.uiState.selectedBaby?.babyId,
                        language       = currentLanguage,
                        onBack         = {
                            selectedTab   = originTab
                            currentScreen = Screen.Home
                        }
                    )
                }

                // ── Notifications ─────────────────────────────────────────────  ← NEW
                Screen.Notifications -> {
                    NotificationScreen(
                        viewModel  = notificationViewModel,
                        onBack     = {
                            selectedTab   = originTab
                            currentScreen = Screen.Home
                        },
                        // Deep-link navigation is handled by the LaunchedEffect
                        // above that watches pendingNavigateTo, so onNavigate
                        // here is intentionally a no-op (the ViewModel already
                        // set pendingNavigateTo and the effect will fire).
                        onNavigate = { /* handled by LaunchedEffect above */ }
                    )
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