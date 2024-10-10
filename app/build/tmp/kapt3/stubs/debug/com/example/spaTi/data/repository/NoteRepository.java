package com.example.spaTi.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J6\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052$\u0010\u0006\u001a \u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\n0\t0\b\u0012\u0004\u0012\u00020\u00030\u0007H&J*\u0010\u000b\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0018\u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\b\u0012\u0004\u0012\u00020\u00030\u0007H&J(\u0010\f\u001a\u00020\u00032\u001e\u0010\u0006\u001a\u001a\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\b\u0012\u0004\u0012\u00020\u00030\u0007H&J*\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0018\u0010\u0006\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\b\u0012\u0004\u0012\u00020\u00030\u0007H&\u00a8\u0006\u000f"}, d2 = {"Lcom/example/spaTi/data/repository/NoteRepository;", "", "addNote", "", "note", "Lcom/example/spaTi/data/models/Note;", "result", "Lkotlin/Function1;", "Lcom/example/spaTi/util/UiState;", "Lkotlin/Pair;", "", "deleteNote", "getNotes", "", "updateNote", "app_debug"})
public abstract interface NoteRepository {
    
    public abstract void getNotes(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<? extends java.util.List<com.example.spaTi.data.models.Note>>, kotlin.Unit> result);
    
    public abstract void addNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<kotlin.Pair<com.example.spaTi.data.models.Note, java.lang.String>>, kotlin.Unit> result);
    
    public abstract void updateNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result);
    
    public abstract void deleteNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.spaTi.util.UiState<java.lang.String>, kotlin.Unit> result);
}