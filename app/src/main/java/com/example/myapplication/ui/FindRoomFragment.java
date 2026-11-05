package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.BookingRepository;
import com.example.myapplication.data.Room;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * E1 Find a room. Pre-filled with the next free hour so most searches are one tap.
 * The date and time fields open Material pickers instead of accepting typed text,
 * which removes a whole class of input errors.
 */
public class FindRoomFragment extends Fragment {

    private static final String KEY_START = "start";
    private static final String KEY_END = "end";
    private static final String KEY_PEOPLE = "people";
    private static final String TAG_DATE = "date_picker";
    private static final String TAG_START = "start_picker";
    private static final String TAG_END = "end_picker";
    /** The largest room holds 40, so there is no point asking for more. */
    private static final int MAX_PEOPLE = 40;

    private long start;
    private long end;
    private int people = 1;
    private boolean initialised;

    private TextInputEditText dateField;
    private TextInputEditText startField;
    private TextInputEditText endField;
    private TextInputLayout startLayout;
    private TextInputLayout endLayout;
    private TextView peopleCount;
    private MaterialButton minus;
    private MaterialButton plus;
    private ChipGroup typeGroup;
    private MaterialButton search;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            start = savedInstanceState.getLong(KEY_START);
            end = savedInstanceState.getLong(KEY_END);
            people = savedInstanceState.getInt(KEY_PEOPLE);
            initialised = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!initialised) {
            // Only on first open. Coming back from E2, or after rotating, keeps what the user chose.
            start = BookingRepository.nextFreeSlotStart();
            end = start + BookingRepository.HOUR;
            initialised = true;
        }

        dateField = view.findViewById(R.id.date);
        startField = view.findViewById(R.id.start);
        endField = view.findViewById(R.id.end);
        startLayout = view.findViewById(R.id.start_layout);
        endLayout = view.findViewById(R.id.end_layout);
        peopleCount = view.findViewById(R.id.people_count);
        minus = view.findViewById(R.id.people_minus);
        plus = view.findViewById(R.id.people_plus);
        typeGroup = view.findViewById(R.id.type_group);
        search = view.findViewById(R.id.search);

        // The field and its icon both open the picker, so either is a valid target.
        View.OnClickListener openDate = v -> showDatePicker();
        View.OnClickListener openStart = v -> showTimePicker(true);
        View.OnClickListener openEnd = v -> showTimePicker(false);
        dateField.setOnClickListener(openDate);
        ((TextInputLayout) view.findViewById(R.id.date_layout)).setEndIconOnClickListener(openDate);
        startField.setOnClickListener(openStart);
        startLayout.setEndIconOnClickListener(openStart);
        endField.setOnClickListener(openEnd);
        endLayout.setEndIconOnClickListener(openEnd);

        minus.setOnClickListener(v -> {
            people = Math.max(1, people - 1);
            render();
        });
        plus.setOnClickListener(v -> {
            people = Math.min(MAX_PEOPLE, people + 1);
            render();
        });

        search.setOnClickListener(v -> {
            if (isValid()) {
                ((MainActivity) requireActivity()).showAvailableRooms(start, end, people, selectedType());
            }
        });

        reattachPickers();
        render();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.nav_find);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_START, start);
        outState.putLong(KEY_END, end);
        outState.putInt(KEY_PEOPLE, people);
    }

    private void render() {
        dateField.setText(Formats.day(start));
        startField.setText(Formats.time(start));
        endField.setText(Formats.time(end));
        peopleCount.setText(String.valueOf(people));
        peopleCount.setContentDescription(Formats.people(requireContext(), people));
        // Disable rather than hide the buttons at the limits, so the layout never jumps.
        minus.setEnabled(people > 1);
        plus.setEnabled(people < MAX_PEOPLE);
        search.setEnabled(isValid());
    }

    /** Errors appear under the field that needs changing, and Search stays disabled until fixed. */
    private boolean isValid() {
        // A minute of slack so a slot that starts "now" is still allowed.
        boolean startOk = start >= System.currentTimeMillis() - 60_000L;
        boolean endOk = end > start;
        startLayout.setError(startOk ? null : getString(R.string.error_start_past));
        endLayout.setError(endOk ? null : getString(R.string.error_end_before_start));
        return startOk && endOk;
    }

    @Nullable
    private Room.Type selectedType() {
        int id = typeGroup.getCheckedChipId();
        if (id == R.id.chip_meeting) {
            return Room.Type.MEETING;
        } else if (id == R.id.chip_seminar) {
            return Room.Type.SEMINAR;
        }
        return null;
    }

    // ---- Pickers ----

    private void showDatePicker() {
        FragmentManager fm = getChildFragmentManager();
        if (fm.findFragmentByTag(TAG_DATE) != null) {
            return;
        }
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_date)
                .setSelection(toUtcMidnight(start))
                .setCalendarConstraints(constraints)
                .build();
        attachDateListener(picker);
        picker.show(fm, TAG_DATE);
    }

    private void showTimePicker(boolean isStart) {
        FragmentManager fm = getChildFragmentManager();
        String tag = isStart ? TAG_START : TAG_END;
        if (fm.findFragmentByTag(tag) != null) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(isStart ? start : end);
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(c.get(Calendar.HOUR_OF_DAY))
                .setMinute(c.get(Calendar.MINUTE))
                .setTitleText(isStart ? R.string.pick_start : R.string.pick_end)
                .build();
        attachTimeListener(picker, isStart);
        picker.show(fm, tag);
    }

    /** Pickers survive rotation but their listeners do not, so they are added back here. */
    @SuppressWarnings("unchecked")
    private void reattachPickers() {
        FragmentManager fm = getChildFragmentManager();
        Fragment date = fm.findFragmentByTag(TAG_DATE);
        if (date instanceof MaterialDatePicker) {
            attachDateListener((MaterialDatePicker<Long>) date);
        }
        Fragment startPicker = fm.findFragmentByTag(TAG_START);
        if (startPicker instanceof MaterialTimePicker) {
            attachTimeListener((MaterialTimePicker) startPicker, true);
        }
        Fragment endPicker = fm.findFragmentByTag(TAG_END);
        if (endPicker instanceof MaterialTimePicker) {
            attachTimeListener((MaterialTimePicker) endPicker, false);
        }
    }

    private void attachDateListener(MaterialDatePicker<Long> picker) {
        picker.addOnPositiveButtonClickListener(selection -> {
            // The picker works in UTC; copy the chosen day onto the local start time.
            Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utc.setTimeInMillis(selection);
            Calendar local = Calendar.getInstance();
            local.setTimeInMillis(start);
            local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));
            long length = end - start;
            start = local.getTimeInMillis();
            end = start + length;
            render();
        });
    }

    private void attachTimeListener(MaterialTimePicker picker, boolean isStart) {
        picker.addOnPositiveButtonClickListener(v -> {
            long chosen = withTime(start, picker.getHour(), picker.getMinute());
            if (isStart) {
                // Moving the start keeps the same length, which is what people usually mean.
                long length = end - start;
                start = chosen;
                end = start + (length > 0 ? length : BookingRepository.HOUR);
            } else {
                end = chosen;
            }
            render();
        });
    }

    private static long withTime(long day, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static long toUtcMidnight(long localTime) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(localTime);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));
        return utc.getTimeInMillis();
    }
}
