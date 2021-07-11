package com.example.mynotes.viewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mynotes.entities.Note;
import com.example.mynotes.repositories.NoteRepository;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private final NoteRepository noteRepository;
    private final LiveData<List<Note>> notesList;
    private final LiveData<List<Note>> favoriteList;
    private final LiveData<List<Note>> lockedList;
    private final LiveData<List<Note>> trashList;

    public NoteViewModel(Application application){
        super(application);
        noteRepository = new NoteRepository(application);
        notesList = noteRepository.getAllNotes();
        favoriteList = noteRepository.getFavoriteNotes();
        lockedList = noteRepository.getLockedNotes();
        trashList = noteRepository.getTrashNotes();
    }

    public void insertNote(Note note){
        noteRepository.insertNote(note);
    }

    public void updateNote(Note note){
        noteRepository.updateNote(note);
    }

    public void deleteNote(Note note){
        noteRepository.deleteNote(note);
    }

    public LiveData<List<Note>> getAllNotes(){
        return notesList;
    }

    public LiveData<List<Note>> getFavoriteNotes(){
        return favoriteList;
    }

    public LiveData<List<Note>> getLockedNotes(){ return lockedList;}

    public LiveData<List<Note>> getTrashNotes(){
        return trashList;
    }
}
