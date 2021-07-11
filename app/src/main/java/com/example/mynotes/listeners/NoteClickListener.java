package com.example.mynotes.listeners;

import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.entities.Note;

public interface NoteClickListener {

    void onNoteClicked(Note note,int position);

    void onNoteLongClicked(NotesListener notesListener,
                           NoteAdapter.NoteViewHolder itemHolder,
                           Note note,
                           int position);
}
