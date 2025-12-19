/**
 * SupportCallActivity - Emergency support call interface
 *
 * This activity provides emergency calling functionality with:
 * - Quick support button for predefined emergency number
 * - Custom phone number input for any contact
 * - Call history tracking and display
 * - Phone permission handling
 * - Integration with main navigation menu
 *
 * All calls are logged to a local database for reference.
 */
package com.example.offlinenav;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SupportCallActivity extends AppCompatActivity {

    // Constants for phone permissions and default numbers
    private static final int REQUEST_CALL_PHONE = 1001;
    private static final String SUPPORT_NUMBER = "+1-800-SUPPORT"; // Default support number

    // Database helper for call logging
    private CallDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_call);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Support Call");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new CallDbHelper(this);

        final EditText etNumber = findViewById(R.id.et_phone);
        final EditText etName = findViewById(R.id.et_name);
        final Button btnCall = findViewById(R.id.btn_call);
        final Button btnQuickSupport = findViewById(R.id.btn_quick_support);
        final ListView lv = findViewById(R.id.lv_calls);

        // Quick Support Button - calls predefined support number
        btnQuickSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNumber(SUPPORT_NUMBER, "Support Center");
                loadCalls(lv);
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = etNumber.getText().toString().trim();
                String name = etName.getText().toString().trim();
                if (number.isEmpty()) { Toast.makeText(SupportCallActivity.this, "Enter phone number", Toast.LENGTH_SHORT).show(); return; }
                callNumber(number, name);
                loadCalls(lv);
            }
        });

        loadCalls(lv);
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
        } else if (id == R.id.menu_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_support) {
            // Already on support page
            Toast.makeText(this, "Already on Support Call", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_about) {
            showAboutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        String message = "OfflineNavApp v1.1\n\n" +
                        "Support Call Feature:\n" +
                        "• Quick support button\n" +
                        "• Custom number calling\n" +
                        "• Call history tracking\n\n" +
                        "© 2025 OfflineNav";
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Initiate a phone call and log it to database
     *
     * Saves call details to local database for history tracking,
     * then requests phone permission if needed and places the call.
     *
     * @param number Phone number to call
     * @param name Contact name (can be empty for anonymous calls)
     */
    private void callNumber(String number, String name) {
        // Log call to database for history tracking
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CallDbHelper.COL_NAME, name);
        cv.put(CallDbHelper.COL_NUMBER, number);
        cv.put(CallDbHelper.COL_TIMESTAMP, System.currentTimeMillis());
        long id = db.insert(CallDbHelper.TABLE_NAME, null, cv);

        // Request phone permission if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        } else {
            placeCall(number);
        }
    }

    /**
     * Execute the actual phone call using Android's dialer
     *
     * Creates an ACTION_CALL intent with normalized phone number
     * and starts the call through the system's phone app.
     *
     * @param number Phone number to dial
     */
    private void placeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(android.net.Uri.parse("tel:" + PhoneNumberUtils.normalizeNumber(number)));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) return;
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // No direct number reference here; user will re-press call
                Toast.makeText(this, "Permission granted. Press Call again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Load and display call history in the list view
     *
     * Queries the call database and populates the list view with
     * call history, showing name, number, and timestamp for each call.
     * Results are ordered by timestamp (newest first).
     *
     * @param lv ListView to populate with call history
     */
    private void loadCalls(ListView lv) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query calls ordered by timestamp (newest first)
        Cursor c = db.query(CallDbHelper.TABLE_NAME, null, null, null, null, null, CallDbHelper.COL_TIMESTAMP + " DESC");

        // Map database columns to list item views
        String[] from = new String[]{CallDbHelper.COL_NAME, CallDbHelper.COL_NUMBER, CallDbHelper.COL_TIMESTAMP};
        int[] to = new int[]{R.id.item_name, R.id.item_number, R.id.item_time};

        // Create and set adapter
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item_call, c, from, to, 0);
        lv.setAdapter(adapter);
    }
}
