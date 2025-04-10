// SessionManager.kt
import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "bearer_token"
        const val USER_EMAIL = "user_email"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(USER_EMAIL, email).apply()
    }

    fun fetchUserEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}