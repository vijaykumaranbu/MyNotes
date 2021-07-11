package com.example.mynotes.bottomSheetDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynotes.R;
import com.example.mynotes.activities.MainActivity;
import com.example.mynotes.activities.SettingsActivity;
import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.entities.Note;
import com.example.mynotes.listeners.AlertDialogInterface;
import com.example.mynotes.listeners.NotesListener;
import com.example.mynotes.viewModels.NoteViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Objects;

public class NotesBottomSheet extends BottomSheetDialogFragment {

    private Context context;
    private static Note note;
    private static NotesListener notesListener;
    private static int notePosition;
    @SuppressLint("StaticFieldLeak")
    private static NoteAdapter.NoteViewHolder noteViewHolder;
    private static AlertDialogInterface dialogInterface;

    public NotesBottomSheet() {}

    public static NotesBottomSheet newInstance(NotesListener listener,
                                               AlertDialogInterface alertDialogInterface,
                                               NoteAdapter.NoteViewHolder holder,
                                               Note clickedNote,
                                               int position) {
        notesListener = listener;
        dialogInterface = alertDialogInterface;
        note = clickedNote;
        noteViewHolder = holder;
        notePosition = position;
        return new NotesBottomSheet();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notes_bottom_sheet, container, false);
        LinearLayout layoutFavorite = view.findViewById(R.id.layoutFavorite);
        LinearLayout layoutLocked = view.findViewById(R.id.layoutLocked);
        LinearLayout layoutDelete = view.findViewById(R.id.layoutDelete);
        TextView textFavorite = view.findViewById(R.id.textFavorite);
        TextView textLocked = view.findViewById(R.id.b_locked_text);
        ImageView favoriteImage = view.findViewById(R.id.b_fav_image);
        ImageView lockedImage = view.findViewById(R.id.b_locked_image);
        NoteViewModel noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        if(Objects.equals(getCurrentFragment().getTag(),MainActivity.TRASH_FRAGMENT_TAG)){
            layoutFavorite.setEnabled(false);
            layoutLocked.setEnabled(false);
            favoriteImage.setColorFilter(ContextCompat.getColor(context,R.color.colorIcon));
            textFavorite.setTextColor(ContextCompat.getColor(context,R.color.colorIcon));
            lockedImage.setColorFilter(ContextCompat.getColor(context,R.color.colorIcon));
            textLocked.setTextColor(ContextCompat.getColor(context,R.color.colorIcon));
        }
        else{
            if(SettingsActivity.isNoteLocked(getContext(),"isLockedNote")){
                layoutLocked.setEnabled(true);
                lockedImage.setColorFilter(ContextCompat.getColor(context,R.color.colorWhite));
                textLocked.setTextColor(ContextCompat.getColor(context,R.color.colorWhite));
            }
            else {
                layoutLocked.setEnabled(false);
                lockedImage.setColorFilter(ContextCompat.getColor(context,R.color.colorIcon));
                textLocked.setTextColor(ContextCompat.getColor(context,R.color.colorIcon));
            }
        }

        if (note.isFavorite())
            textFavorite.setText("Remove");
        else
            textFavorite.setText("Favorite");

        if(note.isLocked())
            textLocked.setText("Unlock");
        else
            textLocked.setText("Lock");


        layoutFavorite.setOnClickListener(view1 -> {

            notesListener.updateNoteListener(noteViewHolder,notePosition,note);
            dismiss();
        });

        layoutLocked.setOnClickListener(view12 -> {
            if (note.isLocked()) {
                dialogInterface.onShowDialogLockedNote(false,true, note);
            } else {
                note.setLocked(true);
                noteViewModel.updateNote(note);
                noteViewHolder.imageLayout.setVisibility(View.VISIBLE);
                noteViewHolder.imageLocked.setVisibility(View.VISIBLE);
            }
            dismiss();
        });

        layoutDelete.setOnClickListener(view13 -> {

            notesListener.deleteNoteListener(notePosition,note);
            dismiss();
        });

        return view;
    }


    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetTheme;
    }

    @Override
    public void onDetach() {
        notesListener = null;
        super.onDetach();
    }

    public Fragment getCurrentFragment() {
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }
}
