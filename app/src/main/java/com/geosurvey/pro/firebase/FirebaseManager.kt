package com.geosurvey.pro.firebase

import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Singleton
class FirebaseManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Firebase related logic
}
