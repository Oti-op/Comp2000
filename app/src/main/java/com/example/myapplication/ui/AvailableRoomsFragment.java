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
import com.example.myapplication.data.Room;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/** E2 Available rooms. Employees only ever see rooms that are free for the time they chose. */
public class AvailableRoomsFragment extends Fragment {

    private static final String ARG_START = "start";
    private static final String ARG_END = "end";
    private static final String ARG_PEOPLE = "people";
    private static final String ARG_TYPE = "type";

    private long start;
    private long end;
    private int people;
    @Nullable
    private Room.Type type;

    private RoomAdapter adapter;
    private View empty;

    public static AvailableRoomsFragment newInstance(long start, long end, int people, @Nullable Room.Type type) {
        Bundle args = new Bundle();
        args.putLong(ARG_START, start);
        args.putLong(ARG_END, end);
        args.putInt(ARG_PEOPLE, people);
        args.putString(ARG_TYPE, type == null ? null : type.name());
        AvailableRoomsFragment f = new AvailableRoomsFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = requireArguments();
        start = args.getLong(ARG_START);
        end = args.getLong(ARG_END);
        people = args.getInt(ARG_PEOPLE);
        String typeName = args.getString(ARG_TYPE);
        type = typeName == null ? null : Room.Type.valueOf(typeName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_available_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView summary = view.findViewById(R.id.summary);
        summary.setText(getString(R.string.search_summary,
                Formats.range(requireContext(), start, end), Formats.people(requireContext(), people)));

        View.OnClickListener back = v -> getParentFragmentManager().popBackStack();
        view.findViewById(R.id.edit).setOnClickListener(back);
        view.findViewById(R.id.empty_action).setOnClickListener(back);
        empty = view.findViewById(R.id.empty);

        RecyclerView list = view.findViewById(R.id.list);
        // One column on a phone in portrait, more when there is room (see res/values-*/layout_config.xml).
        list.setLayoutManager(new GridLayoutManager(requireContext(),
                getResources().getInteger(R.integer.room_columns)));
        adapter = new RoomAdapter(this::confirmRequest);
        list.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.title_available);
        List<Room> rooms = BookingRepository.get().getAvailableRooms(start, end, people, type);
        adapter.setRooms(rooms);
        empty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** E3: the dialog says plainly that this is a request, not a booking (change C1). */
    private void confirmRequest(Room room) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.request_title)
                .setMessage(getString(R.string.request_message, room.name,
                        Formats.range(requireContext(), start, end), Formats.people(requireContext(), people)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.send_request, (dialog, which) -> {
                    Booking booking = BookingRepository.get().requestBooking(room, start, end, people);
                    ((MainActivity) requireActivity()).onRequestSent(booking);
                })
                .show();
    }
}
