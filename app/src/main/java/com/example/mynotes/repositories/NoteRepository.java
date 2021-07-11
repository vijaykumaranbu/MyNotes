package com.example.mynotes.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.mynotes.dao.NoteDao;
import com.example.mynotes.database.NoteDatabase;
import com.example.mynotes.entities.Note;
import java.util.List;

public class NoteRepository {
    private NoteDao noteDao;
    private LiveData<List<Note>> notesList;
    private LiveData<List<Note>> favoriteList;
    private LiveData<List<Note>> lockedList;
    private LiveData<List<Note>> trashList;

    public NoteRepository(Application application){
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);
        noteDao = noteDatabase.noteDao();
        notesList = noteDao.getAllNotes();
        favoriteList = noteDao.getFavoriteNotes();
        lockedList = noteDao.getLockedNotes();
        trashList = noteDao.getTrashNotes();
    }

    public void insertNote(Note note){
        new InsertAsyncTask(noteDao).execute(note);
    }

    public void updateNote(Note note){
        new UpdateAsyncTask(noteDao).execute(note);
    }

    public void deleteNote(Note note){
        new DeleteAsyncTask(noteDao).execute(note);
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

    private static class InsertAsyncTask extends AsyncTask<Note,Void,Void>{
        private NoteDao noteDao;

        public InsertAsyncTask(NoteDao noteDao){
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.insertNote(notes[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Note,Void,Void>{
        private NoteDao noteDao;

        public UpdateAsyncTask(NoteDao noteDao){
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.updateNote(notes[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Note,Void,Void>{
        private NoteDao noteDao;

        public DeleteAsyncTask(NoteDao noteDao){
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.deleteNote(notes[0]);
            return null;
        }
    }
}
