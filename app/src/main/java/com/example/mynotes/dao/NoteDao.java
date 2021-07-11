package com.example.mynotes.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mynotes.entities.Note;

import java.util.List;

@Dao
public interface NoteDao{

//    @Query("SELECT * FROM notes ORDER BY id DESC")
    @Query("SELECT * FROM notes WHERE trash LIKE 0 ORDER BY id DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE id LIKE :id")
    Note getNote(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Query("SELECT * FROM notes WHERE favorite LIKE 1 ORDER BY id DESC")
    LiveData<List<Note>> getFavoriteNotes();

    @Query("SELECT * FROM notes WHERE trash LIKE 1 ORDER BY id DESC")
    LiveData<List<Note>> getTrashNotes();

    @Query("SELECT * FROM notes WHERE locked LIKE 1 ORDER BY id DESC")
    LiveData<List<Note>> getLockedNotes();

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);
}
