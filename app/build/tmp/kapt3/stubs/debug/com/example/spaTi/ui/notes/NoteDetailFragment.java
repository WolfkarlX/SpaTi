package com.example.spaTi.ui.notes;

@dagger.hilt.android.AndroidEntryPoint
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010!\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u001f\u001a\u00020 2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00040\u0014H\u0002J\b\u0010\"\u001a\u00020\u000eH\u0002J\u0012\u0010#\u001a\u00020 2\b\b\u0002\u0010$\u001a\u00020%H\u0002J\b\u0010&\u001a\u00020 H\u0002J$\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020*2\b\u0010+\u001a\u0004\u0018\u00010,2\b\u0010-\u001a\u0004\u0018\u00010.H\u0016J\u001a\u0010/\u001a\u00020 2\u0006\u00100\u001a\u00020(2\b\u0010-\u001a\u0004\u0018\u00010.H\u0016J\b\u00101\u001a\u00020 H\u0002J\b\u00102\u001a\u00020 H\u0002J\b\u00103\u001a\u00020%H\u0002R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u001a\u0010\u0007\u001a\u00020\bX\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001c\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R \u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00040\u0014X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018R\u001b\u0010\u0019\u001a\u00020\u001a8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001d\u0010\u001e\u001a\u0004\b\u001b\u0010\u001c\u00a8\u00064"}, d2 = {"Lcom/example/spaTi/ui/notes/NoteDetailFragment;", "Landroidx/fragment/app/Fragment;", "()V", "TAG", "", "getTAG", "()Ljava/lang/String;", "binding", "Lcom/example/spaTi/databinding/FragmentNoteDetailBinding;", "getBinding", "()Lcom/example/spaTi/databinding/FragmentNoteDetailBinding;", "setBinding", "(Lcom/example/spaTi/databinding/FragmentNoteDetailBinding;)V", "objNote", "Lcom/example/spaTi/data/models/Note;", "getObjNote", "()Lcom/example/spaTi/data/models/Note;", "setObjNote", "(Lcom/example/spaTi/data/models/Note;)V", "tagsList", "", "getTagsList", "()Ljava/util/List;", "setTagsList", "(Ljava/util/List;)V", "viewModel", "Lcom/example/spaTi/ui/notes/NoteViewModel;", "getViewModel", "()Lcom/example/spaTi/ui/notes/NoteViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "addTags", "", "note", "getNote", "isMakeEnableUI", "isDisable", "", "observer", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "view", "showAddTagDialog", "updateUI", "validation", "app_debug"})
public final class NoteDetailFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String TAG = "NoteDetailFragment";
    public com.example.spaTi.databinding.FragmentNoteDetailBinding binding;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.Nullable
    private com.example.spaTi.data.models.Note objNote;
    @org.jetbrains.annotations.NotNull
    private java.util.List<java.lang.String> tagsList;
    
    public NoteDetailFragment() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTAG() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.spaTi.databinding.FragmentNoteDetailBinding getBinding() {
        return null;
    }
    
    public final void setBinding(@org.jetbrains.annotations.NotNull
    com.example.spaTi.databinding.FragmentNoteDetailBinding p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.spaTi.ui.notes.NoteViewModel getViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.example.spaTi.data.models.Note getObjNote() {
        return null;
    }
    
    public final void setObjNote(@org.jetbrains.annotations.Nullable
    com.example.spaTi.data.models.Note p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getTagsList() {
        return null;
    }
    
    public final void setTagsList(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> p0) {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override
    public void onViewCreated(@org.jetbrains.annotations.NotNull
    android.view.View view, @org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void observer() {
    }
    
    private final void updateUI() {
    }
    
    private final void showAddTagDialog() {
    }
    
    private final void addTags(java.util.List<java.lang.String> note) {
    }
    
    private final void isMakeEnableUI(boolean isDisable) {
    }
    
    private final boolean validation() {
        return false;
    }
    
    private final com.example.spaTi.data.models.Note getNote() {
        return null;
    }
}