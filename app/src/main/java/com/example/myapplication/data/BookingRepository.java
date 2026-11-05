package com.example.myapplication.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * In-memory stand-in for the backend, which Exercise 5 does not require.
 * It holds sample data so every screen has something realistic to show,
 * and it never offers a room that would overlap an existing booking.
 */
public final class BookingRepository {

    public interface Listener {
        void onBookingsChanged();
    }

    public static final long HOUR = 60L * 60L * 1000L;
    private static final long DAY = 24L * HOUR;

    private static BookingRepository instance;

    private final List<Room> rooms = new ArrayList<>();
    /** Other people's bookings. They block rooms but are not shown to this employee. */
    private final List<Booking> others = new ArrayList<>();
    private final List<Booking> mine = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();
    private long nextId = 1;

    public static synchronized BookingRepository get() {
        if (instance == null) {
            instance = new BookingRepository();
        }
        return instance;
    }

    private BookingRepository() {
        rooms.add(new Room("MR1", "Meeting Room 1", Room.Type.MEETING, 6));
        rooms.add(new Room("MR2", "Meeting Room 2", Room.Type.MEETING, 8));
        rooms.add(new Room("MR3", "Meeting Room 3", Room.Type.MEETING, 8));
        rooms.add(new Room("MR4", "Meeting Room 4", Room.Type.MEETING, 10));
        rooms.add(new Room("MR5", "Meeting Room 5", Room.Type.MEETING, 4));
        rooms.add(new Room("SR1", "Seminar Room 1", Room.Type.SEMINAR, 30));
        rooms.add(new Room("SR2", "Seminar Room 2", Room.Type.SEMINAR, 40));

        long slot = nextFreeSlotStart();
        others.add(new Booking(nextId++, room("MR2"), slot - HOUR, slot + HOUR, 6, Booking.Status.APPROVED));
        others.add(new Booking(nextId++, room("SR1"), slot, slot + HOUR, 20, Booking.Status.APPROVED));
        others.add(new Booking(nextId++, room("MR4"), slot + 2 * HOUR, slot + 3 * HOUR, 5, Booking.Status.APPROVED));

        mine.add(new Booking(nextId++, room("SR1"), slot + DAY, slot + DAY + 2 * HOUR, 12, Booking.Status.APPROVED));
        mine.add(new Booking(nextId++, room("MR3"), slot - 2 * DAY, slot - 2 * DAY + HOUR, 3, Booking.Status.APPROVED));
    }

    /**
     * The start of the next whole hour in working hours (08:00 to 17:00).
     * Used to pre-fill the search so most people only need to press Search.
     */
    public static long nextFreeSlotStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.HOUR_OF_DAY, 1);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour < 8) {
            c.set(Calendar.HOUR_OF_DAY, 9);
        } else if (hour > 17) {
            c.add(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 9);
        }
        return c.getTimeInMillis();
    }

    private Room room(String code) {
        for (Room r : rooms) {
            if (r.code.equals(code)) {
                return r;
            }
        }
        throw new IllegalArgumentException("No room " + code);
    }

    /** Rooms big enough, of the right type and free for the whole time. type may be null for any. */
    public List<Room> getAvailableRooms(long start, long end, int people, Room.Type type) {
        List<Room> result = new ArrayList<>();
        for (Room r : rooms) {
            if (r.capacity >= people && (type == null || r.type == type) && !isTaken(r, start, end)) {
                result.add(r);
            }
        }
        return result;
    }

    private boolean isTaken(Room room, long start, long end) {
        for (Booking b : others) {
            if (b.room == room && b.overlaps(start, end)) {
                return true;
            }
        }
        // A waiting request also holds the room, so two people cannot ask for the same slot.
        for (Booking b : mine) {
            if (b.room == room && b.overlaps(start, end)) {
                return true;
            }
        }
        return false;
    }

    public Booking requestBooking(Room room, long start, long end, int people) {
        Booking b = new Booking(nextId++, room, start, end, people, Booking.Status.WAITING);
        mine.add(b);
        notifyListeners();
        return b;
    }

    /** Used by Undo on the "Request sent" message. */
    public void remove(Booking booking) {
        mine.remove(booking);
        notifyListeners();
    }

    /** A waiting request is cancelled at once; an approved booking needs a manager to confirm. */
    public void cancel(Booking booking) {
        if (booking.status == Booking.Status.WAITING) {
            mine.remove(booking);
        } else if (booking.status == Booking.Status.APPROVED) {
            booking.status = Booking.Status.CANCEL_REQUESTED;
        }
        notifyListeners();
    }

    /** Used by Undo after a cancellation. */
    public void undoCancel(Booking booking, Booking.Status previous) {
        if (!mine.contains(booking)) {
            mine.add(booking);
        }
        booking.status = previous;
        notifyListeners();
    }

    public List<Booking> getUpcoming(long now) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : mine) {
            if (b.end > now) {
                result.add(b);
            }
        }
        Collections.sort(result, (a, b) -> Long.compare(a.start, b.start));
        return result;
    }

    public List<Booking> getPast(long now) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : mine) {
            if (b.end <= now) {
                result.add(b);
            }
        }
        Collections.sort(result, (a, b) -> Long.compare(b.start, a.start));
        return result;
    }

    public int countWaiting() {
        int n = 0;
        for (Booking b : mine) {
            if (b.status == Booking.Status.WAITING) {
                n++;
            }
        }
        return n;
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        for (Listener l : new ArrayList<>(listeners)) {
            l.onBookingsChanged();
        }
    }
}
