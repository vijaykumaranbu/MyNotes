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
import com.google.android.material.snackbar.Snackbar;

public class FavoriteFragment extends Fragment implements NotesListener{

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static RecyclerView favoriteRecyclerView;
    @SuppressLint("StaticFieldLeak")
    public static NoteAdapter noteAdapter;
    private NoteViewModel noteViewModel;
    private AlertDialogInterface alertDialog;

    public FavoriteFragment(){}

    public FavoriteFragment(AlertDialogInterface alertDialogInterface){
        this.alertDialog = alertDialogInterface;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        FavoriteFragment.context = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite, container, false);

        favoriteRecyclerView = v.findViewById(R.id.favoriteRecyclerview);
        TextView noFavoriteText = v.findViewById(R.id.noFavoriteText);

        noteAdapter = new NoteAdapter(getContext(),
                (NoteClickListener) getActivity(),
                this);

        setLayoutManager();
        favoriteRecyclerView.setHasFixedSize(true);

        favoriteRecyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
        noteViewModel.getFavoriteNotes().observe(getViewLifecycleOwner(), notes -> {
            if(!notes.isEmpty()){
                noteAdapter.submit(notes);
                noFavoriteText.setVisibility(View.GONE);
            }else{
                noteAdapter.submit(notes);
                noFavoriteText.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }

    public static void setLayoutManager() {
        if (MainActivity.getRecyclerViewType(context)) {
            favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            favoriteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2
                    , StaggeredGridLayoutManager.VERTICAL));
        }
    }


    @Override
    public void updateNoteListener(NoteAdapter.NoteViewHolder noteViewHolder,int position, Note note) {
        if(note.isFavorite()){
            note.setFavorite(false);
            noteViewModel.updateNote(note);
        }
    }

    @Override
    public void deleteNoteListener(int position,Note note) {
        if(!note.isLocked()) {
            note.setFavorite(false);
            note.setTrash(true);
            noteViewModel.updateNote(note);
            Snackbar snackbar = Snackbar.make(favoriteRecyclerView, "Move to Trash", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", view -> {
                        note.setFavorite(true);
                        note.setTrash(false);
                        noteViewModel.updateNote(note);
                    });
            snackbar.show();
        }
        else {
            alertDialog.onShowDialogLockedNote(true,false,note);
        }
    }
}