package com.geosurvey.pro.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.geosurvey.pro.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : StorageRepository {

    override suspend fun uploadFile(uri: Uri, path: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Compress image before upload
                val compressedData = compressImage(uri)
                val ref = storage.reference.child(path)
                ref.putBytes(compressedData).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                Result.success(downloadUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun compressImage(uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            // Compress to 70% quality to significantly reduce size while maintaining "Sakya" quality
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.toByteArray()
        }
    }
}
