package com.example.mynotes.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mynotes.R;
import com.example.mynotes.activities.MainActivity;
import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.entities.Note;
import com.example.mynotes.listeners.AlertDialogInterface;
import com.example.mynotes.listeners.NoteClickListener;
import com.example.mynotes.listeners.NotesListener;
import com.example.mynotes.viewModels.NoteViewModel;

public class LockedFragment extends Fragment implements NotesListener {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static RecyclerView lockedRecyclerView;
    @SuppressLint("StaticFieldLeak")
    public static NoteAdapter noteAdapter;
    private NoteViewModel noteViewModel;
    private AlertDialogInterface alertDialog;

    public LockedFragment(){}

    public LockedFragment(AlertDialogInterface alertDialogInterface){
        this.alertDialog = alertDialogInterface;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        LockedFragment.context = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_locked, container, false);

        lockedRecyclerView = v.findViewById(R.id.lockedRecyclerview);
        TextView noLockedText = v.findViewById(R.id.noLockedText);

        noteAdapter = new NoteAdapter(getContext(),
                (NoteClickListener) getActivity(),
                this);

        setLayoutManager();
        lockedRecyclerView.setHasFixedSize(true);

        lockedRecyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
        noteViewModel.getLockedNotes().observe(getViewLifecycleOwner(), notes -> {
            if(!notes.isEmpty()){
                noteAdapter.submit(notes);
                noLockedText.setVisibility(View.GONE);
            }else{
                noteAdapter.submit(notes);
                noLockedText.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }

    public static void setLayoutManager() {
        if (MainActivity.getRecyclerViewType(context)) {
            lockedRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            lockedRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2
                    , StaggeredGridLayoutManager.VERTICAL));
        }
    }

    @Override
    public void updateNoteListener(NoteAdapter.NoteViewHolder noteViewHolder, int position, Note note) {
        if (note.isFavorite()) {
            noteViewHolder.imageLayout.setVisibility(View.GONE);
            noteViewHolder.imageFavorite.setVisibility(View.GONE);
            note.setFavorite(false);
        } else {
            noteViewHolder.imageLayout.setVisibility(View.VISIBLE);
            noteViewHolder.imageFavorite.setVisibility(View.VISIBLE);
            note.setFavorite(true);
        }
        noteViewModel.updateNote(note);
    }

    @Override
    public void deleteNoteListener(int position, Note note) {
        alertDialog.onShowDialogLockedNote(true,false,note);
    }
}