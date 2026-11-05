package com.example.myapplication.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Room;

import java.util.ArrayList;
import java.util.List;

/** Room cards for E2. Tapping a card opens the request dialog (E3). */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.Holder> {

    public interface OnRoomClick {
        void onRoomClick(Room room);
    }

    private final List<Room> rooms = new ArrayList<>();
    private final OnRoomClick listener;

    public RoomAdapter(OnRoomClick listener) {
        this.listener = listener;
    }

    public void setRooms(List<Room> newRooms) {
        rooms.clear();
        rooms.addAll(newRooms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Room room = rooms.get(position);
        Context c = h.itemView.getContext();
        String capacity = c.getResources().getQuantityString(R.plurals.up_to_people, room.capacity, room.capacity);
        h.name.setText(room.name);
        h.capacity.setText(capacity);
        StatusStyle.apply(h.status, R.color.status_ok_bg, R.color.status_ok_fg);
        // TalkBack reads the card as one item instead of three separate pieces of text.
        h.itemView.setContentDescription(c.getString(R.string.room_card_cd, room.name, capacity));
        h.itemView.setOnClickListener(v -> listener.onRoomClick(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView capacity;
        final TextView status;

        Holder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.room_name);
            capacity = v.findViewById(R.id.room_capacity);
            status = v.findViewById(R.id.room_status);
        }
    }
}
