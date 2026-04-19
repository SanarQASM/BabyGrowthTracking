package org.example.project.babygrowthtrackingapplication.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import org.example.project.babygrowthtrackingapplication.admin.AdminHomeScreen
import org.example.project.babygrowthtrackingapplication.admin.AdminLoginScreen
import org.example.project.babygrowthtrackingapplication.admin.AdminLoginViewModel
import org.example.project.babygrowthtrackingapplication.admin.AdminViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.splash.CompleteSplashScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.HomeScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddBabyScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.AddMeasurementScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.BabyProfileScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.MemoryScreen
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen.PreCheckInvestigationScreen
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
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.PreCheckInvestigationViewModel
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
import org.example.project.babygrowthtrackingapplication.team.TeamVaccinationScreen
import org.example.project.babygrowthtrackingapplication.team.TeamVaccinationViewModel
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
    Screen.PreCheckInvestigation,
    Screen.AllMeasurements,
    Screen.AdminHome,
    Screen.TeamHome,    // ← team screen is restorable
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
    PreCheckInvestigation,
    SleepGuide,
    FeedingGuide,
    Memory,
    Notifications,

    // ── Admin screens ─────────────────────────────────────────────────────────
    AdminLogin,
    AdminHome,

    // ── Team screens ──────────────────────────────────────────────────────────
    TeamHome,
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

private fun isAdminSession(preferencesManager: PreferencesManager): Boolean {
    val role = preferencesManager.getString("user_role", "")
    return role.equals("ADMIN", ignoreCase = true)
}

/**
 * Returns true when the logged-in user has the VACCINATION_TEAM role.
 */
private fun isTeamSession(preferencesManager: PreferencesManager): Boolean {
    val role = preferencesManager.getString("user_role", "")
    return role.equals("VACCINATION_TEAM", ignoreCase = true)
}

private fun deepLinkRouteToScreen(route: String): Screen? = when (route) {
    DeepLinkRoutes.HOME            -> Screen.Home
    DeepLinkRoutes.GROWTH_CHART    -> Screen.Home
    DeepLinkRoutes.FAMILY_HISTORY  -> Screen.FamilyHistory
    DeepLinkRoutes.CHILD_ILLNESSES -> Screen.ChildIllnesses
    DeepLinkRoutes.VISION_MOTOR    -> Screen.ChildDevVisionMotor
    DeepLinkRoutes.HEARING_SPEECH  -> Screen.ChildDevHearingSpeech
    DeepLinkRoutes.MEMORIES        -> Screen.Memory
    DeepLinkRoutes.SETTINGS        -> Screen.Home
    DeepLinkRoutes.ADD_MEASUREMENT -> Screen.AddMeasurement
    else                           -> null
}

