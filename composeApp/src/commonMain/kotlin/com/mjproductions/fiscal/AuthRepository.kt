package com.mjproductions.fiscal


import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

object AuthRepository {

    private const val KEY_JWT   = "supabase_jwt"
    private const val KEY_EMAIL = "saved_email"

    // ---------- Auth actions ----------

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email    = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email    = email
                this.password = password
            }
            val token = supabaseClient.auth.currentSessionOrNull()?.accessToken
                ?: return Result.failure(Exception("No session returned"))
            println("JWT: $token")
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(settings: Settings) {
        settings.remove(KEY_JWT)
        settings.remove(KEY_EMAIL)
    }

    // ---------- Token persistence ----------

    fun saveToken(settings: Settings, token: String, email: String) {
        settings[KEY_JWT]   = token
        settings[KEY_EMAIL] = email
    }

    fun getSavedToken(settings: Settings): String? {
        val token = settings.getStringOrNull(KEY_JWT)
        return if (token.isNullOrBlank()) null else token
    }

    fun getSavedEmail(settings: Settings): String {
        return settings.getStringOrNull(KEY_EMAIL) ?: ""
    }
}