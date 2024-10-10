package com.example.spaTi.ui.notes;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u000f\u001a\u00020\u001b2\u0006\u0010\u0015\u001a\u00020\tJ\u000e\u0010\u0013\u001a\u00020\u001b2\u0006\u0010\u0015\u001a\u00020\tJ\u0006\u0010\u001c\u001a\u00020\u001bJ\u000e\u0010\u0019\u001a\u00020\u001b2\u0006\u0010\u0015\u001a\u00020\tR&\u0010\u0005\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R)\u0010\u000f\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u00070\u00108F\u00a2\u0006\u0006\u001a\u0004\b\u0011\u0010\u0012R\u001d\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u00108F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0012R#\u0010\u0015\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\u00070\u00108F\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u001d\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u00108F\u00a2\u0006\u0006\u001a\u0004\b\u001a\u0010\u0012\u00a8\u0006\u001d"}, d2 = {"Lcom/example/spaTi/ui/notes/NoteViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/example/spaTi/data/repository/NoteRepository;", "(Lcom/example/spaTi/data/repository/NoteRepository;)V", "_addNote", "Landroidx/lifecycle/MutableLiveData;", "Lcom/example/spaTi/util/UiState;", "Lkotlin/Pair;", "Lcom/example/spaTi/data/models/Note;", "", "_deleteNote", "_notes", "", "_updateNote", "addNote", "Landroidx/lifecycle/LiveData;", "getAddNote", "()Landroidx/lifecycle/LiveData;", "deleteNote", "getDeleteNote", "note", "getNote", "getRepository", "()Lcom/example/spaTi/data/repository/NoteRepository;", "updateNote", "getUpdateNote", "", "getNotes", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class NoteViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.example.spaTi.data.repository.NoteRepository repository = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.example.spaTi.util.UiState<java.util.List<com.example.spaTi.data.models.Note>>> _notes = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.example.spaTi.util.UiState<kotlin.Pair<com.example.spaTi.data.models.Note, java.lang.String>>> _addNote = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.example.spaTi.util.UiState<java.lang.String>> _updateNote = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.example.spaTi.util.UiState<java.lang.String>> _deleteNote = null;
    
    @javax.inject.Inject
    public NoteViewModel(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.repository.NoteRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.spaTi.data.repository.NoteRepository getRepository() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.example.spaTi.util.UiState<java.util.List<com.example.spaTi.data.models.Note>>> getNote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.example.spaTi.util.UiState<kotlin.Pair<com.example.spaTi.data.models.Note, java.lang.String>>> getAddNote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.example.spaTi.util.UiState<java.lang.String>> getUpdateNote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.example.spaTi.util.UiState<java.lang.String>> getDeleteNote() {
        return null;
    }
    
    public final void getNotes() {
    }
    
    public final void addNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note) {
    }
    
    public final void updateNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note) {
    }
    
    public final void deleteNote(@org.jetbrains.annotations.NotNull
    com.example.spaTi.data.models.Note note) {
    }
}