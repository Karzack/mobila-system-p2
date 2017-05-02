package inlamning.bjosve.p2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private SocketHandler socketHandler;
    private SupportMapFragment mapFragment;
    public LinearLayout llTexts;
    public ScrollView svText;
    private EditText etGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        svText = (ScrollView)findViewById(R.id.svText);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        etGroup = (EditText)findViewById(R.id.etGroup);
        llTexts = (LinearLayout) findViewById(R.id.llTexts);
        buttonInit();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapFragment.getMapAsync(this);



    }

    private void buttonInit() {
        Button btnMembers = (Button) findViewById(R.id.btnMembers);
        btnMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketHandler.viewMembers(etGroup.getText().toString());
            }
        });
        Button btnGroups = (Button)findViewById(R.id.btnGroup);
        btnGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketHandler.viewGroups();

            }
        });
        Button btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketHandler.registerToGroup(etGroup.getText().toString());

            }
        });
        Button btnUnregister = (Button)findViewById(R.id.btnUnregister);
        btnUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketHandler.unregister();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        socketHandler = new SocketHandler(this);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socketHandler.runningListener = false;
        socketHandler.shutDownSocket();
        socketHandler = null;
    }

    public Location getLocationString() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        CurrentLocation listener = new CurrentLocation(this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0,
                            0, listener);
                    Log.d("Network", "Network Enabled");
                    if (mLocationManager != null) {
                        currentLocation = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (currentLocation == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0,
                                0, listener);
                        Log.d("GPS", "GPS Enabled");
                        if (mLocationManager != null) {
                            currentLocation = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationManager.removeUpdates(listener);
        return currentLocation;
    }

    public void clearMap(){
        mMap.clear();
    }

    public void pinMap(String name, double longitude, double latitude){
        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(name));
    }

    public LocationManager recieveLocationManager() {
        return mLocationManager;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        clearMap();
        Location theLocation = getLocationString();
        LatLng location = new LatLng(theLocation.getLatitude(),theLocation.getLongitude());
        pinMap("Testing",theLocation.getLongitude(),theLocation.getLatitude());
        socketHandler = new SocketHandler(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
      //
        //
      /*
        // Add a marker in Sydney and move the camera
       // LatLng location = new LatLng(theLocation.getLatitude(), theLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(location).title("Bjaern is here!"));//
        */
    }

    public void sendNewLocation() {
        Location location = getLocationString();
        socketHandler.sendLocation(location);
    }
}
