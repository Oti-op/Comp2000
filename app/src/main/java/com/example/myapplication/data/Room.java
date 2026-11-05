package com.example.myapplication.data;

/** One bookable room. The app has five meeting rooms and two seminar rooms. */
public class Room {

    public enum Type { MEETING, SEMINAR }

    public final String code;
    public final String name;
    public final Type type;
    public final int capacity;

    public Room(String code, String name, Type type, int capacity) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }
}
