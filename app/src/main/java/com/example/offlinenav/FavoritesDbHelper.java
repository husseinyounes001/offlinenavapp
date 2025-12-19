/**
 * FavoritesDbHelper - SQLite database helper for favorite locations
 *
 * Manages the local database that stores user's saved favorite locations.
 * Each favorite includes name, coordinates, address, and timestamp.
 * Used by FavoritesActivity and MainActivity for location persistence.
 */
package com.example.offlinenav;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    // Database configuration
    private static final String DATABASE_NAME = "favorites.db";    // Database filename
    private static final int DATABASE_VERSION = 1;                // Schema version

    // Table and column constants
    public static final String TABLE_NAME = "favorites";           // Table name
    public static final String COL_ID = "_id";                     // Primary key (auto-increment)
    public static final String COL_NAME = "name";                  // Location name
    public static final String COL_LATITUDE = "latitude";          // Latitude coordinate
    public static final String COL_LONGITUDE = "longitude";        // Longitude coordinate
    public static final String COL_ADDRESS = "address";            // Human-readable address
    public static final String COL_TIMESTAMP = "timestamp";        // Save timestamp (milliseconds)

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the favorites table when database is first created
     *
     * Defines the schema for storing favorite locations with all necessary
     * fields: ID, name, coordinates, address, and timestamp.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL, " +
                COL_ADDRESS + " TEXT, " +
                COL_TIMESTAMP + " INTEGER)";
        db.execSQL(createTable);
    }

    /**
     * Handle database schema upgrades
     *
     * For version upgrades, drops the existing table and recreates it.
     * In a production app, this would handle migrations more gracefully
     * to preserve user data.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
