package com.example.mynotes.listeners;

import com.example.mynotes.entities.Note;

public interface AlertDialogInterface {

    void onShowDialogLockedNote(boolean isDelete,boolean isUnlock, Note note);
}
