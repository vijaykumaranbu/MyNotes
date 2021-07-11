package com.example.mynotes.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynotes.R;
import com.example.mynotes.entities.Note;
import com.example.mynotes.viewModels.NoteViewModel;

import java.util.List;


public class SettingsActivity extends AppCompatActivity {

    private TextView textSetOrUnset;
    private AlertDialog dialogLockNote;
    private CheckBox checkBoxLockNote;
    private NoteViewModel noteViewModel;
    private List<Note> lockedNotes;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getLockedNotes().observe(this, notes -> lockedNotes = notes);

        ImageView imageBack = findViewById(R.id.settings_image_back);
        imageBack.setOnClickListener(view -> onBackPressed());


        RelativeLayout notePinCodeLayout = findViewById(R.id.note_pin_code_layout);
        notePinCodeLayout.setOnClickListener(view -> {
            if (isNoteLocked(SettingsActivity.this, "isLockedNote"))
                showLockNoteDialog("Remove");
            else
                showLockNoteDialog("Set");
        });

        textSetOrUnset = findViewById(R.id.text_pincode_set);
        checkBoxLockNote = findViewById(R.id.note_pin_code_checkBox);
        if (isNoteLocked(SettingsActivity.this, "isLockedNote")) {
            checkBoxLockNote.setChecked(true);
            textSetOrUnset.setText("PinCode Set");
        } else {
            checkBoxLockNote.setChecked(false);
            textSetOrUnset.setText("Pincode Unset");
        }

    }

    @SuppressLint("SetTextI18n")
    public void showLockNoteDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.layout_lock_note_dialog,
                null
        );
        builder.setView(view);

        dialogLockNote = builder.create();
        if (dialogLockNote.getWindow() != null) {
            dialogLockNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final EditText inputSetPin = view.findViewById(R.id.inputSetPin);
        final TextView textSet = view.findViewById(R.id.textSet);
        inputSetPin.requestFocus();
        textSet.requestFocus();
        textSet.setText(text);

        textSet.setOnClickListener(view1 -> {
            String textPassword = inputSetPin.getText().toString().trim();
            if (!isNoteLocked(SettingsActivity.this, "isLockedNote")) {
                if (textPassword.length() == 4) {
                    saveLockNotePassword(SettingsActivity.this, textPassword);
                    checkBoxLockNote.setChecked(true);
                    textSetOrUnset.setText("Pincode Set");
                    Toast.makeText(SettingsActivity.this, "Saved", Toast.LENGTH_LONG).show();
                    dialogLockNote.dismiss();
                } else {
                    Toast.makeText(SettingsActivity.this, "Enter 4 digit only", Toast.LENGTH_LONG).show();
                }
            } else {
                if (getLockNotePassword(SettingsActivity.this, "notePassword") != null) {
                    if (getLockNotePassword(SettingsActivity.this, "notePassword").equals(textPassword)) {
                        removeLock(SettingsActivity.this);
                        checkBoxLockNote.setChecked(false);
                        textSetOrUnset.setText("Pincode Unset");
                        removeLockInNotes();
                        dialogLockNote.dismiss();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Pincode is : " +
                                        SettingsActivity.getLockNotePassword(SettingsActivity.this, "notePassword")
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        view.findViewById(R.id.textLockCancel).setOnClickListener(view12 -> dialogLockNote.dismiss());

        dialogLockNote.show();
    }

    private void removeLockInNotes() {
       for(Note note : lockedNotes){
           if(note.isLocked()){
               note.setLocked(false);
               noteViewModel.updateNote(note);
           }
       }
       Toast.makeText(SettingsActivity.this,"Lock Removed",Toast.LENGTH_SHORT).show();
    }

    public void saveLockNotePassword(Context context, String password) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("isLockedNote", true).apply();
        preferences.edit().putString("notePassword", password).apply();
    }

    public void removeLock(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("isLockedNote", false).apply();
        preferences.edit().putString("notePassword", null).apply();
    }

    public static String getLockNotePassword(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("notePassword", null);
    }

    public static boolean isNoteLocked(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("isLockedNote", false);
    }
}