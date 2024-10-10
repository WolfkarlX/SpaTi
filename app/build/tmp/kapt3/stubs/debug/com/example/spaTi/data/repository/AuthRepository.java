package com.example.spaTi.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001J*\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0018\u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0004\u0012\u00020\u00030\u0007H&J2\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\t2\u0018\u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0004\u0012\u00020\u00030\u0007H&J:\u0010\r\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u00052\u0018\u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0004\u0012\u00020\u00030\u0007H&J*\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0018\u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0004\u0012\u00020\u00030\u0007H&\u00a8\u0006\u000f"}, d2 = {"Lcom/example/spaTi/data/repository/AuthRepository;", "", "forgotPassword", "", "user", "Lcom/example/spaTi/data/models/User;", "result", "Lkotlin/Function1;", "Lcom/example/spaTi/util/UiState;", "", "loginUser", "email", "password", "registerUser", "updateUserInfo", "app_debug"})
public abstract interface AuthRepository {
    
    public abstract void registerUser(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password, @org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.User user, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result);
    
    public abstract void updateUserInfo(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.User user, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result);
    
    public abstract void loginUser(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result);
    
    public abstract void forgotPassword(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.User user, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result);
}