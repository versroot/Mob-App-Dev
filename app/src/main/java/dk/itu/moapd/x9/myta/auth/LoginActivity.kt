package dk.itu.moapd.x9.myta.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import dk.itu.moapd.x9.myta.ui.MainActivity

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
            .setTheme(dk.itu.moapd.x9.myta.R.style.Theme_X9myta_FirebaseAuth)
            .setTosAndPrivacyPolicyUrls(
                "https://firebase.google.com/terms/",
                "https://firebase.google.com/policies"
            )
            .build()

        signInLauncher.launch(signInIntent)
    }


    private fun onSignInResult(
        result: FirebaseAuthUIAuthenticationResult
    ) {
        val response = result.idpResponse
        when (result.resultCode) {
            RESULT_OK -> {
                startMainActivity()
            }
            else -> {// Sign-in failed or cancelled
                if (response == null) {
                    // User pressed back — just cancelled
                    Log.w("LoginActivity", "User canceled sign-in")
                    // exit the app
                    finish()
                } else {
                    // Actual error
                    Log.e("LoginActivity",
                        "Sign-in error code: ${response.error?.errorCode}",
                        response.error)
                    finish()
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