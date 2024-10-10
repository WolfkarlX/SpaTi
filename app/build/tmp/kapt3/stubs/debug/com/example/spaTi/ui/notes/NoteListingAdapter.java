package com.example.spaTi.ui.notes;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u001bB\u001f\u0012\u0018\u0010\u0003\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0004\u00a2\u0006\u0002\u0010\bJ\b\u0010\u0011\u001a\u00020\u0005H\u0016J\u001c\u0010\u0012\u001a\u00020\u00072\n\u0010\u0013\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0014\u001a\u00020\u0005H\u0016J\u001c\u0010\u0015\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0005H\u0016J\u000e\u0010\u0019\u001a\u00020\u00072\u0006\u0010\u0014\u001a\u00020\u0005J\u0014\u0010\u001a\u001a\u00020\u00072\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00060\nR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00060\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R#\u0010\u0003\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006\u001c"}, d2 = {"Lcom/example/spaTi/ui/notes/NoteListingAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/example/spaTi/ui/notes/NoteListingAdapter$MyViewHolder;", "onItemClicked", "Lkotlin/Function2;", "", "Lcom/example/spaTi/data/models/Note;", "", "(Lkotlin/jvm/functions/Function2;)V", "list", "", "getOnItemClicked", "()Lkotlin/jvm/functions/Function2;", "sdf", "Ljava/text/SimpleDateFormat;", "getSdf", "()Ljava/text/SimpleDateFormat;", "getItemCount", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "removeItem", "updateList", "MyViewHolder", "app_debug"})
public final class NoteListingAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.example.spaTi.ui.notes.NoteListingAdapter.MyViewHolder> {
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function2<java.lang.Integer, com.example.spaTi.data.models.Note, kotlin.Unit> onItemClicked = null;
    @org.jetbrains.annotations.NotNull
    private final java.text.SimpleDateFormat sdf = null;
    @org.jetbrains.annotations.NotNull
    private java.util.List<com.example.spaTi.data.models.Note> list;
    
    public NoteListingAdapter(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super com.example.spaTi.data.models.Note, kotlin.Unit> onItemClicked) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlin.jvm.functions.Function2<java.lang.Integer, com.example.spaTi.data.models.Note, kotlin.Unit> getOnItemClicked() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.text.SimpleDateFormat getSdf() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public com.example.spaTi.ui.notes.NoteListingAdapter.MyViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull
    com.example.spaTi.ui.notes.NoteListingAdapter.MyViewHolder holder, int position) {
    }
    
    public final void updateList(@org.jetbrains.annotations.NotNull
    java.util.List<com.example.spaTi.data.models.Note> list) {
    }
    
    public final void removeItem(int position) {
    }
    
    @java.lang.Override
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u000b"}, d2 = {"Lcom/example/spaTi/ui/notes/NoteListingAdapter$MyViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/example/spaTi/databinding/ItemNoteLayoutBinding;", "(Lcom/example/spaTi/ui/notes/NoteListingAdapter;Lcom/example/spaTi/databinding/ItemNoteLayoutBinding;)V", "getBinding", "()Lcom/example/spaTi/databinding/ItemNoteLayoutBinding;", "bind", "", "item", "Lcom/example/spaTi/data/models/Note;", "app_debug"})
    public final class MyViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull
        private final com.example.spaTi.databinding.ItemNoteLayoutBinding binding = null;
        
        public MyViewHolder(@org.jetbrains.annotations.NotNull
        com.example.spaTi.databinding.ItemNoteLayoutBinding binding) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.example.spaTi.databinding.ItemNoteLayoutBinding getBinding() {
            return null;
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull
        com.example.spaTi.data.models.Note item) {
        }
    }
}