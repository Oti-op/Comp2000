# Room Booking App (Employee)

An Android app for booking shared office rooms. This version covers the **Employee** side: finding a free room, sending a booking request and managing your bookings.

Built for Assessment 1 (Design Exercises), Exercise 5.

## What you can do

- **Sign in** with a work email and password
- **Find a room** by date, time, number of people and room type
- **See only rooms that are free** for that time and big enough for your group
- **Send a booking request** for a manager to approve
- **Check your bookings**, with a clear status: Waiting for approval, Approved or Cancellation requested
- **Cancel** a booking, with Undo if you change your mind

## How to run it

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync.
3. Run it on an emulator or phone (Android 7.0 or newer).
4. Sign in with any email, such as `employee@company.com`, and any password.

There is no backend. The app uses sample rooms and bookings stored in memory, so everything resets when the app closes.

## Rooms

| Room | Type | Capacity |
| --- | --- | --- |
| Meeting Room 1 to 5 | Meeting | 4 to 10 |
| Seminar Room 1 and 2 | Seminar | 30 and 40 |

## Screen sizes

- **Portrait:** bottom navigation bar, one column of rooms
- **Landscape:** navigation rail on the left, two columns
- **Tablet:** wider spacing, and up to three columns in landscape
- Dark mode is supported

## Project structure

```
app/src/main/
├── java/com/example/myapplication/
│   ├── SignInActivity.java       Sign in screen
│   ├── MainActivity.java         Toolbar and navigation
│   ├── data/                     Rooms, bookings and sample data
│   └── ui/                       Find a room, Available rooms, My bookings
└── res/
    ├── layout/                   Portrait layouts
    ├── layout-land/              Landscape layouts
    └── values*/                  Text, colours, themes and column counts
```

## Built with

Java, XML layouts and Material Design 3 components (bottom navigation, navigation rail, date and time pickers, cards, chips, dialogs and snackbars).
