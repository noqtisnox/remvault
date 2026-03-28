package dev.remvault.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.remvault.client.api.RemVaultApi
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Temporary DTO
@Serializable
data class LoginRequest(val email: String, val password: String)

@Composable
fun App() {
    MaterialTheme {
        // Form State
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        // Request State
        var isLoading by remember { mutableStateOf(false) }
        var resultMessage by remember { mutableStateOf<String?>(null) }
        var isError by remember { mutableStateOf(false) }

        // Coroutine scope for launching the API call
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to RemVault",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    resultMessage = null
                    isError = false

                    scope.launch {
                        try {
                            // Adjust the path ("/api/auth/login") if your AuthRoutes.kt uses a different endpoint!
                            val response = RemVaultApi.client.post("${RemVaultApi.BASE_URL}/api/v1/auth/login") {
                                contentType(ContentType.Application.Json)
                                setBody(LoginRequest(email, password))
                            }

                            if (response.status.isSuccess()) {
                                resultMessage = "Success! Token received."
                                isError = false
                            } else {
                                resultMessage = "Login failed: ${response.status.description}"
                                isError = true
                            }
                        } catch (e: Exception) {
                            resultMessage = "Network error: ${e.message}"
                            isError = true
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the result of the API call
            resultMessage?.let {
                Text(
                    text = it,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}