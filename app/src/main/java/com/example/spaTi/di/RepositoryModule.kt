package com.example.spaTi.di

import android.content.SharedPreferences
import com.example.spaTi.data.repository.AppointmentRepository
import com.example.spaTi.data.repository.AppointmentRepositoryImpl
import com.example.spaTi.data.repository.AuthRepository
import com.example.spaTi.data.repository.AuthRepositoryImp
import com.example.spaTi.data.repository.NoteRepository
import com.example.spaTi.data.repository.NoteRepositoryImp
import com.example.spaTi.data.repository.ProfileRepository
import com.example.spaTi.data.repository.ProfileRepositoryImpl
import com.example.spaTi.data.repository.ReportRepository
import com.example.spaTi.data.repository.ReportRepositoryImpl
import com.example.spaTi.data.repository.ServiceRepository
import com.example.spaTi.data.repository.ServiceRepositoryImpl
import com.example.spaTi.data.repository.SpaAuthRepository
import com.example.spaTi.data.repository.SpaAuthRepositoryImpl
import com.example.spaTi.data.repository.SpaProfileRepository
import com.example.spaTi.data.repository.SpaProfileRepositoryImpl
import com.example.spaTi.data.repository.SpaRepository
import com.example.spaTi.data.repository.SpaRepositoryImpl
import com.example.spaTi.data.repository.TagRepository
import com.example.spaTi.data.repository.TagRepositoryImpl
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
        database: FirebaseFirestore,
        tagRepository: TagRepository,
        sparepository: SpaAuthRepository,
    ): ServiceRepository {
        return  ServiceRepositoryImpl(database,sparepository, tagRepository)
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        database: FirebaseFirestore
    ): TagRepository {
        return  TagRepositoryImpl(database)
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

    @Provides
    @Singleton
    fun provideSpaRepository(
        database: FirebaseFirestore
    ): SpaRepository {
        return SpaRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideMySpaRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): SpaProfileRepository {
        // Return the implementation of the interface
        return SpaProfileRepositoryImpl(auth, database, appPreferences, gson)
    }

    @Provides
    @Singleton
    fun appointmentRepository(
        database: FirebaseFirestore,
        appPreferences: SharedPreferences,
        gson: Gson
    ): AppointmentRepository {
        // Return the implementation of the interface
        return AppointmentRepositoryImpl(database, appPreferences, gson)
    }

    @Provides
    @Singleton
    fun ReportRepository(
        database: FirebaseFirestore,
        appPreferences: SharedPreferences,
        gson: Gson
    ): ReportRepository {
        // Return the implementation of the interface
        return ReportRepositoryImpl(database)
    }
}