# Offline Navigation Setup Guide

## Overview

To make your OfflineNavApp work completely offline, you need to provide offline map tiles. The app currently uses online maps from OpenStreetMap, but it's designed to support offline MBTiles files.

## What You Need for Offline Navigation

### 1. **MBTiles Map File**

MBTiles is a file format for storing map tiles in a single SQLite database. You need to:

#### Option A: Download Pre-made MBTiles
- **OpenMapTiles**: https://openmaptiles.com/downloads/
  - Free downloads available for some regions
  - Paid options for worldwide coverage
  
- **Protomaps**: https://protomaps.com/
  - Open-source alternative
  - Can download specific regions

- **MapTiler**: https://www.maptiler.com/
  - Commercial service with free tier
  - High-quality maps

#### Option B: Create Your Own MBTiles
Use tools like:
- **TileMill** (Desktop app)
- **MOBAC** (Mobile Atlas Creator)
- **tippecanoe** (Command-line tool)

### 2. **File Placement**

Once you have an MBTiles file:

1. **Rename it to:** `map.mbtiles`
2. **Place it in the app's files directory:**
   - Path: `/data/data/com.example.offlinenav/files/map.mbtiles`

#### How to Transfer the File:

**Method 1: Using ADB (Recommended)**
```cmd
adb push map.mbtiles /sdcard/Download/
adb shell
run-as com.example.offlinenav
cp /sdcard/Download/map.mbtiles /data/data/com.example.offlinenav/files/
exit
```

**Method 2: Using File Manager App**
1. Copy `map.mbtiles` to your phone's Download folder
2. Install a file manager app with root access (if rooted)
3. Navigate to `/data/data/com.example.offlinenav/files/`
4. Copy the file there

**Method 3: Modify App to Use External Storage**
The app can be modified to read from external storage (SD card) instead.

## Current Implementation Status

### ✅ What's Already Implemented:
- MBTiles file detection
- Fallback to online maps if offline file not found
- Toast notification when offline map is missing

### ⚠️ What Needs Enhancement:

The current code has a simplified implementation. To fully enable offline maps, you need to:

1. **Update MainActivity.java** to properly load MBTiles
2. **Add MBTiles provider configuration**
3. **Handle tile source switching**

## Enhanced Offline Implementation

Here's what needs to be added to [`MainActivity.java`](app/src/main/java/com/example/offlinenav/MainActivity.java):

### Required Changes:

```java
// In onCreate() method, replace the current map setup with:

File mbtiles = new File(getFilesDir(), "map.mbtiles");
if (mbtiles.exists()) {
    try {
        // Create offline tile provider
        MBTilesFileArchive archive = MBTilesFileArchive.getDatabaseFileArchive(mbtiles);
        MapTileFileArchiveProvider provider = new MapTileFileArchiveProvider(
            new SimpleRegisterReceiver(this),
            new MBTilesFileArchive[]{archive}
        );
        
        // Create custom tile source
        XYTileSource tileSource = new XYTileSource(
            "MBTiles",
            0, 18,  // min/max zoom levels
            256,    // tile size
            ".png", // tile extension
            new String[]{}
        );
        
        // Set up map with offline tiles
        MapTileProviderArray tileProvider = new MapTileProviderArray(
            tileSource,
            null,
            new MapTileModuleProviderBase[]{provider}
        );
        
        map.setTileProvider(tileProvider);
        Toast.makeText(this, "Using offline map", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        Toast.makeText(this, "Offline map error, using online: " + e.getMessage(), 
                      Toast.LENGTH_LONG).show();
    }
} else {
    map.setTileSource(TileSourceFactory.MAPNIK);
    Toast.makeText(this, "Using online map. Place map.mbtiles for offline.", 
                  Toast.LENGTH_LONG).show();
}
```

## Offline Search Considerations

### Current Search Limitation:
The search functionality uses Android's `Geocoder`, which requires an internet connection to convert location names to coordinates.

### Offline Search Solutions:

#### Option 1: Pre-load Common Locations
Create a local database of important locations:
```java
// locations.db with columns: name, latitude, longitude
// Search locally first, then fall back to online Geocoder
```

#### Option 2: Use Offline Geocoding Library
- **Nominatim Offline**: Local OpenStreetMap geocoding
- **Pelias**: Self-hosted geocoding service
- **Photon**: Lightweight geocoding API

#### Option 3: Coordinate-Only Search
Allow users to enter coordinates directly:
```
Format: 48.8583, 2.2944
```

## Storage Requirements

### Map File Sizes (Approximate):
- **City (e.g., Paris)**: 50-200 MB
- **Country (e.g., France)**: 1-5 GB
- **Continent (e.g., Europe)**: 10-30 GB
- **World**: 50-100+ GB

### Recommendations:
1. Start with a small region for testing
2. Use zoom levels 0-15 for reasonable file size
3. Consider splitting large areas into multiple MBTiles files

## Testing Offline Mode

### Steps to Test:
1. Install the app
2. Place `map.mbtiles` in the correct location
3. Enable Airplane Mode on your device
4. Open the app
5. Verify map tiles load from the offline file
6. Test navigation and location features

### Expected Behavior:
- ✅ Map tiles display without internet
- ✅ GPS location tracking works (GPS doesn't need internet)
- ✅ Zoom and pan work normally
- ❌ Search requires internet (unless enhanced)
- ❌ Initial tile download requires internet (if not pre-loaded)

## Alternative: Bundling Maps with APK

### Option: Include MBTiles in APK Assets

1. **Add to assets folder:**
   ```
   app/src/main/assets/map.mbtiles
   ```

2. **Copy to files directory on first run:**
   ```java
   private void copyAssetToFiles() {
       File dest = new File(getFilesDir(), "map.mbtiles");
       if (!dest.exists()) {
           try {
               InputStream in = getAssets().open("map.mbtiles");
               FileOutputStream out = new FileOutputStream(dest);
               byte[] buffer = new byte[1024];
               int read;
               while ((read = in.read(buffer)) != -1) {
                   out.write(buffer, 0, read);
               }
               in.close();
               out.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
   ```

**Pros:**
- No manual file transfer needed
- Works immediately after installation

**Cons:**
- Increases APK size significantly
- Google Play has 100MB APK limit (need expansion files for larger maps)
- Updates require re-downloading entire APK

## Recommended Workflow

### For Development/Testing:
1. Use small MBTiles file (city-level)
2. Transfer via ADB
3. Test offline functionality

### For Production:
1. Implement download manager in app
2. Let users download maps for their region
3. Store in external storage
4. Provide map management UI (delete, update)

## Resources

### Tools:
- **MOBAC**: https://mobac.sourceforge.io/
- **TileMill**: https://tilemill-project.github.io/tilemill/
- **tippecanoe**: https://github.com/felt/tippecanoe

### Map Data Sources:
- **OpenStreetMap**: https://www.openstreetmap.org/
- **Natural Earth**: https://www.naturalearthdata.com/
- **OpenMapTiles**: https://openmaptiles.org/

### Documentation:
- **OSMDroid Wiki**: https://github.com/osmdroid/osmdroid/wiki
- **MBTiles Spec**: https://github.com/mapbox/mbtiles-spec

## Next Steps

Would you like me to:
1. **Implement full offline MBTiles support** in the code?
2. **Add a map download manager** to the app?
3. **Create offline search** with local database?
4. **Bundle a sample map** with the APK?

Let me know which direction you'd like to take!
