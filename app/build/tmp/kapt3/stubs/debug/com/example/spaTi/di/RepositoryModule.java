package com.example.spaTi.di;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u000b"}, d2 = {"Lcom/example/spaTi/di/RepositoryModule;", "", "()V", "provideAutghRepository", "Lcom/example/spaTi/data/repository/AuthRepository;", "database", "Lcom/google/firebase/firestore/FirebaseFirestore;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "provideNoteRepository", "Lcom/example/spaTi/data/repository/NoteRepository;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class RepositoryModule {
    @org.jetbrains.annotations.NotNull
    public static final com.example.spaTi.di.RepositoryModule INSTANCE = null;
    
    private RepositoryModule() {
        super();
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.example.spaTi.data.repository.NoteRepository provideNoteRepository(@org.jetbrains.annotations.NotNull
    com.google.firebase.firestore.FirebaseFirestore database) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.example.spaTi.data.repository.AuthRepository provideAutghRepository(@org.jetbrains.annotations.NotNull
    com.google.firebase.firestore.FirebaseFirestore database, @org.jetbrains.annotations.NotNull
    com.google.firebase.auth.FirebaseAuth auth) {
        return null;
    }
}