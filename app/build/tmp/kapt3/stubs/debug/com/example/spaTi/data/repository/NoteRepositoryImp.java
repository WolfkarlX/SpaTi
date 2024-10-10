package com.example.spaTi.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J6\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2$\u0010\u000b\u001a \u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000f0\u000e0\r\u0012\u0004\u0012\u00020\b0\fH\u0016J*\u0010\u0010\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0018\u0010\u000b\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\r\u0012\u0004\u0012\u00020\b0\fH\u0016J(\u0010\u0011\u001a\u00020\b2\u001e\u0010\u000b\u001a\u001a\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00120\r\u0012\u0004\u0012\u00020\b0\fH\u0016J*\u0010\u0013\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0018\u0010\u000b\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\r\u0012\u0004\u0012\u00020\b0\fH\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0014"}, d2 = {"Lcom/example/spaTi/data/repository/NoteRepositoryImp;", "Lcom/example/spaTi/data/repository/NoteRepository;", "database", "Lcom/google/firebase/firestore/FirebaseFirestore;", "(Lcom/google/firebase/firestore/FirebaseFirestore;)V", "getDatabase", "()Lcom/google/firebase/firestore/FirebaseFirestore;", "addNote", "", "note", "Lcom/example/spaTi/data/models/Note;", "result", "Lkotlin/Function1;", "Lcom/example/spaTi/util/UiState;", "Lkotlin/Pair;", "", "deleteNote", "getNotes", "", "updateNote", "app_debug"})
public final class NoteRepositoryImp implements com.example.spaTi.data.repository.NoteRepository {
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.firestore.FirebaseFirestore database = null;
    
    public NoteRepositoryImp(@org.jetbrains.annotations.NotNull
    com.google.firebase.firestore.FirebaseFirestore database) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.google.firebase.firestore.FirebaseFirestore getDatabase() {
        return null;
    }
    
    @java.lang.Override
    public void getNotes(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<? extends java.util.List<com.example.spaTi.data.models.Note>>, kotlin.Unit> result) {
    }
    
    @java.lang.Override
    public void addNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<kotlin.Pair<com.example.spaTi.data.models.Note, java.lang.String>>, kotlin.Unit> result) {
    }
    
    @java.lang.Override
    public void updateNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result) {
    }
    
    @java.lang.Override
    public void deleteNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result) {
    }
}