package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.data.Booking;
import com.example.myapplication.data.BookingRepository;
import com.example.myapplication.data.Room;
import com.example.myapplication.ui.AvailableRoomsFragment;
import com.example.myapplication.ui.FindRoomFragment;
import com.example.myapplication.ui.MyBookingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

/**
 * Hosts the employee screens. Portrait uses a bottom navigation bar and landscape a
 * navigation rail; both share the id "nav", so this class only sees a NavigationBarView.
 */
public class MainActivity extends AppCompatActivity implements BookingRepository.Listener {

    private View root;
    private NavigationBarView nav;
    private final BookingRepository repo = BookingRepository.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nav = findViewById(R.id.nav);
        nav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        // Tapping the current tab again goes back to its first screen.
        nav.setOnItemReselectedListener(item ->
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE));

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateUpButton);
        if (savedInstanceState == null) {
            showTab(R.id.nav_find);
        }
        updateUpButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        repo.addListener(this);
        updateBadge();
    }

    @Override
    protected void onStop() {
        repo.removeListener(this);
        super.onStop();
    }

    @Override
    public void onBookingsChanged() {
        updateBadge();
    }

    private void showTab(int itemId) {
        Fragment fragment = itemId == R.id.nav_bookings ? new MyBookingsFragment() : new FindRoomFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, fragment)
                .commit();
    }

    /** Switches tab as if the user had tapped it, so the bar always matches the screen. */
    public void selectTab(int itemId) {
        nav.setSelectedItemId(itemId);
    }

    /** E1 to E2. Added to the back stack so Back and the toolbar arrow return to the search. */
    public void showAvailableRooms(long start, long end, int people, Room.Type type) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, AvailableRoomsFragment.newInstance(start, end, people, type))
                .addToBackStack(null)
                .commit();
    }

    /** After E3: show the request on My bookings and confirm it with an Undo option. */
    public void onRequestSent(Booking booking) {
        selectTab(R.id.nav_bookings);
        showMessage(getString(R.string.request_sent), () -> repo.remove(booking));
    }

    public void showMessage(String message, Runnable undo) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        if (undo != null) {
            snackbar.setAction(R.string.undo, v -> undo.run());
        }
        if (nav instanceof BottomNavigationView) {
            snackbar.setAnchorView(nav);
        }
        snackbar.show();
    }

    /** The badge on My bookings counts requests still waiting for a manager. */
    private void updateBadge() {
        int waiting = repo.countWaiting();
        BadgeDrawable badge = nav.getOrCreateBadge(R.id.nav_bookings);
        badge.setNumber(waiting);
        badge.setVisible(waiting > 0);
    }

    private void updateUpButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(
                    getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.sign_out_confirm)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.sign_out, (d, w) -> {
                        startActivity(new Intent(this, SignInActivity.class));
                        finish();
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
