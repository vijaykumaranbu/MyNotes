package com.example.mynotes.fragments;

import android.annotation.SuppressLint;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mynotes.activities.MainActivity;
import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.entities.Note;
import com.example.mynotes.listeners.AlertDialogInterface;
import com.example.mynotes.listeners.NotesListener;
import com.example.mynotes.listeners.NoteClickListener;
import com.example.mynotes.R;
import com.example.mynotes.viewModels.NoteViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class NotesFragment extends Fragment implements NotesListener {

    private static RecyclerView noteRecyclerView;
    private NoteViewModel noteViewModel;
    @SuppressLint("StaticFieldLeak")
    public static NoteAdapter noteAdapter;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private TextView noNotesText;
    private AlertDialogInterface alertDialog;

    public NotesFragment(){}

    public NotesFragment(AlertDialogInterface alertDialogInterface) {
        this.alertDialog = alertDialogInterface;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        NotesFragment.context = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notes, container, false);

        noteRecyclerView = v.findViewById(R.id.noteRecyclerview);
        noNotesText = v.findViewById(R.id.noNotesText);

        noteAdapter = new NoteAdapter(getContext(),
                (NoteClickListener) getActivity(),
                this);

        setLayoutManager();
        noteRecyclerView.setHasFixedSize(true);

        noteRecyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            if (!notes.isEmpty()) {
                noteAdapter.submit(notes);
                noNotesText.setVisibility(View.GONE);
            } else {
                noteAdapter.submit(notes);
                noNotesText.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }

    public static void setLayoutManager() {
        if (MainActivity.getRecyclerViewType(context)) {
            noteRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2
                    , StaggeredGridLayoutManager.VERTICAL));
        }
    }

    @Override
    public void updateNoteListener(NoteAdapter.NoteViewHolder noteViewHolder, int position, Note note) {
        if (note.isFavorite()) {
            noteViewHolder.imageFavorite.setVisibility(View.GONE);
            note.setFavorite(false);

            if(!note.isLocked()){
                noteViewHolder.imageLayout.setVisibility(View.GONE);
            }

        } else {
            noteViewHolder.imageLayout.setVisibility(View.VISIBLE);
            noteViewHolder.imageFavorite.setVisibility(View.VISIBLE);

            note.setFavorite(true);
        }
        noteViewModel.updateNote(note);
    }

    @Override
    public void deleteNoteListener(int position, Note note) {
        if(!note.isLocked()) {
            boolean isFavorite = note.isFavorite();
            if(note.isFavorite())
                note.setFavorite(false);
            note.setTrash(true);
            noteViewModel.updateNote(note);
            Snackbar snackbar = Snackbar.make(noteRecyclerView, "Move to Trash", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", view -> {
                        note.setFavorite(isFavorite);
                        note.setTrash(false);
                        noteViewModel.updateNote(note);
                    });
            snackbar.show();
        }
        else{
            alertDialog.onShowDialogLockedNote(true,false,note);
        }
    }
}