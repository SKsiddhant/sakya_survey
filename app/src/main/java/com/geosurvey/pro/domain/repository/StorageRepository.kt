package com.geosurvey.pro.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadFile(uri: Uri, path: String): Result<String>
}
