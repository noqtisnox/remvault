package dev.remvault.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.remvault.client.ui.auth.LoginScreen
import dev.remvault.client.ui.characters.CharacterCreatorScreen
import kotlinx.serialization.Serializable

// Temporary DTOs until moved to :shared
@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val userId: String, val username: String, val role: String)

@Serializable
data class CreateCharacterRequest(
    val name: String, val race: String, val characterClass: String, val background: String,
    val level: Int = 1,
    // Optional stats (if null, backend rolls 4d6)
    val strength: Int? = null, val dexterity: Int? = null, val constitution: Int? = null,
    val intelligence: Int? = null, val wisdom: Int? = null, val charisma: Int? = null
)

enum class Screen { LOGIN, CHARACTER_CREATOR }

@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var authToken by remember { mutableStateOf<String?>(null) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.LOGIN -> {
                    LoginScreen(onLoginSuccess = { token ->
                        authToken = token
                        currentScreen = Screen.CHARACTER_CREATOR
                    })
                }
                Screen.CHARACTER_CREATOR -> {
                    CharacterCreatorScreen(token = authToken!!)
                }
            }
        }
    }
}