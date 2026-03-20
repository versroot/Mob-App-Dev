package dk.itu.moapd.x9.myta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar

class LoginActivity : ComponentActivity() {
    private val signInLauncher = //This object launches a new activity and receives back some result data.
        registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result -> onSignInResult(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSignInIntent()
    }
    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setCredentialManagerEnabled(false)
            .setTosAndPrivacyPolicyUrls(
                "https://firebase.google.com/terms/",
                "https://firebase.google.com/policies"
            )
            .build()

        signInLauncher.launch(signInIntent)
    }


    private fun onSignInResult(
        result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        when (result.resultCode) {
            RESULT_OK -> {
                startMainActivity()
            }
            else -> {// Sign-in failed or cancelled
                if (response == null) {
                    // User pressed back — just cancelled
                    android.util.Log.w("LoginActivity", "User canceled sign-in")
                } else {
                    // Actual error
                    android.util.Log.e(
                        "LoginActivity",
                        "Sign-in error code: ${response.error?.errorCode}",
                        response.error
                    )
                }
            }
        }
    }
    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }
}