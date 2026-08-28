package com.venkateshgowda.personallibrary

import android.os.SystemClock
import android.view.WindowManager
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import com.venkateshgowda.personallibrary.data.LibraryDatabase
import com.venkateshgowda.personallibrary.data.AppSettings
import com.venkateshgowda.personallibrary.ui.LibraryApp
import com.venkateshgowda.personallibrary.ui.LibraryTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private var appLockEnabled = false
    private var lockTimeout = "5 minutes"
    private var stoppedAtMillis: Long? = null
    private var authenticated = false
    private var authenticationInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = AppSettings(applicationContext)
        lifecycleScope.launch {
            settings.appLockEnabled.collect { appLockEnabled = it }
        }
        lifecycleScope.launch {
            settings.lockTimeout.collect { lockTimeout = it }
        }
        val database = Room.databaseBuilder(applicationContext, LibraryDatabase::class.java, "library.db")
            .addMigrations(LibraryDatabase.MIGRATION_5_6, LibraryDatabase.MIGRATION_6_7, LibraryDatabase.MIGRATION_7_8, LibraryDatabase.MIGRATION_8_9, LibraryDatabase.MIGRATION_9_10, LibraryDatabase.MIGRATION_10_11, LibraryDatabase.MIGRATION_11_12, LibraryDatabase.MIGRATION_12_13)
            .build()
        setContentView(ComposeView(this).apply { setContent {
            val theme by settings.theme.collectAsState("System")
            LibraryTheme(theme) {
                Surface(color = MaterialTheme.colorScheme.background) { LibraryApp(database.bookDao(), database.libraryDao(), database.loanDao(), database.wishlistDao(), database.catalogDao(), database.userDao(), database, settings) }
            }
        } })
        val restorePreferences = getSharedPreferences("restore_status", MODE_PRIVATE)
        if (restorePreferences.getBoolean("show_success", false)) {
            restorePreferences.edit().remove("show_success").apply()
            Toast.makeText(this, "Data restored successfully.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStop() {
        super.onStop()
        stoppedAtMillis = SystemClock.elapsedRealtime()
        if (appLockEnabled) window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onResume() {
        super.onResume()
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (appLockEnabled && needsAuthentication()) authenticate()
    }

    private fun needsAuthentication(): Boolean {
        if (authenticationInProgress) return false
        val stoppedAt = stoppedAtMillis
        if (!authenticated || stoppedAt == null) return true
        val timeout = when (lockTimeout) {
            "Immediate" -> 0L
            "15 minutes" -> 15 * 60_000L
            "Never" -> Long.MAX_VALUE
            else -> 5 * 60_000L
        }
        return SystemClock.elapsedRealtime() - stoppedAt >= timeout
    }

    private fun authenticate() {
        authenticationInProgress = true
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                authenticated = true
                authenticationInProgress = false
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authenticationInProgress = false
            }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock personal library").setSubtitle("Use biometrics or your device credential").setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL).build())
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryAppPreview() {
    LibraryTheme { Surface { androidx.compose.material3.Text("Library preview") } }
}