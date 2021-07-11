package com.example.mynotes.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mynotes.R;
import com.example.mynotes.activities.MainActivity;
import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.entities.Note;
import com.example.mynotes.listeners.NoteClickListener;
import com.example.mynotes.listeners.NotesListener;
import com.example.mynotes.viewModels.NoteViewModel;

public class TrashFragment extends Fragment implements NotesListener {

    private AlertDialog dialogDeleteNote;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static RecyclerView trashRecyclerView;
    @SuppressLint("StaticFieldLeak")
    public static NoteAdapter noteAdapter;
    private NoteViewModel noteViewModel;

    public TrashFragment(){}

    @Override
    public void onAttach(@NonNull Context context) {
        TrashFragment.context= context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trash, container, false);

        trashRecyclerView = v.findViewById(R.id.trashRecyclerview);
        TextView noTrashText = v.findViewById(R.id.noTrashText);

        noteAdapter = new NoteAdapter(getContext(),
                (NoteClickListener) getActivity(),
                this);

        setLayoutManager();
        trashRecyclerView.setHasFixedSize(true);

        trashRecyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
        noteViewModel.getTrashNotes().observe(getViewLifecycleOwner(), notes -> {
            if(!notes.isEmpty()){
                noteAdapter.submit(notes);
                noTrashText.setVisibility(View.GONE);
            }
            else {
                noteAdapter.submit(notes);
                noTrashText.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }

    public static void setLayoutManager(){
        if(MainActivity.getRecyclerViewType(context)){
            trashRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        else {
            trashRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2
                    , StaggeredGridLayoutManager.VERTICAL));
        }
    }

    @Override
    public void updateNoteListener(NoteAdapter.NoteViewHolder noteViewHolder, int position, Note note) {
    }

    @Override
    public void deleteNoteListener(int position,Note note) {
         showDeleteNoteDialog(position,note);
    }

    public void showDeleteNoteDialog(int position, Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_delete_note_dialog,
                null
        );
        builder.setView(view);

        dialogDeleteNote = builder.create();
        if (dialogDeleteNote.getWindow() != null) {
            dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        view.findViewById(R.id.textDelete).setOnClickListener(view1 -> {

            noteViewModel.deleteNote(note);

            dialogDeleteNote.dismiss();
        });

        view.findViewById(R.id.textDeleteCancel).

                setOnClickListener(view12 -> dialogDeleteNote.dismiss());

        dialogDeleteNote.show();
    }
}