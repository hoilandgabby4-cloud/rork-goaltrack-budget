package com.rork.budgetflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.budgetflow.MainActivity
import com.rork.budgetflow.auth.AuthManager
import com.rork.budgetflow.data.SyncRepository
import com.rork.budgetflow.ui.screens.HouseholdCreatedScreen
import com.rork.budgetflow.ui.screens.HouseholdSetupScreen
import com.rork.budgetflow.ui.screens.MainScaffold
import com.rork.budgetflow.ui.screens.SignInScreen
import kotlinx.coroutines.launch

/** Household info after creation or join. */
private data class HouseholdInfo(val id: String, val code: String, val name: String)

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val syncRepo = remember { SyncRepository(context, authManager) }
    val scope = rememberCoroutineScope()

    val user by authManager.user.collectAsStateWithLifecycle()
    var isSigningIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }
    var isSettingUpHousehold by remember { mutableStateOf(false) }
    var householdError by remember { mutableStateOf<String?>(null) }
    var householdInfo by remember { mutableStateOf<HouseholdInfo?>(null) }

    // Handle OAuth callback from Custom Tabs redirect
    val activity = context as MainActivity
    LaunchedEffect(activity.pendingAuthUri) {
        val uri = activity.pendingAuthUri ?: return@LaunchedEffect
        activity.clearPendingAuthUri()
        val success = authManager.handleCallback(uri)
        if (!success) {
            signInError = "Sign in failed. Please try again."
        }
        isSigningIn = false
    }

    if (user == null) {
        SignInScreen(
            isSigningIn = isSigningIn,
            error = signInError,
            onSignIn = { provider ->
                isSigningIn = true
                signInError = null
                scope.launch {
                    val authUrl = authManager.beginSignIn(provider)
                    if (authUrl != null) {
                        // Launch Custom Tabs via intent
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(authUrl))
                        context.startActivity(intent)
                    } else {
                        isSigningIn = false
                        signInError = "Could not start sign in. Please try again."
                    }
                }
            },
            onClearError = { signInError = null },
        )
        return
    }

    // User is signed in — check household
    if (householdInfo == null) {
        HouseholdSetupScreen(
            isLoading = isSettingUpHousehold,
            error = householdError,
            onCreateHousehold = {
                isSettingUpHousehold = true
                householdError = null
                scope.launch {
                    val hh = syncRepo.createHousehold()
                    if (hh != null) {
                        householdInfo = HouseholdInfo(hh.id, hh.code, hh.name)
                    } else {
                        householdError = "Failed to create household. Check your connection."
                    }
                    isSettingUpHousehold = false
                }
            },
            onJoinHousehold = { code ->
                isSettingUpHousehold = true
                householdError = null
                scope.launch {
                    val hh = syncRepo.joinHousehold(code)
                    if (hh != null) {
                        householdInfo = HouseholdInfo(hh.id, hh.code, hh.name)
                    } else {
                        householdError = "Invalid code or household not found."
                    }
                    isSettingUpHousehold = false
                }
            },
        )
        return
    }

    // If the household was just created, show the share code screen briefly, then go to main
    MainScaffold()
}
