package com.example.spaTi.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J*\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0018\u0010\u000f\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u0011\u0012\u0004\u0012\u00020\f0\u0010H\u0016J2\u0010\u0013\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u00122\u0018\u0010\u000f\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u0011\u0012\u0004\u0012\u00020\f0\u0010H\u0016J:\u0010\u0016\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u00122\u0006\u0010\r\u001a\u00020\u000e2\u0018\u0010\u000f\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u0011\u0012\u0004\u0012\u00020\f0\u0010H\u0016J*\u0010\u0017\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0018\u0010\u000f\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u0011\u0012\u0004\u0012\u00020\f0\u0010H\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0018"}, d2 = {"Lcom/example/spaTi/data/repository/AuthRepositoryImp;", "Lcom/example/spaTi/data/repository/AuthRepository;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "database", "Lcom/google/firebase/firestore/FirebaseFirestore;", "(Lcom/google/firebase/auth/FirebaseAuth;Lcom/google/firebase/firestore/FirebaseFirestore;)V", "getAuth", "()Lcom/google/firebase/auth/FirebaseAuth;", "getDatabase", "()Lcom/google/firebase/firestore/FirebaseFirestore;", "forgotPassword", "", "user", "Lcom/example/spaTi/data/models/User;", "result", "Lkotlin/Function1;", "Lcom/example/spaTi/util/UiState;", "", "loginUser", "email", "password", "registerUser", "updateUserInfo", "app_debug"})
public final class AuthRepositoryImp implements com.example.spaTi.data.repository.AuthRepository {
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.firestore.FirebaseFirestore database = null;
    
    public AuthRepositoryImp(@org.jetbrains.annotations.NotNull
    com.google.firebase.auth.FirebaseAuth auth, @org.jetbrains.annotations.NotNull
    com.google.firebase.firestore.FirebaseFirestore database) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.google.firebase.auth.FirebaseAuth getAuth() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.google.firebase.firestore.FirebaseFirestore getDatabase() {
        return null;
    }
    
    @java.lang.Override
    public void registerUser(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password, @org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.User user, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result) {
    }
    
    @java.lang.Override
    public void updateUserInfo(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.User user, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result) {
    }
    
    @java.lang.Override
    public void loginUser(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result) {
    }
    
    @java.lang.Override
    public void forgotPassword(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.User user, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result) {
    }
}