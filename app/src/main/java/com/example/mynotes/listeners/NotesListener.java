package com.example.mynotes.listeners;

import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.entities.Note;

public interface NotesListener {

    void updateNoteListener(final NoteAdapter.NoteViewHolder noteViewHolder,
                            final int position,
                            final Note note);

    void deleteNoteListener(final int position, final Note note);

}
