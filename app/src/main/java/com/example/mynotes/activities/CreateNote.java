package com.example.mynotes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynotes.R;
import com.example.mynotes.entities.Note;
import com.example.mynotes.viewModels.NoteViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNote extends AppCompatActivity {

    private EditText inputTitle, inputSubtitle, inputNoteText;
    private ImageView back, save;
    private TextView dateTime;
    private View subtitleIndicator;
    private String selectedNoteColor;
    private RoundedImageView imageNote;
    private ImageView removeImage, removeWebURL;
    private String selectedImagePath;
    private LinearLayout layoutWebURL;
    private TextView textWebURL;
    private AlertDialog dialogAddURL, dialogSaveNote;
    private AlertDialog dialogDeleteNote;
    private Note alreadyAvailableNote;
    private NoteViewModel noteViewModel;
    private CoordinatorLayout coordinatorLayout;
    private boolean isNoteChanged = false;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    public static final String EXTRA_IS_ADD_NOTE = "isAddNote";
    public static final String EXTRA_IS_UPDATE_NOTE = "isUpdateNote";
    public static final String EXTRA_IS_NOTE_FAVORITE = "isNoteFavorite";
    public static final String EXTRA_IS_NOTE_LOCKED = "isNoteLocked";
    public static final String EXTRA_ACTION_TYPE = "actionType";
    public static final String EXTRA_NOTE = "note";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        doInit();
        setListeners();
        setUpdateNote();
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void setUpdateNote() {
        if (getIntent().getBooleanExtra(EXTRA_IS_UPDATE_NOTE, false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra(EXTRA_NOTE);
            setDataForUpdate();
            if (alreadyAvailableNote.isTrash()) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Can't edit in Trash", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RESTORE", view -> {
                            alreadyAvailableNote.setTrash(false);
                            noteViewModel.updateNote(alreadyAvailableNote);
                            Toast.makeText(CreateNote.this, "Note Restored", Toast.LENGTH_SHORT).show();
                        });
                snackbar.show();
            }
        }
        if (getIntent().getBooleanExtra(EXTRA_IS_ADD_NOTE, false)) {
            String type = getIntent().getStringExtra(EXTRA_ACTION_TYPE);
            if (type.equals("image")) {
                selectedImagePath = getIntent().getStringExtra("imagePath");
                imageNote.setImageBitmap(BitmapFactory
                        .decodeFile(selectedImagePath));
                imageNote.setVisibility(View.VISIBLE);
                removeImage.setVisibility(View.VISIBLE);
            } else if (type.equals("URL")) {
                textWebURL.setText(getIntent().getStringExtra("URL"));
                textWebURL.setVisibility(View.VISIBLE);
                layoutWebURL.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setListeners() {
        back.setOnClickListener(view -> onBackPressed());
        save.setOnClickListener(view -> saveNote());
        removeImage.setOnClickListener(view -> {
            imageNote.setImageBitmap(null);
            imageNote.setVisibility(View.GONE);
            removeImage.setVisibility(View.GONE);
            selectedImagePath = "";
            if(alreadyAvailableNote != null)
                isNoteChanged = !selectedImagePath.equals(alreadyAvailableNote.getImage_path());
        });
        removeWebURL.setOnClickListener(view -> {
            textWebURL.setText(null);
            layoutWebURL.setVisibility(View.GONE);
        });
        inputTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isNoteChanged = true;
            }
        });
        inputSubtitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isNoteChanged = true;
            }
        });
        inputNoteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
               isNoteChanged = true;
            }
        });
        textWebURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isNoteChanged = true;
            }
        });
    }

    private void setDataForUpdate() {
        inputTitle.setText(alreadyAvailableNote.getTitle());
        inputSubtitle.setText(alreadyAvailableNote.getSubtitle());
        dateTime.setText(alreadyAvailableNote.getDate_time());
        inputNoteText.setText(alreadyAvailableNote.getNote_text());

        if (alreadyAvailableNote.getImage_path() != null && !alreadyAvailableNote.getImage_path().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImage_path()));
            imageNote.setVisibility(View.VISIBLE);
            removeImage.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImage_path();
        }

        if (alreadyAvailableNote.getWeb_link() != null && !alreadyAvailableNote.getWeb_link().trim().isEmpty()) {
            textWebURL.setText(alreadyAvailableNote.getWeb_link());
            layoutWebURL.setVisibility(View.VISIBLE);
            removeWebURL.setVisibility(View.VISIBLE);
        }
        isNoteChanged = false;
    }

    private void saveNote() {
        if (inputTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Note title can't be empty", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (inputSubtitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Note subtitle can't be empty", Toast.LENGTH_LONG).show();
                return;
            } else {
                if (inputNoteText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Note text can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        if (alreadyAvailableNote != null) {
            if (alreadyAvailableNote.isTrash()) {
                Toast.makeText(CreateNote.this, "Can't be edit !", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final Note note = new Note();
        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
            note.setFavorite(alreadyAvailableNote.isFavorite());
            note.setLocked(alreadyAvailableNote.isLocked());
        }
        note.setTitle(inputTitle.getText().toString());
        note.setSubtitle(inputSubtitle.getText().toString());
        note.setNote_text(inputNoteText.getText().toString());
        note.setDate_time(dateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImage_path(selectedImagePath);

        if (layoutWebURL.getVisibility() == View.VISIBLE)
            note.setWeb_link(textWebURL.getText().toString());

        if (getIntent().getBooleanExtra(EXTRA_IS_ADD_NOTE, false)) {
            note.setFavorite(getIntent().getBooleanExtra(EXTRA_IS_NOTE_FAVORITE, false));

            if (getIntent().getBooleanExtra(EXTRA_IS_NOTE_LOCKED, false)) {
                note.setLocked(SettingsActivity.isNoteLocked(getApplicationContext(), "isLockedNote"));
            }
        }

        note.setTrash(false);

        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_NEW_NOTE, note);
        setResult(RESULT_OK, intent);
        finish();

    }

    @SuppressLint("SetTextI18n")
    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);

        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);
        final ImageView imageColor6 = layoutMiscellaneous.findViewById(R.id.imageColor6);

        imageColor1.setOnClickListener(view -> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_check);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            imageColor6.setImageResource(0);
            setSubtitleIndicatorColor();
            isNoteChanged = !selectedNoteColor.equals(alreadyAvailableNote.getColor());
        });

        imageColor2.setOnClickListener(view -> {
            selectedNoteColor = "#F26C6C";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_check);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            imageColor6.setImageResource(0);
            setSubtitleIndicatorColor();
            isNoteChanged = !selectedNoteColor.equals(alreadyAvailableNote.getColor());
        });

        imageColor3.setOnClickListener(view -> {
            selectedNoteColor = "#FDBE3D";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_check);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            imageColor6.setImageResource(0);
            setSubtitleIndicatorColor();
            isNoteChanged = !selectedNoteColor.equals(alreadyAvailableNote.getColor());
        });

        imageColor4.setOnClickListener(view -> {
            selectedNoteColor = "#08BDB7";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_check);
            imageColor5.setImageResource(0);
            imageColor6.setImageResource(0);
            setSubtitleIndicatorColor();
            isNoteChanged = !selectedNoteColor.equals(alreadyAvailableNote.getColor());
        });

        imageColor5.setOnClickListener(view -> {
            selectedNoteColor = "#5088FF";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_check);
            imageColor6.setImageResource(0);
            setSubtitleIndicatorColor();
            isNoteChanged = !selectedNoteColor.equals(alreadyAvailableNote.getColor());
        });

        imageColor6.setOnClickListener(view -> {
            selectedNoteColor = "#E87191";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            imageColor6.setImageResource(R.drawable.ic_check);
            setSubtitleIndicatorColor();
            isNoteChanged = !selectedNoteColor.equals(alreadyAvailableNote.getColor());
        });

        if (alreadyAvailableNote != null) {
            switch (alreadyAvailableNote.getColor()) {
                case "#333333":
                    imageColor1.performClick();
                    break;
                case "#F26C6C":
                    imageColor2.performClick();
                    break;
                case "#FDBE3D":
                    imageColor3.performClick();
                    break;
                case "#08BDB7":
                    imageColor4.performClick();
                    break;
                case "#5088FF":
                    imageColor5.performClick();
                    break;
                case "#E87191":
                    imageColor6.performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.addImageLayout).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateNote.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddURL).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });

        if (alreadyAvailableNote != null) {

            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(view -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();
            });
        }

    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) subtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().
                query(contentUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(CreateNote.this, "Permission Dined!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {

                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        imageNote.setImageBitmap(imageBitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        removeImage.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                        if(alreadyAvailableNote != null)
                            isNoteChanged = !selectedImagePath.equals(alreadyAvailableNote.getImage_path());
                    } catch (Exception exception) {
                        Toast.makeText(CreateNote.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void showAddURLDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.layout_add_url_dialog,
                (ViewGroup) findViewById(R.id.addURLDialogContainer),
                false
        );
        builder.setView(view);

        dialogAddURL = builder.create();
        if (dialogAddURL.getWindow() != null) {
            dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final EditText inputURL = view.findViewById(R.id.inputAddURL);
        inputURL.requestFocus();

        view.findViewById(R.id.textAdd).setOnClickListener(view12 -> {
            if (inputURL.getText().toString().trim().isEmpty()) {
                Toast.makeText(CreateNote.this, "Enter URL", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                Toast.makeText(CreateNote.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
            } else {
                textWebURL.setText(inputURL.getText().toString());
                layoutWebURL.setVisibility(View.VISIBLE);
                dialogAddURL.dismiss();
            }
        });

        view.findViewById(R.id.textCancel).setOnClickListener(view1 -> dialogAddURL.dismiss());

        dialogAddURL.show();
    }

    public void showDeleteNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNote.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.layout_delete_note_dialog,
                null
        );
        builder.setView(view);

        dialogDeleteNote = builder.create();
        if (dialogDeleteNote.getWindow() != null) {
            dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        view.findViewById(R.id.textDelete);
        view.setOnClickListener(view1 -> {

            Intent intent = new Intent();
            intent.putExtra("isNoteDeleted", true);
            intent.putExtra(MainActivity.EXTRA_NEW_NOTE, alreadyAvailableNote);
            setResult(RESULT_OK, intent);
            dialogDeleteNote.dismiss();
            finish();

        });

        view.findViewById(R.id.textDeleteCancel).setOnClickListener(view12 -> dialogDeleteNote.dismiss());

        dialogDeleteNote.show();
    }

    @Override
    public void onBackPressed() {
        if (isNoteChanged)
            showSaveNoteDialog();
        else
            super.onBackPressed();
    }

    private void showSaveNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNote.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.dialog_save_note, (ViewGroup) findViewById(R.id.dialogSaveNote)
        );
        builder.setView(view);
        dialogSaveNote = builder.create();
        if (dialogSaveNote.getWindow() != null) {
            dialogSaveNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        TextView textYes = view.findViewById(R.id.textYesDialog);
        TextView textNo = view.findViewById(R.id.textNoDialog);
        Typeface font = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_bold);
        textYes.setTypeface(font);
        textNo.setTypeface(font);
        textYes.setOnClickListener(view1 -> {
            saveNote();
            super.onBackPressed();
        });
        textNo.setOnClickListener(view12 -> {
            dialogSaveNote.dismiss();
            super.onBackPressed();
        });
        dialogSaveNote.show();
    }

    private void doInit() {
        back = findViewById(R.id.back);
        save = findViewById(R.id.save);
        inputTitle = findViewById(R.id.inputNoteTitle);
        inputSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNoteText);
        dateTime = findViewById(R.id.dateTime);
        subtitleIndicator = findViewById(R.id.subtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        layoutWebURL = findViewById(R.id.layoutWebURL);
        textWebURL = findViewById(R.id.textWebURL);
        removeImage = findViewById(R.id.removeImage);
        removeWebURL = findViewById(R.id.removeWebURL);
        coordinatorLayout = findViewById(R.id.createNoteCoordinator);
        dateTime.setText(
                new SimpleDateFormat("EEEE, dd MMM yyyy hh:mm a", Locale.getDefault())
                        .format(new Date())
        );
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        selectedImagePath = "";
        // default note color
        selectedNoteColor = "#333333";
    }

}