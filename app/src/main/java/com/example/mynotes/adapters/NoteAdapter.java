package com.example.mynotes.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynotes.activities.MainActivity;
import com.example.mynotes.entities.Note;
import com.example.mynotes.listeners.NoteClickListener;
import com.example.mynotes.listeners.NotesListener;
import com.example.mynotes.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class NoteAdapter extends ListAdapter<Note,NoteAdapter.NoteViewHolder> implements Filterable {

    private List<Note> notesAll;
    private final NoteClickListener noteClickListener;
    private final NotesListener notesListener;
    private final Context context;

    public NoteAdapter(Context context, NoteClickListener noteClickListener, NotesListener notesListener) {
        super(diffCallback);
        this.context = context;
        this.noteClickListener = noteClickListener;
        this.notesListener = notesListener;
    }

    public void submit(List<Note> notes) {
        notesAll = notes;
        submitList(notes);
    }

    private static final DiffUtil.ItemCallback<Note> diffCallback = new DiffUtil.ItemCallback<Note>() {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getSubtitle().equals(newItem.getSubtitle()) &&
                    oldItem.getNote_text().equals(newItem.getNote_text()) &&
                    oldItem.getDate_time().equals(newItem.getDate_time()) &&
                    oldItem.getImage_path().equals(newItem.getImage_path()) &&
                    oldItem.getColor().equals(newItem.getColor()) &&
                    oldItem.isFavorite() == newItem.isFavorite() &&
                    oldItem.isLocked() == newItem.isLocked() &&
                    oldItem.isTrash() == newItem.isTrash();
        }
    };


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_item_container, parent, false),
                context);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteViewHolder holder, int position) {

        holder.setNoteText(getItem(holder.getBindingAdapterPosition()));
        holder.layoutNote.setOnClickListener(view -> noteClickListener.onNoteClicked(getItem(holder.getBindingAdapterPosition()),
                holder.getBindingAdapterPosition()));

        holder.layoutNote.setOnLongClickListener(view -> {

            noteClickListener.onNoteLongClicked(notesListener,holder,getItem(holder.getBindingAdapterPosition()),
                    holder.getBindingAdapterPosition());


            return true;
        });
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {

        // run on background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            List<Note> filteredList = new ArrayList<Note>();

            if (charSequence.toString().isEmpty()) {
                filteredList.addAll(notesAll);
            } else {
                String searchKeyWord = charSequence.toString().toLowerCase().trim();
                for (Note note : notesAll) {
                    if (note.getTitle().toLowerCase().contains(searchKeyWord) ||
                            note.getSubtitle().toLowerCase().contains(searchKeyWord) ||
                            note.getNote_text().toLowerCase().contains(searchKeyWord)) {
                        filteredList.add(note);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        // run on UI thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            submitList((List<Note>) filterResults.values);
        }
    };


    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        public TextView textTitle, textSubtitle, textDateTime;
        public ConstraintLayout layoutNote;
        public RoundedImageView imageNote;
        public ImageView imageFavorite;
        public ImageView imageLocked;
        public LinearLayout imageLayout;
        private final Context context;

        public NoteViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.noteLayoutGrid);
            imageNote = itemView.findViewById(R.id.noteImage);
            imageFavorite = itemView.findViewById(R.id.favoriteImage);
            imageLocked = itemView.findViewById(R.id.lockedImage);
            imageLayout = itemView.findViewById(R.id.layoutImage);
        }

        public void setNoteText(Note note) {
            if (note != null) {
                textTitle.setText(note.getTitle());
                textSubtitle.setText(note.getSubtitle());
                textDateTime.setText(note.getDate_time());
                GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
                if (note.getColor() != null) {
                    gradientDrawable.setColor(Color.parseColor(note.getColor()));
                } else {
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

                if (note.getImage_path() != null) {
                    Bitmap imageBitmap = BitmapFactory.decodeFile(note.getImage_path());
                    imageNote.setImageBitmap(imageBitmap);
                    imageNote.setVisibility(View.VISIBLE);
                } else {
                    imageNote.setVisibility(View.GONE);
                }

                if (Objects.equals(getCurrentFragment().getTag(), MainActivity.NOTE_FRAGMENT_TAG)) {
                    if(note.isFavorite() || note.isLocked())
                        imageLayout.setVisibility(View.VISIBLE);
                    else if(!note.isFavorite() && !note.isLocked())
                        imageLayout.setVisibility(View.GONE);

                    if (note.isFavorite())
                        imageFavorite.setVisibility(View.VISIBLE);
                    else
                        imageFavorite.setVisibility(View.GONE);

                    if (note.isLocked())
                        imageLocked.setVisibility(View.VISIBLE);
                    else
                        imageLocked.setVisibility(View.GONE);

                } else if(Objects.equals(getCurrentFragment().getTag(),MainActivity.FAVORITE_FRAGMENT_TAG)){
                    imageFavorite.setVisibility(View.GONE);

                    if (note.isLocked()) {
                        imageLayout.setVisibility(View.VISIBLE);
                        imageLocked.setVisibility(View.VISIBLE);
                    }
                    else {
                        imageLayout.setVisibility(View.GONE);
                        imageLocked.setVisibility(View.GONE);
                    }

                } else if(Objects.equals(getCurrentFragment().getTag(),MainActivity.LOCKED_FRAGMENT_TAG)){
                    imageLocked.setVisibility(View.GONE);

                    if (note.isFavorite()) {
                        imageLayout.setVisibility(View.VISIBLE);
                        imageFavorite.setVisibility(View.VISIBLE);
                    }
                    else {
                        imageLayout.setVisibility(View.GONE);
                        imageFavorite.setVisibility(View.GONE);
                    }
                } else if(Objects.equals(getCurrentFragment().getTag(),MainActivity.TRASH_FRAGMENT_TAG)){
                    imageLayout.setVisibility(View.GONE);
                }

            }

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
}

