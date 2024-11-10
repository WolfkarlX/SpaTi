package com.example.spaTi.di

import android.content.SharedPreferences
import com.example.spaTi.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(
        database: FirebaseFirestore
    ): NoteRepository {
        return NoteRepositoryImp(database)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): AuthRepository {
        return AuthRepositoryImp(auth, database, appPreferences, gson)
    }

    @Provides
    @Singleton
    fun provideSpaAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): SpaAuthRepository {
        return SpaAuthRepositoryImpl(auth, database, appPreferences, gson)
    }

    @Provides
    @Singleton
    fun provideServiceRepository(
        database: FirebaseFirestore,
        tagRepository: TagRepository,
        spaRepository: SpaAuthRepository
    ): ServiceRepository {
        return ServiceRepositoryImpl(database, spaRepository, tagRepository)
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        database: FirebaseFirestore
    ): TagRepository {
        return TagRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): ProfileRepository {
        return ProfileRepositoryImpl(auth, database, appPreferences, gson)
    }

    @Provides
    @Singleton
    fun provideSpaRepository(
        database: FirebaseFirestore
    ): SpaRepository {
        return SpaRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideMySpaRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson,
        storage: FirebaseStorage
    ): SpaProfileRepository {
        return SpaProfileRepositoryImpl(auth, database, appPreferences, gson, storage)
    }

    @Provides
    @Singleton
    fun provideAppointmentRepository(
        database: FirebaseFirestore,
        appPreferences: SharedPreferences,
        gson: Gson
    ): AppointmentRepository {
        return AppointmentRepositoryImpl(database, appPreferences, gson)
    }
}
