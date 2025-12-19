/**
 * FavoritesActivity - Manages user's saved favorite locations
 *
 * This activity displays a list of saved locations with the following features:
 * - View all favorite locations with names, addresses, and coordinates
 * - Navigate to any favorite location (opens MainActivity with destination set)
 * - Delete favorite locations with confirmation dialog
 * - Empty state when no favorites exist
 * - Integration with main navigation menu
 *
 * Uses SQLite database through FavoritesDbHelper for data persistence.
 */
package com.example.offlinenav;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class FavoritesActivity extends AppCompatActivity {

    // Database and UI components
    private FavoritesDbHelper dbHelper;           // Database helper for favorites
    private ListView listView;                    // List view for displaying favorites
    private TextView emptyView;                   // Empty state message
    private FavoritesCursorAdapter adapter;       // Custom cursor adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Favorite Locations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new FavoritesDbHelper(this);
        listView = findViewById(R.id.lv_favorites);
        emptyView = findViewById(R.id.tv_empty_favorites);

        loadFavorites();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_navigation) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_support) {
            Intent intent = new Intent(this, SupportCallActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_favorites) {
            Toast.makeText(this, "Already on Favorites", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_about) {
            showAboutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage("OfflineNavApp v1.2\n\nFavorite Locations:\n• Save important places\n• Quick navigation\n• Tap Delete to remove\n\n© 2025 OfflineNav")
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Load and display all favorite locations from database
     *
     * Queries the favorites database and populates the list view.
     * Shows empty state message if no favorites exist.
     * Orders results by timestamp (newest first).
     */
    private void loadFavorites() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query favorites ordered by timestamp (newest first)
        Cursor c = db.query(FavoritesDbHelper.TABLE_NAME, null, null, null, null, null,
                           FavoritesDbHelper.COL_TIMESTAMP + " DESC");

        if (c.getCount() == 0) {
            // Show empty state
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            // Show list with favorites
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            adapter = new FavoritesCursorAdapter(this, c);
            listView.setAdapter(adapter);
        }
    }

    /**
     * Custom CursorAdapter for displaying favorite locations
     *
     * Handles the display of each favorite item in the list view,
     * including name, address, coordinates, and action buttons
     * for navigation and deletion.
     */
    private class FavoritesCursorAdapter extends CursorAdapter {

        public FavoritesCursorAdapter(android.content.Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(android.content.Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        }

        /**
         * Bind cursor data to list item view
         *
         * Populates each list item with favorite location data and sets up
         * click listeners for navigation and deletion actions.
         */
        @Override
        public void bindView(View view, android.content.Context context, Cursor cursor) {
            // Get UI elements from the item layout
            TextView nameView = view.findViewById(R.id.item_fav_name);
            TextView addressView = view.findViewById(R.id.item_fav_address);
            TextView coordsView = view.findViewById(R.id.item_fav_coords);
            Button navigateButton = view.findViewById(R.id.btn_navigate_to);
            Button deleteButton = view.findViewById(R.id.btn_delete_favorite);

            // Extract data from cursor
            final long id = cursor.getLong(cursor.getColumnIndexOrThrow(FavoritesDbHelper.COL_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDbHelper.COL_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDbHelper.COL_ADDRESS));
            final double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(FavoritesDbHelper.COL_LATITUDE));
            final double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(FavoritesDbHelper.COL_LONGITUDE));

            // Populate UI elements
            nameView.setText(name);
            addressView.setText(address);
            coordsView.setText(String.format("%.4f, %.4f", lat, lon));

            // Navigate button - opens MainActivity with this location as destination
            navigateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                    intent.putExtra("latitude", lat);
                    intent.putExtra("longitude", lon);
                    intent.putExtra("name", name);
                    intent.putExtra("navigate", true);  // Flag to trigger navigation
                    startActivity(intent);
                }
            });

            // Delete button - removes favorite with confirmation dialog
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new android.app.AlertDialog.Builder(FavoritesActivity.this)
                        .setTitle("Delete Favorite")
                        .setMessage("Remove '" + name + "' from favorites?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Delete from database
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.delete(FavoritesDbHelper.TABLE_NAME,
                                     FavoritesDbHelper.COL_ID + "=?",
                                     new String[]{String.valueOf(id)});
                            // Refresh the list
                            loadFavorites();
                            Toast.makeText(FavoritesActivity.this, "Favorite removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                }
            });
        }
    }
}
