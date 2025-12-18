package com.example.offlinenav;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);

        map = findViewById(R.id.mapview);

        try {
            // Attempt to load MBTiles from app files directory (app expects map.mbtiles)
            File mbtiles = new File(getFilesDir(), "map.mbtiles");
            if (mbtiles.exists()) {
                MapTileModuleProviderBase archiveProvider = new MapTileFileArchiveProvider(
                        new SimpleRegisterReceiver(this), new MBTilesFileArchive[]{MBTilesFileArchive.getDatabaseFileArchive(mbtiles)});

                MapTileModuleProviderBase[] providers = new MapTileModuleProviderBase[]{archiveProvider};
                MapTileProviderArray providerArray = new MapTileProviderArray(TileSourceFactory.MAPNIK, null, providers);
                map.setTileProvider(providerArray);
            } else {
                // default to online tiles (user should add MBTiles to files dir for offline)
                map.setTileSource(TileSourceFactory.MAPNIK);
                Toast.makeText(this, "MBTiles not found in files/. Using online map. Place map.mbtiles into app files/ for offline.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Map load error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        map.getController().setZoom(12.0);
        map.getController().setCenter(new GeoPoint(48.8583, 2.2944));

        findViewById(R.id.btn_support_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SupportCallActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDetach();
        }
    }

    // Simple wrapper since osmdroid's SimpleRegisterReceiver is in examples; provide minimal implementation
    static class SimpleRegisterReceiver implements IRegisterReceiver {
        private final android.content.Context ctx;
        SimpleRegisterReceiver(android.content.Context c) { ctx = c; }
        @Override public android.content.Context getContext() { return ctx; }
        @Override public void destroy() {}
        @Override public void registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter) { ctx.registerReceiver(receiver, filter); }
        @Override public void unregisterReceiver(android.content.BroadcastReceiver receiver) { try { ctx.unregisterReceiver(receiver); } catch (Exception ignored){} }
    }
}
