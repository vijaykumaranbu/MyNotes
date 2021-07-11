package com.example.mynotes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynotes.R;
import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.bottomSheetDialog.NotesBottomSheet;
import com.example.mynotes.entities.Note;
import com.example.mynotes.fragments.FavoriteFragment;
import com.example.mynotes.fragments.LockedFragment;
import com.example.mynotes.fragments.NotesFragment;
import com.example.mynotes.fragments.TrashFragment;
import com.example.mynotes.listeners.AlertDialogInterface;
import com.example.mynotes.listeners.NoteClickListener;
import com.example.mynotes.listeners.NotesListener;
import com.example.mynotes.viewModels.NoteViewModel;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        AlertDialogInterface,
        NoteClickListener {

    private Toolbar toolbar;
    private FloatingActionMenu floatingActionMenu;
    private AlertDialog dialogAddURL;
    private NoteViewModel noteViewModel;
    private NotesListener notesListener;
    private AlertDialog dialogLockNote;
    private NoteAdapter.NoteViewHolder clickedItemHolder;

    public static DrawerLayout drawer;
    private int clickedPosition = -1;

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;

    public static final String EXTRA_NEW_NOTE = "new_note";
    public static final String RECYCLER_VIEW_TYPE = "layout_type";
    public static final String NOTE_FRAGMENT_TAG = "note_fragment";
    public static final String FAVORITE_FRAGMENT_TAG = "favorite_fragment";
    public static final String LOCKED_FRAGMENT_TAG = "locked_fragment";
    public static final String TRASH_FRAGMENT_TAG = "trash_fragment";
    public static final String BOTTOM_SHEET_TAG = "noteBottomSheet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        floatingActionMenu = findViewById(R.id.floatingActionBtn);
        drawer = findViewById(R.id.drawer_layout);
        FloatingActionButton addNote = findViewById(R.id.newNote);
        FloatingActionButton addImage = findViewById(R.id.imageNewNote);
        FloatingActionButton addWebLink = findViewById(R.id.webLinkNewNote);
        View drawerHeader = navigationView.getHeaderView(0);
        ImageView imageDrawer = drawerHeader.findViewById(R.id.nav_image_drawer);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.notes_menu);
        NotesFragment notesFragment = new NotesFragment(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                notesFragment, NOTE_FRAGMENT_TAG).commit();
        notesListener = notesFragment;

        Picasso.get().load(R.drawable.image_drawer1).centerCrop().fit().into(imageDrawer);

        addNote.setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, CreateNote.class);
            intent.putExtra(CreateNote.EXTRA_IS_ADD_NOTE, true);
            intent.putExtra(CreateNote.EXTRA_ACTION_TYPE, "new");

            if (Objects.equals(getCurrentFragment().getTag(), FAVORITE_FRAGMENT_TAG))
                intent.putExtra(CreateNote.EXTRA_IS_NOTE_FAVORITE, true);
            else if (Objects.equals(getCurrentFragment().getTag(), LOCKED_FRAGMENT_TAG))
                intent.putExtra(CreateNote.EXTRA_IS_NOTE_LOCKED, true);

            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

            floatingActionMenu.close(true);
        });

        addImage.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
            floatingActionMenu.close(true);
        });

        addWebLink.setOnClickListener(view -> {
            showAddURLDialog();
            floatingActionMenu.close(true);
        });

    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().
                query(contentUri, new String[]{MediaStore.MediaColumns.DATA},
                        null, null, null);
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

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void showAddURLDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

        view.findViewById(R.id.textAdd).setOnClickListener(view1 -> {
            if (inputURL.getText().toString().trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter URL", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                Toast.makeText(getApplicationContext(), "Enter Valid URL", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getApplicationContext(), CreateNote.class);
                intent.putExtra(CreateNote.EXTRA_IS_ADD_NOTE, true);
                intent.putExtra(CreateNote.EXTRA_ACTION_TYPE, "URL");
                intent.putExtra("URL", inputURL.getText().toString());

                if (Objects.equals(getCurrentFragment().getTag(), FAVORITE_FRAGMENT_TAG))
                    intent.putExtra(CreateNote.EXTRA_IS_NOTE_FAVORITE, true);

                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

                dialogAddURL.dismiss();
            }
        });

        view.findViewById(R.id.textCancel).setOnClickListener(view12 -> dialogAddURL.dismiss());

        dialogAddURL.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                Note note = (Note) data.getSerializableExtra(EXTRA_NEW_NOTE);
                if (note != null)
                    noteViewModel.insertNote(note);
            }
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                Note note = (Note) data.getSerializableExtra(EXTRA_NEW_NOTE);
                if (note != null) {
                    if (data.getBooleanExtra("isNoteDeleted", false)) {
                        notesListener.deleteNoteListener(clickedPosition, note);
                    } else
                        noteViewModel.updateNote(note);
                }
            }
        }

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {
                    String selectedImagePath = getPathFromUri(selectedImageUri);
                    Intent intent = new Intent(MainActivity.this, CreateNote.class);
                    intent.putExtra(CreateNote.EXTRA_IS_ADD_NOTE, true);
                    intent.putExtra(CreateNote.EXTRA_ACTION_TYPE, "image");
                    intent.putExtra("imagePath", selectedImagePath);

                    if (Objects.equals(getCurrentFragment().getTag(), FAVORITE_FRAGMENT_TAG))
                        intent.putExtra(CreateNote.EXTRA_IS_NOTE_FAVORITE, true);
                    else if (Objects.equals(getCurrentFragment().getTag(), LOCKED_FRAGMENT_TAG))
                        intent.putExtra(CreateNote.EXTRA_IS_NOTE_LOCKED, true);

                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Dined!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.notes_menu:
                NotesFragment notesFragment = new NotesFragment(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, notesFragment, NOTE_FRAGMENT_TAG)
                        .commit();
                notesListener = notesFragment;
                toolbar.setTitle("Notes");
                floatingActionMenu.setVisibility(View.VISIBLE);
                break;
            case R.id.favorite_menu:
                FavoriteFragment favoriteFragment = new FavoriteFragment(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, favoriteFragment, FAVORITE_FRAGMENT_TAG)
                        .commit();
                notesListener = favoriteFragment;
                toolbar.setTitle("Favorite");
                floatingActionMenu.setVisibility(View.VISIBLE);
                break;
            case R.id.locked_menu:
                LockedFragment lockedFragment = new LockedFragment(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, lockedFragment, LOCKED_FRAGMENT_TAG)
                        .commit();
                notesListener = lockedFragment;
                toolbar.setTitle("Locked");
                floatingActionMenu.setVisibility(View.VISIBLE);
                break;
            case R.id.trash_menu:
                TrashFragment trashFragment = new TrashFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, trashFragment, TRASH_FRAGMENT_TAG)
                        .commit();
                notesListener = trashFragment;
                toolbar.setTitle("Trash");
                floatingActionMenu.setVisibility(View.GONE);
                break;
            case R.id.settings_menu:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.rateThisApp_menu:
                Toast.makeText(getApplicationContext(), "Rate this app", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share_menu:
                Toast.makeText(getApplicationContext(), "Share this app", Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.onActionViewCollapsed();

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                toolbar.getMenu().findItem(R.id.grid_menu).setVisible(false);
                return true;
            }

            @SuppressLint("ResourceType")
            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                toolbar.getMenu().findItem(R.id.grid_menu).setVisible(true);
                invalidateOptionsMenu();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (Objects.equals(getCurrentFragment().getTag(), NOTE_FRAGMENT_TAG))
                    NotesFragment.noteAdapter.getFilter().filter(newText);
                else if (Objects.equals(getCurrentFragment().getTag(), FAVORITE_FRAGMENT_TAG))
                    FavoriteFragment.noteAdapter.getFilter().filter(newText);
                else if (Objects.equals(getCurrentFragment().getTag(), TRASH_FRAGMENT_TAG))
                    TrashFragment.noteAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "NonConstantResourceId"})
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_menu:
                return true;
            case R.id.grid_menu:
                if (getRecyclerViewType(getApplicationContext())) {
                    setRecyclerViewType(getApplicationContext(), false);
                    item.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_linear));
                    item.setTitle("linear");
                } else {
                    setRecyclerViewType(getApplicationContext(), true);
                    item.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_grid));
                    item.setTitle("grid");
                }

                if (Objects.equals(getCurrentFragment().getTag(), NOTE_FRAGMENT_TAG))
                    NotesFragment.setLayoutManager();
                else if (Objects.equals(getCurrentFragment().getTag(), FAVORITE_FRAGMENT_TAG)) {
                    FavoriteFragment.setLayoutManager();
                } else if (Objects.equals(getCurrentFragment().getTag(), TRASH_FRAGMENT_TAG))
                    TrashFragment.setLayoutManager();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getRecyclerViewType(getApplicationContext()))
            menu.findItem(R.id.grid_menu).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_grid));
        else
            menu.findItem(R.id.grid_menu).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_linear));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onNoteClicked(Note note, int position) {

        clickedPosition = position;
        if (note.isLocked()) {
            onShowDialogLockedNote(false, false, note);
        } else {
            Intent intent = new Intent(MainActivity.this, CreateNote.class);
            intent.putExtra(CreateNote.EXTRA_IS_UPDATE_NOTE, true);
            intent.putExtra(CreateNote.EXTRA_NOTE, note);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        }

    }

    @Override
    public void onNoteLongClicked(NotesListener notesListener,
                                  NoteAdapter.NoteViewHolder itemHolder,
                                  Note note,
                                  int position) {

        clickedItemHolder = itemHolder;
        NotesBottomSheet.newInstance(notesListener, this, itemHolder, note, position)
                .show(getSupportFragmentManager(), MainActivity.BOTTOM_SHEET_TAG);
    }

    public static void setRecyclerViewType(Context context, boolean type) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(RECYCLER_VIEW_TYPE, type);
        editor.apply();
    }

    public static boolean getRecyclerViewType(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(RECYCLER_VIEW_TYPE, false);
    }

    public Fragment getCurrentFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onShowDialogLockedNote(boolean isDelete, boolean isUnlock, Note note) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.layout_lock_note_dialog,
                null
        );
        builder.setView(view);

        dialogLockNote = builder.create();
        if (dialogLockNote.getWindow() != null) {
            dialogLockNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final EditText inputPin = view.findViewById(R.id.inputSetPin);
        final TextView textSet = view.findViewById(R.id.textSet);
        inputPin.requestFocus();
        textSet.requestFocus();

        if (isDelete)
            textSet.setText("Delete");
        else if(isUnlock)
            textSet.setText("Remove");
        else
            textSet.setText("Done");

        textSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SettingsActivity.isNoteLocked(getApplicationContext(), "isLockedNote")) {
                    String textPassword = inputPin.getText().toString().trim();
                    if (textPassword.equals(SettingsActivity.getLockNotePassword(getApplicationContext(),
                            "notePassword"))) {

                        if (isDelete)
                            deleteLockedNote(note);
                        else if (isUnlock)
                            unLockNote(note);
                        else {
                            Intent intent = new Intent(MainActivity.this, CreateNote.class);
                            intent.putExtra(CreateNote.EXTRA_IS_UPDATE_NOTE, true);
                            intent.putExtra(CreateNote.EXTRA_NOTE, note);
                            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Invalid Pincode"
                                , Toast.LENGTH_SHORT).show();
                    }
                }
                dialogLockNote.dismiss();
            }
        });

        view.findViewById(R.id.textLockCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogLockNote.dismiss();
            }
        });

        dialogLockNote.show();
    }

    private void deleteLockedNote(Note note) {
        note.setLocked(false);
        boolean isFavorite = note.isFavorite();
        if (note.isFavorite())
            note.setFavorite(false);
        note.setTrash(true);
        noteViewModel.updateNote(note);
        Snackbar snackbar = Snackbar.make(drawer, "Move to Trash", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        note.setLocked(true);
                        note.setFavorite(isFavorite);
                        note.setTrash(false);
                        noteViewModel.updateNote(note);
                    }
                });
        snackbar.show();
    }

    private void unLockNote(Note note) {
        note.setLocked(false);
        noteViewModel.updateNote(note);
        if(clickedItemHolder != null)
            clickedItemHolder.imageLocked.setVisibility(View.GONE);
        if(!note.isFavorite())
            clickedItemHolder.imageLayout.setVisibility(View.GONE);
    }
}