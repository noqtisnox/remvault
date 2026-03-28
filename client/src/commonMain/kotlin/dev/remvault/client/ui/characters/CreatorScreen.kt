package dev.remvault.client.ui.characters

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.remvault.client.CreateCharacterRequest
import dev.remvault.client.api.RemVaultApi
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.launch

@Composable
fun CharacterCreatorScreen(token: String) {
    var name by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var charClass by remember { mutableStateOf("") }
    var background by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create New Character", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Character Name") })
        OutlinedTextField(value = race, onValueChange = { race = it }, label = { Text("Race (e.g. Elf)") })
        OutlinedTextField(value = charClass, onValueChange = { charClass = it }, label = { Text("Class (e.g. Rogue)") })
        OutlinedTextField(value = background, onValueChange = { background = it }, label = { Text("Background") })

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                isSubmitting = true
                scope.launch {
                    try {
                        // Ensure this path matches your CharacterRoutes.kt!
                        val response = RemVaultApi.client.post("${RemVaultApi.BASE_URL}/api/v1/characters") {
                            bearerAuth(token) // <--- THIS IS MAGIC. It adds the JWT to the header!
                            contentType(ContentType.Application.Json)

                            val req = CreateCharacterRequest(
                                name = name, race = race, characterClass = charClass, background = background, level = 1
                            )
                            setBody(req)
                        }

                        if (response.status.isSuccess()) {
                            resultMessage = "Character created & stats rolled successfully!"
                        } else {
                            resultMessage = "Error: ${response.status.description}"
                        }
                    } catch (e: Exception) {
                        resultMessage = "Network error: ${e.message}"
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            enabled = !isSubmitting && name.isNotBlank() && charClass.isNotBlank()
        ) {
            Text("Create & Roll Stats")
        }

        resultMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}