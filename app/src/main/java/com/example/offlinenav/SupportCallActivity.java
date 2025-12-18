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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SupportCallActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PHONE = 1001;
    private CallDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_call);

        dbHelper = new CallDbHelper(this);

        final EditText etNumber = findViewById(R.id.et_phone);
        final EditText etName = findViewById(R.id.et_name);
        final Button btnCall = findViewById(R.id.btn_call);
        final ListView lv = findViewById(R.id.lv_calls);

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = etNumber.getText().toString().trim();
                String name = etName.getText().toString().trim();
                if (number.isEmpty()) { Toast.makeText(SupportCallActivity.this, "Enter phone number", Toast.LENGTH_SHORT).show(); return; }
                // Save to DB
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(CallDbHelper.COL_NAME, name);
                cv.put(CallDbHelper.COL_NUMBER, number);
                cv.put(CallDbHelper.COL_TIMESTAMP, System.currentTimeMillis());
                long id = db.insert(CallDbHelper.TABLE_NAME, null, cv);

                // Ask permission if needed
                if (ContextCompat.checkSelfPermission(SupportCallActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SupportCallActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                } else {
                    placeCall(number);
                }
                loadCalls(lv);
            }
        });

        loadCalls(lv);
    }

    private void placeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(android.net.Uri.parse("tel:" + PhoneNumberUtils.normalizeNumber(number)));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) return;
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    private void loadCalls(ListView lv) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(CallDbHelper.TABLE_NAME, null, null, null, null, null, CallDbHelper.COL_TIMESTAMP + " DESC");
        String[] from = new String[]{CallDbHelper.COL_NAME, CallDbHelper.COL_NUMBER, CallDbHelper.COL_TIMESTAMP};
        int[] to = new int[]{R.id.item_name, R.id.item_number, R.id.item_time};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item_call, c, from, to, 0);
        lv.setAdapter(adapter);
    }
}
