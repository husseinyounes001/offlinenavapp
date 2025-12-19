/**
 * CallDbHelper - SQLite database helper for call history logging
 *
 * Manages the local database that stores call history for the support call feature.
 * Each call record includes contact name, phone number, and timestamp.
 * Used by SupportCallActivity to maintain a log of emergency/support calls.
 */
package com.example.offlinenav;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CallDbHelper extends SQLiteOpenHelper {

    // Database configuration constants
    public static final String DB_NAME = "calls.db";           // Database filename
    public static final int DB_VERSION = 1;                    // Database version
    public static final String TABLE_NAME = "calls";           // Table name

    // Column names for the calls table
    public static final String COL_ID = "_id";                 // Primary key (auto-increment)
    public static final String COL_NAME = "name";              // Contact name
    public static final String COL_NUMBER = "number";          // Phone number
    public static final String COL_TIMESTAMP = "timestamp";    // Call timestamp (milliseconds)

    public CallDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Create the calls table when database is first created
     *
     * Defines the schema for storing call history with auto-incrementing ID,
     * contact name, phone number, and timestamp fields.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, " + COL_NUMBER + " TEXT, " + COL_TIMESTAMP + " INTEGER" + ")";
        db.execSQL(sql);
    }

    /**
     * Handle database schema upgrades
     *
     * For version upgrades, drops the existing table and recreates it.
     * In a production app, this would handle migrations more gracefully.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
