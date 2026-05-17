package com.geosurvey.pro.di

import android.content.Context
import com.geosurvey.pro.data.repository.AuthRepositoryImpl
import com.geosurvey.pro.data.repository.FirestoreRepositoryImpl
import com.geosurvey.pro.data.repository.StorageRepositoryImpl
import com.geosurvey.pro.domain.repository.AuthRepository
import com.geosurvey.pro.domain.repository.FirestoreRepository
import com.geosurvey.pro.domain.repository.StorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging

import com.geosurvey.pro.data.repository.LocationRepositoryImpl
import com.geosurvey.pro.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext

import com.geosurvey.pro.data.repository.AnalyticsRepositoryImpl
import com.geosurvey.pro.domain.repository.AnalyticsRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnalyticsRepository(firestore: FirebaseFirestore): AnalyticsRepository = 
        AnalyticsRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ): LocationRepository = LocationRepositoryImpl(context, fusedLocationClient)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = com.google.firebase.firestore.firestoreSettings {
            // Enable persistent cache for extreme speed on repeat visits
            setLocalCacheSettings(com.google.firebase.firestore.PersistentCacheSettings.newBuilder().build())
        }
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, firestore: FirebaseFirestore): AuthRepository = 
        AuthRepositoryImpl(auth, firestore)

    @Provides
    @Singleton
    fun provideFirestoreRepository(firestore: FirebaseFirestore): FirestoreRepository = 
        FirestoreRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideStorageRepository(
        storage: FirebaseStorage,
        @ApplicationContext context: Context
    ): StorageRepository = StorageRepositoryImpl(storage, context)
}
