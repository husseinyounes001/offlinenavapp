/**
 * MenuActivity - Main menu screen for OfflineNavApp
 *
 * This activity serves as the app's home screen, providing navigation buttons
 * to access different features:
 * - Navigation: Main map and routing interface
 * - Favorites: Saved locations management
 * - Support: Emergency call functionality
 * - About: App information and features
 *
 * The menu hides the action bar for a clean interface and overrides
 * back button behavior to exit the app completely.
 */
package com.example.offlinenav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    /**
     * Initialize the menu activity and set up navigation buttons
     *
     * Sets up the main menu interface with buttons for different app features.
     * Hides the action bar for a clean menu appearance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Hide action bar for clean menu interface
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize menu buttons
        Button btnNavigation = findViewById(R.id.btn_menu_navigation);
        Button btnFavorites = findViewById(R.id.btn_menu_favorites);
        Button btnSupport = findViewById(R.id.btn_menu_support);
        Button btnAbout = findViewById(R.id.btn_menu_about);

        // Navigation button - opens main map interface
        btnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        // Favorites button - opens saved locations management
        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, FavoritesActivity.class));
            }
        });

        // Support button - opens emergency call interface
        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, SupportCallActivity.class));
            }
        });

        // About button - shows app information dialog
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

    /**
     * Display app information dialog with features and version details
     *
     * Shows a comprehensive overview of the app's capabilities,
     * version information, and Lebanon-specific optimizations.
     */
    private void showAboutDialog() {
        String message = "OfflineNavApp v1.2\n" +
                        "Lebanon Edition\n\n" +
                        "Complete Features:\n" +
                        "• Offline Map Navigation\n" +
                        "• GPS Location Tracking\n" +
                        "• Location Search\n" +
                        "• Route Planning\n" +
                        "• Favorite Locations\n" +
                        "• Emergency Support Calls\n\n" +
                        "Optimized for Lebanon\n" +
                        "Default Location: Beirut\n\n" +
                        "© 2025 OfflineNav";

        new android.app.AlertDialog.Builder(this)
            .setTitle("About OfflineNavApp")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Handle back button press - exit the application completely
     *
     * On the main menu, pressing back should exit the app rather than
     * navigating to a previous activity (since this is the home screen).
     */
    @Override
    public void onBackPressed() {
        // Exit app when back is pressed on menu
        finishAffinity();
    }
}
