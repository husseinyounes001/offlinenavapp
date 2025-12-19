/**
 * CallRecord - Data model for call history entries
 *
 * Simple POJO (Plain Old Java Object) representing a single call record
 * in the support call history. Used for data transfer between database
 * and UI components in the SupportCallActivity.
 */
package com.example.offlinenav;

public class CallRecord {

    // Database fields matching CallDbHelper table schema
    public long id;           // Unique identifier (primary key)
    public String name;       // Contact name (can be null for anonymous calls)
    public String number;     // Phone number called
    public long timestamp;    // Call time in milliseconds since epoch
}
