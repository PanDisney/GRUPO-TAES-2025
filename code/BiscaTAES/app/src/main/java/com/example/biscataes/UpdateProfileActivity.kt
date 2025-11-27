@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var editTextNickname: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSelectAvatar: Button
    private lateinit var imageViewAvatar: ImageView
    private lateinit var buttonSaveChanges: Button

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
            imageViewAvatar.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        editTextNickname = findViewById(R.id.editTextNickname)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonSelectAvatar = findViewById(R.id.buttonSelectAvatar)
        imageViewAvatar = findViewById(R.id.imageViewAvatar)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)

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

        editTextNickname.setText(currentNickname)
        // Password is not pre-filled for security reasons

        // Load avatar if available (you'll need an image loading library like Coil or Glide for actual URLs)
        // For local files, you can use setImageURI or similar
        // if (!currentAvatarFilename.isNullOrEmpty()) {
        //     val avatarUri = Uri.parse("http://10.0.2.2:8000/storage/avatars/$currentAvatarFilename")
        //     imageViewAvatar.setImageURI(avatarUri)
        // }
    }

    private fun setupListeners() {
        buttonSelectAvatar.setOnClickListener {
            openImageChooser()
        }

        buttonSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun saveChanges() {
        val newNickname = editTextNickname.text.toString().trim()
        val newPassword = editTextPassword.text.toString().trim()

        lifecycleScope.launch {
            try {
                val requestBody = mutableMapOf<String, String>()
                var hasChanges = false

                if (newNickname.isNotEmpty()) {
                    requestBody["nickname"] = newNickname
                    hasChanges = true
                }
                if (newPassword.isNotEmpty()) {
                    requestBody["password"] = newPassword
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
                        val errorBody = response.body<String>()
                        Log.e("UpdateProfileActivity", "Error updating profile: $errorBody")
                        Toast.makeText(this@UpdateProfileActivity, "Failed to update profile: ${response.status}", Toast.LENGTH_LONG).show()
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
                        val errorBody = response.body<String>()
                        Log.e("UpdateProfileActivity", "Error updating profile: $errorBody")
                        Toast.makeText(this@UpdateProfileActivity, "Failed to update profile: ${response.status}", Toast.LENGTH_LONG).show()
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