package com.example.myapplication.data;

/** A booking or booking request. Times are stored as epoch milliseconds. */
public class Booking {

    public enum Status {
        /** Sent by an employee, not yet approved by a manager. */
        WAITING,
        APPROVED,
        /** Approved, but the employee has asked a manager to cancel it. */
        CANCEL_REQUESTED
    }

    public final long id;
    public final Room room;
    public final long start;
    public final long end;
    public final int people;
    public Status status;

    public Booking(long id, Room room, long start, long end, int people, Status status) {
        this.id = id;
        this.room = room;
        this.start = start;
        this.end = end;
        this.people = people;
        this.status = status;
    }

    /** Two time ranges overlap if each one starts before the other ends. */
    public boolean overlaps(long otherStart, long otherEnd) {
        return start < otherEnd && otherStart < end;
    }
}
