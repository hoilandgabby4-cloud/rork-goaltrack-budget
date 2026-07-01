package com.rork.budgetflow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rork.budgetflow.ui.navigation.AppNavigation
import com.rork.budgetflow.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    /** Pending OAuth callback URI, consumed by AppNavigation. */
    var pendingAuthUri: Uri? = null
        private set

    fun clearPendingAuthUri() {
        pendingAuthUri = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle OAuth callback from Custom Tabs redirect
        handleIntent(intent)

        setContent {
            AppTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "rork-5ju1u2rlxx2vaws9w054p") {
            pendingAuthUri = uri
        }
    }
}
