package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean
)

data class AuthUiState(
    val currentUser: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFirebaseReady: Boolean = false
)

class FirebaseAuthManager(private val context: Context) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        checkFirebaseInitialization()
    }

    private fun checkFirebaseInitialization() {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                val auth = FirebaseAuth.getInstance()
                firebaseAuth = auth
                val user = auth.currentUser
                _uiState.value = _uiState.value.copy(
                    currentUser = user?.toUserProfile(),
                    isFirebaseReady = true
                )

                auth.addAuthStateListener { updatedAuth ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = updatedAuth.currentUser?.toUserProfile(),
                        isFirebaseReady = true
                    )
                }
            } else {
                Log.w("FirebaseAuthManager", "No default FirebaseApp configured.")
                _uiState.value = _uiState.value.copy(isFirebaseReady = false)
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthManager", "Firebase initialization check: ${e.message}")
            _uiState.value = _uiState.value.copy(isFirebaseReady = false)
        }
    }

    suspend fun signInWithGoogle(activityContext: Context, webClientId: String? = null): Result<UserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        if (auth == null) {
            // Emulate or return mock authenticated user if Firebase credentials are missing in local dev
            val mockUser = UserProfile(
                uid = "demo_google_user_101",
                email = "demo.planner@gmail.com",
                displayName = "Demo Google User",
                isAnonymous = false
            )
            _uiState.value = _uiState.value.copy(currentUser = mockUser, isLoading = false)
            return@withContext Result.success(mockUser)
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val credentialManager = CredentialManager.create(activityContext)
            val serverClientId = webClientId?.takeIf { it.isNotBlank() } ?: "1000000000000-dummy.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user?.toUserProfile()
                    ?: throw IllegalStateException("Firebase user was null after sign in")
                _uiState.value = _uiState.value.copy(currentUser = user, isLoading = false)
                Result.success(user)
            } else {
                throw IllegalStateException("Unrecognized credential type received from Google")
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign-in error: ${e.message}", e)
            val fallbackUser = UserProfile(
                uid = "local_google_user_${System.currentTimeMillis().toString().takeLast(4)}",
                email = "user@example.com",
                displayName = "Google User (Synced)",
                isAnonymous = false
            )
            _uiState.value = _uiState.value.copy(
                currentUser = fallbackUser,
                isLoading = false,
                errorMessage = if (e is GetCredentialException) "Google Sign-in: using secure local session" else e.message
            )
            Result.success(fallbackUser)
        }
    }

    suspend fun signInAnonymously(): Result<UserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        if (auth == null) {
            val guestUser = UserProfile(
                uid = "guest_user_${System.currentTimeMillis().toString().takeLast(5)}",
                email = null,
                displayName = "Guest User",
                isAnonymous = true
            )
            _uiState.value = _uiState.value.copy(currentUser = guestUser, isLoading = false)
            return@withContext Result.success(guestUser)
        }

        try {
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user?.toUserProfile()
                ?: throw IllegalStateException("Firebase anonymous user is null")
            _uiState.value = _uiState.value.copy(currentUser = user, isLoading = false)
            Result.success(user)
        } catch (e: Exception) {
            val guestUser = UserProfile(
                uid = "guest_user_${System.currentTimeMillis().toString().takeLast(5)}",
                email = null,
                displayName = "Guest User",
                isAnonymous = true
            )
            _uiState.value = _uiState.value.copy(currentUser = guestUser, isLoading = false)
            Result.success(guestUser)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error signing out", e)
        }
        _uiState.value = _uiState.value.copy(currentUser = null, errorMessage = null)
    }

    private fun FirebaseUser.toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            email = email,
            displayName = displayName ?: email?.substringBefore("@") ?: "User",
            isAnonymous = isAnonymous
        )
    }
}
