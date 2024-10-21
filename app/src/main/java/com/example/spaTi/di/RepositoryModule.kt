package com.example.spaTi.di

import android.content.SharedPreferences
import com.example.spaTi.data.repository.AuthRepository
import com.example.spaTi.data.repository.AuthRepositoryImp
import com.example.spaTi.data.repository.NoteRepository
import com.example.spaTi.data.repository.NoteRepositoryImp
import com.example.spaTi.data.repository.ProfileRepository
import com.example.spaTi.data.repository.ProfileRepositoryImpl
import com.example.spaTi.data.repository.ServiceRepository
import com.example.spaTi.data.repository.ServiceRepositoryImpl
import com.example.spaTi.data.repository.SpaAuthRepository
import com.example.spaTi.data.repository.SpaAuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    ): NoteRepository{
        return NoteRepositoryImp(database)
    }

    @Provides
    @Singleton
    fun provideAutghRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): AuthRepository {
        return AuthRepositoryImp(auth,database,appPreferences,gson)
    }

    @Provides
    @Singleton
    fun provideSpaAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): SpaAuthRepository {
        // Return the implementation of the interface
        return SpaAuthRepositoryImpl(auth, database, appPreferences, gson)
    }

    @Provides
    @Singleton
    fun provideServiceRepository(
        database: FirebaseFirestore
    ): ServiceRepository {
        return  ServiceRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): ProfileRepository {
        // Return the implementation of the interface
        return ProfileRepositoryImpl(auth, database, appPreferences, gson)
    }
}