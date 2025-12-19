package com.example.offlinenav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Hide action bar on menu page
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button btnNavigation = findViewById(R.id.btn_menu_navigation);
        Button btnFavorites = findViewById(R.id.btn_menu_favorites);
        Button btnSupport = findViewById(R.id.btn_menu_support);
        Button btnAbout = findViewById(R.id.btn_menu_about);

        btnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, FavoritesActivity.class));
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, SupportCallActivity.class));
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

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

    @Override
    public void onBackPressed() {
        // Exit app when back is pressed on menu
        finishAffinity();
    }
}
