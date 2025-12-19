package com.example.offlinenav;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private MapView map = null;
    private MyLocationNewOverlay myLocationOverlay;
    private LocationManager locationManager;
    private EditText sourceEditText, destinationEditText;
    private Geocoder geocoder;
    private Marker sourceMarker, destinationMarker;
    private Polyline routeLine;
    private boolean isOfflineMode = false;
    private FavoritesDbHelper favoritesDbHelper;
    private GeoPoint sourcePoint = null;
    private GeoPoint destinationPoint = null;
    
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);

        // Enable action bar without back button (this is main screen)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Offline Navigation");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        favoritesDbHelper = new FavoritesDbHelper(this);

        map = findViewById(R.id.mapview);
        sourceEditText = findViewById(R.id.et_source);
        destinationEditText = findViewById(R.id.et_destination);
        Button useMyLocationButton = findViewById(R.id.btn_use_my_location);
        Button searchDestinationButton = findViewById(R.id.btn_search_destination);
        Button calculateRouteButton = findViewById(R.id.btn_calculate_route);
        Button saveFavoriteButton = findViewById(R.id.btn_save_favorite);
        Button myLocationButton = findViewById(R.id.btn_my_location);
        Button supportCallButton = findViewById(R.id.btn_support_call);

        // Initialize geocoder for search
        geocoder = new Geocoder(this, Locale.getDefault());

        // Setup map with offline support
        setupMapWithOfflineSupport();

        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);
        // Default center: Beirut, Lebanon
        map.getController().setCenter(new GeoPoint(33.8886, 35.4955));

        // Setup location tracking
        setupLocationTracking();

        // Check if launched from favorites with navigation intent
        handleNavigationIntent();

        // Use My Location as source
        useMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                    sourcePoint = myLocationOverlay.getMyLocation();
                    sourceEditText.setText("My Location");
                    Toast.makeText(MainActivity.this, "Source set to your location", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Location not available yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Search for destination
        searchDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLocation(destinationEditText.getText().toString(), true);
            }
        });

        // Calculate route button
        calculateRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateRouteFromInputs();
            }
        });

        // Save Favorite button
        saveFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentDestinationAsFavorite();
            }
        });

        // My Location button (center on location)
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerOnMyLocation();
            }
        });

        // Support Call button
        supportCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SupportCallActivity.class);
                startActivity(i);
            }
        });
    }

    private void calculateRouteFromInputs() {
        try {
            // Get source
            String sourceText = sourceEditText.getText().toString().trim();
            if (sourceText.isEmpty() || sourceText.equals("My Location")) {
                if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                    sourcePoint = myLocationOverlay.getMyLocation();
                    sourceEditText.setText("My Location");
                } else {
                    Toast.makeText(this, "Please wait for GPS or enter source location", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (sourcePoint == null) {
                // Need to search for source first
                Toast.makeText(this, "Searching for source location...", Toast.LENGTH_SHORT).show();
                searchLocation(sourceText, false);
                // Will be called again after search completes
                return;
            }

            // Get destination
            String destText = destinationEditText.getText().toString().trim();
            if (destText.isEmpty()) {
                Toast.makeText(this, "Please enter destination", Toast.LENGTH_SHORT).show();
                return;
            } else if (destinationPoint == null) {
                // Need to search for destination first
                Toast.makeText(this, "Searching for destination...", Toast.LENGTH_SHORT).show();
                searchLocation(destText, true);
                // Will be called again after search completes
                return;
            }

            // Both points available, calculate route
            if (sourcePoint != null && destinationPoint != null) {
                Toast.makeText(this, "Both locations set, calculating route...", Toast.LENGTH_SHORT).show();
                calculateRoute(sourcePoint, destinationPoint);
            } else {
                Toast.makeText(this, "Please set both source and destination", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void searchLocation(String query, final boolean isDestination) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(isDestination ? destinationEditText.getWindowToken() : sourceEditText.getWindowToken(), 0);

        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                
                if (isDestination) {
                    destinationPoint = point;
                    // Move map to destination
                    map.getController().animateTo(point);
                    map.getController().setZoom(15.0);
                    
                    // Add destination marker
                    if (destinationMarker == null) {
                        destinationMarker = new Marker(map);
                        map.getOverlays().add(destinationMarker);
                        destinationMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                showMarkerOptions(marker, true);
                                return true;
                            }
                        });
                    }
                    destinationMarker.setPosition(point);
                    destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    destinationMarker.setTitle("Destination: " + query);
                    destinationMarker.setSnippet(address.getAddressLine(0));
                    destinationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
                } else {
                    sourcePoint = point;
                    // Add source marker
                    if (sourceMarker == null) {
                        sourceMarker = new Marker(map);
                        map.getOverlays().add(sourceMarker);
                        sourceMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                showMarkerOptions(marker, false);
                                return true;
                            }
                        });
                    }
                    sourceMarker.setPosition(point);
                    sourceMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    sourceMarker.setTitle("Source: " + query);
                    sourceMarker.setSnippet(address.getAddressLine(0));
                }
                
                map.invalidate();
                Toast.makeText(this, "Found: " + address.getAddressLine(0), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found. Try a different search.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleNavigationIntent() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("navigate", false)) {
            double lat = intent.getDoubleExtra("latitude", 0);
            double lon = intent.getDoubleExtra("longitude", 0);
            String name = intent.getStringExtra("name");
            
            if (lat != 0 && lon != 0) {
                destinationPoint = new GeoPoint(lat, lon);
                destinationEditText.setText(name != null ? name : "Favorite Location");
                
                // Set source to my location
                if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                    sourcePoint = myLocationOverlay.getMyLocation();
                    sourceEditText.setText("My Location");
                    calculateRoute(sourcePoint, destinationPoint);
                }
            }
        }
    }

    private void setupMapWithOfflineSupport() {
        File mbtiles = new File(getFilesDir(), "map.mbtiles");
        
        if (mbtiles.exists()) {
            try {
                MBTilesFileArchive[] archives = new MBTilesFileArchive[]{
                    MBTilesFileArchive.getDatabaseFileArchive(mbtiles)
                };
                
                MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
                    new SimpleRegisterReceiver(this),
                    TileSourceFactory.MAPNIK,
                    archives
                );
                
                MapTileModuleProviderBase[] providers = new MapTileModuleProviderBase[]{archiveProvider};
                MapTileProviderArray tileProvider = new MapTileProviderArray(
                    TileSourceFactory.MAPNIK,
                    null,
                    providers
                );
                
                map.setTileProvider(tileProvider);
                isOfflineMode = true;
                Toast.makeText(this, "✓ Using offline map (" + (mbtiles.length() / 1024 / 1024) + " MB)", 
                              Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                map.setTileSource(TileSourceFactory.MAPNIK);
                isOfflineMode = false;
                Toast.makeText(this, "Offline map error, using online: " + e.getMessage(), 
                              Toast.LENGTH_LONG).show();
            }
        } else {
            map.setTileSource(TileSourceFactory.MAPNIK);
            isOfflineMode = false;
            Toast.makeText(this, "Using online map. Place map.mbtiles in app files/ for offline mode.", 
                          Toast.LENGTH_LONG).show();
        }
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
            Toast.makeText(this, "Already on Navigation", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_support) {
            Intent intent = new Intent(this, SupportCallActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_about) {
            showAboutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        String mode = isOfflineMode ? "Offline" : "Online";
        String message = "OfflineNavApp v1.2\n\n" +
                        "Mode: " + mode + "\n" +
                        "Features:\n" +
                        "• Map Navigation\n" +
                        "• Manual Route Planning\n" +
                        "• GPS Tracking\n" +
                        "• Favorite Locations\n" +
                        "• Emergency Support Calls\n\n" +
                        "Optimized for Lebanon\n\n" +
                        "© 2025 OfflineNav";
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showMarkerOptions(final Marker marker, final boolean isDestination) {
        final GeoPoint position = marker.getPosition();
        final String name = marker.getTitle();
        final String address = marker.getSnippet();
        
        new android.app.AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage(address)
            .setPositiveButton("Add to Favorites", (dialog, which) -> {
                saveFavorite(name, address, position);
            })
            .setNeutralButton("Close", null)
            .show();
    }

    private void saveCurrentDestinationAsFavorite() {
        if (destinationPoint == null) {
            Toast.makeText(this, "Please search for a destination first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String name = destinationEditText.getText().toString().trim();
        if (name.isEmpty()) {
            name = "Saved Location";
        }
        
        String address = destinationMarker != null ? destinationMarker.getSnippet() : "";
        saveFavorite(name, address, destinationPoint);
    }

    private void saveFavorite(String name, String address, GeoPoint position) {
        SQLiteDatabase db = favoritesDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FavoritesDbHelper.COL_NAME, name != null ? name : "Unnamed Location");
        cv.put(FavoritesDbHelper.COL_ADDRESS, address != null ? address : "");
        cv.put(FavoritesDbHelper.COL_LATITUDE, position.getLatitude());
        cv.put(FavoritesDbHelper.COL_LONGITUDE, position.getLongitude());
        cv.put(FavoritesDbHelper.COL_TIMESTAMP, System.currentTimeMillis());
        
        long id = db.insert(FavoritesDbHelper.TABLE_NAME, null, cv);
        if (id > 0) {
            Toast.makeText(this, "★ Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupLocationTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            enableLocationTracking();
        }
    }

    private void enableLocationTracking() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        map.getOverlays().add(myLocationOverlay);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void centerOnMyLocation() {
        if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
            GeoPoint myLocation = myLocationOverlay.getMyLocation();
            map.getController().animateTo(myLocation);
            map.getController().setZoom(16.0);
            Toast.makeText(this, "Centered on your location", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not available yet. Please wait...", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateRoute(final GeoPoint start, final GeoPoint end) {
        // Check internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        
        if (isConnected) {
            // Try online routing (Google Maps-style)
            Toast.makeText(this, "Calculating online route (following roads)...", Toast.LENGTH_SHORT).show();
            calculateOnlineRoute(start, end);
        } else {
            // Use offline routing (straight-line)
            Toast.makeText(this, "No internet - using offline routing", Toast.LENGTH_SHORT).show();
            drawOfflineRoute(start, end);
        }
    }

    private void calculateOnlineRoute(final GeoPoint start, final GeoPoint end) {
        new AsyncTask<Void, Void, Road>() {
            @Override
            protected Road doInBackground(Void... params) {
                try {
                    RoadManager roadManager = new OSRMRoadManager(MainActivity.this, "OfflineNavApp");
                    ArrayList<GeoPoint> waypoints = new ArrayList<>();
                    waypoints.add(start);
                    waypoints.add(end);
                    return roadManager.getRoad(waypoints);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Road road) {
                if (road == null || road.mStatus != Road.STATUS_OK) {
                    Toast.makeText(MainActivity.this, "Online routing failed, using offline mode",
                                  Toast.LENGTH_LONG).show();
                    drawOfflineRoute(start, end);
                    return;
                }

                // Remove old route if exists
                if (routeLine != null) {
                    map.getOverlays().remove(routeLine);
                }

                // Draw online route (follows actual roads like Google Maps)
                routeLine = RoadManager.buildRoadOverlay(road);
                routeLine.setColor(Color.rgb(66, 133, 244)); // Google Maps blue
                routeLine.setWidth(12f);
                map.getOverlays().add(routeLine);

                // Update markers
                updateRouteMarkers(start, end);
                map.invalidate();

                // Show route info
                double distance = road.mLength; // in km
                int duration = (int) (road.mDuration / 60); // in minutes
                Toast.makeText(MainActivity.this,
                              String.format("✓ Online Route: %.1f km, ~%d min (following roads)", distance, duration),
                              Toast.LENGTH_LONG).show();
                
                // Zoom to show route
                zoomToShowRoute(start, end);
            }
        }.execute();
    }

    private void updateRouteMarkers(GeoPoint start, GeoPoint end) {
        // Add source marker
        if (sourceMarker == null) {
            sourceMarker = new Marker(map);
            map.getOverlays().add(sourceMarker);
        }
        sourceMarker.setPosition(start);
        sourceMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        sourceMarker.setTitle("Start");
        sourceMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_compass));
        
        // Add destination marker
        if (destinationMarker == null) {
            destinationMarker = new Marker(map);
            map.getOverlays().add(destinationMarker);
        }
        destinationMarker.setPosition(end);
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        destinationMarker.setTitle("Destination");
        destinationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
    }

    private void zoomToShowRoute(GeoPoint start, GeoPoint end) {
        double minLat = Math.min(start.getLatitude(), end.getLatitude());
        double maxLat = Math.max(start.getLatitude(), end.getLatitude());
        double minLon = Math.min(start.getLongitude(), end.getLongitude());
        double maxLon = Math.max(start.getLongitude(), end.getLongitude());
        
        GeoPoint center = new GeoPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2);
        map.getController().setCenter(center);
        
        // Adjust zoom to show both points
        double latDiff = maxLat - minLat;
        double lonDiff = maxLon - minLon;
        double maxDiff = Math.max(latDiff, lonDiff);
        
        if (maxDiff < 0.01) map.getController().setZoom(15.0);
        else if (maxDiff < 0.05) map.getController().setZoom(13.0);
        else if (maxDiff < 0.1) map.getController().setZoom(11.0);
        else if (maxDiff < 0.5) map.getController().setZoom(9.0);
        else map.getController().setZoom(7.0);
    }

    private void drawOfflineRoute(GeoPoint start, GeoPoint end) {
        // Remove old route if exists
        if (routeLine != null) {
            map.getOverlays().remove(routeLine);
        }
        
        // Draw route line
        routeLine = new Polyline();
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(8f);
        List<GeoPoint> points = new ArrayList<>();
        points.add(start);
        points.add(end);
        routeLine.setPoints(points);
        map.getOverlays().add(routeLine);
        
        // Add source marker
        if (sourceMarker == null) {
            sourceMarker = new Marker(map);
            map.getOverlays().add(sourceMarker);
        }
        sourceMarker.setPosition(start);
        sourceMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        sourceMarker.setTitle("Start");
        sourceMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_compass));
        
        // Add destination marker
        if (destinationMarker == null) {
            destinationMarker = new Marker(map);
            map.getOverlays().add(destinationMarker);
        }
        destinationMarker.setPosition(end);
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        destinationMarker.setTitle("Destination");
        destinationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
        
        map.invalidate();
        
        // Calculate distance
        double distance = start.distanceToAsDouble(end) / 1000; // Convert to km
        // Estimate time (assuming average speed of 40 km/h for Lebanon roads)
        int estimatedMinutes = (int) ((distance / 40.0) * 60);
        
        Toast.makeText(this, 
                      String.format("Route: %.1f km, ~%d min (estimated)", distance, estimatedMinutes), 
                      Toast.LENGTH_LONG).show();
        
        // Zoom to show both points
        double minLat = Math.min(start.getLatitude(), end.getLatitude());
        double maxLat = Math.max(start.getLatitude(), end.getLatitude());
        double minLon = Math.min(start.getLongitude(), end.getLongitude());
        double maxLon = Math.max(start.getLongitude(), end.getLongitude());
        
        GeoPoint center = new GeoPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2);
        map.getController().setCenter(center);
        
        // Adjust zoom to show both points
        double latDiff = maxLat - minLat;
        double lonDiff = maxLon - minLon;
        double maxDiff = Math.max(latDiff, lonDiff);
        
        if (maxDiff < 0.01) map.getController().setZoom(15.0);
        else if (maxDiff < 0.05) map.getController().setZoom(13.0);
        else if (maxDiff < 0.1) map.getController().setZoom(11.0);
        else if (maxDiff < 0.5) map.getController().setZoom(9.0);
        else map.getController().setZoom(7.0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationTracking();
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied. Location features disabled.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Location updates handled by MyLocationOverlay
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, provider + " enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, provider + " disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDetach();
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    static class SimpleRegisterReceiver implements IRegisterReceiver {
        private final android.content.Context ctx;
        SimpleRegisterReceiver(android.content.Context c) { ctx = c; }
        @Override public void destroy() {}
        @Override public Intent registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter) { return ctx.registerReceiver(receiver, filter); }
        @Override public void unregisterReceiver(android.content.BroadcastReceiver receiver) { try { ctx.unregisterReceiver(receiver); } catch (Exception ignored){} }
    }
}
