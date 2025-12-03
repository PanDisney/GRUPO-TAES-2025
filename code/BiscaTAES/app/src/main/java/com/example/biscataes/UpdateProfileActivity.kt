@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import coil.load
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.Manifest

@Serializable
data class ErrorResponse(
    val message: String,
    val errors: Map<String, List<String>>? = null
)

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var editTextNickname: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordConfirmation: EditText
    private lateinit var buttonSelectAvatar: Button
    private lateinit var imageViewAvatar: ImageView
    private lateinit var buttonSaveChanges: Button
    private lateinit var buttonBackToDashboard: Button // NEW DECLARATION

    private var selectedImageUri: Uri? = null

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
    }

    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = getAuthToken()
                        if (token != null) {
                            BearerTokens(token, "")
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            // Use Coil to load the selected image URI
            imageViewAvatar.load(selectedImageUri) {
                crossfade(true)
                placeholder(R.drawable.anonymous)
                error(R.drawable.anonymous)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        editTextNickname = findViewById(R.id.editTextNickname)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextPasswordConfirmation = findViewById(R.id.editTextPasswordConfirmation)
        buttonSelectAvatar = findViewById(R.id.buttonSelectAvatar)
        imageViewAvatar = findViewById(R.id.imageViewAvatar)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)
        buttonBackToDashboard = findViewById(R.id.buttonBackToDashboard) // NEW INITIALIZATION

        loadUserData()
        setupListeners()
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    private fun loadUserData() {
        // You would typically fetch the current user data from the API here
        // For now, let's assume we pass it from DashboardActivity
        val currentNickname = intent.getStringExtra("CURRENT_NICKNAME")
        val currentAvatarFilename = intent.getStringExtra("CURRENT_AVATAR_FILENAME")

        // editTextNickname.setText(currentNickname)
        // Password is not pre-filled for security reasons

        // Load avatar if available (you'll need an image loading library like Coil or Glide for actual URLs)
        // For local files, you can use setImageURI or similar
        if (!currentAvatarFilename.isNullOrEmpty()) {
            val imageUrl = "http://10.0.2.2:8000/storage/photos_avatars/$currentAvatarFilename"
            imageViewAvatar.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.anonymous) // Assuming 'anonymous.png' is now in drawable
                error(R.drawable.anonymous) // Show the same placeholder on error
            }
        } else {
            imageViewAvatar.setImageResource(R.drawable.anonymous)
        }
    }

    private fun setupListeners() {
        buttonSelectAvatar.setOnClickListener {
            openImageChooser()
        }

        buttonSaveChanges.setOnClickListener {
            saveChanges()
        }

        buttonBackToDashboard.setOnClickListener { // NEW LISTENER
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                launchImageChooser()
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
            }
        }

    private fun launchImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun openImageChooser() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchImageChooser()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                // For this example, we'll just request the permission.
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun saveChanges() {
        val newNickname = editTextNickname.text.toString().trim()
        val newPassword = editTextPassword.text.toString().trim()
        val newPasswordConfirmation = editTextPasswordConfirmation.text.toString().trim()

        lifecycleScope.launch {
            try {
                val requestBody = mutableMapOf<String, String>()
                var hasChanges = false

                if (newNickname.isNotEmpty()) {
                    requestBody["nickname"] = newNickname
                    hasChanges = true
                }
                if (newPassword.isNotEmpty()) {
                    if (newPassword != newPasswordConfirmation) {
                        Toast.makeText(this@UpdateProfileActivity, "Passwords do not match.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    requestBody["password"] = newPassword
                    requestBody["password_confirmation"] = newPasswordConfirmation
                    hasChanges = true
                }

                if (selectedImageUri != null) {
                    val file = uriToFile(this@UpdateProfileActivity, selectedImageUri!!)
                    val response: HttpResponse = client.submitFormWithBinaryData(
                        url = "http://10.0.2.2:8000/api/users/me",
                        formData = formData {
                            append("photo_avatar_filename", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "image/*")
                                append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                            })
                            // Add other fields to form data if needed
                            requestBody.forEach { (key, value) ->
                                append(key, value)
                            }
                        }
                    ) {
                        method = HttpMethod.Post // Laravel expects POST for form data with files
                    }

                    if (response.status == HttpStatusCode.OK) {
                        Toast.makeText(this@UpdateProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        handleUpdateError(response)
                    }
                } else if (hasChanges) {
                    val response: HttpResponse = client.put("http://10.0.2.2:8000/api/users/me") {
                        contentType(ContentType.Application.Json)
                        setBody(requestBody)
                    }

                    if (response.status == HttpStatusCode.OK) {
                        Toast.makeText(this@UpdateProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        handleUpdateError(response)
                    }
                } else {
                    Toast.makeText(this@UpdateProfileActivity, "No changes to save.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("UpdateProfileActivity", "Error saving changes", e)
                Toast.makeText(this@UpdateProfileActivity, "An error occurred: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun handleUpdateError(response: HttpResponse) {
        try {
            val errorResponse = response.body<ErrorResponse>()
            val errorMessage = buildString {
                append(errorResponse.message)
                errorResponse.errors?.values?.flatten()?.forEach {
                    append("\n- $it")
                }
            }
            Toast.makeText(this@UpdateProfileActivity, errorMessage, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            val errorBody = response.body<String>()
            Log.e("UpdateProfileActivity", "Error parsing error response: $errorBody", e)
            Toast.makeText(this@UpdateProfileActivity, "Failed to update profile: ${response.status}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val file = File(context.cacheDir, "temp_avatar")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }
}