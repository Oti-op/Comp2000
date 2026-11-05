package com.example.myapplication.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Booking;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Booking cards for E4. Every upcoming booking has the same single Cancel button (change C4). */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.Holder> {

    public interface OnCancelClick {
        void onCancelClick(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final OnCancelClick listener;
    private boolean past;

    public BookingAdapter(OnCancelClick listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> newBookings, boolean isPast) {
        bookings.clear();
        bookings.addAll(newBookings);
        past = isPast;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Booking b = bookings.get(position);
        Context c = h.itemView.getContext();
        h.room.setText(b.room.name);
        h.time.setText(c.getString(R.string.search_summary,
                Formats.range(c, b.start, b.end), Formats.people(c, b.people)));

        int note = 0;
        if (past) {
            setStatus(h.status, R.string.status_completed, R.drawable.ic_check,
                    R.color.status_neutral_bg, R.color.status_neutral_fg);
        } else if (b.status == Booking.Status.WAITING) {
            // Change C2: say what "waiting" means and what happens next.
            setStatus(h.status, R.string.status_waiting, R.drawable.ic_hourglass,
                    R.color.status_wait_bg, R.color.status_wait_fg);
            note = R.string.note_waiting;
        } else if (b.status == Booking.Status.CANCEL_REQUESTED) {
            setStatus(h.status, R.string.status_cancel_requested, R.drawable.ic_hourglass,
                    R.color.status_neutral_bg, R.color.status_neutral_fg);
            note = R.string.note_cancel_requested;
        } else {
            setStatus(h.status, R.string.status_approved, R.drawable.ic_check,
                    R.color.status_ok_bg, R.color.status_ok_fg);
        }

        h.note.setVisibility(note == 0 ? View.GONE : View.VISIBLE);
        if (note != 0) {
            h.note.setText(note);
        }

        boolean canCancel = !past && b.status != Booking.Status.CANCEL_REQUESTED;
        h.cancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        h.cancel.setContentDescription(c.getString(R.string.cancel_booking_cd, b.room.name));
        h.cancel.setOnClickListener(v -> listener.onCancelClick(b));
    }

    private static void setStatus(TextView pill, int text, int icon, int bg, int fg) {
        pill.setText(text);
        pill.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        StatusStyle.apply(pill, bg, fg);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView room;
        final TextView status;
        final TextView time;
        final TextView note;
        final MaterialButton cancel;

        Holder(@NonNull View v) {
            super(v);
            room = v.findViewById(R.id.booking_room);
            status = v.findViewById(R.id.booking_status);
            time = v.findViewById(R.id.booking_time);
            note = v.findViewById(R.id.booking_note);
            cancel = v.findViewById(R.id.booking_cancel);
        }
    }
}
