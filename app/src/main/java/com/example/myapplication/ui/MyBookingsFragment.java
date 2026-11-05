package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.Booking;
import com.example.myapplication.data.BookingRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/** E4 My bookings, with the E5 cancel dialog. */
public class MyBookingsFragment extends Fragment implements BookingRepository.Listener {

    private static final String KEY_TAB = "tab";

    private final BookingRepository repo = BookingRepository.get();
    private BookingAdapter adapter;
    private TabLayout tabs;
    private View empty;
    private TextView emptyText;
    private View emptyAction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabs = view.findViewById(R.id.tabs);
        empty = view.findViewById(R.id.empty);
        emptyText = view.findViewById(R.id.empty_text);
        emptyAction = view.findViewById(R.id.empty_action);
        emptyAction.setOnClickListener(v -> ((MainActivity) requireActivity()).selectTab(R.id.nav_find));

        RecyclerView list = view.findViewById(R.id.list);
        list.setLayoutManager(new GridLayoutManager(requireContext(),
                getResources().getInteger(R.integer.booking_columns)));
        adapter = new BookingAdapter(this::confirmCancel);
        list.setAdapter(adapter);

        if (savedInstanceState != null) {
            TabLayout.Tab tab = tabs.getTabAt(savedInstanceState.getInt(KEY_TAB, 0));
            if (tab != null) {
                tab.select();
            }
        }
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                render();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        repo.addListener(this);
        render();
    }

    @Override
    public void onStop() {
        repo.removeListener(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.nav_bookings);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tabs != null) {
            outState.putInt(KEY_TAB, tabs.getSelectedTabPosition());
        }
    }

    @Override
    public void onBookingsChanged() {
        render();
    }

    private void render() {
        boolean past = tabs.getSelectedTabPosition() == 1;
        long now = System.currentTimeMillis();
        List<Booking> bookings = past ? repo.getPast(now) : repo.getUpcoming(now);
        adapter.setBookings(bookings, past);
        empty.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
        emptyText.setText(past ? R.string.empty_past : R.string.empty_upcoming);
        emptyAction.setVisibility(past ? View.GONE : View.VISIBLE);
    }

    /**
     * E5. One Cancel button for everything, but the dialog explains the two outcomes:
     * a waiting request goes at once, an approved booking needs a manager (change C4).
     */
    private void confirmCancel(Booking booking) {
        boolean waiting = booking.status == Booking.Status.WAITING;
        String when = Formats.range(requireContext(), booking.start, booking.end);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(waiting ? R.string.cancel_request_title : R.string.ask_cancel_title)
                .setMessage(getString(waiting ? R.string.cancel_request_message : R.string.ask_cancel_message,
                        booking.room.name, when))
                .setNegativeButton(waiting ? R.string.keep_request : R.string.keep_booking, null)
                .setPositiveButton(waiting ? R.string.cancel_request_confirm : R.string.ask_cancel_confirm,
                        (dialog, which) -> {
                            Booking.Status previous = booking.status;
                            repo.cancel(booking);
                            ((MainActivity) requireActivity()).showMessage(
                                    getString(waiting ? R.string.request_cancelled : R.string.cancel_sent),
                                    () -> repo.undoCancel(booking, previous));
                        })
                .show();
    }
}
