package com.haniifah.submission.storyapp.view.camera

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.haniifah.submission.storyapp.R
import com.haniifah.submission.storyapp.api.ApiConfig
import com.haniifah.submission.storyapp.databinding.ActivityCreateStoryBinding
import com.haniifah.submission.storyapp.di.CompanionObject.CAMERA_X_RESULT
import com.haniifah.submission.storyapp.di.CompanionObject.REQUEST_CODE_PERMISSIONS
import com.haniifah.submission.storyapp.di.CompanionObject.REQUIRED_PERMISSIONS
import com.haniifah.submission.storyapp.model.FileUploadResponse
import com.haniifah.submission.storyapp.model.UserPreference
import com.haniifah.submission.storyapp.view.ViewModelFactory
import com.haniifah.submission.storyapp.view.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class CreateStoryActivity : AppCompatActivity() {

    private lateinit var createStoryViewModel: CreateStoryViewModel
    private lateinit var binding: ActivityCreateStoryBinding

    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportActionBar != null) {
            (supportActionBar as ActionBar).title = "New Story"
        }
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViewModel()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.btnCamera.setOnClickListener { startCameraX() }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnUpload.setOnClickListener { uploadImage() }
    }

    private fun setupViewModel() {
        createStoryViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[CreateStoryViewModel::class.java]
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            val result = rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera)

            val bytes = ByteArrayOutputStream()
            result.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(this@CreateStoryActivity.contentResolver, result, "Title", null)
            val uri = Uri.parse(path.toString())
            getFile = uriToFile(uri, this@CreateStoryActivity)

            binding.imgPreview.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri

            val myFile = uriToFile(selectedImg, this@CreateStoryActivity)

            getFile = myFile

            binding.imgPreview.setImageURI(selectedImg)
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun uploadImage() {
        showLoading(true)
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)
            val description = binding.etDescription.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            createStoryViewModel.getUser().observe(this) {
                if (it != null) {

                    val client = ApiConfig.getApiService()
                        .uploadImage("Bearer " + it.token, imageMultipart, description)
                    client.enqueue(object : Callback<FileUploadResponse> {

                        override fun onResponse(
                            call: Call<FileUploadResponse>,
                            response: Response<FileUploadResponse>
                        ) {
                            showLoading(false)

                            val responseBody = response.body()
                            Log.d(TAG, "onResponse: $responseBody")

                            if (response.isSuccessful && responseBody?.message == "Story created successfully") {
                                Toast.makeText(this@CreateStoryActivity,
                                    getString(R.string.upload_success),
                                    Toast.LENGTH_SHORT).show()
                                val intent =
                                    Intent(this@CreateStoryActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e(TAG, "onFailure1: ${response.message()}")
                                Toast.makeText(this@CreateStoryActivity,
                                    getString(R.string.upload_fail),
                                    Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                            showLoading(false)
                            Log.e(TAG, "onFailure2: ${t.message}")
                            Toast.makeText(this@CreateStoryActivity,
                                getString(R.string.upload_fail),
                                Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
        else showLoading(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}