// ─────────────────────────────────────────────────────────────────────────────
// AppNavigation
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    currentLanguage    : Language = Language.ENGLISH,
    startRoute         : String?  = null,
    onLanguageChange   : (Language) -> Unit    = {},
    onDarkModeChange   : (Boolean) -> Unit     = {},
    onGenderThemeChange: (GenderTheme) -> Unit = {},
) {
    val preferencesManager = rememberPreferencesManager()
    var currentScreen by remember { mutableStateOf(Screen.Splash) }

    var resetEmail by remember { mutableStateOf("") }
    var resetCode  by remember { mutableStateOf("") }

    var selectedBaby              by remember { mutableStateOf<BabyResponse?>(null) }
    var measurementBaby           by remember { mutableStateOf<BabyResponse?>(null) }
    var allMeasurementsBaby       by remember { mutableStateOf<BabyResponse?>(null) }
    var familyHistoryBaby         by remember { mutableStateOf<BabyResponse?>(null) }
    var childIllnessesBaby        by remember { mutableStateOf<BabyResponse?>(null) }
    var childDevBaby              by remember { mutableStateOf<BabyResponse?>(null) }
    var preCheckInvestigationBaby by remember { mutableStateOf<BabyResponse?>(null) }

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
    val preCheckInvestigationViewModel = remember {
        PreCheckInvestigationViewModel(
            apiService         = apiService,
            preferencesManager = preferencesManager
        )
    }
    val guideRepository = remember { GuideRepository(apiService) }
    val guideViewModel  = remember { GuideViewModel(repository = guideRepository) }
    val memoryViewModel = remember {
        MemoryViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    val notificationRepository = remember {
        NotificationRepository(
            client   = apiService.httpClient,
            baseUrl  = apiService.baseUrl,
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

    val settingsViewModel = remember {
        SettingsViewModel(
            apiService            = apiService,
            preferencesManager    = preferencesManager,
            accountRepository     = repository,
            notificationViewModel = notificationViewModel,
        )
    }

    val adminLoginViewModel = remember {
        AdminLoginViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }
    val adminViewModel = remember {
        AdminViewModel(apiService = apiService, preferencesManager = preferencesManager)
    }

    // ── Team ViewModel (created once, shared for the session) ─────────────────
    val teamViewModel = remember {
        TeamVaccinationViewModel(
            apiService         = apiService,
            preferencesManager = preferencesManager
        )
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

    LaunchedEffect(startRoute, repository.isLoggedIn()) {
        if (startRoute != null && repository.isLoggedIn()) {
            notificationViewModel.onDeepLinkReceived(startRoute)
        }
    }

    LaunchedEffect(currentScreen, selectedTab) {
        if (currentScreen in RESTORABLE_SCREENS && repository.isLoggedIn()) {
            preferencesManager.saveLastScreen(currentScreen.name, selectedTab.name)
        }
    }

    LaunchedEffect(repository.isLoggedIn()) {
        if (repository.isLoggedIn()) {
            notificationViewModel.startUnreadPolling()
        } else {
            notificationViewModel.stopPolling()
        }
    }

    val notifState = notificationViewModel.uiState
    LaunchedEffect(notifState.pendingNavigateTo) {
        val route = notifState.pendingNavigateTo ?: return@LaunchedEffect
        val targetScreen = deepLinkRouteToScreen(route)
        when (route) {
            DeepLinkRoutes.GROWTH_CHART -> {
                selectedTab = NavigationTab.CHARTS; currentScreen = Screen.Home
            }
            DeepLinkRoutes.SETTINGS -> {
                selectedTab = NavigationTab.SETTINGS; currentScreen = Screen.Home
            }
            DeepLinkRoutes.ADD_MEASUREMENT -> {
                val baby = homeViewModel.uiState.selectedBaby
                if (baby != null) {
                    originTab = selectedTab; measurementBaby = baby
                    currentScreen = Screen.AddMeasurement
                } else currentScreen = Screen.Home
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
                    childDevBaby = baby
                    visionMotorViewModel.load(baby.babyId, baby.ageInMonths)
                    currentScreen = Screen.ChildDevVisionMotor
                } else currentScreen = Screen.Home
            }
            DeepLinkRoutes.HEARING_SPEECH -> {
                val baby = homeViewModel.uiState.selectedBaby
                if (baby != null) {
                    childDevBaby = baby
                    hearingSpeechViewModel.load(baby.babyId, baby.ageInMonths)
                    currentScreen = Screen.ChildDevHearingSpeech
                } else currentScreen = Screen.Home
            }
            DeepLinkRoutes.MEMORIES -> {
                originTab = selectedTab; currentScreen = Screen.Memory
            }
            else -> {
                currentScreen = targetScreen ?: Screen.Home
            }
        }
        notificationViewModel.onNavigationHandled()
    }

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
            preCheckInvestigationViewModel.onDestroy()
            guideViewModel.onDestroy()
            memoryViewModel.onDestroy()
            notificationViewModel.onDestroy()
            adminViewModel.onDestroy()
            teamViewModel.onDestroy()          // ← dispose team ViewModel
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

                    Screen.AdminLogin ->
                        fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(tween(300, easing = FastOutSlowInEasing))

                    Screen.AdminHome ->
                        fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(tween(300, easing = FastOutSlowInEasing))

                    Screen.TeamHome ->
                        fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(tween(300, easing = FastOutSlowInEasing))

                    Screen.Welcome -> when (initialState) {
                        Screen.Login,
                        Screen.Signup,
                        Screen.VerifyAccount,
                        Screen.AdminLogin,
                        Screen.TeamHome ->
                            fadeIn(tween(500)) togetherWith fadeOut(tween(300))
                        else ->
                            slideInHorizontally(
                                initialOffsetX = { fullWidth: Int -> -fullWidth },
                                animationSpec  = tween(400, easing = FastOutSlowInEasing)
                            ) + fadeIn(tween(300)) togetherWith
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth: Int -> fullWidth },
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(tween(200))
                    }

                    Screen.Home ->
                        fadeIn(tween(400, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(tween(300, easing = FastOutSlowInEasing))

                    Screen.AddBaby, Screen.EditBaby ->
                        slideInVertically(
                            initialOffsetY = { fullHeight: Int -> fullHeight },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutVertically(
                                    targetOffsetY = { fullHeight: Int -> fullHeight },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(200))

                    else ->
                        slideInHorizontally(
                            initialOffsetX = { fullWidth: Int -> fullWidth },
                            animationSpec  = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth: Int -> fullWidth },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(200))
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
                                when {
                                    isAdminSession(preferencesManager) -> {
                                        currentScreen = Screen.AdminHome
                                    }
                                    isTeamSession(preferencesManager) -> {
                                        currentScreen = Screen.TeamHome
                                    }
                                    else -> {
                                        val restoredScreen = resolveScreen(preferencesManager.getLastScreen())
                                        val restoredTab    = resolveTab(preferencesManager.getLastTab())
                                        if (restoredScreen != null) {
                                            selectedTab = restoredTab
                                            originTab   = restoredTab
                                            homeViewModel.loadHomeData()
                                            settingsViewModel.refreshProfile()
                                            currentScreen = restoredScreen
                                        } else {
                                            homeViewModel.loadHomeData()
                                            settingsViewModel.refreshProfile()
                                            currentScreen = Screen.Home
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                // ── Welcome ───────────────────────────────────────────────────
                Screen.Welcome -> {
                    WelcomeScreen(
                        onLoginClick  = { currentScreen = Screen.Login },
                        onSignUpClick = { currentScreen = Screen.Signup },
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
                            val role = preferencesManager.getString("user_role", "")
                            when {
                                role.equals("ADMIN", ignoreCase = true) -> {
                                    currentScreen = Screen.AdminHome
                                }
                                role.equals("VACCINATION_TEAM", ignoreCase = true) -> {
                                    currentScreen = Screen.TeamHome
                                }
                                else -> {
                                    homeViewModel.loadHomeData()
                                    settingsViewModel.refreshProfile()
                                    notificationViewModel.startUnreadPolling()
                                    currentScreen = Screen.Home
                                }
                            }
                        },
                        onForgotPasswordClick = { currentScreen = Screen.ForgotPassword },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope  = this@AnimatedContent
                    )
                }

                // ── Admin Login ───────────────────────────────────────────────
                Screen.AdminLogin -> {
                    AdminLoginScreen(
                        viewModel     = adminLoginViewModel,
                        onLoginSuccess = { currentScreen = Screen.AdminHome },
                        onBackToLogin  = {
                            preferencesManager.remove("user_role")
                            currentScreen = Screen.Login
                        }
                    )
                }

                // ── Admin Home ────────────────────────────────────────────────
                Screen.AdminHome -> {
                    AdminHomeScreen(
                        viewModel         = adminViewModel,
                        apiService        = apiService,
                        onNavigateToLogin = {
                            preferencesManager.remove("user_role")
                            preferencesManager.clearLastScreen()
                            notificationViewModel.stopPolling()
                            currentScreen = Screen.Welcome
                        }
                    )
                }

                // ── Team Home ─────────────────────────────────────────────────
                Screen.TeamHome -> {
                    TeamVaccinationScreen(
                        viewModel           = teamViewModel,
                        onNavigateToWelcome = {
                            preferencesManager.remove("user_role")
                            preferencesManager.clearLastScreen()
                            notificationViewModel.stopPolling()
                            currentScreen = Screen.Welcome
                        }
                    )
                }

                // ── Signup ────────────────────────────────────────────────────
                Screen.Signup -> {
                    SignupScreen(
                        viewModel              = signupViewModel,
                        onBackClick            = { currentScreen = Screen.Welcome },
                        onRegistrationComplete = {
                            homeViewModel.loadHomeData()
                            settingsViewModel.refreshProfile()
                            notificationViewModel.startUnreadPolling()
                            currentScreen = Screen.Home
                        },
                        onSocialSignupSuccess  = {
                            homeViewModel.loadHomeData()
                            settingsViewModel.refreshProfile()
                            notificationViewModel.startUnreadPolling()
                            currentScreen = Screen.Home
                        },
                        sharedTransitionScope  = this@SharedTransitionLayout,
                        animatedContentScope   = this@AnimatedContent
                    )
                }

                // ── Forgot Password ───────────────────────────────────────────
                Screen.ForgotPassword -> {
                    val viewModel = remember { ForgotPasswordViewModel(repository) }
                    ForgotPasswordScreen(
                        viewModel      = viewModel,
                        onBackClick    = { currentScreen = Screen.Login },
                        onResetSuccess = { email ->
                            resetEmail = email; currentScreen = Screen.EnterCode
                        }
                    )
                }

                // ── Enter Code ────────────────────────────────────────────────
                Screen.EnterCode -> {
                    val viewModel = remember { EnterCodeViewModel(repository, resetEmail) }
                    EnterCodeScreen(
                        viewModel    = viewModel,
                        emailOrPhone = resetEmail,
                        onBackClick  = { currentScreen = Screen.ForgotPassword },
                        onCodeVerified = { email, code ->
                            resetEmail = email
                            resetCode  = code
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
                            resetEmail = ""
                            resetCode  = ""
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
                            notificationViewModel.startUnreadPolling()
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
                        viewModel                       = homeViewModel,
                        healthRecordViewModel           = healthRecordViewModel,
                        settingsViewModel               = settingsViewModel,
                        familyHistoryViewModel          = familyHistoryViewModel,
                        childIllnessesViewModel         = childIllnessesViewModel,
                        visionMotorViewModel            = visionMotorViewModel,
                        hearingSpeechViewModel          = hearingSpeechViewModel,
                        preCheckInvestigationViewModel  = preCheckInvestigationViewModel,
                        guideViewModel                  = guideViewModel,
                        notificationViewModel           = notificationViewModel,
                        currentLanguage                 = currentLanguage,
                        onLanguageChange                = { newLanguage ->
                            preferencesManager.setLanguage(newLanguage)
                            onLanguageChange(newLanguage)
                        },
                        onDarkModeChange                = onDarkModeChange,
                        onGenderThemeChange             = onGenderThemeChange,
                        selectedTab                     = selectedTab,
                        onTabChange                     = { selectedTab = it },
                        onAddBaby                       = {
                            originTab = selectedTab
                            addBabyViewModel.resetForm()
                            currentScreen = Screen.AddBaby
                        },
                        onSeeProfile                    = { baby ->
                            originTab     = selectedTab
                            selectedBaby  = baby
                            currentScreen = Screen.BabyProfile
                        },
                        onEditDetails                   = { baby ->
                            originTab = selectedTab
                            addBabyViewModel.prefillFromBaby(baby)
                            currentScreen = Screen.EditBaby
                        },
                        onAddMeasurement                = { baby ->
                            originTab       = selectedTab
                            measurementBaby = baby
                            currentScreen   = Screen.AddMeasurement
                        },
                        onViewGrowthChart               = { _ -> selectedTab = NavigationTab.CHARTS },
                        onAddMeasurementById            = { babyId ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                originTab       = selectedTab
                                measurementBaby = baby
                                currentScreen   = Screen.AddMeasurement
                            }
                        },
                        onViewAllMeasurementsById       = { babyId ->
                            originTab = selectedTab
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                allMeasurementsBaby = baby
                                currentScreen       = Screen.AllMeasurements
                            }
                        },
                        onNavigateToWelcome             = {
                            preferencesManager.clearLastScreen()
                            preferencesManager.remove("user_role")
                            notificationViewModel.stopPolling()
                            currentScreen = Screen.Welcome
                        },
                        onNavigateToFamilyHistory       = { babyId, _ ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                familyHistoryBaby = baby
                                familyHistoryViewModel.loadFamilyHistory(babyId)
                                currentScreen = Screen.FamilyHistory
                            }
                        },
                        onNavigateToChildIllnesses      = { babyId, _ ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                childIllnessesBaby = baby
                                childIllnessesViewModel.loadIllnesses(babyId)
                                currentScreen = Screen.ChildIllnesses
                            }
                        },
                        onNavigateToVisionMotor         = { babyId, _ ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                childDevBaby = baby
                                visionMotorViewModel.load(baby.babyId, baby.ageInMonths)
                                currentScreen = Screen.ChildDevVisionMotor
                            }
                        },
                        onNavigateToHearingSpeech       = { babyId, _ ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                childDevBaby = baby
                                hearingSpeechViewModel.load(baby.babyId, baby.ageInMonths)
                                currentScreen = Screen.ChildDevHearingSpeech
                            }
                        },
                        onNavigateToPreCheckInvestigation = { babyId, _ ->
                            val baby = homeViewModel.uiState.babies.firstOrNull { it.babyId == babyId }
                            if (baby != null) {
                                preCheckInvestigationBaby = baby
                                preCheckInvestigationViewModel.load(babyId)
                                currentScreen = Screen.PreCheckInvestigation
                            }
                        },
                        onNavigateToSleepGuide          = {
                            originTab     = selectedTab
                            currentScreen = Screen.SleepGuide
                        },
                        onNavigateToFeedingGuide        = {
                            originTab     = selectedTab
                            currentScreen = Screen.FeedingGuide
                        },
                        onNavigateToMemory              = {
                            originTab     = selectedTab
                            currentScreen = Screen.Memory
                        },
                        onNavigateToNotifications       = {
                            originTab = selectedTab
                            notificationViewModel.loadNotifications(refresh = true)
                            currentScreen = Screen.Notifications
                        },
                    )
                }

                // ── Add Baby ──────────────────────────────────────────────────
                Screen.AddBaby -> {
                    AddBabyScreen(
                        viewModel = addBabyViewModel,
                        onBack    = { selectedTab = originTab; currentScreen = Screen.Home },
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
                            selectedBaby = homeViewModel.uiState.babies.find { it.babyId == selectedBaby?.babyId }
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
                            baby             = baby,
                            vaccinations     = homeViewModel.uiState.upcomingVaccinations[baby.babyId] ?: emptyList(),
                            latestGrowth     = homeViewModel.uiState.latestGrowthRecords[baby.babyId],
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
                        val isFemale = baby.gender.equals("FEMALE", ignoreCase = true)
                                || baby.gender.equals("GIRL", ignoreCase = true)
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
                    if (baby == null) currentScreen = Screen.Home
                    else FamilyHistoryScreen(
                        babyId    = baby.babyId,
                        babyName  = baby.fullName,
                        viewModel = familyHistoryViewModel,
                        onBack    = { familyHistoryBaby = null; currentScreen = Screen.Home }
                    )
                }

                // ── Child Illnesses ───────────────────────────────────────────
                Screen.ChildIllnesses -> {
                    val baby = childIllnessesBaby
                    if (baby == null) currentScreen = Screen.Home
                    else ChildIllnessesScreen(
                        babyId    = baby.babyId,
                        babyName  = baby.fullName,
                        viewModel = childIllnessesViewModel,
                        onBack    = { childIllnessesBaby = null; currentScreen = Screen.Home }
                    )
                }

                // ── Child Dev: Vision + Motor ─────────────────────────────────
                Screen.ChildDevVisionMotor -> {
                    val baby = childDevBaby
                    if (baby == null) currentScreen = Screen.Home
                    else VisionMotorScreen(
                        babyId        = baby.babyId,
                        babyName      = baby.fullName,
                        babyAgeMonths = baby.ageInMonths,
                        viewModel     = visionMotorViewModel,
                        onBack        = { childDevBaby = null; currentScreen = Screen.Home }
                    )
                }

                // ── Child Dev: Hearing + Speech ───────────────────────────────
                Screen.ChildDevHearingSpeech -> {
                    val baby = childDevBaby
                    if (baby == null) currentScreen = Screen.Home
                    else HearingSpeechScreen(
                        babyId        = baby.babyId,
                        babyName      = baby.fullName,
                        babyAgeMonths = baby.ageInMonths,
                        viewModel     = hearingSpeechViewModel,
                        onBack        = { childDevBaby = null; currentScreen = Screen.Home }
                    )
                }

                // ── Pre-Check Investigation ───────────────────────────────────
                Screen.PreCheckInvestigation -> {
                    val baby = preCheckInvestigationBaby
                    if (baby == null) currentScreen = Screen.Home
                    else PreCheckInvestigationScreen(
                        babyId    = baby.babyId,
                        babyName  = baby.fullName,
                        viewModel = preCheckInvestigationViewModel,
                        onBack    = { preCheckInvestigationBaby = null; currentScreen = Screen.Home }
                    )
                }

                // ── Sleep Guide ───────────────────────────────────────────────
                Screen.SleepGuide -> {
                    SleepGuideScreen(
                        babies    = homeViewModel.uiState.babies,
                        viewModel = guideViewModel,
                        language  = currentLanguage.code,
                        onBack    = { selectedTab = originTab; currentScreen = Screen.Home }
                    )
                }

                // ── Feeding Guide ─────────────────────────────────────────────
                Screen.FeedingGuide -> {
                    FeedingGuideScreen(
                        babies    = homeViewModel.uiState.babies,
                        viewModel = guideViewModel,
                        language  = currentLanguage.code,
                        onBack    = { selectedTab = originTab; currentScreen = Screen.Home }
                    )
                }

                // ── Memory ────────────────────────────────────────────────────
                Screen.Memory -> {
                    MemoryScreen(
                        viewModel      = memoryViewModel,
                        babies         = homeViewModel.uiState.babies,
                        selectedBabyId = homeViewModel.uiState.selectedBaby?.babyId,
                        language       = currentLanguage,
                        onBack         = { selectedTab = originTab; currentScreen = Screen.Home }
                    )
                }

                // ── Notifications ─────────────────────────────────────────────
                Screen.Notifications -> {
                    NotificationScreen(
                        viewModel  = notificationViewModel,
                        onBack     = { selectedTab = originTab; currentScreen = Screen.Home },
                        onNavigate = { /* handled by LaunchedEffect */ }
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